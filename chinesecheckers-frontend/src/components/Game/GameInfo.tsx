import type { PlayerSlotData, PlayerColor, GamePhase } from '../../types/game';
import { PLAYER_COLORS } from '../../types/game';
import './GameInfo.css';

interface GameInfoProps {
  playerSlots: PlayerSlotData[];
  currentTurn: PlayerColor;
  phase: GamePhase;
  moveCount: number;
  winner: PlayerColor | null;
  finishOrder: PlayerColor[];
  aiAnimating: boolean;
}

export default function GameInfo({ playerSlots, currentTurn, phase, moveCount, winner, finishOrder, aiAnimating }: GameInfoProps) {
  return (
    <div className="game-info">
      <h3>Players</h3>
      <div className="player-list">
        {playerSlots.map((slot) => (
          <div
            key={slot.color}
            className={`player-item ${slot.color === currentTurn && phase === 'PLAYING' ? 'active-turn' : ''} ${slot.finished ? 'finished' : ''}`}
          >
            <span
              className="player-dot"
              style={{ backgroundColor: PLAYER_COLORS[slot.color] }}
            />
            <span className="player-name">
              {slot.color}
              {slot.type === 'AI' && ` (${slot.difficulty})`}
            </span>
            {slot.finished && slot.finishOrder && (
              <span className="finish-badge">#{slot.finishOrder}</span>
            )}
            {slot.color === currentTurn && phase === 'PLAYING' && slot.type === 'AI' && aiAnimating && (
              <span className="thinking">thinking...</span>
            )}
          </div>
        ))}
      </div>

      <div className="game-stats">
        <div className="stat">
          <span className="stat-label">Moves</span>
          <span className="stat-value">{moveCount}</span>
        </div>
        <div className="stat">
          <span className="stat-label">Turn</span>
          <span className="stat-value" style={{ color: PLAYER_COLORS[currentTurn] }}>{currentTurn}</span>
        </div>
      </div>

      {winner && (
        <div className="winner-display">
          <span style={{ color: PLAYER_COLORS[winner] }}>{winner}</span> wins!
        </div>
      )}

      {finishOrder.length > 0 && (
        <div className="rankings">
          <h4>Rankings</h4>
          {finishOrder.map((color, i) => (
            <div key={color} className="rank-item">
              <span className="rank-number">#{i + 1}</span>
              <span style={{ color: PLAYER_COLORS[color] }}>{color}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
