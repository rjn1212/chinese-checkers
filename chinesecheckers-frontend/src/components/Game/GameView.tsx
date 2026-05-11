import { useEffect } from 'react';
import Board from '../Board/Board';
import GameInfo from './GameInfo';
import type { GameState, MoveData, PlayerColor } from '../../types/game';
import './GameView.css';

interface GameViewProps {
  game: GameState;
  selectedPiece: { row: number; col: number } | null;
  legalMoves: MoveData[];
  onPositionClick: (row: number, col: number) => void;
  aiAnimating: boolean;
  aiMoveQueue: MoveData[];
  processNextAiMove: () => void;
  lastMove: { from: { row: number; col: number }; to: { row: number; col: number } } | null;
  activeColors: PlayerColor[];
  onNewGame: () => void;
}

export default function GameView({
  game, selectedPiece, legalMoves, onPositionClick,
  aiAnimating, aiMoveQueue, processNextAiMove, lastMove,
  activeColors, onNewGame,
}: GameViewProps) {

  useEffect(() => {
    if (aiAnimating && aiMoveQueue.length > 0) {
      const timer = setTimeout(processNextAiMove, 700);
      return () => clearTimeout(timer);
    }
  }, [aiAnimating, aiMoveQueue, processNextAiMove]);

  const isHumanTurn = !aiAnimating &&
    game.phase === 'PLAYING' &&
    game.playerSlots.find(s => s.color === game.currentTurn)?.type === 'HUMAN';

  return (
    <div className="game-view">
      <div className="board-container">
        <Board
          pieces={game.board.pieces}
          selectedPiece={selectedPiece}
          legalMoves={legalMoves}
          onPositionClick={onPositionClick}
          disabled={!isHumanTurn}
          lastMove={lastMove}
          activeColors={activeColors}
        />
      </div>
      <div className="info-container">
        <GameInfo
          playerSlots={game.playerSlots}
          currentTurn={game.currentTurn}
          phase={game.phase}
          moveCount={game.moveCount}
          winner={game.winner}
          finishOrder={game.finishOrder}
          aiAnimating={aiAnimating}
        />
        {game.phase === 'GAME_OVER' && (
          <button className="new-game-btn" onClick={onNewGame}>New Game</button>
        )}
        {game.phase === 'PLAYING' && (
          <button className="surrender-btn" onClick={onNewGame}>Surrender</button>
        )}
      </div>
    </div>
  );
}
