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

package de.schildbach.portal.persistence.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andreas Schildbach
 */
public class GameDaoTest extends AbstractGameDaoTest
{
	@Autowired
	private GameDao gameDao;

	@Test
	public void shouldCreateReadAndDeleteSingleGame()
	{
		SingleGame singleGame = gameDao.newSingleGame(user1, new Date(), Rules.CHESS, "", Aid.NONE);

		int count = countRowsInTable(Game.TABLE_NAME);

		int id = gameDao.create(singleGame);
		flushSession();

		assertEquals(++count, countRowsInTable(Game.TABLE_NAME));
		assertFalse(id == 0);

		singleGame = gameDao.read(SingleGame.class, id);

		gameDao.delete(singleGame);
		flushSession();

		assertEquals(--count, countRowsInTable(Game.TABLE_NAME));
	}

	@Test
	public void shouldCreateReadAndDeleteGameGroup()
	{
		GameGroup gameGroup = gameDao.newGameGroup(user1, new Date(), Rules.CHESS, "", Aid.NONE);

		int count = countRowsInTable(Game.TABLE_NAME);

		int id = gameDao.create(gameGroup);
		flushSession();

		assertEquals(++count, countRowsInTable(Game.TABLE_NAME));
		assertFalse(id == 0);

		gameGroup = gameDao.read(GameGroup.class, id);

		gameDao.delete(gameGroup);
		flushSession();

		assertEquals(--count, countRowsInTable(Game.TABLE_NAME));
	}

	@Test
	public void shouldAutoCreateAndDeleteWatch()
	{
		game.getWatches().add(new GameWatch(game, user1));

		int count = countRowsInTable(GameWatch.TABLE_NAME);
		flushSession();

		assertEquals(++count, countRowsInTable(GameWatch.TABLE_NAME));

		game.getWatches().remove(game.getWatches().iterator().next());
		flushSession();

		assertEquals(--count, countRowsInTable(GameWatch.TABLE_NAME));
	}

	@Test
	public void shouldFindWatchedGame()
	{
		GameWatch watch = new GameWatch(game, user1);
		game.getWatches().add(watch);
		flushSession();

		List<Game> games = gameDao.findGames(null, null, null, user1, null, null, null, null, null, 0, null, false);
		assertEquals(1, games.size());
		Game game = games.get(0);
		assertEquals(1, game.getWatches().size());
		assertTrue(game == watch.getGame());
		assertTrue(game.getWatches().iterator().next() == watch);
	}

	@Test
	public void shouldAutoCreateAndDeletePlayer()
	{
		game.getPlayers().add(new GamePlayer(game, user1, new Date()));

		int count = countRowsInTable(GamePlayer.TABLE_NAME);
		flushSession();

		assertEquals(++count, countRowsInTable(GamePlayer.TABLE_NAME));

		game.getPlayers().remove(game.getPlayers().iterator().next());
		flushSession();

		assertEquals(--count, countRowsInTable(GamePlayer.TABLE_NAME));
	}

	@Test
	public void shouldAutoCreateAndDeleteGameConditionalMoves()
	{
		GamePlayer player = new GamePlayer(game, user1, new Date());
		game.getPlayers().add(player);
		player.getConditionalMoves().add(new GameConditionalMoves(player, "marshalled", "moves", new Date()));

		int count = countRowsInTable(GameConditionalMoves.TABLE_NAME);
		flushSession();

		assertEquals(++count, countRowsInTable(GameConditionalMoves.TABLE_NAME));

		player.getConditionalMoves().remove(player.getConditionalMoves().iterator().next());
		flushSession();

		assertEquals(--count, countRowsInTable(GameConditionalMoves.TABLE_NAME));
	}

	@Test
	public void shouldFindGameThatUserHasJoined()
	{
		GamePlayer player = new GamePlayer(game, user1, new Date());
		game.getPlayers().add(player);
		flushSession();

		List<Game> games = gameDao.findGames(null, user1, null, null, null, null, null, null, null, 0, null, false);
		assertEquals(1, games.size());
		Game game = games.get(0);
		assertEquals(1, game.getPlayers().size());
		assertTrue(game == player.getGame());
		assertTrue(game.getPlayers().iterator().next() == player);
	}

	@Test
	public void shouldAutoCreateAndDeleteInvitation()
	{
		game.getInvitations().add(new GameInvitation(game, user1));

		int count = countRowsInTable(GameInvitation.TABLE_NAME);
		flushSession();

		assertEquals(++count, countRowsInTable(GameInvitation.TABLE_NAME));

		game.getInvitations().remove(game.getInvitations().iterator().next());
		flushSession();

		assertEquals(--count, countRowsInTable(GameInvitation.TABLE_NAME));
	}

	@Test
	public void shouldFindGameThatUserIsInvitedTo()
	{
		GameInvitation invitation = new GameInvitation(game, user1);
		game.getInvitations().add(invitation);
		flushSession();

		List<Game> games = gameDao.findGames(null, null, user1, null, null, null, null, null, null, 0, null, false);
		assertEquals(1, games.size());
		Game game = games.get(0);
		assertEquals(1, game.getInvitations().size());
		assertTrue(game == invitation.getGame());
		assertTrue(game.getInvitations().iterator().next() == invitation);
	}
}
