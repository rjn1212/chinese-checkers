# Chinese Checkers

2-6 player Chinese Checkers web app. Java 21 / Spring Boot 3.3.5 backend, React 18 / TypeScript / Vite frontend. Maven multi-module build.

See `IMPLEMENTATION_PLAN.md` for the full build plan.

## Build & test

```bash
# Build everything (backend + frontend)
mvn clean package

# Run backend only
cd chinesecheckers-backend && mvn spring-boot:run

# Run frontend dev server (proxies /api to :8083)
cd chinesecheckers-frontend && npm run dev

# Run tests (85 tests across 8 test classes)
mvn test

# Quick start (builds + runs)
./start.sh
```

- Backend: http://localhost:8083
- Frontend dev: http://localhost:5176 (proxied to backend)

## Project structure

- `chinesecheckers-backend/` — Spring Boot REST API, all game logic, AI engine
- `chinesecheckers-frontend/` — React SPA, bundled into backend's `static/` by Maven

## Key concepts

### Board representation

121-position star board stored as `Map<Position, Piece>`. 17 rows (0-16) with variable widths. Adjacency computed via canonical X-coordinate: `canonicalX(row, col) = (13 - rowWidth) / 2.0 + col`. Two positions are adjacent if same row and |col diff|==1, or adjacent rows and |canonicalX diff|==0.5.

### Player slots

Any slot can be HUMAN or AI. Valid player counts: 2, 3, 4, or 6. Each player gets 10 pieces in a home triangle; goal is the opposite triangle. Colors: RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE.

### Movement

Step to adjacent empty position, OR hop over adjacent piece to empty space beyond — jumped piece stays (no captures). Hop chains allowed (keep hopping in any direction). Jumping is never mandatory. Pieces cannot leave their goal triangle once entered.

### AI

Three levels: EASY (random with advancement bias), MEDIUM (heuristic scoring), HARD (paranoid minimax with alpha-beta pruning, depth adapts to player count).

### API pattern

After a human move, the server auto-plays all consecutive AI turns and returns all AI moves in the same response. Frontend animates them sequentially with 700ms delays, using intermediate board states for smooth piece-by-piece animation.

### Frontend architecture

- **SVG board** renders all 121 positions inline in `Board.tsx` (no separate position/piece components)
- **`useGame` hook** uses `useReducer` with `preAiGame`/`finalGame` split to animate AI moves incrementally rather than jumping to the final state
- **Game-over and surrender** handled inline in `GameView`/`GameInfo` (no separate dialog)
- **Mobile-responsive**: full-width board on phones, enlarged invisible touch targets (1.4x), `touch-action: manipulation`, safe-area-inset support for notched devices

## Platform integration

This game integrates with the game-platform portal at `/Users/randyn/projects/git/game-platform`.

- CORS origins are configurable via `cors.allowed-origins` property
- Frontend API client supports auth headers via `getHeaders()` pattern
- Platform health endpoint: `GET /platform/health`
- Game API namespace: `/api/v1/games/`
- `games.json` entry needed in game-platform repo for registration
