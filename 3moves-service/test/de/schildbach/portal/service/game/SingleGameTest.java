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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.schildbach.game.GameMove;
import de.schildbach.game.GameRules;
import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameResolution;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.OrderType;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.Iso8601Format;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.bo.CreateTournamentCommand;
import de.schildbach.portal.service.game.bo.Deadline;
import de.schildbach.portal.service.game.bo.DeadlineOption;
import de.schildbach.portal.service.game.bo.NumberOfPlayers;
import de.schildbach.portal.service.game.bo.RequiredRating;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
public class SingleGameTest extends AbstractGameServiceImplTest
{
	private Iterator<String> sampleGame;

	@Before
	public void setupSampleGame()
	{
		sampleGame = Arrays.asList("a4", "a5", "b4", "b5", "c4", "c5", "d4", "d5", "e4", "e5", "f4", "f5", "g4", "g5", "h4", "h5", "axb5", "axb4",
				"dxc5", "dxc4", "hxg5", "hxg4", "exf5", "exf4").iterator();
	}

	@Test
	public void testRemindAndDisqulify() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 2004-03-03 12:00... a game is created and players join
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		Game game = newRunningSingleGame(null, null);
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// 2004-03-08 12:00... user2 can remind active player (user1)
		now = isoDateFormat.parse("2004-03-08 12:00:00");
		RequestTime.set(now);
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertTrue(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// 2004-03-08 12:00... active player enters his email
		now = isoDateFormat.parse("2004-03-08 12:00:00");
		RequestTime.set(now);
		userService.setEmail("user1", "bla@blubb.de");
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertTrue(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// user1 schedules holidays
		now = isoDateFormat.parse("2004-03-06 00:00:00"); // fake past
		RequestTime.set(now);
		userService.addHolidays("user1", isoDateFormat.parse("2004-03-07 00:00:00"), isoDateFormat.parse("2004-03-09 00:00:00"));
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// user1 removes holidays
		userService.removeHolidays("user1", isoDateFormat.parse("2004-03-07 00:00:00"));
		now = isoDateFormat.parse("2004-03-08 12:00:00"); // back to reality
		RequestTime.set(now);
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertTrue(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// 2004-03-08 13:00... user2 reminds active player (user1)
		now = isoDateFormat.parse("2004-03-08 13:00:00");
		RequestTime.set(now);
		gameService.remindActivePlayer("user2", game.getId(), "text");
		assertEquals(GameState.RUNNING, game.getState());
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// 2004-03-13 13:00... user2 can disqualify active player (user1)
		now = isoDateFormat.parse("2004-03-13 13:00:00");
		RequestTime.set(now);
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertTrue(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));

		// 2004-03-13 14:00... user2 reminds active player (user1)
		now = isoDateFormat.parse("2004-03-13 14:00:00");
		RequestTime.set(now);
		gameService.disqualifyActivePlayer("user2", game.getId());
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(now, game.getFinishedAt());
		assertEquals(GameResolution.DISQUALIFY, game.getResolution());
		assertTrue(game.getWinner().getSubject().equals(userDao.read(User.class, "user2")));
		assertFalse(gameService.canRemindActivePlayer("user1", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("user2", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("owner", game.getId()));
		assertFalse(gameService.canRemindActivePlayer("unknown", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user1", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("user2", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("owner", game.getId()));
		assertFalse(gameService.canDisqualifyActivePlayer("unknown", game.getId()));
	}

	@Test
	public void testActivateNextPlayer() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		SingleGame game = newRunningSingleGame(null, null);
		assertEquals(game.getActivePlayer(), game.getPlayers().get(0));
		assertEquals(new Integer(1), game.getTurn());
		assertEquals(createdAt, game.getLastActiveAt());

		Date now = isoDateFormat.parse("2004-03-03 12:10:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Integer(1), game.getTurn());
		assertEquals(now, game.getLastActiveAt());

		now = isoDateFormat.parse("2004-03-03 12:20:00");
		RequestTime.set(now);
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Integer(2), game.getTurn());
		assertEquals(now, game.getLastActiveAt());

		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Integer(2), game.getTurn());
		assertEquals(now, game.getLastActiveAt());

		now = isoDateFormat.parse("2004-03-03 12:40:00");
		RequestTime.set(now);
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Integer(3), game.getTurn());
		assertEquals(now, game.getLastActiveAt());

		// last move won the game
		((GameServiceImpl) gameService).winGame(game, game.getPlayers().get(1));
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(now, game.getFinishedAt());
		assertEquals(GameResolution.WIN, game.getResolution());
		assertNull(game.getActivePlayer());
		assertEquals(new Integer(3), game.getTurn());
		assertEquals(now, game.getLastActiveAt());
	}

	@Test
	public void testActivatePlayer() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		SingleGame game = newRunningSingleGame(null, "5d+1d*");
		assertEquals(game.getActivePlayer(), game.getPlayers().get(0));
		assertEquals(new Integer(1), game.getTurn());
		assertEquals(createdAt, game.getLastActiveAt());
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		Date now = isoDateFormat.parse("2004-03-03 12:10:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Integer(1), game.getTurn());
		assertEquals(now, game.getLastActiveAt());
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:20:00");
		RequestTime.set(now);
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Integer(2), game.getTurn());
		assertEquals(now, game.getLastActiveAt());
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Integer(2), game.getTurn());
		assertEquals(now, game.getLastActiveAt());
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:40:00");
		RequestTime.set(now);
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Integer(3), game.getTurn());
		assertEquals(now, game.getLastActiveAt());
		assertEquals(new Long(7 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(7 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(1).getClock());
	}

	@Test
	public void testClock() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		SingleGame game = newRunningSingleGame(null, "5d+1d*");
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		Date now = isoDateFormat.parse("2004-03-03 12:10:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(5 * 24 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:20:00");
		RequestTime.set(now);
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(6 * 24 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:40:00");
		RequestTime.set(now);
		Date lastActiveAt = now;
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(new Long(7 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(7 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(1).getClock());

		// player resigns
		now = isoDateFormat.parse("2004-03-03 12:50:00");
		RequestTime.set(now);
		gameService.resignGame("user1", game.getId());
		assertEquals(new Long(7 * 24 * 60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(7 * 24 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(1).getClock());
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(now, game.getFinishedAt());
		assertEquals(GameResolution.RESIGN, game.getResolution());
		assertNull(game.getActivePlayer());
		assertEquals(new Integer(3), game.getTurn());
		assertEquals(lastActiveAt, game.getLastActiveAt());
	}

	@Test
	public void testClockExtensive() throws Exception
	{
		SingleGame game = newRunningSingleGame(null, "30d/10*:30d");

		GregorianCalendar now = new GregorianCalendar();
		now.setTime(createdAt);

		long expected = 30l * 24 * 60 * 60 * 1000;
		for (int i = 1; i < 11; i++)
		{
			assertEquals(new Long(expected), game.getPlayers().get(0).getClock());
			assertEquals(new Long(expected), game.getPlayers().get(1).getClock());

			now.add(Calendar.MINUTE, 1);
			RequestTime.set(now.getTime());
			commitMove("user1", game, sampleGame.next(), false);

			assertEquals(new Long(expected), game.getPlayers().get(1).getClock());
			expected -= 60 * 1000;
			assertEquals(new Long(expected), game.getPlayers().get(0).getClock());

			now.add(Calendar.MINUTE, 1);
			RequestTime.set(now.getTime());
			commitMove("user2", game, sampleGame.next(), false);
		}

		expected = 30l * 24 * 60 * 60 * 1000;
		assertEquals(new Long(expected), game.getPlayers().get(0).getClock());
		assertEquals(new Long(expected), game.getPlayers().get(1).getClock());
	}

	@Test
	public void testClockTimeout() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		SingleGame game = newRunningSingleGame(null, "2h");
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		Date now = isoDateFormat.parse("2004-03-03 12:10:00");
		RequestTime.set(now);
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(new Long(2 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:20:00");
		RequestTime.set(now);
		commitMove("user2", game, sampleGame.next(), false);
		assertEquals(new Long(2 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		Date lastActiveAt = now;
		commitMove("user1", game, sampleGame.next(), false);
		assertEquals(new Long(2 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 10 * 60 * 1000), game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 14:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 20 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(0), game.getPlayers().get(1).getClock());
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(isoDateFormat.parse("2004-03-03 14:20:00"), game.getFinishedAt());
		assertEquals(GameResolution.TIMEOUT, game.getResolution());
		assertNull(game.getActivePlayer());
		assertEquals(new Integer(2), game.getTurn());
		assertEquals(lastActiveAt, game.getLastActiveAt());
	}

	@Test
	public void testEloRatingSingleGame() throws Exception
	{
		assertNull(findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO));
		assertNull(findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO));

		// game 1 is created
		SingleGame game = newRunningSingleGame(Rating.CHESS_COMPUTER_ELO, null);
		List<GamePlayer> players = game.getPlayers();
		assertEquals(GameState.RUNNING, game.getState());
		assertNull(players.get(0).getRatingAtStart());
		assertNull(players.get(0).getRatingAtFinish());
		assertNull(players.get(1).getRatingAtStart());
		assertNull(players.get(1).getRatingAtFinish());
		assertNull(findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO));
		assertNull(findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO));

		// game 1 is won
		((GameServiceImpl) gameService).winGame(game, game.getActivePlayer());
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(GameResolution.WIN, game.getResolution());
		assertNull(players.get(0).getRatingAtStart());
		assertEquals("1516", players.get(0).getRatingAtFinish());
		assertNull(players.get(1).getRatingAtStart());
		assertEquals("1484", players.get(1).getRatingAtFinish());
		assertEquals("1516", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1500", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getLastValue());
		assertEquals("1484", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1500", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getLastValue());

		// later, game 2 is created
		game = newRunningSingleGame(Rating.CHESS_COMPUTER_ELO, null);
		players = game.getPlayers();
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals("1516", players.get(0).getRatingAtStart());
		assertNull(players.get(0).getRatingAtFinish());
		assertEquals("1484", players.get(1).getRatingAtStart());
		assertNull(players.get(1).getRatingAtFinish());
		assertEquals("1516", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1500", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getLastValue());
		assertEquals("1484", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1500", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getLastValue());

		// game 2 is won
		((GameServiceImpl) gameService).winGame(game, game.getActivePlayer());
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(GameResolution.WIN, game.getResolution());
		assertEquals("1516", players.get(0).getRatingAtStart());
		assertEquals("1531", players.get(0).getRatingAtFinish());
		assertEquals("1484", players.get(1).getRatingAtStart());
		assertEquals("1469", players.get(1).getRatingAtFinish());
		assertEquals("1531", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1516", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getLastValue());
		assertEquals("1469", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1484", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getLastValue());
	}

	@Test
	public void shouldReceiveGameStartNotifications() throws Exception
	{
		newRunningSingleGame(null, null);

		// each player gets a game start notification
		gameEventService.processCollectedGameEvents();
		assertEquals(1, userEventService.eventCount("user1"));
		assertEquals(1, userEventService.eventCount("user2"));
		userEventService.reset();
	}

	@Test
	public void testRunningInactiveSystemReminder() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		userService.setEmail("user2", "user2@schildbach.local");

		newRunningSingleGame(null, null);

		// swallow game start notifications
		gameEventService.processCollectedGameEvents();
		userEventService.reset();

		// a second before reminder, nothing happens
		RequestTime.set(isoDateFormat.parse("2004-05-03 11:59:59"));
		assertEquals(0, gameService.checkInactiveSystemReminder());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(0, userEventService.eventCount("user2"));
		userEventService.reset();

		// it's reminder time
		RequestTime.set(isoDateFormat.parse("2004-05-03 12:00:00"));
		assertEquals(1, gameService.checkInactiveSystemReminder());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(1, userEventService.eventCount("user2"));
		userEventService.reset();

		// an hour later, nothing happens
		RequestTime.set(isoDateFormat.parse("2004-05-03 13:00:00"));
		assertEquals(0, gameService.checkInactiveSystemReminder());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(0, userEventService.eventCount("user2"));
		userEventService.reset();
	}

	@Test
	public void inactiveSystemReminderShouldBeDelayedByActivity() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		userService.setEmail("user2", "user2@schildbach.local");

		SingleGame game = newRunningSingleGame(null, null);

		// swallow game start notifications
		gameEventService.processCollectedGameEvents();
		userEventService.reset();

		Date now = isoDateFormat.parse("2004-05-04 12:00:00");
		game.setLastActiveAt(now);
		now = isoDateFormat.parse("2004-06-04 12:00:00");
		RequestTime.set(now);
		assertEquals(0, gameService.checkInactiveSystemReminder());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(0, userEventService.eventCount("user2"));
		userEventService.reset();

		now = isoDateFormat.parse("2004-07-04 12:00:00");
		RequestTime.set(now);
		assertEquals(1, gameService.checkInactiveSystemReminder());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(1, userEventService.eventCount("user2"));
	}

	@Test
	public void inactiveSystemReminderShouldBeDelayedByReminder() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		userService.setEmail("user2", "user2@schildbach.local");

		SingleGame game = newRunningSingleGame(null, null);

		// swallow game start notifications
		gameEventService.processCollectedGameEvents();
		userEventService.reset();

		Date now = isoDateFormat.parse("2004-05-04 12:00:00");
		game.setLastReminderAt(now);
		now = isoDateFormat.parse("2004-06-04 12:00:00");
		RequestTime.set(now);
		int count = gameService.checkInactiveSystemReminder();
		assertEquals(0, count);
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(0, userEventService.eventCount("user2"));
		userEventService.reset();

		now = isoDateFormat.parse("2004-07-04 12:00:00");
		RequestTime.set(now);
		count = gameService.checkInactiveSystemReminder();
		assertEquals(1, count);
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(1, userEventService.eventCount("user2"));
	}

	@Test
	public void shouldDisqualifyAfter3Months() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		SingleGame game = newRunningSingleGame(null, null);

		// swallow game start notifications
		gameEventService.processCollectedGameEvents();
		userEventService.reset();

		// a second before disqualification, nothing happens
		RequestTime.set(isoDateFormat.parse("2004-06-03 11:59:59"));
		assertEquals(0, gameService.checkInactiveDisqualify());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(0, userEventService.eventCount("user2"));
		assertThat(game.getResolution(), not(GameResolution.DISQUALIFY));
		userEventService.reset();

		RequestTime.set(isoDateFormat.parse("2004-06-03 12:00:00"));
		assertEquals(1, gameService.checkInactiveDisqualify());
		gameEventService.processCollectedGameEvents();
		assertEquals(1, userEventService.eventCount("user1"));
		assertEquals(1, userEventService.eventCount("user2"));
		assertEquals(GameResolution.DISQUALIFY, game.getResolution());
		userEventService.reset();
	}

	@Test
	public void shouldNotDisqualifyWhenReminderLessThan3MonthsAgo() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		SingleGame game = newRunningSingleGame(null, null);

		// swallow game start notifications
		gameEventService.processCollectedGameEvents();
		userEventService.reset();

		// does not disqualify because of reminder
		Date now = isoDateFormat.parse("2004-06-03 12:00:00");
		RequestTime.set(now);
		game.setLastReminderAt(now);
		assertEquals(0, gameService.checkInactiveDisqualify());
		gameEventService.processCollectedGameEvents();
		assertEquals(0, userEventService.eventCount("user1"));
		assertEquals(0, userEventService.eventCount("user2"));
		assertThat(game.getResolution(), not(GameResolution.DISQUALIFY));
		userEventService.reset();

		// disqualify now because reminder is 3 months ago
		RequestTime.set(isoDateFormat.parse("2004-09-03 12:00:00"));
		assertEquals(1, gameService.checkInactiveDisqualify());
		gameEventService.processCollectedGameEvents();
		assertEquals(1, userEventService.eventCount("user1"));
		assertEquals(1, userEventService.eventCount("user2"));
		assertEquals(GameResolution.DISQUALIFY, game.getResolution());
	}

	@Test
	public void shouldNotStartChildGamesWhenViewing() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();
		Date createAt = isoDateFormat.parse("2008-02-08 00:00:00");
		Date startAt = isoDateFormat.parse("2008-02-09 00:00:00");

		// set up tournament
		RequestTime.set(createAt);

		CreateTournamentCommand command = new CreateTournamentCommand();
		command.setRules(Rules.CHESS);
		command.setNumPlayers(new NumberOfPlayers(3, 3));
		command.setStartAt(new Deadline());
		command.getStartAt().setOption(DeadlineOption.TIME);
		command.getStartAt().setDate(startAt);
		command.setRequiredRatings(new LinkedList<RequiredRating>());
		command.getRequiredRatings().add(new RequiredRating());
		int gameId = gameService.createTournament("owner", command)[0];

		gameService.joinGame("user1", gameId);
		gameService.joinGame("user2", gameId);
		gameService.joinGame("user3", gameId);

		assertEquals(GameState.READY, gameService.game(gameId).getState());
		for (SingleGame game : ((GameGroup) gameService.game(gameId)).getChildGames())
			assertEquals(GameState.READY, game.getState());

		// try, but should not start child games
		RequestTime.set(startAt);

		for (SingleGame game : ((GameGroup) gameService.game(gameId)).getChildGames())
		{
			gameService.viewGame(game.getId());
			assertEquals(GameState.READY, game.getState());
		}

		// start parent game
		RequestTime.set(startAt);

		gameService.viewGame(gameId);
		assertEquals(GameState.RUNNING, gameService.game(gameId).getState());
		for (SingleGame game : ((GameGroup) gameService.game(gameId)).getChildGames())
			assertEquals(GameState.RUNNING, game.getState());
	}

	@Test
	public void test() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a game is created and populated
		SingleGame game = newRunningSingleGame(null, "2h");
		assertEquals(GameState.RUNNING, game.getState());
		Date now = Iso8601Format.parseDateTime("2004-03-03 12:00:00");
		RequestTime.set(now);
		assertEquals(userDao.read("user1"), game.getActivePlayer().getSubject());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		// 12:15... move user1
		now = isoDateFormat.parse("2004-03-03 12:15:00");
		RequestTime.set(now);
		commitMove("user1", game, "d2d4", false);
		assertEquals(userDao.read("user2"), game.getActivePlayer().getSubject());
		assertFalse(gameService.canCommitMove("user1", game.getId()));
		assertTrue(gameService.canCommitMove("user2", game.getId()));
		assertEquals(new Long(2 * 60 * 60 * 1000 - 15 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(1).getClock());

		// 12:30... move user2
		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		commitMove("user2", game, "d7d5", false);
		assertEquals(userDao.read("user1"), game.getActivePlayer().getSubject());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));
		assertEquals(new Long(2 * 60 * 60 * 1000 - 15 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 15 * 60 * 1000), game.getPlayers().get(1).getClock());

		// 14:30... move user1, too late
		now = isoDateFormat.parse("2004-03-03 14:30:00");
		RequestTime.set(now);
		try
		{
			commitMove("user1", game, "e2e4", false);
		}
		catch (NotAuthorizedException x)
		{
		}
		assertNull(game.getActivePlayer());
		assertFalse(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));
		assertEquals(new Long(0), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 15 * 60 * 1000), game.getPlayers().get(1).getClock());
		assertEquals(GameState.FINISHED, game.getState());
		assertEquals(GameResolution.TIMEOUT, game.getResolution());
	}

	@Test
	public void testThemedGameEvenInitialMoves() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 2004-03-03 12:00... a game is created and players join
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		Subject owner = userDao.read("owner");
		String initialHistory = "1. d4 d5 2. c4 dxc4";
		SingleGame game = gameDao.newSingleGame(owner, now, Rules.CHESS, initialHistory, Aid.NONE, false, OrderType.FORWARD, null, "1h/3*", readyAt,
				startAt, null);
		gameDao.create(game);
		RequestTime.set(now);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());
		assertNull(((SingleGame) game).getHistoryNotation());
		assertNull(game.getTurn());
		assertNull(game.getActivePlayer());
		assertNull(game.getPlayers().get(0).getClock());
		assertNull(game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertNull(((SingleGame) game).getHistoryNotation());
		assertNull(game.getTurn());
		assertNull(game.getActivePlayer());
		assertNull(game.getPlayers().get(0).getClock());
		assertNull(game.getPlayers().get(1).getClock());

		// game is now ready
		now = isoDateFormat.parse("2004-03-03 13:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(initialHistory, ((SingleGame) game).getHistoryNotation());
		assertEquals(new Integer(3), game.getTurn());
		assertNull(game.getActivePlayer());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		// game is now started
		now = isoDateFormat.parse("2004-03-03 14:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(initialHistory, ((SingleGame) game).getHistoryNotation());
		assertEquals(3, game.getTurn().intValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user1", game, "b1-c3", false);
		assertEquals(3, game.getTurn().intValue());
		assertEquals(60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user2", game, "c8-e6", false);
		assertEquals(4, game.getTurn().intValue());
		assertEquals(60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user1", game, "c1-f4", false);
		assertEquals(4, game.getTurn().intValue());
		assertEquals(60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user2", game, "c7-c5", false);
		assertEquals(5, game.getTurn().intValue());
		assertEquals(60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user1", game, "d4-c5", false);
		assertEquals(5, game.getTurn().intValue());
		assertEquals(60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user2", game, "d8-b6", false);
		assertEquals(6, game.getTurn().intValue());
		assertEquals(2 * 60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(2 * 60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());

		commitMove("user1", game, "c5-b6", false);
		assertEquals(6, game.getTurn().intValue());
		assertEquals(2 * 60 * 60 * 1000 - 30 * 60 * 1000, game.getPlayers().get(0).getClock().longValue());
		assertEquals(2 * 60 * 60 * 1000, game.getPlayers().get(1).getClock().longValue());
	}

	@Test
	public void testThemedGameOddInitialMoves() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 2004-03-03 12:00... a game is created and players join
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		Subject owner = userDao.read("owner");
		String initialHistory = "1. d4 d5 2. c4";
		SingleGame game = gameDao.newSingleGame(owner, now, Rules.CHESS, initialHistory, Aid.NONE, false, OrderType.FORWARD, null, "1h/3*", readyAt,
				startAt, null);
		gameDao.create(game);
		RequestTime.set(now);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());
		assertNull(((SingleGame) game).getHistoryNotation());
		assertNull(game.getTurn());
		assertNull(game.getActivePlayer());
		assertNull(game.getPlayers().get(0).getClock());
		assertNull(game.getPlayers().get(1).getClock());

		now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertNull(((SingleGame) game).getHistoryNotation());
		assertNull(game.getTurn());
		assertNull(game.getActivePlayer());
		assertNull(game.getPlayers().get(0).getClock());
		assertNull(game.getPlayers().get(1).getClock());

		// game is now ready
		now = isoDateFormat.parse("2004-03-03 13:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(initialHistory, ((SingleGame) game).getHistoryNotation());
		assertEquals(new Integer(2), game.getTurn());
		assertNull(game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(1).getClock());

		// game is now started
		now = isoDateFormat.parse("2004-03-03 14:30:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(initialHistory, ((SingleGame) game).getHistoryNotation());
		assertEquals(new Integer(2), game.getTurn());
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(1).getClock());
		assertFalse(gameService.canCommitMove("user1", game.getId()));
		assertTrue(gameService.canCommitMove("user2", game.getId()));

		commitMove("user2", game, "d5-c4", false);
		assertEquals(new Integer(3), game.getTurn());
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user1", game, "b1-c3", false);
		assertEquals(new Integer(3), game.getTurn());
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user2", game, "c8-e6", false);
		assertEquals(new Integer(4), game.getTurn());
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user1", game, "c1-f4", false);
		assertEquals(new Integer(4), game.getTurn());
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user2", game, "c7-c5", false);
		assertEquals(new Integer(5), game.getTurn());
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Long(60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user1", game, "d4-c5", false);
		assertEquals(new Integer(5), game.getTurn());
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user2", game, "d8-b6", false);
		assertEquals(new Integer(6), game.getTurn());
		assertEquals(game.getPlayers().get(0), game.getActivePlayer());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());

		commitMove("user1", game, "c5-b6", false);
		assertEquals(new Integer(6), game.getTurn());
		assertEquals(game.getPlayers().get(1), game.getActivePlayer());
		assertEquals(new Long(2 * 60 * 60 * 1000), game.getPlayers().get(0).getClock());
		assertEquals(new Long(2 * 60 * 60 * 1000 - 30 * 60 * 1000), game.getPlayers().get(1).getClock());
	}

	@Test
	public void testConditionalMoves() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 2004-03-03 12:00... a game is created and players join
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		Subject owner = userDao.read("owner");
		SingleGame game = gameDao
				.newSingleGame(owner, now, Rules.CHESS, null, Aid.NONE, false, OrderType.FORWARD, null, null, startAt, startAt, null);
		gameDao.create(game);
		RequestTime.set(now);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());
		RequestTime.set(startAt);
		gameService.viewGame(game.getId());
		assertEquals(GameState.RUNNING, game.getState());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));

		// set conditional move
		gameService.addConditionalMoves("user2", game.getId(), "1. d4 d5 2. e4 e5");
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals("1. d4 d5 2. e4 e5", game.getPlayers().get(1).getConditionalMoves().iterator().next().getMoves());

		// partner moves as expected
		commitMove("user1", game, "d4", false);
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals("1. d4 d5 2. e4 e5", game.getPlayers().get(1).getConditionalMoves().iterator().next().getMoves());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));

		// partner does not move as expected
		commitMove("user1", game, "f4", false);
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals(0, game.getPlayers().get(1).getConditionalMoves().size());
		assertFalse(gameService.canCommitMove("user1", game.getId()));
		assertTrue(gameService.canCommitMove("user2", game.getId()));
	}

	@Test
	public void shouldRemoveOutOfDateConditionalMoveAfterExecuted() throws Exception
	{
		SingleGame game = newRunningSingleGame(null, null);

		// set conditional move
		gameService.addConditionalMoves("user2", game.getId(), "1. d4 d5");
		assertEquals(1, game.getPlayers().get(1).getConditionalMoves().size());

		// partner moves as expected
		commitMove("user1", game, "d4", false);
		assertEquals("1. d4 d5", game.getHistoryNotation());

		// conditional move should be removed
		assertEquals(0, game.getPlayers().get(1).getConditionalMoves().size());
	}

	@Test
	public void testConditionalMovesWithOfferRemis() throws Exception
	{
		SingleGame game = newRunningSingleGame(null, null);
		RequestTime.set(startAt);
		gameService.viewGame(game.getId());
		assertEquals(GameState.RUNNING, game.getState());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));

		// set conditional move
		gameService.addConditionalMoves("user2", game.getId(), "1. d4 d5 2. e4 e5");
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals("1. d4 d5 2. e4 e5", game.getPlayers().get(1).getConditionalMoves().iterator().next().getMoves());

		// partner moves as expected, but sets remis
		commitMove("user1", game, "d4", true);
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals("1. d4 d5 2. e4 e5", game.getPlayers().get(1).getConditionalMoves().iterator().next().getMoves());
		assertEquals("1. d4", game.getHistoryNotation());
		assertFalse(gameService.canCommitMove("user1", game.getId()));
		assertTrue(gameService.canCommitMove("user2", game.getId()));
	}

	@Test
	public void testConditionalMovesCasteling() throws Exception
	{
		SingleGame game = newRunningSingleGame(null, null);
		gameService
				.addConditionalMoves(
						"user2",
						game.getId(),
						"1. d4 d5 2. Nf3 e6 3. e3 Bb4+ 4. c3 Bd6 5. Bd3 Nf6 6. Nbd2 Nbd7 7. Qc2 c5 8. dxc5 Nxc5 9. Bb5+ Bd7 10. Bxd7+ Qxd7 11. O-O O-O 12. Rd1 Rfc8 13. Nf1 e5 14. Qe2 e4 15. Nd4 a5 16. Nb5 Nd3 17. Nxd6");
		commitMove("user1", game, "d4", false);
		commitMove("user1", game, "Nf3", false);
		commitMove("user1", game, "e3", false);
		commitMove("user1", game, "c3", false);
		commitMove("user1", game, "Bd3", false);
		commitMove("user1", game, "Nbd2", false);
		commitMove("user1", game, "Qc2", false);
		commitMove("user1", game, "dxc5", false);
		commitMove("user1", game, "Bb5+", false);
		commitMove("user1", game, "Bxd7+", false);
		commitMove("user1", game, "O-O", false);
		assertEquals("1. d4 d5 2. Nf3 e6 3. e3 Bb4+ 4. c3 Bd6 5. Bd3 Nf6 6. Nbd2 Nbd7 7. Qc2 c5 8. dxc5 Nxc5 9. Bb5+ Bd7 "
				+ "10. Bxd7+ Qxd7 11. O-O O-O", game.getHistoryNotation());
	}

	@Test
	public void testConditionalMovesBug() throws Exception
	{
		SingleGame game = newRunningSingleGame(null, null);
		assertEquals(GameState.RUNNING, game.getState());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));

		// set conditional move
		gameService.addConditionalMoves("user2", game.getId(), "1. e4 d5 2. exd5 Qxd5");
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals("1. e4 d5 2. exd5 Qxd5", game.getPlayers().get(1).getConditionalMoves().iterator().next().getMoves());

		// partner moves as expected
		commitMove("user1", game, "e4", false);
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		assertEquals("1. e4 d5 2. exd5 Qxd5", game.getPlayers().get(1).getConditionalMoves().iterator().next().getMoves());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));

		// partner moves as expected
		commitMove("user1", game, "exd5", false);
		assertEquals(0, game.getPlayers().get(0).getConditionalMoves().size());
		// assertNull(((HibernateGamePlayer) game.getPlayers().get(1)).getConditionalMoves());
		assertTrue(gameService.canCommitMove("user1", game.getId()));
		assertFalse(gameService.canCommitMove("user2", game.getId()));
	}

	@Test
	public void testRatedGame() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();
		Subject owner = userDao.read("owner");
		Subject user1 = userDao.read("user1");
		Subject user2 = userDao.read("user2");
		Subject user3 = userDao.read("user3");
		assertNull(subjectRatingDao.findRating(user1, Rating.CHESS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user2, Rating.CHESS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user3, Rating.CHESS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user1, Rating.CHECKERS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user2, Rating.CHECKERS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user3, Rating.CHECKERS_COMPUTER_ELO));

		// a chess game is resigned
		Date createdAt = isoDateFormat.parse("2004-03-03 12:00:00");
		Date now = isoDateFormat.parse("2004-03-03 14:00:00");
		RequestTime.set(now);
		Game game = gameDao.newSingleGame(owner, createdAt, Rules.CHESS, null, Aid.NONE, false, OrderType.FORWARD, Rating.CHESS_COMPUTER_ELO, null,
				now, now, null);
		gameDao.create(game);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());
		gameService.viewGame(game.getId());
		gameService.resignGame("user1", game.getId());
		assertEquals("1484", subjectRatingDao.findRating(user1, Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1516", subjectRatingDao.findRating(user2, Rating.CHESS_COMPUTER_ELO).getValue());
		assertNull(subjectRatingDao.findRating(user3, Rating.CHESS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user1, Rating.CHECKERS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user2, Rating.CHECKERS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user3, Rating.CHECKERS_COMPUTER_ELO));

		// a checkers game is resigned
		game = gameDao.newSingleGame(owner, createdAt, Rules.CHECKERS, null, Aid.NONE, false, OrderType.FORWARD, Rating.CHECKERS_COMPUTER_ELO, null,
				now, now, null);
		gameDao.create(game);
		gameService.joinGame("user2", game.getId());
		gameService.joinGame("user3", game.getId());
		gameService.viewGame(game.getId());
		gameService.resignGame("user2", game.getId());
		assertEquals("1484", subjectRatingDao.findRating(user1, Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1516", subjectRatingDao.findRating(user2, Rating.CHESS_COMPUTER_ELO).getValue());
		assertNull(subjectRatingDao.findRating(user3, Rating.CHESS_COMPUTER_ELO));
		assertNull(subjectRatingDao.findRating(user1, Rating.CHECKERS_COMPUTER_ELO));
		assertEquals("1484", subjectRatingDao.findRating(user2, Rating.CHECKERS_COMPUTER_ELO).getValue());
		assertEquals("1516", subjectRatingDao.findRating(user3, Rating.CHECKERS_COMPUTER_ELO).getValue());

		// a chess game is resigned
		game = gameDao.newSingleGame(owner, createdAt, Rules.CHESS, null, Aid.NONE, false, OrderType.FORWARD, Rating.CHESS_COMPUTER_ELO, null, now,
				now, null);
		gameDao.create(game);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user3", game.getId());
		gameService.viewGame(game.getId());
		gameService.resignGame("user1", game.getId());
		assertEquals("1469", subjectRatingDao.findRating(user1, Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1516", subjectRatingDao.findRating(user2, Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1515", subjectRatingDao.findRating(user3, Rating.CHESS_COMPUTER_ELO).getValue());
		assertNull(subjectRatingDao.findRating(user1, Rating.CHECKERS_COMPUTER_ELO));
		assertEquals("1484", subjectRatingDao.findRating(user2, Rating.CHECKERS_COMPUTER_ELO).getValue());
		assertEquals("1516", subjectRatingDao.findRating(user3, Rating.CHECKERS_COMPUTER_ELO).getValue());

		// a chess game is drawn
		game = gameDao.newSingleGame(owner, createdAt, Rules.CHESS, null, Aid.NONE, false, OrderType.FORWARD, Rating.CHESS_COMPUTER_ELO, null, now,
				now, null);
		gameDao.create(game);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());
		gameService.viewGame(game.getId());
		((GameServiceImpl) gameService).drawGame(game.getId());
		assertEquals("1471", subjectRatingDao.findRating(user1, Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1514", subjectRatingDao.findRating(user2, Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1515", subjectRatingDao.findRating(user3, Rating.CHESS_COMPUTER_ELO).getValue());
		assertNull(subjectRatingDao.findRating(user1, Rating.CHECKERS_COMPUTER_ELO));
		assertEquals("1484", subjectRatingDao.findRating(user2, Rating.CHECKERS_COMPUTER_ELO).getValue());
		assertEquals("1516", subjectRatingDao.findRating(user3, Rating.CHECKERS_COMPUTER_ELO).getValue());
	}

	private SingleGame newRunningSingleGame(Rating rating, String clockConstraint)
	{
		Subject owner = userDao.read("owner");
		SingleGame game = gameDao.newSingleGame(owner, createdAt, Rules.CHESS, null, Aid.NONE, false, OrderType.FORWARD, rating, clockConstraint,
				null, null, null);
		gameDao.create(game);
		RequestTime.set(createdAt);
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());
		return game;
	}

	private void commitMove(String username, SingleGame singleGame, String moveNotation, boolean offerRemis)
	{
		// unmarshal game
		GameRules rules = GameRulesHelper.rules(singleGame);
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);
		GameMove move = rules.parseMove(moveNotation, Locale.ENGLISH, game);

		gameService.commitMove(username, singleGame.getId(), move, offerRemis);
	}

	private SubjectRating findRating(List<SubjectRating> ratings, Rating rating)
	{
		for (SubjectRating subjectRating : ratings)
			if (subjectRating.getRating() == rating)
				return subjectRating;

		return null;
	}
}
