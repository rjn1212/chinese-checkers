import { useState } from 'react';
import type { PlayerColor, PlayerType, AiDifficulty, CreateGameRequest } from '../../types/game';
import { PLAYER_COLORS, VALID_CONFIGS } from '../../types/game';
import './NewGameDialog.css';

interface SlotConfig {
  color: PlayerColor;
  type: PlayerType;
  difficulty: AiDifficulty;
}

interface NewGameDialogProps {
  onStart: (request: CreateGameRequest) => void;
}

const PLAYER_COUNTS = [2, 3, 4, 6] as const;

export default function NewGameDialog({ onStart }: NewGameDialogProps) {
  const [playerCount, setPlayerCount] = useState<number>(2);
  const [slots, setSlots] = useState<SlotConfig[]>(buildSlots(2));
  const [showRules, setShowRules] = useState(false);

  function buildSlots(count: number): SlotConfig[] {
    const colors = VALID_CONFIGS[count];
    return colors.map((color, i) => ({
      color,
      type: i === 0 ? 'HUMAN' : 'AI',
      difficulty: 'MEDIUM' as AiDifficulty,
    }));
  }

  function handleCountChange(count: number) {
    setPlayerCount(count);
    setSlots(buildSlots(count));
  }

  function handleTypeChange(index: number, type: PlayerType) {
    setSlots(prev => prev.map((s, i) => i === index ? { ...s, type } : s));
  }

  function handleDifficultyChange(index: number, difficulty: AiDifficulty) {
    setSlots(prev => prev.map((s, i) => i === index ? { ...s, difficulty } : s));
  }

  const hasHuman = slots.some(s => s.type === 'HUMAN');

  function handleStart() {
    if (!hasHuman) return;
    onStart({
      playerSlots: slots.map(s => ({
        color: s.color,
        type: s.type,
        difficulty: s.type === 'AI' ? s.difficulty : null,
      })),
    });
  }

  return (
    <div className="new-game-overlay">
      <div className="new-game-dialog">
        <h2>Chinese Checkers</h2>

        <button className="how-to-play-link" onClick={() => setShowRules(!showRules)}>
          {showRules ? 'Hide Rules' : 'How to Play'}
        </button>

        {showRules && (
          <div className="rules-panel">
            <h3>Objective</h3>
            <p>Move all 10 of your pieces from your home triangle to the opposite triangle before your opponents do.</p>
            <h3>Movement</h3>
            <ul>
              <li><strong>Step:</strong> Move one space to any adjacent empty position.</li>
              <li><strong>Hop:</strong> Jump over an adjacent piece (any color) to the empty space beyond it. The jumped piece stays on the board &mdash; there are no captures.</li>
              <li><strong>Chain hops:</strong> After a hop you may keep hopping in any direction. You can stop at any point in the chain.</li>
            </ul>
            <h3>Key Rules</h3>
            <ul>
              <li>Jumping is <strong>never mandatory</strong> &mdash; you can always choose a simple step.</li>
              <li>Once a piece enters its goal triangle, it <strong>cannot leave</strong>.</li>
              <li>2, 3, 4, or 6 players. Each player can be human or AI.</li>
            </ul>
          </div>
        )}

        <div className="player-count-selector">
          {PLAYER_COUNTS.map(count => (
            <button
              key={count}
              className={`player-count-btn ${playerCount === count ? 'selected' : ''}`}
              onClick={() => handleCountChange(count)}
            >
              {count}P
            </button>
          ))}
        </div>

        <div className="slot-config">
          {slots.map((slot, i) => (
            <div key={slot.color} className="slot-row">
              <span className="slot-color-dot" style={{ backgroundColor: PLAYER_COLORS[slot.color] }} />
              <span className="slot-color-name" style={{ color: PLAYER_COLORS[slot.color] }}>
                {slot.color}
              </span>
              <div className="slot-controls">
                <div className="slot-type-toggle">
                  <button
                    className={slot.type === 'HUMAN' ? 'active' : ''}
                    onClick={() => handleTypeChange(i, 'HUMAN')}
                  >
                    Human
                  </button>
                  <button
                    className={slot.type === 'AI' ? 'active' : ''}
                    onClick={() => handleTypeChange(i, 'AI')}
                  >
                    AI
                  </button>
                </div>
                <select
                  className="slot-difficulty"
                  value={slot.difficulty}
                  onChange={e => handleDifficultyChange(i, e.target.value as AiDifficulty)}
                  disabled={slot.type !== 'AI'}
                >
                  <option value="EASY">Easy</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HARD">Hard</option>
                </select>
              </div>
            </div>
          ))}
        </div>

        {!hasHuman && (
          <div className="validation-error">At least one player must be Human</div>
        )}

        <button
          className="start-game-btn"
          onClick={handleStart}
          disabled={!hasHuman}
        >
          Start Game
        </button>
      </div>
    </div>
  );
}
