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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.schildbach.chess.presentation.ChessBoardUIState;
import de.schildbach.game.Board;
import de.schildbach.game.Coordinate;
import de.schildbach.game.GameMove;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.checkers.CheckersRules;
import de.schildbach.game.chess.ChessRules;
import de.schildbach.game.dragonchess.DragonchessRules;
import de.schildbach.game.go.GoRules;
import de.schildbach.go.presentation.GoBoardUIState;

/**
 * @author Andreas Schildbach
 */
public class RuleBasedBoardUIState extends AbstractBoardUIState
{
	private GameRules rules;
	private GamePosition position;
	private Board initialBoard;
	private GameMove move;
	private Collection<? extends GameMove> allowedMoves;
	/** keeps track of the possible moves that can be addressed with the current selection of clickables */
	private Set<GameMove> stillPossibleMoves;
	private Set<String> clicked = new HashSet<String>();

	public static RuleBasedBoardUIState getInstance(GameRules rules, GamePosition position, Board initialBoard)
	{
		if (rules instanceof ChessRules)
			return new ChessBoardUIState(rules, position, initialBoard);
		else if (rules instanceof CheckersRules)
			return new RuleBasedBoardUIState(rules, position, initialBoard);
		else if (rules instanceof GoRules)
			return new GoBoardUIState(rules, position, initialBoard);
		else if (rules instanceof DragonchessRules)
			return new RuleBasedBoardUIState(rules, position, initialBoard);
		else
			throw new IllegalArgumentException("can't handle " + rules.getClass().getName());
	}

	protected RuleBasedBoardUIState(GameRules rules, GamePosition position, Board initialBoard)
	{
		this.rules = rules;
		this.initialBoard = initialBoard;
		setPosition(position);
	}
	
	public final GameRules getRules()
	{
		return rules;
	}
	
	public final void setPosition(GamePosition position)
	{
		this.position = position;
		init();
	}

	public final GamePosition getPosition()
	{
		return position;
	}

	public final void setMove(GameMove move)
	{
		this.move = move;
	}
	
	public void init()
	{
		this.allowedMoves = rules.allowedMoves(position, initialBoard);
		clear();
	}

	public void clear()
	{
		this.stillPossibleMoves = new HashSet<GameMove>(allowedMoves);
		this.clicked.clear();
	}
	
	public boolean isInitial()
	{
		return clicked.isEmpty();
	}

	@Override
	public Board getGameBoard()
	{
		return position.getBoard();
	}

	/** returns all clickables that are relevant to further narrow the possible moves */
	protected final Set<String> narrowingClickables()
	{
		Set<String> clickables = new HashSet<String>();

		for(GameMove move : stillPossibleMoves)
		{
			for(String clickable : rules.clickables(move))
			{
				// don't keep fixed clickables, because they are irrelevant for narrowing the possible moves 
				if(!isFixedClickable(clickable))
					clickables.add(clickable);
			}
		}

		return clickables;
	}

	protected boolean isBaseClickable(String clickable)
	{
		return true;
	}
	
	@Override
	public Map<Coordinate, String> getClickables()
	{
		Map<Coordinate, String> clickables = new HashMap<Coordinate, String>();

		for(String clickable : narrowingClickables())
		{
			if(isBaseClickable(clickable))
			{
				Coordinate coordinate = rules.getBoardGeometry().locateCoordinate(clickable);
				clickables.put(coordinate, clickable);
			}
		}

		return clickables;
	}

	private final boolean isFixedClickable(String clickable)
	{
		for(GameMove move : stillPossibleMoves)
		{
			if(!rules.clickables(move).contains(clickable))
			{
				return false;
			}
		}

		return true;
	}
	
	@Override
	public Map<Coordinate, Cursor> getCursors()
	{
		Map<Coordinate, Cursor> cursors = new HashMap<Coordinate, Cursor>();

		// add cursors for displayed (last) move
		if(move != null && isInitial())
		{
			for(String clickable : rules.clickables(move))
			{
				if(isBaseClickable(clickable))
				{
					Coordinate coordinate = rules.getBoardGeometry().locateCoordinate(clickable);
					cursors.put(coordinate, Cursor.LAST);
				}
			}
		}

		// add cursors for currently entered move
		for(GameMove move : stillPossibleMoves)
		{
			for(String clickable : rules.clickables(move))
			{
				if(isBaseClickable(clickable) && isFixedClickable(clickable))
				{
					Coordinate coordinate = rules.getBoardGeometry().locateCoordinate(clickable);
					cursors.put(coordinate, Cursor.CURRENT);
				}
			}
		}

		return cursors;
	}
	
	@Override
	public Map<Coordinate, Marker> getMarkers()
	{
		Map<Coordinate, Marker> markers = new HashMap<Coordinate, Marker>();

		for(String clickable : narrowingClickables())
		{
			if(isBaseClickable(clickable))
			{
				Coordinate coordinate = rules.getBoardGeometry().locateCoordinate(clickable);
				markers.put(coordinate, Marker.PRIMARY);
			}
		}

		return markers;
	}

	@Override
	public final void click(String clickable)
	{
		if(!isClickValid(clickable))
			return; // swallow double clicks

		for(Iterator<GameMove> i = stillPossibleMoves.iterator(); i.hasNext();)
		{
			GameMove move = i.next();
			if(!rules.clickables(move).contains(clickable))
				i.remove();
		}

		clicked.add(clickable);
	}

	private boolean isClickValid(String clickable)
	{
		for(GameMove move : stillPossibleMoves)
		{
			if(rules.clickables(move).contains(clickable))
			{
				return true;
			}
		}
		return false;
	}

	public final int getNumberOfAllowedMoves()
	{
		return stillPossibleMoves.size();
	}

	public final GameMove getDistinctMove()
	{
		if(stillPossibleMoves.size() != 1)
			return null;
		return stillPossibleMoves.iterator().next();
	}

	public final String getMoveNotation()
	{
		GameMove move = getDistinctMove();
		if(move != null)
			return rules.formatMove(move);
		
		return "?";
	}
}
