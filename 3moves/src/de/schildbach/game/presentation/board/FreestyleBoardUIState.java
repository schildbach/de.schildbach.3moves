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

package de.schildbach.game.presentation.board;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.schildbach.game.Board;
import de.schildbach.game.BoardGeometry;
import de.schildbach.game.Coordinate;
import de.schildbach.game.Piece;
import de.schildbach.game.PieceSet;

/**
 * @author Andreas Schildbach
 */
public class FreestyleBoardUIState extends AbstractBoardUIState
{
	private Board board;
	private BoardGeometry geometry;
	private PieceSet pieceSet;
	private Coordinate selectedCoordinate;
	private boolean isNewPieceSelect = false;
	private Map<Coordinate, String> newPieceSelectClickables;
	private Board newPieceSelectBoard;

	public FreestyleBoardUIState(BoardGeometry geometry, Board board, PieceSet pieceSet)
	{
		this.board = board;
		this.geometry = geometry;
		this.pieceSet = pieceSet;
		
		// prepare new piece select mode
		newPieceSelectClickables = new HashMap<Coordinate, String>();
		newPieceSelectBoard = geometry.newBoard();
		Iterator<Coordinate> iCoordinate = geometry.coordinateIterator();
		for (Piece piece : pieceSet.getPieces())
		{
			Coordinate coordinate = iCoordinate.next();
			newPieceSelectClickables.put(coordinate, pieceSet.getStringRepresentation(piece));
			newPieceSelectBoard.setPiece(coordinate, piece);
		}
	}
	
	public BoardGeometry getGeometry()
	{
		return geometry;
	}
	
	public PieceSet getPieceSet()
	{
		return pieceSet;
	}

	@Override
	public Map<Coordinate, String> getClickables()
	{
		if (!isNewPieceSelect)
		{
			Map<Coordinate, String> clickables = new HashMap<Coordinate, String>();
			for (Iterator<Coordinate> i = geometry.coordinateIterator(); i.hasNext();)
			{
				Coordinate coordinate = i.next();
				clickables.put(coordinate, coordinate.getNotation());
			}
			return clickables;
		}
		else
		{
			return newPieceSelectClickables;
		}
	}

	@Override
	public Map<Coordinate, Cursor> getCursors()
	{
		if(!isNewPieceSelect)
		{
			Map<Coordinate, Cursor> cursors = new HashMap<Coordinate, Cursor>();
			if(selectedCoordinate != null)
				cursors.put(selectedCoordinate, Cursor.CURRENT);
			return cursors;
		}
		else
		{
			return super.getCursors();
		}
	}

	@Override
	public void click(String clickable)
	{
		if(!isNewPieceSelect)
		{
			Coordinate clickedCoordinate = geometry.locateCoordinate(clickable);
			if(selectedCoordinate == null)
			{
				// select coordinate
				selectedCoordinate = clickedCoordinate;

				if(board.getPiece(selectedCoordinate) == null)
				{
					// switch to new piece select mode
					isNewPieceSelect = true;
				}
			}
			else
			{
				if(clickedCoordinate != selectedCoordinate)
				{
					if(board.getPiece(selectedCoordinate) != null)
					{
						// move piece
						board.movePiece(selectedCoordinate, clickedCoordinate);
					}
				}
				else
				{
					// remove piece
					board.setPiece(clickedCoordinate, null); // TODO complete
				}
				selectedCoordinate = null;
			}
		}
		else
		{
			// set new piece
			board.setPiece(selectedCoordinate, pieceSet.getPiece(clickable));
			selectedCoordinate = null;
			isNewPieceSelect = false;
		}
	}
	
	@Override
	public Board getGameBoard()
	{
		if(!isNewPieceSelect)
			return board;
		else
			return newPieceSelectBoard;
	}
}
