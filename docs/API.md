# REST API

The backend is a Spring Boot application on port 9000 (`http://localhost:9000`). It exchanges JSON,
except for the photo upload, which is `multipart/form-data`. Every controller allows cross-origin
requests from any origin.

This document was checked against the controllers in
`back_end/src/main/java/com/Xplored/Xplored/Controller/`. The documentation handed in on
14 December 2025 ([REST.md](../Documents/3%C2%AAEntrega/REST/REST.md)) describes an older version.

## Conventions

- **No authentication.** No endpoint checks a token or a password after login. The app sends the
  email of the signed-in user (`userEmail`, `email`, `adminEmail` or `authorId`) and the server
  trusts it. The only server-side rule tied to a role is the ADMIN check of `/admin/**`.
- **Error body.** Most errors return `{"error": "<message>"}`. Messages produced by the check-in,
  coupon, reset and moderation rules are in European Portuguese because the app shows them
  as they are. A few older endpoints return a plain string (marked below).
- **Photo URLs.** The database stores relative paths (`/uploads/<uuid>.<ext>`). Responses return
  absolute URLs built from the incoming request, for example `http://10.0.2.2:9000/uploads/...`.
  A URL that already starts with `http` is returned unchanged.
- **Timestamps** in history responses are strings produced by `java.sql.Timestamp.toString()`,
  for example `2025-12-01 14:30:00.0`.
- **Ledger.** Every change to a user balance also writes a `points_ledger` row (see
  [DATA_MODEL.md](DATA_MODEL.md#points-ledger)).

## Overview

| Area | Method and path |
|---|---|
| Users | `POST /user/register`, `POST /user/login`, `GET /user/by-email`, `PUT /user/update-profile` |
| Password reset | `POST /user/password-reset/request`, `POST /user/password-reset/confirm` |
| Points history | `GET /user/points-history` |
| Leaderboard | `GET /user/leaderboard` |
| Categories | `GET /category/get-all`, `POST /category/save` |
| Places | `GET /places`, `POST /places`, `DELETE /places/{id}` |
| Reviews | `POST /reviews`, `GET /reviews/by-place/{placeId}`, `GET /reviews/by-user` |
| Reactions | `POST /reactions/toggle` |
| Photos | `POST /photos/upload`, `GET /photos/by-place/{placeId}`, `GET /photos/by-user/{userIdOrEmail}` |
| Favourites | `GET /favorites`, `POST /favorites`, `DELETE /favorites` |
| Visits | `POST /visits`, `GET /visits/count` |
| Pedipapers | `GET /pedipapers`, `GET /pedipapers/{id}/stops`, `POST /pedipapers/{id}/join`, `POST /pedipapers/{id}/stops/{stopId}/checkin`, `GET /pedipapers/{id}/progress`, `POST /pedipapers/{id}/complete` |
| Coupons | `GET /coupons/active`, `POST /coupons/redeem`, `GET /coupons/redeemed` |
| Moderation | `GET /admin/places/pending`, `POST /admin/places/{id}/approve`, `POST /admin/places/{id}/reject` |
| Static files | `GET /uploads/{file}` |

## Users

Controller: `UserController`.

### POST /user/register

Creates a user with role `USER` and 0 points. The password is stored as a BCrypt hash.

Body:

```json
{ "name": "Ana Silva", "email": "ana@example.pt", "password": "secret" }
```

| Status | Meaning |
|---|---|
| 201 | The created `User` |
| 400 | `name`, `email` or `password` is missing or blank: `{"error": "name, email and password are required"}` |
| 409 | The email is already registered: `{"error": "Email already registered"}` |

`User` fields: `id`, `name`, `email`, `role`, `country`, `points`, `profilePhoto` (absolute URL or
null), `about`, `createdAt`. The password hash is never serialised.

### POST /user/login

Body: `{ "email": "...", "password": "..." }`

| Status | Meaning |
|---|---|
| 200 | The `User` |
| 401 | Unknown email, wrong password, or blank field. Empty body |

### GET /user/by-email

Query: `email` (required).

| Status | Meaning |
|---|---|
| 200 | The `User` |
| 404 | No user with that email |

### PUT /user/update-profile

Query parameters, all required: `email`, `name`, `about`, `country`.

| Status | Meaning |
|---|---|
| 200 | The updated `User` |
| 404 | No user with that email |

## Password reset

Controller: `PasswordResetController`. Flow described in
[ARCHITECTURE.md](ARCHITECTURE.md#password-reset).

### POST /user/password-reset/request

Body: `{ "email": "ana@xplored.pt" }`

Always answers 200 with `{"message": "Se a conta existir, foi gerado um código de recuperação."}`,
whether the account exists or not, so it cannot be used to discover accounts. If the account
exists, older unused codes are invalidated, a 6 digit code is stored as a BCrypt hash with an expiry
of 15 minutes, and the code is written to the server log. No email is sent.

### POST /user/password-reset/confirm

Body: `{ "email": "...", "code": "123456", "newPassword": "newsecret" }`

| Status | Meaning |
|---|---|
| 200 | `{"message": "Palavra-passe redefinida com sucesso."}`; the code is marked used |
| 400 | `{"error": "..."}`: a field is blank, the new password has fewer than 6 characters, the email is unknown, there is no valid code, the code is wrong (the attempt counter increases), or 5 attempts were already used |

## Points history

Controller: `PointsController`.

### GET /user/points-history

Query: `email` (required). Returns the points movements of the user, newest first:

```json
[ { "amount": 10, "reason": "Visita: Jardim de Moscavide", "createdAt": "2025-12-01 14:30:00.0" } ]
```

An unknown email returns an empty list. Status 200.

## Leaderboard

Controller: `LeaderboardController`.

### GET /user/leaderboard

Query: `email` (optional). Ranks users with role `USER` by points, descending, ties broken by name.

```json
{
  "top": [ { "position": 1, "name": "Rita Ferreira", "points": 630, "profilePhoto": "https://..." } ],
  "me": { "position": 4, "name": "Ana Silva", "points": 480 }
}
```

`top` has at most 10 entries. `me` is `null` when `email` is missing or does not belong to a ranked
user (ADMIN and BUSINESS accounts are not ranked). Status 200.

## Categories

Controller: `CategoryController`.

### GET /category/get-all

Returns every category: `categoryId`, `name`, `colorHex`, `iconName`. Status 200.

### POST /category/save

Body: `{ "name": "...", "colorHex": "#RRGGBB", "iconName": "..." }`

| Status | Meaning |
|---|---|
| 201 | The created category |
| 400 | `name` is blank |
| 409 | The name already exists, or the supplied id already exists |

The app does not call this endpoint.

## Places

Controller: `PlaceController`.

`PlaceResponse` fields: `placeId`, `name`, `description`, `lat`, `lng`, `addressFull`,
`categoryId`, `authorId` (the author's **email**), `avgRating`, `coverImageUrl` (first gallery
photo, absolute URL or null), `isVerified`, `status` (`APPROVED`, `PENDING` or `REJECTED`).

### GET /places

Query: `userEmail` (optional).

Returns the `APPROVED` places. With `userEmail`, the `PENDING` places created by that user are
included too, so authors see their own submissions. Status 200.

### POST /places

Body:

```json
{
  "name": "Pastelaria Central",
  "description": "Pastéis de nata ao balcão.",
  "lat": 38.7790,
  "lng": -9.1010,
  "addressFull": "Rua Exemplo 1, Moscavide",
  "categoryId": 3,
  "authorId": "ana@xplored.pt"
}
```

`authorId` is the email of the author. A place created by an `ADMIN` is `APPROVED` and verified at
once. A place created by anyone else is `PENDING` and not verified.

| Status | Meaning |
|---|---|
| 201 | The created `PlaceResponse` |
| 400 | `Author is required`, `Unknown author`, `Category is required` or `Unknown category` |

### DELETE /places/{id}

Query: `userEmail` (required). Only the author can delete a place.

| Status | Meaning |
|---|---|
| 200 | `{"message": "Deleted"}` |
| 403 | The email is not the author's: `Only the author can delete this place` |
| 404 | `Place not found` |

Reviews, photos, favourites and visits of the place are removed by the database cascade.

## Reviews

Controller: `ReviewController`.

### POST /reviews

Body:

```json
{ "userEmail": "ana@xplored.pt", "placeId": 1, "rating": 5, "title": "Ótimo", "comment": "Voltarei." }
```

| Status | Meaning |
|---|---|
| 201 | The saved review (`reviewId`, `userId`, `placeId`, `rating`, `title`, `comment`, `createdAt`) |
| 400 | `User not found`, `Rating must be between 1 and 5`, `Place not found`, or `Title is too long (max 100 characters)` |

After saving, `places.avg_rating` is recalculated from all the reviews of the place. Photos of the
review are uploaded afterwards with `reviewId` set (see [Photos](#photos)).

### GET /reviews/by-place/{placeId}

Query: `userEmail` (optional, used to fill `currentUserReaction`).

Returns a list of `ReviewResponse`: `reviewId`, `userId`, `placeId`, `rating`, `title`, `comment`,
`createdAt`, `userName`, `userPhotoUrl`, `placeName`, `placeCoverUrl`, `likesCount`,
`dislikesCount`, `currentUserReaction` (`USEFUL`, `NOT_USEFUL` or null) and `reviewPhotoUrls`.
Status 200.

### GET /reviews/by-user

Query: `email` (required). Same `ReviewResponse` shape, for the reviews written by that user, with
`currentUserReaction` computed for that same user. An unknown email returns an empty list.
Status 200.

## Reactions

Controller: `ReactionController`.

### POST /reactions/toggle

Body: `{ "userEmail": "...", "reviewId": 3, "type": "USEFUL" }` where `type` is `USEFUL` or
`NOT_USEFUL`.

| Status | Body | Meaning |
|---|---|---|
| 200 | `Created` | The user had no reaction on this review |
| 200 | `Updated` | The user had the other reaction, which was switched |
| 200 | `Removed` | The user sent the same reaction again, which was removed |
| 400 | `User not found`, `Invalid reaction type` or `Review not found` | Plain text, not JSON |

A user has at most one reaction per review.

## Photos

Controller: `PhotoController`. Files are stored in `uploads/` with a random UUID name and served at
`/uploads/**`. The size limit is 10 MB per file and per request.

### POST /photos/upload

`multipart/form-data` fields:

| Field | Required | Meaning |
|---|---|---|
| `file` | yes | Image with extension `jpg`, `jpeg`, `png` or `webp` |
| `userId` | yes | User email or numeric user id |
| `placeId` | no | Place the photo belongs to |
| `reviewId` | no | Review the photo belongs to, or a special value (below) |

Special values of `reviewId`:

- Starts with `AVATAR`: the file becomes the profile photo of the user. No photo row is created.
- Starts with `STOP-EVIDENCE-`: the photo is stored with kind `EVIDENCE`, for a pedipaper stop.

Any other photo is stored with kind `GALLERY`.

| Status | Body | Meaning |
|---|---|---|
| 200 | `{"status": "Photo Saved", "id": 12, "url": "http://..."}` | Gallery or evidence photo |
| 200 | `{"status": "Avatar Updated", "url": "http://..."}` | Profile photo replaced |
| 400 | `{"error": "..."}` | Empty file, extension not allowed, `User not found`, `Place not found`, `Review not found` or `Invalid file path` |
| 500 | `{"error": "Could not store file"}` | The file could not be written |

### GET /photos/by-place/{placeId}

Gallery photos of a place. Each item: `photoId`, `url`, `placeId`, `userId`, `reviewId`, `kind`,
`status`. Status 200.

### GET /photos/by-user/{userIdOrEmail}

Gallery photos uploaded by a user, same shape. An unknown user returns an empty list. Status 200.

## Favourites

Controller: `FavoriteController`.

### GET /favorites

Query: `userEmail` (required). Returns the favourite places, most recently added first, with the
`PlaceResponse` fields `placeId`, `name`, `description`, `lat`, `lng`, `addressFull`, `categoryId`,
`authorId`, `avgRating` and `coverImageUrl`. Status 200.

### POST /favorites

Body: `{ "userEmail": "...", "placeId": 1 }`

| Status | Meaning |
|---|---|
| 201 | `{"message": "Favourite added"}` |
| 200 | `{"message": "Already a favourite"}` |
| 400 | Missing fields or `User not found` |
| 404 | `Place not found` |

### DELETE /favorites

Query: `userEmail`, `placeId` (both required). Status 204 whether or not the favourite existed.

## Visits

Controller: `VisitController`. A visit is a GPS check-in at a place ("Estive aqui").

### POST /visits

Body: `{ "userEmail": "...", "placeId": 1, "lat": 38.7785, "lng": -9.1017 }`

The position must be within 100 m of the place (haversine distance). The first valid check-in of a
user at a place stores the visit, adds 10 points and writes the ledger row `Visita: <place name>`.

| Status | Body | Meaning |
|---|---|---|
| 201 | `{"pointsAwarded": 10, "newBalance": 260, "visitCount": 3}` | First visit to this place |
| 200 | `{"pointsAwarded": 0, "newBalance": 260, "visitCount": 3}` | Repeated check-in: no new row, no points |
| 400 | `{"error": "..."}` | Missing fields, `User not found`, or `Estás demasiado longe do local (máximo 100 m)` |
| 404 | `{"error": "Place not found"}` | Unknown place |

### GET /visits/count

Query: `userEmail` (required). Returns `{"visitCount": 3}`; 0 for an unknown user. Status 200.

## Pedipapers

Controller: `PedipaperController`. A pedipaper is a route with ordered stops. The user joins it,
checks in at each stop, then completes it.

### GET /pedipapers

Active pedipapers: `pediId`, `name`, `description`, `totalPoints`, `active`, `createdAt`.
Status 200.

### GET /pedipapers/{id}/stops

Stops in order. Each item: `stopId`, `orderNum`, `taskDescription`, `requiresPhoto`, `placeId`,
`placeName`, `lat`, `lng`, `placeCoverUrl`. An unknown id returns an empty list. Status 200.

### POST /pedipapers/{id}/join

Query: `userEmail` (required).

| Status | Body |
|---|---|
| 200 | `{"message": "Joined successfully"}` or `{"message": "Already joined"}` |
| 400 | `{"error": "User not found"}` |
| 404 | `{"error": "Pedipaper not found"}` |

### POST /pedipapers/{id}/stops/{stopId}/checkin

Body: `{ "userEmail": "...", "lat": 38.7785, "lng": -9.1017, "photoId": 12 }`. `photoId` is the
`id` returned by the upload and is required only when the stop has `requiresPhoto = true`.

Checks, in this order: the user exists, the pedipaper exists, the user joined it, the stop belongs
to it, the stop was not checked in before (repeating is answered with the current counts), the
position is within 50 m of the stop's place, and for photo stops the photo exists, belongs to the
user and has kind `EVIDENCE`.

| Status | Body | Meaning |
|---|---|---|
| 200 | `{"completedStops": 2, "totalStops": 3}` | Check-in saved, or already saved |
| 400 | `{"error": "..."}` | Missing fields, `User not found`, `Ainda não entraste nesta rota`, `Paragem não pertence a esta rota`, `Paragem sem local associado`, `Estás demasiado longe da paragem (máximo 50 m)`, `Esta paragem exige uma foto`, `Foto inválida para esta paragem` |
| 404 | `{"error": "Pedipaper not found"}` | Unknown pedipaper |

### GET /pedipapers/{id}/progress

Query: `userEmail` (required).

```json
{ "joined": true, "completed": false, "completedStopIds": [1, 2], "totalStops": 3, "pointsAwarded": 0 }
```

For an unknown user or a user who has not joined, `joined` is false and the other fields are empty
or zero. Status 200.

### POST /pedipapers/{id}/complete

Query: `userEmail` (required). Awards `totalPoints` of the pedipaper once, and only if every stop has
a check-in. The ledger row is `Pedipaper concluído: <name>`.

| Status | Body | Meaning |
|---|---|---|
| 200 | `{"pointsAwarded": 500, "newBalance": 750}` | Completed now, or completed before (same values, no extra points) |
| 400 | `{"error": "..."}` | `User not found`, `Not participating in this route`, or `Ainda faltam paragens por visitar` |
| 404 | `{"error": "Pedipaper not found"}` | Unknown pedipaper |

## Coupons

Controller: `CouponController`.

### GET /coupons/active

Active coupons. The code is never included here.

```json
[ { "id": 1, "title": "Café Grátis", "description": "...", "code": null,
    "merchant": "Grupo Cantinho", "details": null, "cost": 100 } ]
```

Status 200.

### POST /coupons/redeem

Body: `{ "userEmail": "...", "couponId": 1 }`. Runs in one transaction: the balance is debited only
if it still covers the cost, a `redemptions` row is inserted and a negative ledger row
`Cupão resgatado: <title>` is written.

| Status | Body | Meaning |
|---|---|---|
| 200 | `{"message": "Success", "newBalance": 150, "code": "CAFE100"}` | Redeemed; this is where the code is revealed |
| 400 | `{"error": "..."}` | Missing fields, `User not found`, `Coupon is not active` or `Pontos insuficientes.` |
| 404 | `{"error": "Coupon not found"}` | Unknown coupon |

### GET /coupons/redeemed

Query: `email` (required). Coupons redeemed by the user, newest first:

```json
[ { "couponId": 1, "title": "Café Grátis", "merchantName": "Grupo Cantinho",
    "code": "CAFE100", "redeemedAt": "2025-12-01 14:30:00.0" } ]
```

Status 200.

## Moderation

Controller: `AdminController`. Every endpoint requires `adminEmail` to belong to a user with role
`ADMIN`; otherwise the answer is 403 with `{"error": "Acesso restrito a administradores"}`.

### GET /admin/places/pending

Query: `adminEmail`. Returns the `PENDING` places (full `PlaceResponse`), oldest first. Status 200.

### POST /admin/places/{id}/approve

Query: `adminEmail`. Sets `status = APPROVED` and `is_verified = TRUE`.

### POST /admin/places/{id}/reject

Query: `adminEmail`. Sets `status = REJECTED` and `is_verified = FALSE`.

Both return 200 `{"message": "Updated", "status": "APPROVED"}` (or `REJECTED`), 403 for a non-admin
and 404 `{"error": "Place not found"}` for an unknown place.

## Static files

### GET /uploads/{file}

Serves files from the `uploads/` folder of the backend working directory.
