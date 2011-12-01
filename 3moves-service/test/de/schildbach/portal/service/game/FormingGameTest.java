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

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Date;

import org.junit.Test;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameInvitation;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.OrderType;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.Iso8601Format;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
public class FormingGameTest extends AbstractGameServiceImplTest
{
	@Test
	public void gameWaitingForReadyShouldBeForming()
	{
		Game game = newSingleGame(readyAt, null, false);
		assertEquals(GameState.FORMING, game.getState());

		// even if players join
		RequestTime.set(createdAt);
		gameService.joinGame("user1", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		gameService.joinGame("user2", game.getId());
		assertEquals(GameState.FORMING, game.getState());
	}

	@Test
	public void gameWaitingForStartShouldBeReady()
	{
		Game game = newSingleGame(null, startAt, false);
		RequestTime.set(createdAt);
		gameService.joinGame("user1", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		gameService.joinGame("user2", game.getId());
		assertEquals(GameState.READY, game.getState());
		assertEquals(createdAt, game.getReadyAt());
		assertEquals(startAt, game.getStartedAt());
	}

	@Test
	public void gameWithoutReadyAndWithoutStartShouldStartWhenFull()
	{
		Game game = newSingleGame(null, null, false);
		RequestTime.set(createdAt);
		gameService.joinGame("user1", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		gameService.joinGame("user2", game.getId());

		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(createdAt, game.getReadyAt());
		assertEquals(createdAt, game.getStartedAt());
	}

	@Test
	public void testCheckGameStateTimeplannedGame() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a game is created
		Game game = newSingleGame(readyAt, startAt, false);

		// 12:30... two players join
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		gameService.joinGame("user1", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		gameService.joinGame("user2", game.getId());
		assertEquals(GameState.FORMING, game.getState());

		// 12:45... check game kicks in
		now = isoDateFormat.parse("2004-03-03 12:45:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(GameState.FORMING, game.getState());

		// 13:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 13:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.FORMING));
		assertEquals(GameState.READY, game.getState());
		assertEquals(readyAt, game.getReadyAt());

		// 14:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.READY));
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(startAt, game.getStartedAt());
	}

	@Test
	public void testCheckFormingGames() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();
		Date readyAt = isoDateFormat.parse("2004-03-04 12:00:00");
		Date startAt = isoDateFormat.parse("2004-03-05 12:00:00");

		// game with ready at
		Game gameR = newSingleGame(readyAt, null, false);

		// game with start at
		Game gameS = newSingleGame(null, startAt, false);

		// game with ready at and start at
		Game gameRS = newSingleGame(readyAt, startAt, false);

		// game without any
		Game game = newSingleGame(null, null, false);

		// check forming games
		int count = gameService.checkGamesWithState(GameState.FORMING);
		assertEquals(0, count);
		assertEquals(GameState.FORMING, gameR.getState());
		assertEquals(GameState.FORMING, gameS.getState());
		assertEquals(GameState.FORMING, gameRS.getState());
		assertEquals(GameState.FORMING, game.getState());

		// check forming games just after ready at
		Date now = isoDateFormat.parse("2004-03-04 13:00:00");
		RequestTime.set(now);
		count = gameService.checkGamesWithState(GameState.FORMING);
		assertEquals(0, count);
		assertEquals(GameState.FORMING, gameR.getState());
		assertEquals(GameState.FORMING, gameS.getState());
		assertEquals(GameState.FORMING, gameRS.getState());
		assertEquals(GameState.FORMING, game.getState());

		// check forming games just after start at
		now = isoDateFormat.parse("2004-03-05 13:00:00");
		RequestTime.set(now);
		count = gameService.checkGamesWithState(GameState.FORMING);
		assertEquals(2, count);
		assertEquals(GameState.FORMING, gameR.getState());
		assertThat(gameS.getState(), not(GameState.FORMING));
		assertThat(gameRS.getState(), not(GameState.FORMING));
		assertEquals(GameState.FORMING, game.getState());

		// check forming games just before 1 month later
		now = isoDateFormat.parse("2004-04-03 11:59:00");
		RequestTime.set(now);
		count = gameService.checkGamesWithState(GameState.FORMING);
		assertEquals(0, count);
		assertEquals(GameState.FORMING, gameR.getState());
		assertThat(gameS.getState(), not(GameState.FORMING));
		assertThat(gameRS.getState(), not(GameState.FORMING));
		assertEquals(GameState.FORMING, game.getState());

		// check forming games exactly 1 month later
		now = isoDateFormat.parse("2004-04-03 12:00:00");
		RequestTime.set(now);
		count = gameService.checkGamesWithState(GameState.FORMING);
		assertEquals(2, count);
		assertThat(gameR.getState(), not(GameState.FORMING));
		assertThat(gameS.getState(), not(GameState.FORMING));
		assertThat(gameRS.getState(), not(GameState.FORMING));
		assertThat(game.getState(), not(GameState.FORMING));
	}

	@Test
	public void testInvitations() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a game is created
		Subject owner = userDao.read("owner");
		Game game = newSingleGame(readyAt, startAt, true);
		assertFalse(isSubjectPlayerOfGame(owner, game));
		assertFalse(isSubjectInvitedToGame(userDao.read(User.class, "user1"), game));
		assertFalse(gameService.canJoinGame("user1", game.getId()));
		assertFalse(gameService.canJoinGame("user2", game.getId()));
		// TODO assertFalse(gameService.canInviteSubjectToGame("user1", game.getId(), "user1"));
		// TODO assertTrue(gameService.canInviteSubjectToGame("owner", game.getId(), "user1"));

		// owner invites user1 to game
		gameService.inviteSubjectToGame("owner", game.getId(), "user1");
		assertTrue(isSubjectInvitedToGame(userDao.read(User.class, "user1"), game));
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		assertFalse(gameService.canJoinGame("user2", game.getId()));
		// TODO assertFalse(gameService.canInviteSubjectToGame("user1", game.getId(), "user1"));
		// TODO assertFalse(gameService.canInviteSubjectToGame("owner", game.getId(), "user1"));

		// user1 joins the game
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		RequestTime.set(now);
		gameService.joinGame("user1", game.getId());
		assertFalse(gameService.canJoinGame("user1", game.getId()));
		assertFalse(gameService.canJoinGame("user2", game.getId()));
		assertFalse(gameService.canRemoveInvitationFromGame("user2", game.getId(), "user2"));

		// owner invites user2 to game
		gameService.inviteSubjectToGame("owner", game.getId(), "user2");
		assertTrue(isSubjectInvitedToGame(userDao.read(User.class, "user2"), game));
		assertTrue(gameService.canRemoveInvitationFromGame("user2", game.getId(), "user2"));

		// owner removes invitation
		gameService.removeInvitationFromGame("owner", game.getId(), "user2");
		assertFalse(isSubjectInvitedToGame(userDao.read(User.class, "user2"), game));

		// invited user removes invitation
		gameService.removeInvitationFromGame("user1", game.getId(), "user1");
		assertFalse(isSubjectInvitedToGame(userDao.read(User.class, "user1"), game));

		// owner invites again
		gameService.inviteSubjectToGame("owner", game.getId(), "user1");
		gameService.inviteSubjectToGame("owner", game.getId(), "user2");
		assertTrue(isSubjectInvitedToGame(userDao.read(User.class, "user1"), game));
		assertTrue(isSubjectInvitedToGame(userDao.read(User.class, "user2"), game));

		// user2 joins
		gameService.joinGame("user2", game.getId());
		assertTrue(isSubjectPlayerOfGame(userDao.read(User.class, "user1"), game));
		assertTrue(isSubjectPlayerOfGame(userDao.read(User.class, "user2"), game));

		// 13:00 game is readied...
		RequestTime.set(readyAt);
		gameService.viewGame(game.getId());
		assertEquals(GameState.READY, game.getState());
		assertTrue(game.getInvitations().isEmpty());
	}

	private SingleGame newSingleGame(Date readyAt, Date startAt, boolean isClosed)
	{
		Subject owner = userDao.read("owner");
		SingleGame game = gameDao.newSingleGame(owner, createdAt, Rules.CHESS, null, Aid.NONE, isClosed, OrderType.FORWARD, null, null, readyAt,
				startAt, null);
		gameDao.create(game);
		return game;
	}

	private boolean isSubjectInvitedToGame(Subject subject, Game game)
	{
		for (GameInvitation invitation : game.getInvitations())
			if (invitation.getSubject().equals(subject))
				return true;

		return false;
	}

	private boolean isSubjectPlayerOfGame(Subject subject, Game game)
	{
		for (GamePlayer player : game.getPlayers())
			if (player.getSubject().equals(subject))
				return true;

		return false;
	}
}
