# Career OS

Career OS is a learning-led career assistant. Stage 0 contains only a Next.js
frontend, a Spring Boot API, and one health check connecting them.

## Requirements

- Node.js 24 LTS
- npm 11 (included with Node.js 24)
- Java Development Kit (JDK) 21 or newer; JDK 25 LTS is recommended

A system Maven installation is not required; the backend includes the Maven
Wrapper. PostgreSQL, Docker, authentication, Gmail, and AI are intentionally not
part of this stage.

Check your installed versions:

```bash
node --version
npm --version
java -version
```

## Install the frontend

From the repository root:

```bash
cd apps/web
npm install
```

The backend downloads its Maven dependencies automatically the first time the
Maven Wrapper runs.

## Run locally

Open two terminals from the repository root.

Start the API:

```bash
cd apps/api
./mvnw spring-boot:run
```

Start the frontend:

```bash
cd apps/web
npm run dev
```

Then open:

- Frontend: http://localhost:3000
- API health endpoint: http://localhost:8080/api/v1/health

The frontend uses `http://localhost:8080` by default. To use another API URL,
copy `.env.example` to `apps/web/.env.local` and change `API_BASE_URL` there.
Because the health request is made by the Next.js server, Stage 0 does not need
browser CORS configuration.

## Run checks

Backend tests (requires JDK 21):

```bash
cd apps/api
./mvnw test
```

Frontend linting and type checking:

```bash
cd apps/web
npm run lint
npm run typecheck
```

You can also verify a production frontend build with `npm run build`.

## Known setup issues

- Java 17 cannot compile this project. Install JDK 21 or newer and ensure the
  correct JDK is active before running the backend. The project targets Java 21
  bytecode and has been tested locally with JDK 25 LTS.
- This project has been tested with Node.js 24 LTS. If `node --version` still
  reports an older installation after upgrading through nvm, reload the terminal
  or run `source ~/.zshrc`.
- The first Maven Wrapper run needs internet access to download Maven and the
  Spring dependencies.
- `npm audit` currently reports a moderate PostCSS advisory through Next.js.
  The suggested forced fix would install an incompatible Next.js version, so do
  not run `npm audit fix --force`; update when a compatible Next.js release
  includes the patched transitive dependency.
- If the frontend shows **API unavailable**, confirm the backend is running on
  port 8080 and refresh the page.
- If port 3000 or 8080 is already in use, stop the conflicting process or
  configure another port and update `API_BASE_URL` accordingly.
