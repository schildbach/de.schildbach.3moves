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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.schildbach.game.Board;
import de.schildbach.game.Coordinate;
import de.schildbach.game.Piece;
import de.schildbach.portal.service.game.Chess960Helper;

/**
 * @author Andreas Schildbach
 * @todo this class/package should move into a dedicated service layer module
 */
public class Chess960HelperTest
{
	@Test
	public void shouldYield960DifferentBoards()
	{
		Set<Board> boards = new HashSet<Board>();

		for (int id = 0; id < 960; id++)
			boards.add(Chess960Helper.getBoardById(id));

		assertEquals(960, boards.size());
	}

	@Test
	public void shouldAllYieldSamePieceSet()
	{
		List<Piece> piecesOnDefaultBoard = pieces(Chess960Helper.getBoardById(0));
		Collections.sort(piecesOnDefaultBoard);
		for (int id = 0; id < 960; id++)
		{
			Board board = Chess960Helper.getBoardById(id);
			List<Piece> pieces = pieces(board);
			Collections.sort(pieces);
			assertEquals("id=" + id, piecesOnDefaultBoard, pieces);
		}
	}

	private List<Piece> pieces(Board board)
	{
		List<Piece> pieces = new LinkedList<Piece>();

		for (Coordinate c : board.locateOccupiedFields())
			pieces.add(board.getPiece(c));

		return pieces;
	}
}
