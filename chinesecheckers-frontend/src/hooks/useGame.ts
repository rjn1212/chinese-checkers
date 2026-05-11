import { useReducer, useCallback } from 'react';
import type { GameState, MoveData, Position, CreateGameRequest, PlayerColor } from '../types/game';
import * as api from '../api/gameApi';

interface GameStore {
  game: GameState | null;
  finalGame: GameState | null;
  selectedPiece: Position | null;
  legalMoves: MoveData[];
  loading: boolean;
  error: string | null;
  aiAnimating: boolean;
  aiMoveQueue: MoveData[];
  lastMove: { from: Position; to: Position } | null;
}

type Action =
  | { type: 'SET_GAME'; game: GameState }
  | { type: 'SET_LOADING'; loading: boolean }
  | { type: 'SET_ERROR'; error: string | null }
  | { type: 'SELECT_PIECE'; pos: Position; moves: MoveData[] }
  | { type: 'CLEAR_SELECTION' }
  | { type: 'MOVE_MADE'; preAiGame: GameState; finalGame: GameState; aiMoves: MoveData[]; lastHumanMove: { from: Position; to: Position } }
  | { type: 'AI_MOVE_APPLIED'; move: MoveData; remaining: MoveData[] }
  | { type: 'AI_ANIMATION_DONE' }
  | { type: 'RESET' };

const initialState: GameStore = {
  game: null,
  finalGame: null,
  selectedPiece: null,
  legalMoves: [],
  loading: false,
  error: null,
  aiAnimating: false,
  aiMoveQueue: [],
  lastMove: null,
};

function reducer(state: GameStore, action: Action): GameStore {
  switch (action.type) {
    case 'SET_GAME':
      return { ...state, game: action.game, finalGame: null, loading: false, error: null };
    case 'SET_LOADING':
      return { ...state, loading: action.loading };
    case 'SET_ERROR':
      return { ...state, error: action.error, loading: false };
    case 'SELECT_PIECE':
      return { ...state, selectedPiece: action.pos, legalMoves: action.moves };
    case 'CLEAR_SELECTION':
      return { ...state, selectedPiece: null, legalMoves: [] };
    case 'MOVE_MADE':
      return {
        ...state,
        game: action.preAiGame,
        finalGame: action.aiMoves.length > 0 ? action.finalGame : null,
        selectedPiece: null,
        legalMoves: [],
        loading: false,
        lastMove: action.lastHumanMove,
        aiAnimating: action.aiMoves.length > 0,
        aiMoveQueue: action.aiMoves,
      };
    case 'AI_MOVE_APPLIED': {
      if (!state.game) return state;
      const move = action.move;
      const updatedPieces = state.game.board.pieces.map(p =>
        p.row === move.from.row && p.col === move.from.col
          ? { ...p, row: move.to.row, col: move.to.col }
          : p
      );
      const done = action.remaining.length === 0;
      return {
        ...state,
        game: done && state.finalGame ? state.finalGame : {
          ...state.game,
          board: { pieces: updatedPieces },
        },
        finalGame: done ? null : state.finalGame,
        lastMove: { from: move.from, to: move.to },
        aiMoveQueue: action.remaining,
        aiAnimating: !done,
      };
    }
    case 'AI_ANIMATION_DONE':
      return {
        ...state,
        game: state.finalGame ?? state.game,
        finalGame: null,
        aiAnimating: false,
        aiMoveQueue: [],
      };
    case 'RESET':
      return initialState;
    default:
      return state;
  }
}

async function executeMoveApi(
  game: GameState,
  from: Position,
  toRow: number,
  toCol: number,
  dispatch: React.Dispatch<Action>,
) {
  dispatch({ type: 'SET_LOADING', loading: true });
  try {
    const response = await api.makeMove(game.gameId, from.row, from.col, toRow, toCol);

    const finalGame: GameState = {
      ...game,
      board: response.board,
      phase: response.phase,
      currentTurn: response.currentTurn,
      playerSlots: response.playerSlots,
      winner: response.winner,
      finishOrder: response.finishOrder,
      moveCount: response.moveCount,
      aiMoves: [],
    };

    const preAiPieces = game.board.pieces.map(p =>
      p.row === from.row && p.col === from.col
        ? { ...p, row: toRow, col: toCol }
        : p
    );
    const preAiGame: GameState = {
      ...game,
      board: { pieces: preAiPieces },
      moveCount: response.moveCount,
      aiMoves: [],
    };

    dispatch({
      type: 'MOVE_MADE',
      preAiGame: response.aiMoves.length > 0 ? preAiGame : finalGame,
      finalGame,
      aiMoves: response.aiMoves,
      lastHumanMove: { from, to: { row: toRow, col: toCol } },
    });
  } catch (e) {
    dispatch({ type: 'SET_ERROR', error: (e as Error).message });
  }
}

export function useGame() {
  const [state, dispatch] = useReducer(reducer, initialState);

  const createGame = useCallback(async (request: CreateGameRequest) => {
    dispatch({ type: 'SET_LOADING', loading: true });
    try {
      const game = await api.createGame(request);
      dispatch({ type: 'SET_GAME', game });

      if (game.aiMoves && game.aiMoves.length > 0) {
        dispatch({
          type: 'MOVE_MADE',
          preAiGame: game,
          finalGame: game,
          aiMoves: game.aiMoves,
          lastHumanMove: { from: { row: 0, col: 0 }, to: { row: 0, col: 0 } },
        });
      }
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, []);

  const selectPiece = useCallback(async (row: number, col: number) => {
    if (!state.game || state.aiAnimating) return;

    const currentTurn = state.game.currentTurn;
    const piece = state.game.board.pieces.find(p => p.row === row && p.col === col);

    if (piece && piece.owner === currentTurn) {
      const currentSlot = state.game.playerSlots.find(s => s.color === currentTurn);
      if (!currentSlot || currentSlot.type !== 'HUMAN') return;

      try {
        const response = await api.getLegalMoves(state.game.gameId, row, col);
        dispatch({ type: 'SELECT_PIECE', pos: { row, col }, moves: response.moves });
      } catch (e) {
        dispatch({ type: 'SET_ERROR', error: (e as Error).message });
      }
    } else if (state.selectedPiece) {
      const isTarget = state.legalMoves.some(m => m.to.row === row && m.to.col === col);
      if (isTarget) {
        await executeMoveApi(state.game, state.selectedPiece, row, col, dispatch);
      } else {
        dispatch({ type: 'CLEAR_SELECTION' });
      }
    }
  }, [state.game, state.selectedPiece, state.legalMoves, state.aiAnimating]);

  const processNextAiMove = useCallback(() => {
    if (state.aiMoveQueue.length === 0) {
      dispatch({ type: 'AI_ANIMATION_DONE' });
      return;
    }
    const [nextMove, ...remaining] = state.aiMoveQueue;
    dispatch({
      type: 'AI_MOVE_APPLIED',
      move: nextMove,
      remaining,
    });
  }, [state.aiMoveQueue]);

  const clearError = useCallback(() => {
    dispatch({ type: 'SET_ERROR', error: null });
  }, []);

  const resetGame = useCallback(() => {
    dispatch({ type: 'RESET' });
  }, []);

  const activeColors: PlayerColor[] = state.game
    ? state.game.playerSlots.map(s => s.color)
    : [];

  return {
    ...state,
    activeColors,
    createGame,
    selectPiece,
    processNextAiMove,
    clearError,
    resetGame,
  };
}
