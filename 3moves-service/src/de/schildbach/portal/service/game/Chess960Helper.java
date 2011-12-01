/*
 * Copyright 2001-2011 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.portal.service.game;

import de.schildbach.game.Board;
import de.schildbach.game.BoardGeometry;
import de.schildbach.game.Coordinate;
import de.schildbach.game.Piece;
import de.schildbach.game.PieceSet;
import de.schildbach.game.chess.ChessRules;
import de.schildbach.game.chess.piece.Bishop;
import de.schildbach.game.chess.piece.King;
import de.schildbach.game.chess.piece.Knight;
import de.schildbach.game.chess.piece.Pawn;
import de.schildbach.game.chess.piece.Queen;
import de.schildbach.game.chess.piece.Rook;

/**
 * @author Andreas Schildbach
 * @todo this class/package should move into a dedicated service layer module
 */
public class Chess960Helper
{
	@SuppressWarnings("unchecked")
	private static final Class<Piece>[][] CHESS960_TABLE = new Class[][] {
			new Class[] { Knight.class, Knight.class, Rook.class, King.class, Rook.class },
			new Class[] { Knight.class, Rook.class, Knight.class, King.class, Rook.class },
			new Class[] { Knight.class, Rook.class, King.class, Knight.class, Rook.class },
			new Class[] { Knight.class, Rook.class, King.class, Rook.class, Knight.class },
			new Class[] { Rook.class, Knight.class, Knight.class, King.class, Rook.class },
			new Class[] { Rook.class, Knight.class, King.class, Knight.class, Rook.class },
			new Class[] { Rook.class, Knight.class, King.class, Rook.class, Knight.class },
			new Class[] { Rook.class, King.class, Knight.class, Knight.class, Rook.class },
			new Class[] { Rook.class, King.class, Knight.class, Rook.class, Knight.class },
			new Class[] { Rook.class, King.class, Rook.class, Knight.class, Knight.class } };

	private static final ChessRules CHESS_RULES = new ChessRules(null);

	public static Board getBoardById(int boardId)
	{
		BoardGeometry geometry = CHESS_RULES.getBoardGeometry();
		PieceSet pieceSet = CHESS_RULES.getPieceSet();

		// disassemble id
		Board board = geometry.newBoard();
		int whiteBishopId = boardId % 4;
		char whiteBishopFile = (char) ('b' + whiteBishopId * 2);
		boardId /= 4;
		int blackBishopId = boardId % 4;
		char blackBishopFile = (char) ('a' + blackBishopId * 2);
		boardId /= 4;
		int queenId = boardId % 6;
		boardId /= 6;

		// place pawns
		for (char file = 'a'; file <= 'h'; file++)
		{
			board.setPiece(geometry.locateCoordinate(file + "2"), pieceSet.getPiece(Pawn.class, 0));
			board.setPiece(geometry.locateCoordinate(file + "7"), pieceSet.getPiece(Pawn.class, 1));
		}

		// place bishops
		board.setPiece(geometry.locateCoordinate(whiteBishopFile + "1"), pieceSet.getPiece(Bishop.class, 0));
		board.setPiece(geometry.locateCoordinate(blackBishopFile + "1"), pieceSet.getPiece(Bishop.class, 0));

		// place queen
		for (char file = 'a'; file <= 'h'; file++)
		{
			Coordinate coordinate = geometry.locateCoordinate(file + "1");

			if (board.getPiece(coordinate) == null)
			{
				if (queenId == 0)
				{
					board.setPiece(coordinate, pieceSet.getPiece(Queen.class, 0));
					break;
				}

				queenId--;
			}
		}

		// place rest
		int i = 0;
		for (char file = 'a'; file <= 'h'; file++)
		{
			Coordinate coordinate = geometry.locateCoordinate(file + "1");

			if (board.getPiece(coordinate) == null)
			{
				Piece piece = pieceSet.getPiece(CHESS960_TABLE[boardId][i++], 0);
				board.setPiece(coordinate, piece);
			}
		}

		// copy pieces
		for (char file = 'a'; file <= 'h'; file++)
		{
			Piece piece = board.getPiece(geometry.locateCoordinate(file + "1"));
			board.setPiece(geometry.locateCoordinate(file + "8"), pieceSet.getPiece(piece.getClass(), 1));
		}

		return board;
	}

}
