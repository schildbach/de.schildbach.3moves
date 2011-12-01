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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.schildbach.game.Board;
import de.schildbach.game.Coordinate;

/**
 * @author Andreas Schildbach
 */
public abstract class AbstractBoardUIState implements Serializable
{
	private static final Map<Coordinate, String> EMPTY_CLICKABLES = new HashMap<Coordinate, String>();
	private static final Map<Coordinate, Marker> EMPTY_MARKERS = new HashMap<Coordinate, Marker>();
	private static final Map<Coordinate, Cursor> EMPTY_CURSORS = new HashMap<Coordinate, Cursor>();

	/**
	 * returns all elements that are clickable in the current state of the
	 * interface
	 */
	public Map<Coordinate, String> getClickables()
	{
		return EMPTY_CLICKABLES;
	}
	
	/**
	 * returns all board markers in the current state of the interface 
	 */
	public Map<Coordinate, Marker> getMarkers()
	{
		return EMPTY_MARKERS;
	}
	
	/**
	 * returns all board cursors in the current state of the interface
	 */
	public Map<Coordinate, Cursor> getCursors()
	{
		return EMPTY_CURSORS;
	}

	/**
	 * changes state by clicking on an element
	 */
	public abstract void click(String clickable);
	
	/**
	 * gets a reference to the internal game board
	 */
	public abstract Board getGameBoard();
	
	public static enum Marker
	{
		PRIMARY, SECONDARY;
	}
	
	public static enum Cursor
	{
		CURRENT, LAST;
	}
}
