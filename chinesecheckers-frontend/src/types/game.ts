export type PlayerColor = 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE' | 'PURPLE';
export type PlayerType = 'HUMAN' | 'AI';
export type AiDifficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type GamePhase = 'PLAYING' | 'GAME_OVER';

export interface Position {
  row: number;
  col: number;
}

export interface PieceData {
  id: string;
  owner: PlayerColor;
  row: number;
  col: number;
}

export interface PlayerSlotData {
  color: PlayerColor;
  type: PlayerType;
  difficulty: AiDifficulty | null;
  finished: boolean;
  finishOrder: number | null;
  stalemated: boolean;
}

export interface MoveData {
  player: PlayerColor | null;
  from: Position;
  to: Position;
  path: Position[];
}

export interface GameState {
  gameId: string;
  phase: GamePhase;
  currentTurn: PlayerColor;
  playerSlots: PlayerSlotData[];
  board: { pieces: PieceData[] };
  winner: PlayerColor | null;
  finishOrder: PlayerColor[];
  moveCount: number;
  aiMoves: MoveData[];
}

export interface MoveResponse {
  humanMove: MoveData;
  aiMoves: MoveData[];
  board: { pieces: PieceData[] };
  phase: GamePhase;
  currentTurn: PlayerColor;
  playerSlots: PlayerSlotData[];
  winner: PlayerColor | null;
  finishOrder: PlayerColor[];
  moveCount: number;
}

export interface LegalMovesResponse {
  moves: MoveData[];
}

export interface CreateGameRequest {
  playerSlots: {
    color: PlayerColor;
    type: PlayerType;
    difficulty: AiDifficulty | null;
  }[];
}

export const PLAYER_COLORS: Record<PlayerColor, string> = {
  RED: '#E53935',
  ORANGE: '#FB8C00',
  YELLOW: '#FDD835',
  GREEN: '#43A047',
  BLUE: '#1E88E5',
  PURPLE: '#8E24AA',
};

export const PLAYER_COLORS_LIGHT: Record<PlayerColor, string> = {
  RED: '#FFCDD2',
  ORANGE: '#FFE0B2',
  YELLOW: '#FFF9C4',
  GREEN: '#C8E6C9',
  BLUE: '#BBDEFB',
  PURPLE: '#E1BEE7',
};

export const ALL_COLORS: PlayerColor[] = ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'PURPLE'];

export const VALID_CONFIGS: Record<number, PlayerColor[]> = {
  2: ['RED', 'GREEN'],
  3: ['RED', 'YELLOW', 'BLUE'],
  4: ['RED', 'ORANGE', 'GREEN', 'BLUE'],
  6: ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'PURPLE'],
};
