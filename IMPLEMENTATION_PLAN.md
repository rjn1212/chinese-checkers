# Chinese Checkers -- Full Implementation Plan

## Context

Build a complete 2-6 player Chinese Checkers game following the established Stratego/Checkers architecture pattern: Java 21 / Spring Boot 3.3.5 backend + React 18 / TypeScript / Vite frontend, Maven multi-module build. Any player slot can be human or AI (up to 5 AI). Backend on :8083, frontend on :5176. The game integrates with the game-platform portal via the established contract.

---

## Architecture Overview

```
┌──────────────────┐     REST API (JSON)     ┌────────────────────────────┐
│  React Web App   │ <─────────────────────> │  Spring Boot Backend       │
│  (Vite + TS)     │    /api/v1/*            │                            │
│  :5176           │                         │  ┌──────────────────────┐  │
└──────────────────┘                         │  │  GameController      │  │
                                             │  └──────────┬───────────┘  │
┌──────────────────┐                         │  ┌──────────▼───────────┐  │
│  React Native    │ <─── same API ────────> │  │  GameService         │  │
│  (future)        │                         │  │  (turn cycling,      │  │
└──────────────────┘                         │  │   AI auto-play)      │  │
                                             │  └──────────┬───────────┘  │
┌──────────────────┐                         │  ┌──────────▼───────────┐  │
│  Platform Portal │ <─ /platform/health ──> │  │  GameEngine          │  │
│  (Next.js)       │                         │  │  MoveValidator       │  │
└──────────────────┘                         │  │  WinConditionChecker │  │
                                             │  │  BoardInitializer    │  │
                                             │  └──────────┬───────────┘  │
                                             │  ┌──────────▼───────────┐  │
                                             │  │  AI Engine           │  │
                                             │  │  Easy/Medium/Hard    │  │
                                             │  └──────────────────────┘  │
                                             │  :8083                     │
                                             └────────────────────────────┘
```

**Key decisions (matching Stratego/Checkers):**
- Server-authoritative: all game logic on server, clients are pure renderers
- Synchronous AI: after a human move, server auto-plays all consecutive AI turns and returns all AI moves in the same HTTP response
- In-memory storage: `ConcurrentHashMap<String, Game>` -- no database for v1
- Multi-player turn cycling: turns cycle through players in order; any slot can be human or AI
- No fog of war, no setup phase (pieces start in fixed positions)

---

## Project Structure

```
ChineseCheckers/
├── pom.xml                                     (parent POM, modules: backend + frontend)
├── IMPLEMENTATION_PLAN.md
├── CLAUDE.md
├── start.sh
├── .gitignore
├── chinesecheckers-backend/
│   ├── pom.xml                                 (Spring Boot 3.3.5, Java 21)
│   └── src/
│       ├── main/java/com/gitgames/chinesecheckers/
│       │   ├── ChineseCheckersApplication.java
│       │   ├── config/
│       │   │   ├── WebConfig.java              (CORS, SPA forwarding)
│       │   │   └── JacksonConfig.java
│       │   ├── controller/
│       │   │   ├── GameController.java
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   └── dto/
│       │   │       ├── CreateGameRequest.java
│       │   │       ├── MakeMoveRequest.java
│       │   │       ├── GameStateResponse.java
│       │   │       ├── MoveResponse.java
│       │   │       ├── LegalMovesResponse.java
│       │   │       ├── BoardDto.java
│       │   │       ├── PieceDto.java
│       │   │       ├── MoveDto.java
│       │   │       ├── PositionDto.java
│       │   │       ├── PlayerSlotDto.java
│       │   │       └── ErrorResponse.java
│       │   ├── model/
│       │   │   ├── Game.java                   (aggregate root, 2-6 player slots)
│       │   │   ├── Board.java                  (121-position star graph)
│       │   │   ├── Piece.java                  (id, owner, position)
│       │   │   ├── PlayerColor.java            (enum: RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE)
│       │   │   ├── PlayerSlot.java             (color, type, difficulty, finished, finishOrder)
│       │   │   ├── PlayerType.java             (enum: HUMAN, AI)
│       │   │   ├── GamePhase.java              (enum: PLAYING, GAME_OVER)
│       │   │   ├── Position.java               (record: row, col)
│       │   │   ├── Move.java                   (from, to, path for hop chains)
│       │   │   ├── MoveRecord.java
│       │   │   └── AiDifficulty.java           (enum: EASY, MEDIUM, HARD)
│       │   ├── engine/
│       │   │   ├── GameEngine.java             (orchestrates turns, applies moves)
│       │   │   ├── MoveValidator.java          (step + hop chain detection via DFS)
│       │   │   ├── WinConditionChecker.java    (goal triangle occupancy)
│       │   │   └── BoardInitializer.java       (2/3/4/6 player setups)
│       │   ├── ai/
│       │   │   ├── AiStrategy.java             (interface)
│       │   │   ├── EasyAiStrategy.java         (random with advancement bias)
│       │   │   ├── MediumAiStrategy.java       (heuristic scoring)
│       │   │   └── HardAiStrategy.java         (minimax/paranoid with alpha-beta)
│       │   ├── service/
│       │   │   ├── GameService.java
│       │   │   ├── GameRepository.java         (ConcurrentHashMap wrapper)
│       │   │   ├── GameNotFoundException.java
│       │   │   └── InvalidMoveException.java
│       │   └── platform/
│       │       └── PlatformController.java     (GET /platform/health)
│       └── test/java/com/gitgames/chinesecheckers/
│           ├── model/PositionTest.java         (9 tests)
│           ├── model/BoardTest.java            (24 tests)
│           ├── engine/BoardInitializerTest.java (8 tests)
│           ├── engine/MoveValidatorTest.java   (14 tests)
│           ├── engine/WinConditionCheckerTest.java (5 tests)
│           ├── engine/GameEngineTest.java      (8 tests)
│           ├── ai/AiStrategyTest.java          (6 tests)
│           └── controller/GameControllerTest.java (11 tests)
├── chinesecheckers-frontend/
│   ├── pom.xml                                 (frontend-maven-plugin, Node v22.15.0)
│   ├── package.json
│   ├── index.html                              (viewport-fit=cover for notched phones)
│   ├── vite.config.ts                          (port 5176, proxy /api to :8083)
│   ├── tsconfig.json, tsconfig.app.json, tsconfig.node.json
│   └── src/
│       ├── main.tsx
│       ├── App.tsx                             (routes: NewGameDialog → GameView)
│       ├── App.css                             (safe-area-insets, mobile padding)
│       ├── index.css                           (dark theme base, dvh units)
│       ├── api/gameApi.ts                      (fetch wrapper, extensible auth headers)
│       ├── types/game.ts                       (mirrors API types, color hex maps)
│       ├── hooks/useGame.ts                    (useReducer, AI move queue with
│       │                                        preAiGame/finalGame split)
│       └── components/
│           ├── Board/
│           │   ├── Board.tsx                   (SVG renderer: 121 positions, pieces,
│           │   │                                selection, legal moves, touch targets)
│           │   ├── Board.css                   (touch-action, tap highlight, transitions)
│           │   └── boardLayout.ts              (pixel coords for 121 positions)
│           ├── Game/
│           │   ├── GameView.tsx                (board + info + surrender/new-game)
│           │   ├── GameView.css                (flex→column on mobile)
│           │   ├── GameInfo.tsx                (player list, stats, winner, rankings)
│           │   └── GameInfo.css                (grid player list on mobile)
│           └── Common/
│               ├── NewGameDialog.tsx            (player count, slot config, difficulty)
│               └── NewGameDialog.css            (responsive slot rows, 44px+ buttons)
```

---

## Chinese Checkers Rules

- **Board:** 6-pointed star with 121 positions. 6 triangles (10 positions each) + central hexagon (61 positions).
- **Players:** 2, 3, 4, or 6 (not 5 -- no symmetric arrangement). Each gets 10 pieces in a home triangle.
- **Setup by player count:** 2 = opposite triangles. 3 = alternating. 4 = two opposite pairs. 6 = all.
- **Movement:** Step to any adjacent empty position (1 space, 6 directions). OR hop over an adjacent piece to the empty space beyond -- jumped piece stays on the board (no captures ever).
- **Chain hops:** After a hop, may continue hopping in any direction. Stopping is always optional -- player can end the hop chain at any intermediate landing.
- **Jumping is NOT mandatory** (unlike Checkers). Player can always choose a step instead.
- **Goal-exit restriction:** Once a piece enters its goal triangle, it cannot leave.
- **Win:** First player to move all 10 pieces into the opposite triangle wins. Game continues for remaining rankings (2nd, 3rd, etc.).

---

## Board Representation -- The 121-Position Star

### Row Layout

17 rows (0-16) with variable widths:

| Rows | Positions per row | Region |
|------|------------------|--------|
| 0-3  | 1, 2, 3, 4       | Top triangle (RED home) |
| 4-12 | 13, 12, 11, 10, 9, 10, 11, 12, 13 | Hex body (includes side triangles) |
| 13-16 | 4, 3, 2, 1      | Bottom triangle (GREEN home) |

### Triangle Assignments

| Triangle | Color | Positions |
|----------|-------|-----------|
| Top | RED | (0,0), (1,0-1), (2,0-2), (3,0-3) |
| Upper-right | ORANGE | (4,9-12), (5,9-11), (6,9-10), (7,9) |
| Lower-right | YELLOW | (9,9), (10,9-10), (11,9-11), (12,9-12) |
| Bottom | GREEN | (13,0-3), (14,0-2), (15,0-1), (16,0) |
| Lower-left | BLUE | (9,0), (10,0-1), (11,0-2), (12,0-3) |
| Upper-left | PURPLE | (4,0-3), (5,0-2), (6,0-1), (7,0) |

Each player's goal is the opposite triangle (RED->GREEN, ORANGE->BLUE, YELLOW->PURPLE).

### Adjacency via Canonical X-Coordinate

Each position (row, col) maps to a canonical x-coordinate:

```java
double canonicalX(int row, int col) {
    int width = getPositionCountForRow(row);
    return (13.0 - width) / 2.0 + col;
}
```

Two positions are adjacent iff:
- **Same row** and `|col1 - col2| == 1` (horizontal neighbor)
- **Adjacent rows** (`|row1 - row2| == 1`) and `|canonicalX(r1,c1) - canonicalX(r2,c2)| == 0.5` (diagonal neighbor)

This handles all transitions uniformly, including triangle-to-hexbody edges. The adjacency graph is pre-computed at class load time as a static `Map<Position, List<Position>>`.

### Hop Landing Calculation

For a hop from A over B, landing C is at:
- `rowC = 2 * B.row - A.row`
- `canonicalX(C) = 2 * canonicalX(B) - canonicalX(A)`
- Convert back: `colC = canonicalX(C) - (13 - widthOfRowC) / 2.0`
- Valid only if rowC is in range 0-16, colC is a valid integer index, and the position is empty.

---

## Domain Model

### Key Classes

**PlayerColor** -- 6 colors in turn order with `opposite()` and `next()`:
RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE

**PlayerSlot** -- one seat at the table:
```
color, type (HUMAN|AI), difficulty (null if HUMAN), finished, finishOrder, platformUserId
```

**Game** (aggregate root):
```
id, board, List<PlayerSlot> playerSlots (2-6), currentTurnIndex, phase, winner,
List<MoveRecord> moveHistory, createdAt, lastActivityAt
```
Turn advances by incrementing `currentTurnIndex`, skipping finished players.

**Board** -- `Map<Position, Piece>` over 121 valid positions, plus static adjacency graph and triangle sets. `deepCopy()` for AI search.

**Move** -- `from (Position)`, `to (Position)`, `path (List<Position>)` for hop chains.

**Piece** -- `id (String)`, `owner (PlayerColor)`, `row`, `col`. Cloneable for AI deepCopy.

---

## Movement Validation (MoveValidator)

### Step Moves
For each neighbor of the piece's position: if empty and doesn't violate goal-exit restriction, it's a valid step.

### Hop Chains (DFS)
```
getHopMoves(board, piece):
  visited = {piece.position}
  dfs(current=piece.position, visited, pathSoFar=[], results)

dfs(current, visited, pathSoFar, results):
  for each neighbor of current:
    if neighbor is empty: skip (need a piece to hop over)
    landing = compute landing position beyond neighbor
    if landing invalid, occupied, or visited: skip
    if goal-exit violation: skip
    add landing to visited, append to path
    record Move(from=piece.position, to=landing, path=copy of path) as valid
    recurse: dfs(landing, visited, path, results)
    remove landing from visited (backtrack)
```

### No Forced Jumps
Unlike Checkers, both step and hop moves are always available. `getLegalMoves()` returns all of them.

---

## REST API Contract

### Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/games` | Create game with player slot config |
| GET | `/api/v1/games/{id}` | Get game state |
| DELETE | `/api/v1/games/{id}` | Abandon game |
| GET | `/api/v1/games/{id}/moves?row={r}&col={c}` | Legal moves for piece |
| POST | `/api/v1/games/{id}/moves` | Make a move (human) |
| GET | `/platform/health` | Platform health check |

### POST /api/v1/games -- Create Game

Request:
```json
{
  "playerSlots": [
    { "color": "RED",    "type": "HUMAN", "difficulty": null },
    { "color": "ORANGE", "type": "AI",    "difficulty": "MEDIUM" },
    { "color": "GREEN",  "type": "AI",    "difficulty": "HARD" }
  ]
}
```
Valid slot counts: 2, 3, 4, or 6. Colors must match standard configurations for the count. At least 1 human required. If first player is AI, their moves are auto-played and included in the response.

### POST /api/v1/games/{id}/moves -- Make Move

Request: `{ "from": {"row": 3, "col": 2}, "to": {"row": 4, "col": 6} }`

Response includes `humanMove` + `aiMoves[]` (0-5 AI moves that occurred before the next human's turn) + updated board state:
```json
{
  "humanMove": { "player": "RED", "from": {...}, "to": {...}, "path": [...] },
  "aiMoves": [
    { "player": "ORANGE", "from": {...}, "to": {...}, "path": [...] },
    { "player": "GREEN", "from": {...}, "to": {...}, "path": [...] }
  ],
  "board": { "pieces": [...] },
  "phase": "PLAYING",
  "currentTurn": "RED",
  "playerSlots": [...],
  "winner": null,
  "finishOrder": []
}
```

### Error Responses
`GAME_NOT_FOUND`, `INVALID_MOVE`, `NOT_YOUR_TURN`, `GAME_OVER`, `INVALID_GAME_CONFIG`

---

## AI Engine

### Interface
```java
Move selectMove(Board board, PlayerColor player, List<PlayerSlot> allPlayers);
```

### Easy AI
Random move selection with 70% bias toward moves that advance pieces toward the goal.

### Medium AI (Heuristic Scoring)
Score each move and pick the highest:

| Heuristic | Weight | Description |
|-----------|--------|-------------|
| Distance reduction | +10/row | Total distance of all pieces to goal |
| Goal occupancy | +50/piece | Pieces already in goal triangle |
| Hop chain length | +5/hop | Prefer longer chains |
| Center control | +3 | Pieces in central hexagon |
| Home exit | +15 | Moves that leave starting triangle |

### Hard AI (Paranoid Minimax)
Multi-player minimax treating all opponents as one adversary. Alpha-beta pruning. Depth adapts to player count: 4 (2P), 3 (3-4P), 2 (6P). 2-second time limit. Move ordering via Medium heuristic scores. Top-15 move pruning per ply.

---

## Frontend Design

### SVG Board (boardLayout.ts)
Pre-compute pixel coordinates for 121 positions using the row-count array. Center each row horizontally. Use hex spacing ratio (sqrt(3)/2) for vertical spacing. SVG `viewBox` of 560x~700 scales responsively via CSS `width: 100%`.

### Component Architecture
Positions, pieces, selection highlights, legal move indicators, and touch targets are all rendered inline within `Board.tsx` as SVG circles -- no separate StarPosition or Piece components. This keeps the render tree flat for performance with 121 positions.

### Component Flow
1. **App.tsx** -- routes between NewGameDialog (no game) and GameView (active game)
2. **NewGameDialog** -- player count selector (2/3/4/6), per-slot HUMAN/AI toggle + difficulty dropdown, start button
3. **Board.tsx** -- SVG with 121 position groups, each containing: background circle (neutral or triangle-tinted), legal move indicator dot, piece circle with color, invisible enlarged touch target circle
4. **GameView.tsx** -- board + GameInfo sidebar (column layout on mobile), surrender/new-game buttons
5. **GameInfo.tsx** -- player list with color dots, turn indicator, AI thinking indicator, move counter, winner display, finish rankings

### AI Move Animation
The `useGame` hook splits the server response into `preAiGame` (board after human move only) and `finalGame` (board after all moves). AI moves are applied incrementally to `preAiGame` with 700ms delays via `setTimeout`. When the last AI move is applied, the state swaps to `finalGame` to pick up metadata (phase, playerSlots, winner, finishOrder). CSS transitions on piece circle `cx`/`cy` attributes provide smooth sliding animation.

### Interaction Flow
1. Tap piece -> GET `/moves?row&col` -> highlight legal destinations (green dots)
2. Tap destination -> POST `/moves` -> apply human move, receive AI moves
3. Animate AI moves sequentially (700ms each) -> re-enable human input

### Mobile Responsiveness
- **Board sizing**: SVG takes full viewport width on phones (4px padding). On a 360px phone, the board renders at 352px with ~25px center-to-center spacing between positions.
- **Touch targets**: Invisible circles at 1.4x the visual radius provide 28-34px touch targets on phones.
- **Layout**: Flex row on desktop (board + info side-by-side) switches to column on mobile (board stacked above info).
- **Touch behavior**: `touch-action: manipulation` prevents double-tap zoom while allowing pinch-zoom. `-webkit-tap-highlight-color: transparent` removes blue flash. Hover effects gated behind `@media (hover: hover)`.
- **Safe areas**: `viewport-fit=cover` in meta tag + `env(safe-area-inset-*)` padding for notched phones.
- **Dynamic viewport**: Uses `100dvh` to handle mobile browser chrome correctly.
- **NewGameDialog**: `width: 100%` with `max-width: 500px` (no min-width that would overflow small phones). Slot controls wrap on narrow screens. Buttons have `min-height: 44-48px` for finger tapping.
- **GameInfo**: Player list switches to a responsive grid on mobile for compactness.

### Player Colors
| Color | Hex | Light (triangle tint) |
|-------|-----|----------------------|
| RED | #E53935 | #FFCDD2 |
| ORANGE | #FB8C00 | #FFE0B2 |
| YELLOW | #FDD835 | #FFF9C4 |
| GREEN | #43A047 | #C8E6C9 |
| BLUE | #1E88E5 | #BBDEFB |
| PURPLE | #8E24AA | #E1BEE7 |

---

## Implementation Phases

### Phase 1: Project Skeleton + Domain Model ✅
- Maven parent POM + `chinesecheckers-backend` + `chinesecheckers-frontend` modules
- All model classes (PlayerColor, PlayerType, AiDifficulty, GamePhase, Position, Piece, PlayerSlot, Board, Move, MoveRecord, Game)
- Board.java: 121 valid positions, pre-computed adjacency graph via canonical-x, triangle sets, deepCopy()
- Spring Boot app, WebConfig (CORS for :5176), JacksonConfig, application.properties (port 8083)
- React Vite project, vite.config.ts (port 5176, proxy)
- Unit tests for Position validity, Board adjacency correctness, triangle membership
- **Deliverable:** Both projects build; adjacency graph verified by tests

### Phase 2: Board Initialization + Engine Core ✅
- BoardInitializer: place 10 pieces per player for 2/3/4/6 player configs
- MoveValidator: getLegalMoves, getStepMoves, getHopMoves (DFS chain), goal-exit restriction
- WinConditionChecker: hasPlayerFinished, isGameOver
- GameEngine: validate -> apply -> check win orchestration
- Unit tests: step moves, hop chains, goal-exit, win detection, board init for all player counts
- **Deliverable:** Complete game engine playable programmatically

### Phase 3: AI Engine ✅
- AiStrategy interface
- EasyAiStrategy (random + advancement bias)
- MediumAiStrategy (heuristic scoring)
- HardAiStrategy (paranoid minimax, alpha-beta, depth/time limited, top-15 move pruning)
- Unit tests: all AIs return valid moves, Medium prefers hops, Hard completes within time limit, 6-player works
- **Deliverable:** Three AI difficulty levels functional

### Phase 4: REST API + Service Layer ✅
- GameRepository, GameService (create, getGame, getLegalMoves, makeMove with AI turn cycling, abandon)
- DTOs: CreateGameRequest, MakeMoveRequest, GameStateResponse, MoveResponse, LegalMovesResponse, etc.
- GameController, GlobalExceptionHandler
- Integration tests via MockMvc
- **Deliverable:** Complete game playable via curl

### Phase 5: Frontend -- Board + Game Flow ✅
- TypeScript types (game.ts) with color hex maps, API client (gameApi.ts) with platform token support
- boardLayout.ts (121-position pixel coordinates using hex spacing)
- SVG Board with inline position/piece rendering, triangle zone tinting, selection/legal-move highlights
- useGame hook (useReducer with preAiGame/finalGame split for incremental AI animation)
- GameView + GameInfo (turn indicator, player list, stats, winner, rankings)
- NewGameDialog (player count selector, per-slot HUMAN/AI toggle, difficulty dropdown)
- Game-over and surrender handled inline (no separate dialog needed)
- AI move animation (700ms sequential playback with CSS transitions)
- Dark theme styling
- **Deliverable:** Fully playable game in browser

### Phase 6: Platform Integration ✅
- PlatformController (GET /platform/health)
- Frontend platformToken handling via sessionStorage
- start.sh script (builds both modules, starts backend with bundled frontend)
- CLAUDE.md and IMPLEMENTATION_PLAN.md
- **Deliverable:** Game launches from platform portal

### Phase 7: Mobile Responsiveness + Polish ✅
- Full-width board on phones with minimal padding (4px)
- Enlarged invisible touch targets (1.4x visual radius) for finger tapping
- `touch-action: manipulation` to prevent double-tap zoom
- `-webkit-tap-highlight-color: transparent` to remove blue tap flash
- Hover effects gated behind `@media (hover: hover)` (no sticky hover on touch)
- `viewport-fit=cover` + `env(safe-area-inset-*)` for notched phones
- `100dvh` for dynamic viewport height (mobile browser chrome)
- Responsive NewGameDialog (no min-width overflow, wrapping slot controls, 44px+ buttons)
- GameInfo grid layout for player list on mobile
- Surrender button during gameplay, New Game button on game over
- Error banner with dismiss button
- Loading states
- 85 backend tests passing across 8 test classes
- **Deliverable:** Production-quality game, playable on desktop and mobile

---

## Key Reference Files (existing games followed as patterns)

- `Checkers/checkers-backend/.../service/GameService.java` -- turn cycling + AI auto-play pattern (adapted from 2-player to N-player)
- `Checkers/checkers-backend/.../model/Board.java` -- board with deepCopy() pattern (replaced 8x8 grid with Map<Position, Piece>)
- `Checkers/checkers-backend/.../engine/MoveValidator.java` -- recursive move search pattern (adapted from directional jumps to omnidirectional hops)
- `Checkers/checkers-frontend/src/hooks/useGame.ts` -- frontend state management pattern (extended with preAiGame/finalGame split)
- `Stratego/stratego-backend/pom.xml` -- Maven/Spring Boot dependency pattern
- `game-platform/ARCHITECTURE.md` -- platform contract specification
- `game-platform/games.json` -- game registration format

---

## Test Coverage (85 tests)

| Test Class | Count | Covers |
|------------|-------|--------|
| PositionTest | 9 | Validity checks for all row/col combinations |
| BoardTest | 24 | 121 positions, adjacency symmetry, 1-6 neighbors, row transitions, triangles (10 each, no overlap, goals=opposite homes), piece ops, deepCopy, hop landings |
| BoardInitializerTest | 8 | 2/3/4/6 player setups, piece counts, triangle placement, invalid configs |
| MoveValidatorTest | 14 | Step moves, blocking, hops, chain hops, own-piece hops, goal-exit restriction, hex body moves |
| WinConditionCheckerTest | 5 | Finish detection, partial fill, game over conditions |
| GameEngineTest | 8 | Apply move, validation, wrong player, empty square, piece movement |
| AiStrategyTest | 6 | All 3 AIs return valid moves, Medium prefers advancing, Hard completes within time, 6-player |
| GameControllerTest | 11 | Create game, get game, 404, legal moves, make move with AI response, invalid move 400, delete, health endpoint, invalid config, 6-player |

---

## Verification Plan

1. **Unit tests:** `mvn test` -- 85 tests across adjacency graph, move validation, hop chains, win conditions, AI, board init, controller
2. **API testing:** curl to create 2-player game, make moves, verify AI responds
3. **Browser testing:**
   - 2-player (human vs AI): full game through to victory
   - 4-player (1 human, 3 AI): verify 3 sequential AI moves per turn
   - 6-player (2 human, 4 AI): verify turn cycling alternates between humans
4. **Mobile testing:**
   - NewGameDialog fits on 360px wide screens without horizontal scroll
   - Board touch targets are usable on phones
   - No double-tap zoom on the board
   - Safe areas respected on notched phones
5. **Edge cases:** hop chains crossing center, goal-exit restriction, AI-only opening turns, 5 consecutive AI moves in response
6. **Performance:** 6-player Hard AI game -- response time under 3 seconds
