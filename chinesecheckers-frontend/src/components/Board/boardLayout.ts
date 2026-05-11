import type { PlayerColor } from '../../types/game';

export interface PositionLayout {
  row: number;
  col: number;
  cx: number;
  cy: number;
}

const ROW_COUNTS = [1, 2, 3, 4, 13, 12, 11, 10, 9, 10, 11, 12, 13, 4, 3, 2, 1];
const MAX_WIDTH = 13;

export function computeLayout(boardSize: number): PositionLayout[] {
  const positions: PositionLayout[] = [];
  const spacingX = boardSize / (MAX_WIDTH + 1);
  const spacingY = spacingX * 0.866;
  const paddingTop = spacingY;

  for (let row = 0; row < 17; row++) {
    const count = ROW_COUNTS[row];
    const rowWidth = (count - 1) * spacingX;
    const startX = (boardSize - rowWidth) / 2;

    for (let col = 0; col < count; col++) {
      positions.push({
        row,
        col,
        cx: startX + col * spacingX,
        cy: paddingTop + row * spacingY,
      });
    }
  }
  return positions;
}

export function getTriangleColor(row: number, col: number): PlayerColor | null {
  // Top triangle (RED)
  if (row >= 0 && row <= 3) {
    const width = ROW_COUNTS[row];
    if (col >= 0 && col < width) return 'RED';
  }
  // Bottom triangle (GREEN)
  if (row >= 13 && row <= 16) {
    const width = ROW_COUNTS[row];
    if (col >= 0 && col < width) return 'GREEN';
  }
  // Upper-left (PURPLE)
  if (row >= 4 && row <= 7) {
    const triangleCols = 4 - (row - 4);
    if (col < triangleCols) return 'PURPLE';
  }
  // Upper-right (ORANGE)
  if (row >= 4 && row <= 7) {
    const width = ROW_COUNTS[row];
    const triangleCols = 4 - (row - 4);
    if (col >= width - triangleCols) return 'ORANGE';
  }
  // Lower-left (BLUE)
  if (row >= 9 && row <= 12) {
    const triangleCols = row - 8;
    if (col < triangleCols) return 'BLUE';
  }
  // Lower-right (YELLOW)
  if (row >= 9 && row <= 12) {
    const width = ROW_COUNTS[row];
    const triangleCols = row - 8;
    if (col >= width - triangleCols) return 'YELLOW';
  }
  return null;
}

export function getBoardHeight(boardSize: number): number {
  const spacingX = boardSize / (MAX_WIDTH + 1);
  const spacingY = spacingX * 0.866;
  return spacingY * 18;
}
