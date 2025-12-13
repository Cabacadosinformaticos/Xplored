# Setup

How to run Xplored on a development machine: the MySQL database, the Spring Boot backend and the
Android app. The steps are the same on Windows, macOS and Linux except for the Gradle wrapper
command, shown for both.

## Requirements

| Tool | Version | Used for |
|---|---|---|
| MySQL Server | 8.x (8.4 used in development) | Database `xplored_db` |
| JDK | 21 | Backend (the Gradle build asks for a Java 21 toolchain) |
| Android Studio | Recent release with Android Gradle Plugin 8.13 support | Building and running the app |
| Android SDK | API 36 platform | `compileSdk` and `targetSdk` are 36, `minSdk` is 31 (Android 12) |
| Google Maps API key | Maps SDK for Android enabled | The map screen |

The Gradle wrappers (`back_end/gradlew`, `front_end/gradlew`) download Gradle themselves, so no
separate Gradle install is needed.

## 1. Database

From the repository root, with the MySQL client on the path:

```bash
mysql -u root -p < data_base/create.sql
mysql -u root -p < data_base/populate.sql
```

`create.sql` drops and recreates `xplored_db`, creates the 17 tables and inserts the five
categories. `populate.sql` fills the database with test data and starts with `USE xplored_db;`.
Run the two scripts in this order. Running `create.sql` again wipes the data.

`data_base/queries.sql` holds example queries and is optional.

## 2. Backend

The backend reads its database connection from environment variables. The defaults match a local
MySQL with user `root` and password `root`:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/xplored_db?useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | `root` |

Start the API from the `back_end` folder.

macOS and Linux:

```bash
cd back_end
export DB_PASSWORD=your_mysql_password   # only if it is not "root"
./gradlew bootRun
```

Windows (PowerShell):

```powershell
cd back_end
$env:DB_PASSWORD = "your_mysql_password"   # only if it is not "root"
.\gradlew.bat bootRun
```

The API listens on `http://localhost:9000`. A quick check:

```bash
curl http://localhost:9000/category/get-all
```

Start the backend from `back_end/`: it creates the `uploads/` folder in its working directory
and serves it at `/uploads/**`. That folder is ignored by Git.

`./gradlew compileJava compileTestJava` builds without a database. The test class
(`XploredApplicationTests`) loads the Spring context and needs a live MySQL.

## 3. Android app

1. Open the `front_end` folder in Android Studio (it is the Gradle root; the module is `app`).
2. Create `front_end/local.properties`. Android Studio creates it with `sdk.dir` when it syncs.
   Add the Maps key and, if needed, the backend URL:

   ```properties
   sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
   MAPS_API_KEY=your_google_maps_key
   # API_BASE_URL=http://10.0.2.2:9000/
   ```

   On macOS and Linux `sdk.dir` is a plain path such as `/Users/you/Library/Android/sdk`.
   `local.properties` is ignored by Git and must stay out of commits.
3. Create an emulator with API 31 or newer, or connect a device, and run the `app` configuration.

Values come from the Secrets Gradle Plugin: `local.properties` first, then
`front_end/local.defaults.properties`, which holds `MAPS_API_KEY=DEFAULT_API_KEY` and
`API_BASE_URL=http://10.0.2.2:9000/`. With the default key the app starts but the map tiles do not
load. In the emulator, `10.0.2.2` is the host machine, so the default URL reaches a backend
running on the same computer.

### Real device

A phone cannot reach `10.0.2.2`. Use the LAN address of the computer that runs the backend:

1. Set `API_BASE_URL=http://<lan-ip>:9000/` in `front_end/local.properties`.
2. Add the same address to `front_end/app/src/main/res/xml/network_security_config.xml`, because
   cleartext HTTP is allowed only for listed hosts (`10.0.2.2`, `localhost`, `127.0.0.1`):

   ```xml
   <domain includeSubdomains="false">192.168.1.5</domain>
   ```
3. Allow port 9000 in the firewall of the computer, and keep both devices on the same network.

The location features need the location permission and a GPS fix. In the emulator, set a position
in Extended controls, Location. Use coordinates inside Moscavide or Parque das Nações (for
example 38.7785, -9.1017) to see the seed places nearby.

## Test logins

`populate.sql` creates 13 users. The password of every one is `1234`.

| Email | Role | Notes |
|---|---|---|
| `tiago@xplored.pt` | ADMIN | Can open the moderation screen |
| `ana@xplored.pt` | USER | Has completed one pedipaper and has coupon history |
| `rita@xplored.pt` | USER | Has completed a pedipaper |
| `joao@loja.pt` | BUSINESS | Business owner account of the seed data |
| `miguel@xplored.pt`, `beatriz@xplored.pt`, `diogo@xplored.pt`, `ines@xplored.pt`, `pedro@xplored.pt`, `carolina@xplored.pt`, `andre@xplored.pt`, `sofia@xplored.pt`, `bruno@xplored.pt` | USER | Fill the leaderboard |

Places 17 and 21 in the seed are pending, so they appear in the moderation list of the ADMIN.
New accounts can also be created from the registration screen.

## Password reset codes

There is no mail service. When a user asks for a reset, the backend writes the 6 digit code to its
log at INFO level:

```text
Password reset code for ana@xplored.pt: 123456
```

Read it in the terminal that runs `bootRun`, type it in the app within 15 minutes, and choose a new
password of at least 6 characters. After 5 wrong attempts the code is locked and a new one must be
requested.

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `Communications link failure` at startup | MySQL is not running, or `DB_URL` points to another host or port |
| `Access denied for user 'root'` | Set `DB_USER` and `DB_PASSWORD` |
| `Unknown database 'xplored_db'` | `create.sql` was not run |
| Blank grey map | `MAPS_API_KEY` is missing, invalid, or the Maps SDK for Android is not enabled for the key |
| App cannot reach the API on a device | `API_BASE_URL` still points to `10.0.2.2`, or the LAN IP is missing from `network_security_config.xml` |
| Check-in answers "demasiado longe" | The device or emulator position is more than 100 m from the place (50 m for pedipaper stops) |
