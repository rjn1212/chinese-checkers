import { PlatformClient } from '@gitgames/platform-sdk';
import type { CreateGameRequest, GameState, LegalMovesResponse, MoveResponse } from '../types/game';

export const platform = new PlatformClient();

const BASE_URL = '/api/v1/games';

function getHeaders(): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...platform.getAuthHeaders(),
  };
}

export async function createGame(request: CreateGameRequest): Promise<GameState> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    const err = await response.json();
    throw new Error(err.message || 'Failed to create game');
  }
  return response.json();
}

export async function getGame(id: string): Promise<GameState> {
  const response = await fetch(`${BASE_URL}/${id}`, { headers: getHeaders() });
  if (!response.ok) {
    const err = await response.json();
    throw new Error(err.message || 'Failed to get game');
  }
  return response.json();
}

export async function getLegalMoves(id: string, row: number, col: number): Promise<LegalMovesResponse> {
  const response = await fetch(`${BASE_URL}/${id}/moves?row=${row}&col=${col}`, {
    headers: getHeaders(),
  });
  if (!response.ok) {
    const err = await response.json();
    throw new Error(err.message || 'Failed to get moves');
  }
  return response.json();
}

export async function makeMove(id: string, fromRow: number, fromCol: number, toRow: number, toCol: number): Promise<MoveResponse> {
  const response = await fetch(`${BASE_URL}/${id}/moves`, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify({
      from: { row: fromRow, col: fromCol },
      to: { row: toRow, col: toCol },
    }),
  });
  if (!response.ok) {
    const err = await response.json();
    throw new Error(err.message || 'Failed to make move');
  }
  return response.json();
}

export async function abandonGame(id: string): Promise<void> {
  await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
    headers: getHeaders(),
  });
}
