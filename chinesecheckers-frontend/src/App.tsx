import { useGame } from './hooks/useGame';
import NewGameDialog from './components/Common/NewGameDialog';
import GameView from './components/Game/GameView';
import type { CreateGameRequest } from './types/game';
import './App.css';

export default function App() {
  const {
    game, selectedPiece, legalMoves, loading, error,
    aiAnimating, aiMoveQueue, lastMove, activeColors,
    createGame, selectPiece, processNextAiMove, clearError, resetGame,
  } = useGame();

  function handleNewGame(request: CreateGameRequest) {
    createGame(request);
  }

  function handleBackToSetup() {
    resetGame();
  }

  return (
    <div className="app">
      <div className="app-header">
        <h1>Chinese Checkers</h1>
      </div>

      {error && (
        <div className="error-banner">
          <span>{error}</span>
          <button onClick={clearError}>Dismiss</button>
        </div>
      )}

      <div className="app-content">
        {!game && !loading && (
          <NewGameDialog onStart={handleNewGame} />
        )}

        {loading && !game && (
          <div className="loading-spinner">Creating game...</div>
        )}

        {game && (
          <GameView
            game={game}
            selectedPiece={selectedPiece}
            legalMoves={legalMoves}
            onPositionClick={selectPiece}
            aiAnimating={aiAnimating}
            aiMoveQueue={aiMoveQueue}
            processNextAiMove={processNextAiMove}
            lastMove={lastMove}
            activeColors={activeColors}
            onNewGame={handleBackToSetup}
          />
        )}
      </div>
    </div>
  );
}
