import { useEffect } from 'react';
import { useGame } from './hooks/useGame';
import NewGameDialog from './components/Common/NewGameDialog';
import GameView from './components/Game/GameView';
import type { CreateGameRequest } from './types/game';
import './App.css';

function extractPlatformParams() {
  const params = new URLSearchParams(window.location.search);
  const token = params.get('platformToken');
  const returnUrl = params.get('returnUrl');
  if (token) sessionStorage.setItem('platformToken', token);
  if (returnUrl) sessionStorage.setItem('platformReturnUrl', returnUrl);
  if (token || returnUrl) {
    params.delete('platformToken');
    params.delete('returnUrl');
    const clean = params.toString();
    const url = window.location.pathname + (clean ? '?' + clean : '');
    window.history.replaceState({}, '', url);
  }
}

export default function App() {
  useEffect(() => { extractPlatformParams(); }, []);
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
