Authentication & Users

Overview
- App uses a simple username/password auth backed by `UserRepository` and a separate `authdb` datasource.
- Login returns a JWT stored in `localStorage` under `token` and `username`.

Frontend
- Use the login form presented at app start. Registration is available from the same screen.

Endpoints
- POST /api/auth/register — body { username, password }
- POST /api/auth/login — body { username, password } (returns { token, username })

Notes
- Keep tokens secure. For production, rotate secrets and use HTTPS.
