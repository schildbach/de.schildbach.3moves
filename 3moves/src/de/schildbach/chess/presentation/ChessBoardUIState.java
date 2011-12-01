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

package de.schildbach.chess.presentation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.schildbach.game.Board;
import de.schildbach.game.Coordinate;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.Piece;
import de.schildbach.game.PieceSet;
import de.schildbach.game.chess.ChessBoardGeometry;
import de.schildbach.game.common.OrthogonalBoardGeometry;
import de.schildbach.game.presentation.board.RuleBasedBoardUIState;

/**
 * @author Andreas Schildbach
 */
public class ChessBoardUIState extends RuleBasedBoardUIState implements Serializable
{
	private static final Map<Coordinate, Marker> EMPTY_MARKERS = new HashMap<Coordinate, Marker>();
	private static final Map<Coordinate, Cursor> EMPTY_CURSORS = new HashMap<Coordinate, Cursor>();

	public ChessBoardUIState(GameRules rules, GamePosition position, Board initialBoard)
	{
		super(rules, position, initialBoard);
	}

	public enum View
	{
		CHESSBOARD, PROMOTION
	}

	/**
	 * determines the view in which the clickable is present
	 */
	private View determineViewOfClickable(String clickable)
	{
		if (clickable.charAt(0) == '=')
			return View.PROMOTION;
		else
			return View.CHESSBOARD;
	}

	@Override
	protected final boolean isBaseClickable(String clickable)
	{
		return determineViewOfClickable(clickable) == View.CHESSBOARD;
	}

	private View getActiveView()
	{
		Set<View> views = new HashSet<View>();
		for (String clickable : narrowingClickables())
		{
			views.add(determineViewOfClickable(clickable));
		}

		if (views.contains(View.PROMOTION) && !views.contains(View.CHESSBOARD))
			return View.PROMOTION;

		return View.CHESSBOARD;
	}

	@Override
	public Board getGameBoard()
	{
		if (getActiveView() == View.PROMOTION)
		{
			GameRules rules = getRules();
			PieceSet pieceSet = rules.getPieceSet();
			ChessBoardGeometry geometry = (ChessBoardGeometry) rules.getBoardGeometry();
			int activeColor = getPosition().getActivePlayerIndex();

			Board board = geometry.newBoard();
			for (String clickable : narrowingClickables())
			{
				if (determineViewOfClickable(clickable) == View.PROMOTION)
				{
					char pieceChar = clickable.charAt(1);
					Piece piece = pieceSet.getPiece(pieceChar, activeColor);
					char file = fileOfPiece(pieceChar);
					char rank = rankOfColor(activeColor, geometry);
					board.setPiece(geometry.locateCoordinate("" + file + rank), piece);
				}
			}

			return board;
		}
		else
		{
			return super.getGameBoard();
		}
	}

	@Override
	public Map<Coordinate, String> getClickables()
	{
		if (getActiveView() == View.PROMOTION)
		{
			GamePosition position = getPosition();
			ChessBoardGeometry geometry = (ChessBoardGeometry) getRules().getBoardGeometry();

			Map<Coordinate, String> clickables = new HashMap<Coordinate, String>();
			for (String clickable : narrowingClickables())
			{
				if (determineViewOfClickable(clickable) == View.PROMOTION)
				{
					char file = fileOfPiece(clickable.charAt(1));
					char rank = rankOfColor(position.getActivePlayerIndex(), geometry);
					clickables.put(geometry.locateCoordinate("" + file + rank), clickable);
				}
			}
			return clickables;
		}
		else
		{
			return super.getClickables();
		}
	}

	private char fileOfPiece(char piece)
	{
		switch (piece)
		{
			case 'R':
				return 'c';
			case 'N':
				return 'd';
			case 'B':
				return 'e';
			case 'Q':
				return 'f';
			case 'K':
				return 'g';
			default:
				throw new IllegalArgumentException();
		}
	}

	private char rankOfColor(int color, OrthogonalBoardGeometry geometry)
	{
		return color == 0 ? (char) ('1' + geometry.getHeight() - 1) : '1';
	}

	@Override
	public Map<Coordinate, Cursor> getCursors()
	{
		if (getActiveView() == View.PROMOTION)
		{
			return EMPTY_CURSORS;
		}
		else
		{
			return super.getCursors();
		}
	}

	@Override
	public Map<Coordinate, Marker> getMarkers()
	{
		if (getActiveView() == View.PROMOTION)
		{
			return EMPTY_MARKERS;
		}
		else
		{
			return super.getMarkers();
		}
	}
}
