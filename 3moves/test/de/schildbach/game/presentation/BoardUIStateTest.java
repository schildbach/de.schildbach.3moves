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

package de.schildbach.game.presentation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import de.schildbach.game.Game;
import de.schildbach.game.GameRules;
import de.schildbach.game.checkers.CheckersRules;
import de.schildbach.game.presentation.board.RuleBasedBoardUIState;

/**
 * @author Andreas Schildbach
 */
public class BoardUIStateTest
{
	@Test
	public void testCheckers()
	{
		GameRules rules = new CheckersRules(null);
		Game game = rules.newGame(null);
		RuleBasedBoardUIState ui = RuleBasedBoardUIState.getInstance(rules, game.getActualPosition(), game.getInitialPosition().getBoard());
		assertEquals(new HashSet<String>(Arrays.asList(new String[] { "26", "27", "28", "29", "30", "31", "32", "33", "34", "35" })),
				new HashSet<String>(ui.getClickables().values()));
		// assertEquals(new HashSet(), ui.getFixed());
		ui.click("27");
		assertEquals(new HashSet<String>(Arrays.asList(new String[] { "31", "32" })), new HashSet<String>(ui.getClickables().values()));
		// assertEquals(new HashSet(Arrays.asList(new String[]{"27"})), ui.getFixed());
		ui.click("31");
		assertEquals(new HashSet<String>(), new HashSet<String>(ui.getClickables().values()));
		// assertEquals(new HashSet(Arrays.asList(new String[]{"27", "31"})), ui.getFixed());
		assertEquals("31-27", rules.formatMove(ui.getDistinctMove()));
	}
}
