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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import de.schildbach.game.Board;
import de.schildbach.game.BoardGeometry;
import de.schildbach.game.Game;
import de.schildbach.game.GamePosition;
import de.schildbach.game.chess.ChessMove;
import de.schildbach.game.chess.ChessRules;
import de.schildbach.game.presentation.board.RuleBasedBoardUIState;

/**
 * @author Andreas Schildbach
 */
public class ChessBoardUIStateTest
{
	private static final ChessRules RULES = new ChessRules(null);

	private Game game;
	private RuleBasedBoardUIState ui;

	@Before
	public void setup() throws Exception
	{
		game = RULES.newGame(null);
		GamePosition position = game.getActualPosition();
		Board initialBoard = game.getInitialPosition().getBoard();
		ui = RuleBasedBoardUIState.getInstance(RULES, position, initialBoard);
	}

	@Test
	public void testChess()
	{
		BoardGeometry geometry = RULES.getBoardGeometry();

		assertEquals(new HashSet<String>(Arrays.asList(new String[]{"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "b1", "g1"})), new HashSet<String>(ui.getClickables().values()));
		assertNull(ui.getDistinctMove());
//		assertEquals(new HashSet(), logic.getFixed());
		ui.click("c3");
		assertEquals(new HashSet<String>(Arrays.asList(new String[]{"b1", "c2"})), new HashSet<String>(ui.getClickables().values()));
		assertNull(ui.getDistinctMove());
//		assertEquals(new HashSet(Arrays.asList(new String[]{"c3"})), logic.getFixed());
		ui.click("c2");
		assertEquals(new HashSet<String>(), new HashSet<String>(ui.getClickables().values()));
		assertNotNull(ui.getDistinctMove());
//		assertEquals(new HashSet(Arrays.asList(new String[]{"c3", "c2"})), logic.getFixed());
		
		ui.clear();
		assertEquals(new HashSet<String>(Arrays.asList(new String[]{"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "b1", "g1"})), new HashSet<String>(ui.getClickables().values()));
		assertNull(ui.getDistinctMove());
		ui.click("d4");
		assertEquals(new HashSet<String>(), new HashSet<String>(ui.getClickables().values()));
		assertEquals(new ChessMove(geometry.locateCoordinate("d2"), geometry.locateCoordinate("d4")), ui.getDistinctMove());
		ui.click("d2");
		assertEquals(new HashSet<String>(), new HashSet<String>(ui.getClickables().values()));
		assertEquals(new ChessMove(geometry.locateCoordinate("d2"), geometry.locateCoordinate("d4")), ui.getDistinctMove());
		ui.click("d1"); // this click is swallowed
		assertEquals(new HashSet<String>(), new HashSet<String>(ui.getClickables().values()));
		assertEquals(new ChessMove(geometry.locateCoordinate("d2"), geometry.locateCoordinate("d4")), ui.getDistinctMove());
	}

	@Test
	public void testPromotion()
	{
/*
		ChessGame game = new ChessGame(FenFormat.parseFenPosition("k6K/3P4/8/8/8/8/8/8 w - - 0 1"));
		ui.setGame(game);
		assertEquals(new HashSet(Arrays.asList(new String[]{"h8", "g8", "g7", "h7", "d7", "d8"})), ui.getClickables());
		assertEquals(new HashSet(Arrays.asList(new UserInterface.Marker[]{new UserInterface.Marker("h8", "green"), new UserInterface.Marker("g8", "green"), new UserInterface.Marker("h7", "green"), new UserInterface.Marker("g7", "green"), new UserInterface.Marker("d7", "green"), new UserInterface.Marker("d8", "green")})), ui.getMarkers());
		assertNull(ui.getDistinctMove());
		ui.click("d7");
		assertEquals(new HashSet(Arrays.asList(new String[]{"c8", "d8", "e8", "f8"})), ui.getClickables());
		assertEquals(new HashSet(Arrays.asList(new UserInterface.Marker[]{new UserInterface.Marker("c8", "green"), new UserInterface.Marker("d8", "green"), new UserInterface.Marker("e8", "green"), new UserInterface.Marker("f8", "green")})), ui.getMarkers());
		assertNull(ui.getDistinctMove());
		ui.click("f8");
		assertEquals(new HashSet(), ui.getClickables());
		assertEquals(new HashSet(Arrays.asList(new UserInterface.Marker[]{new UserInterface.Marker("d7", "yellow"), new UserInterface.Marker("d8", "yellow")})), ui.getMarkers());
		assertEquals("d7-d8=Q", ui.getDistinctMove().getNotation());

		game = new ChessGame(FenFormat.parseFenPosition("k6K/8/8/8/8/8/3p4/8 b - - 0 1"));
		ui.setGame(game);
		assertEquals(new HashSet(Arrays.asList(new String[]{"a8", "b8", "a7", "b7", "d2", "d1"})), ui.getClickables());
		assertNull(ui.getDistinctMove());
		ui.click("d1");
		assertEquals(new HashSet(Arrays.asList(new String[]{"c1", "d1", "e1", "f1"})), ui.getClickables());
		assertNull(ui.getDistinctMove());
		ui.click("d1");
		assertEquals(new HashSet(), ui.getClickables());
		assertEquals(new HashSet(Arrays.asList(new UserInterface.Marker[]{new UserInterface.Marker("d1", "yellow"), new UserInterface.Marker("d2", "yellow")})), ui.getMarkers());
		assertEquals("d2-d1=N", ui.getDistinctMove().getNotation());*/
/*		
		for(Iterator i = ui.getMarkers().iterator(); i.hasNext();)
		{
			UserInterface.Marker marker = (UserInterface.Marker) i.next();
			System.out.println(marker.clickable + marker.color);
		}
*/
	}
}
