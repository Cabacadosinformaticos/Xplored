# Architecture

Xplored has three parts: a native Android app, a Spring Boot REST API and a MySQL database. The
app is the only client of the API. There is no web front end and no other service besides the
Google Maps SDK inside the app.

## Components

```mermaid
flowchart LR
    subgraph Phone["Android app (Kotlin, Compose)"]
        UI["ui/screens<br/>one Activity per screen"]
        THEME["ui/theme<br/>colours, light / dark"]
        REPO["repositories<br/>places, photos"]
        NET["network<br/>Retrofit services, ApiClient"]
        MODELS["data/models"]
        SESSION["SessionManager<br/>SharedPreferences"]
        UI --> REPO --> NET
        UI --> NET
        UI --> SESSION
        UI --> THEME
        NET --> MODELS
    end
    MAPS[("Google Maps SDK")]
    UI --> MAPS

    subgraph Server["Backend (Spring Boot, port 9000)"]
        CTRL["Controller<br/>14 REST controllers"]
        JPA["JPA repositories<br/>User, Place, Review, ..."]
        JDBC["JdbcTemplate<br/>queries and rule-heavy writes"]
        STATIC["/uploads/** resource handler"]
        CTRL --> JPA
        CTRL --> JDBC
    end
    FILES[("uploads/ folder")]
    DB[("MySQL 8<br/>xplored_db")]

    NET -- "HTTP + JSON, multipart for photos" --> CTRL
    NET -- "GET photos" --> STATIC
    JPA --> DB
    JDBC --> DB
    CTRL -- "store photo" --> FILES
    STATIC --> FILES
```

### Android app

Package `pt.iade.ei.xplored`, module `app` in the Gradle root `front_end/`.

| Layer | Location | Role |
|---|---|---|
| Screens | `ui/screens/<area>/` | One `ComponentActivity` per screen, with the UI written in Jetpack Compose. Areas: `main` (map), `place`, `reviews`, `pedipaper`, `coupon`, `history`, `leaderboard`, `favorites`, `profile`, `settings`, `auth`, `admin`, `misc` |
| Theme | `ui/theme/` | Colour schemes, typography and `ThemePreferences` (system, light or dark). The map has a dark style in `res/raw/map_style_dark.json` |
| Network | `network/` | `ApiClient` builds one Retrofit instance (Gson, 30 s timeouts, body logging in debug builds). One `*ApiService` interface per area, plus request classes. `PhotoUploadClient` sends the multipart uploads |
| Repositories | `repositories/` | `PlaceRepository` and `PhotoRepository` wrap the calls that several screens share |
| Models | `data/models/` | Classes the app deserialises into, grouped by area |
| Session | `SessionManager.kt` | Signed-in state and the user data (name, email, role, points) in `SharedPreferences`. Accounts live on the server; there is no local user list |

The base URL comes from `BuildConfig.API_BASE_URL`, filled by the Secrets Gradle Plugin from
`local.properties` or `local.defaults.properties`. The Maps key is injected into the manifest the
same way (`MAPS_API_KEY`). See [SETUP.md](SETUP.md).

### Backend

Package `com.Xplored.Xplored`.

| Part | Location | Role |
|---|---|---|
| Controllers | `Controller/` | `AdminController`, `CategoryController`, `CouponController`, `FavoriteController`, `LeaderboardController`, `PasswordResetController`, `PedipaperController`, `PhotoController`, `PlaceController`, `PointsController`, `ReactionController`, `ReviewController`, `UserController`, `VisitController` |
| Model | `Model/` | JPA entities and Spring Data repositories for users, categories, reviews, reactions, photos and the pedipaper tables, plus request and response classes |
| Config | `Config/` | `WebConfig` serves `/uploads/**` from the `uploads/` folder; `UrlUtil` converts stored relative paths into absolute URLs |

Two data access styles are mixed. Simple entity access (users, reviews, reactions, photos,
pedipaper participation) uses JPA repositories. Queries with joins and writes that must be atomic
and carry business rules (places, favourites, visits, coupons, ledger, password reset, moderation,
leaderboard) use `JdbcTemplate` with SQL written in the controller. Multi-step writes are
`@Transactional`.

Dependencies are `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, the MySQL driver and
`spring-security-core`, used only for `BCryptPasswordEncoder`. Spring Security itself is not
configured, so there are no filters, sessions or tokens.

Uploaded photos are written to `uploads/<uuid>.<ext>` in the working directory of the backend.
The database keeps the relative path, and responses turn it into an absolute URL from the
incoming request, so the same data works for the emulator (`10.0.2.2`) and for a LAN address.

### Database

MySQL 8 with 17 tables. See [DATA_MODEL.md](DATA_MODEL.md).

## Main flows

### Login

```mermaid
sequenceDiagram
    participant App
    participant API as UserController
    participant DB as MySQL
    App->>API: POST /user/login {email, password}
    API->>DB: find user by email
    API->>API: BCrypt matches password hash?
    alt valid
        API-->>App: 200 User (no password hash)
        App->>App: SessionManager stores name, email, role, points
    else invalid
        API-->>App: 401
    end
```

The app keeps the email. Later requests carry it as a parameter or in the body; the server never
issues a token (see [Known limitations](#known-limitations)).

### Password reset

1. The user enters an email. The app calls `POST /user/password-reset/request`.
2. The server answers 200 whether or not the account exists. If it exists, it invalidates older
   unused codes, stores the BCrypt hash of a new 6 digit code that expires in 15 minutes, and
   writes the code to the server log.
3. The user types the code. The app asks for the new password twice and calls
   `POST /user/password-reset/confirm` with email, code and password.
4. The server checks the newest valid code. A wrong code adds 1 to `attempts`; at 5 attempts the
   code is refused. A correct code replaces the password hash and marks the code as used.

### Place creation and moderation

```mermaid
sequenceDiagram
    participant U as User app
    participant API
    participant A as Admin app
    U->>API: POST /places {..., authorId = email}
    alt author role is ADMIN
        API-->>U: 201 status APPROVED, verified
    else author role is USER
        API-->>U: 201 status PENDING
        Note over U,API: GET /places?userEmail= shows the place only to its author
        A->>API: GET /admin/places/pending?adminEmail=
        API-->>A: pending places
        A->>API: POST /admin/places/{id}/approve or /reject
        API-->>A: 200 status APPROVED or REJECTED
    end
    U->>API: POST /photos/upload (placeId)
```

Photos of the new place are uploaded after the place exists, because the upload needs its id.
Approved places appear for everyone; rejected places never appear in the lists.

### GPS check-in at a place

The app reads the device location and calls `POST /visits`. The server computes the haversine
distance to the place and refuses anything beyond 100 m. The first valid check-in per user and
place inserts a `visits` row, adds 10 points and writes a ledger row. A repeat answers 200 with
`pointsAwarded: 0`.

### Pedipaper check-in and completion

```mermaid
sequenceDiagram
    participant App
    participant API as PedipaperController
    participant DB as MySQL
    App->>API: GET /pedipapers, GET /pedipapers/{id}/stops
    App->>API: POST /pedipapers/{id}/join
    loop each stop
        opt stop requires a photo
            App->>API: POST /photos/upload (reviewId = STOP-EVIDENCE-...)
            API-->>App: photo id
        end
        App->>API: POST .../stops/{stopId}/checkin {lat, lng, photoId}
        API->>API: distance to the stop's place <= 50 m? photo owned, kind EVIDENCE?
        API->>DB: insert route_stop_checkins
        API-->>App: completedStops / totalStops
    end
    App->>API: POST /pedipapers/{id}/complete
    API->>DB: all stops checked in? set completed, add points, ledger row
    API-->>App: pointsAwarded, newBalance
```

`GET /pedipapers/{id}/progress` lets the game resume at the first stop without a check-in. The
route pays out only once.

### Coupon redemption

`POST /coupons/redeem` runs in one transaction: load the user and the coupon, refuse inactive
coupons and insufficient balances, debit with a guarded `UPDATE`, insert the `redemptions` row and
a negative ledger row, and return the coupon code. The list endpoint `GET /coupons/active` never
returns codes, so a code is visible only after paying for it and in the user's redemption history.

### Points ledger

All three sources of points (visit, pedipaper, coupon) change `users.points` and insert a
`points_ledger` row in the same transaction. `GET /user/points-history` reads the ledger newest
first. The amounts and reason texts are listed in
[DATA_MODEL.md](DATA_MODEL.md#points-ledger).

## Known limitations

- **No authentication or authorisation.** Login returns the user, but no token is issued, and
  endpoints trust the email in the request. Anyone who knows an email can act as that user,
  including as an ADMIN on `/admin/**`. Cleartext HTTP is allowed for local hosts only
  (`network_security_config.xml`).
- **Reset codes are only in the log.** There is no mail or SMS service, by design for the
  coursework.
- **Location is trusted.** The server checks the distance between the coordinates it receives and
  the place, but cannot tell whether those coordinates are real, so GPS spoofing works.
- **Photos on local disk.** Uploads live in the `uploads/` folder of one machine. They are not
  scanned, resized or removed when the row that points to them is deleted.
- **Seed photos are external.** Place photos in `populate.sql` are Wikimedia Commons URLs, and the
  evidence rows point to files that do not exist in a fresh checkout.
- **Moderation covers places only.** Reviews and photos are published without review.
- **Single instance.** No caching, pagination or rate limiting; list endpoints return every row.
- **Tests.** `XploredApplicationTests` needs a live MySQL; there are no unit tests of the rules
  above.
