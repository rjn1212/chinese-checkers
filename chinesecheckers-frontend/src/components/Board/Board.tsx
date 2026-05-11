import { useMemo } from 'react';
import type { PieceData, MoveData, PlayerColor } from '../../types/game';
import { PLAYER_COLORS, PLAYER_COLORS_LIGHT } from '../../types/game';
import { computeLayout, getTriangleColor, getBoardHeight } from './boardLayout';
import './Board.css';

interface BoardProps {
  pieces: PieceData[];
  selectedPiece: { row: number; col: number } | null;
  legalMoves: MoveData[];
  onPositionClick: (row: number, col: number) => void;
  disabled: boolean;
  lastMove: { from: { row: number; col: number }; to: { row: number; col: number } } | null;
  activeColors: PlayerColor[];
}

const BOARD_SIZE = 560;

export default function Board({ pieces, selectedPiece, legalMoves, onPositionClick, disabled, lastMove, activeColors }: BoardProps) {
  const layout = useMemo(() => computeLayout(BOARD_SIZE), []);
  const boardHeight = useMemo(() => getBoardHeight(BOARD_SIZE), []);

  const pieceMap = useMemo(() => {
    const map = new Map<string, PieceData>();
    for (const p of pieces) {
      map.set(`${p.row},${p.col}`, p);
    }
    return map;
  }, [pieces]);

  const legalTargets = useMemo(() => {
    const set = new Set<string>();
    for (const m of legalMoves) {
      set.add(`${m.to.row},${m.to.col}`);
    }
    return set;
  }, [legalMoves]);

  const radius = BOARD_SIZE / (14 * 2.5);
  const touchRadius = radius * 1.4;

  return (
    <svg viewBox={`0 0 ${BOARD_SIZE} ${boardHeight}`} className="board-svg">
      {layout.map((pos) => {
        const key = `${pos.row},${pos.col}`;
        const piece = pieceMap.get(key);
        const isSelected = selectedPiece?.row === pos.row && selectedPiece?.col === pos.col;
        const isTarget = legalTargets.has(key);
        const triangleColor = getTriangleColor(pos.row, pos.col);
        const isLastMoveFrom = lastMove?.from.row === pos.row && lastMove?.from.col === pos.col;
        const isLastMoveTo = lastMove?.to.row === pos.row && lastMove?.to.col === pos.col;

        const isActiveTriangle = triangleColor && activeColors.includes(triangleColor);
        let fillColor = '#3a3a3a';
        if (isActiveTriangle && triangleColor) {
          fillColor = PLAYER_COLORS_LIGHT[triangleColor] + '40';
        }

        return (
          <g key={key} onClick={() => !disabled && onPositionClick(pos.row, pos.col)} style={{ cursor: disabled ? 'default' : 'pointer' }}>
            {/* Position circle */}
            <circle
              cx={pos.cx}
              cy={pos.cy}
              r={radius}
              fill={fillColor}
              stroke={isSelected ? '#fff' : isLastMoveFrom || isLastMoveTo ? '#FFD54F' : '#555'}
              strokeWidth={isSelected ? 2.5 : isLastMoveFrom || isLastMoveTo ? 1.5 : 0.5}
            />

            {/* Legal move target indicator */}
            {isTarget && !piece && (
              <circle
                cx={pos.cx}
                cy={pos.cy}
                r={radius * 0.4}
                fill="rgba(76, 175, 80, 0.6)"
              />
            )}

            {/* Piece */}
            {piece && (
              <circle
                cx={pos.cx}
                cy={pos.cy}
                r={radius * 0.8}
                fill={PLAYER_COLORS[piece.owner]}
                stroke={isSelected ? '#fff' : isTarget ? '#4CAF50' : '#000'}
                strokeWidth={isSelected ? 2.5 : isTarget ? 2 : 1}
                className={`piece-circle ${isSelected ? 'piece-selected' : ''}`}
              />
            )}

            {/* Invisible enlarged touch target */}
            <circle
              cx={pos.cx}
              cy={pos.cy}
              r={touchRadius}
              className="touch-target"
            />
          </g>
        );
      })}
    </svg>
  );
}
