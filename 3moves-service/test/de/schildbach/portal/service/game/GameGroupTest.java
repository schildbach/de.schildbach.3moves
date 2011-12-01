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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameResolution;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.Iso8601Format;
import de.schildbach.portal.service.game.bo.CreateTournamentCommand;
import de.schildbach.portal.service.game.bo.Deadline;
import de.schildbach.portal.service.game.bo.DeadlineOption;
import de.schildbach.portal.service.game.bo.NumberOfPlayers;
import de.schildbach.portal.service.game.bo.RequiredRating;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
public class GameGroupTest extends AbstractGameServiceImplTest
{
	@Test
	public void testCheckGameStateTimePlannedGameGroup() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created
		Game game = newGameGroup(null, 2, 4, false, "disabled", null, null, null, readyAt, startAt, Rules.CHESS, null, null, null);

		// 12:30... four players join
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		gameService.joinGame("user1", game.getId());
		assertFalse(gameService.canJoinGame("user1", game.getId()));
		assertTrue(gameService.canJoinGame("user2", game.getId()));
		gameService.joinGame("user2", game.getId());
		gameService.joinGame("user3", game.getId());
		gameService.joinGame("user4", game.getId());
		assertFalse(gameService.canJoinGame("unknown", game.getId()));
		assertFalse(gameService.canUnjoinGame("unknown", game.getId()));
		assertTrue(gameService.canUnjoinGame("user1", game.getId()));
		gameService.unjoinGame("user1", game.getId());
		assertTrue(gameService.canJoinGame("unknown", game.getId()));

		// 12:45... check game kicks in
		now = isoDateFormat.parse("2004-03-03 12:45:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(GameState.FORMING, game.getState());
		// assertEquals(game.getId(), gameDao.id);

		// 13:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 13:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.FORMING));
		assertEquals(GameState.READY, game.getState());
		assertEquals(readyAt, game.getReadyAt());
		// assertEquals(7, gameDao.id);

		// 14:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.READY));
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(startAt, game.getStartedAt());
		// assertEquals(7, gameDao.id);
	}

	@Test
	public void testCheckGameStateTimePlannedGameGroupUnderful() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created
		Game game = newGameGroup(null, 2, 4, false, "disabled", null, null, null, readyAt, startAt, Rules.CHESS, null, null, null);

		// 12:30... one player joins
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		gameService.joinGame("user1", game.getId());

		// 13:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 13:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(GameState.FORMING, game.getState());

		// 13:20... second player joins
		now = isoDateFormat.parse("2004-03-03 13:20:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user2", game.getId()));
		gameService.joinGame("user2", game.getId());
		assertThat(game.getState(), not(GameState.FORMING));
		assertEquals(GameState.READY, game.getState());
		assertEquals(now, game.getReadyAt());
		// assertEquals(3, gameDao.id);

		// 14:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.READY));
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(startAt, game.getStartedAt());
		// assertEquals(3, gameDao.id);
	}

	@Test
	public void testCheckGameStateTimePlannedGameGroupTooLate() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created
		Game game = newGameGroup(null, 2, 4, false, "disabled", null, null, null, readyAt, startAt, Rules.CHESS, null, null, null);
		assertEquals(startAt, game.getStartedAt());

		// 12:30... one player joins
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		gameService.joinGame("user1", game.getId());
		assertEquals(startAt, game.getStartedAt());

		// 13:15... check game kicks in
		now = isoDateFormat.parse("2004-03-03 13:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertEquals(GameState.FORMING, game.getState());
		assertEquals(startAt, game.getStartedAt());

		// 14:15... check game kicks in again
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertFalse(gameService.canJoinGame("user2", game.getId()));
		assertEquals(GameState.UNACCOMPLISHED, game.getState());
	}

	@Test
	public void testCheckGameStateTimePlannedGameGroupNotTooLate() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created
		Game game = newGameGroup(null, 2, 4, false, "disabled", null, null, null, readyAt, startAt, Rules.CHESS, null, null, null);
		assertEquals(startAt, game.getStartedAt());

		// 12:30... two players join
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		gameService.joinGame("user1", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		assertEquals(startAt, game.getStartedAt());
		assertTrue(gameService.canJoinGame("user2", game.getId()));
		gameService.joinGame("user2", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		assertEquals(startAt, game.getStartedAt());

		// 14:15... check game kicks in
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.FORMING));
		assertThat(game.getState(), not(GameState.UNACCOMPLISHED));
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(startAt, game.getStartedAt());
	}

	@Test
	public void testCheckGameStateGameGroupAutoReady() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created
		Game game = newGameGroup(null, 2, 4, false, "disabled", null, null, null, null, startAt, Rules.CHESS, null, null, null);

		// 12:30... two players join
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		gameService.joinGame("user1", game.getId());
		gameService.joinGame("user2", game.getId());

		// 12:40... two more players join
		now = isoDateFormat.parse("2004-03-03 12:40:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user3", game.getId()));
		gameService.joinGame("user3", game.getId());
		assertEquals(GameState.FORMING, game.getState());
		gameService.joinGame("user4", game.getId());
		assertThat(game.getState(), not(GameState.FORMING));
		assertEquals(GameState.READY, game.getState());
		assertEquals(now, game.getReadyAt());

		// 14:15... check game kicks in
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.READY));
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(startAt, game.getStartedAt());
	}

	@Test
	public void testReadyGame() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created
		Game game = newGameGroup(null, 2, 4, false, "disabled", null, null, null, null, startAt, Rules.CHESS, null, null, null);
		// TODO assertFalse(gameService.canGameBeReadied(game.getId(), now));

		// 12:30... two players join
		Date now = isoDateFormat.parse("2004-03-03 12:30:00");
		RequestTime.set(now);
		assertTrue(gameService.canJoinGame("user1", game.getId()));
		gameService.joinGame("user1", game.getId());
		// TODO assertFalse(gameService.canGameBeReadied(game.getId(), now));
		gameService.joinGame("user2", game.getId());
		// TODO assertTrue(gameService.canGameBeReadied(game.getId(), now));

		// 12:40... game is readied by owner
		now = isoDateFormat.parse("2004-03-03 12:40:00");
		RequestTime.set(now);
		gameService.readyGame("owner", game.getId());
		assertThat(game.getState(), not(GameState.FORMING));
		assertEquals(GameState.READY, game.getState());
		assertEquals(now, game.getReadyAt());

		// 14:15... check game kicks in
		now = isoDateFormat.parse("2004-03-03 14:15:00");
		RequestTime.set(now);
		gameService.viewGame(game.getId());
		assertThat(game.getState(), not(GameState.READY));
		assertEquals(GameState.RUNNING, game.getState());
		assertEquals(startAt, game.getStartedAt());
	}

	@Test
	public void testPointsWin() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created and players join
		GameGroup gamegroup = newGameGroup(null, 2, 2, false, "disabled", null, null, null, null, null, Rules.CHESS, null, null, null);
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		RequestTime.set(now);
		gameService.joinGame("user1", gamegroup.getId());
		gameService.joinGame("user2", gamegroup.getId());
		Set<SingleGame> childGames = gamegroup.getChildGames();
		assertEquals(GameState.RUNNING, gamegroup.getState());
		assertEquals(2, childGames.size());
		Iterator<SingleGame> i = childGames.iterator();
		SingleGame game1 = i.next();
		SingleGame game2 = i.next();
		assertEquals(GameState.RUNNING, game1.getState());
		assertEquals(GameState.RUNNING, game2.getState());

		// 13:00... game1 is won
		now = isoDateFormat.parse("2004-03-03 13:00:00");
		RequestTime.set(now);
		((GameServiceImpl) gameService).winGame(game1, game1.getActivePlayer());
		assertEquals(GameState.FINISHED, game1.getState());
		assertEquals(GameResolution.WIN, game1.getResolution());
		assertThat(game1.getPlayers().get(0).getPoints(), equalTo(1f));
		assertThat(game1.getPlayers().get(1).getPoints(), equalTo(0f));
		assertThat(gamegroup.getState(), not(GameState.FINISHED));
		assertThat(gamegroup.getPlayers().get(0).getPoints(), equalTo(1f));
		assertThat(gamegroup.getPlayers().get(1).getPoints(), equalTo(0f));

		// 14:00... game2 is resigned
		now = isoDateFormat.parse("2004-03-03 14:00:00");
		RequestTime.set(now);
		gameService.resignGame(game2.getActivePlayer().getSubject().getName(), game2.getId());
		assertEquals(GameState.FINISHED, game2.getState());
		assertEquals(GameResolution.RESIGN, game2.getResolution());
		assertThat(game2.getPlayers().get(0).getPoints(), equalTo(0f));
		assertThat(game2.getPlayers().get(1).getPoints(), equalTo(1f));
		gameService.checkFinishGame(gamegroup.getId());
		assertEquals(GameState.FINISHED, gamegroup.getState());
		assertEquals(GameResolution.WIN, gamegroup.getResolution());
		assertThat(gamegroup.getPlayers().get(0).getPoints(), equalTo(2f));
		assertThat(gamegroup.getPlayers().get(1).getPoints(), equalTo(0f));
	}

	@Test
	public void testPointsDraw() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created and players join
		GameGroup gamegroup = newGameGroup(null, 2, 2, false, "disabled", null, null, null, null, null, Rules.CHESS, null, null, null);
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		RequestTime.set(now);
		gameService.joinGame("user1", gamegroup.getId());
		gameService.joinGame("user2", gamegroup.getId());
		Set<SingleGame> childGames = gamegroup.getChildGames();
		assertEquals(GameState.RUNNING, gamegroup.getState());
		assertEquals(2, childGames.size());
		Iterator<SingleGame> i = childGames.iterator();
		Game game1 = i.next();
		Game game2 = i.next();
		assertEquals(GameState.RUNNING, game1.getState());
		assertEquals(GameState.RUNNING, game2.getState());

		// 13:00... game1 is won
		now = isoDateFormat.parse("2004-03-03 13:00:00");
		RequestTime.set(now);
		((GameServiceImpl) gameService).drawGame(game1.getId());
		assertEquals(GameState.FINISHED, game1.getState());
		assertEquals(GameResolution.DRAW, game1.getResolution());
		assertThat(game1.getPlayers().get(0).getPoints(), equalTo(0.5f));
		assertThat(game1.getPlayers().get(1).getPoints(), equalTo(0.5f));
		assertThat(gamegroup.getState(), not(GameState.FINISHED));
		assertThat(gamegroup.getPlayers().get(0).getPoints(), equalTo(0.5f));
		assertThat(gamegroup.getPlayers().get(1).getPoints(), equalTo(0.5f));

		// 14:00... game2 is resigned
		now = isoDateFormat.parse("2004-03-03 14:00:00");
		RequestTime.set(now);
		((GameServiceImpl) gameService).drawGame(game2.getId());
		assertEquals(GameState.FINISHED, game2.getState());
		assertEquals(GameResolution.DRAW, game2.getResolution());
		assertThat(game2.getPlayers().get(0).getPoints(), equalTo(0.5f));
		assertThat(game2.getPlayers().get(1).getPoints(), equalTo(0.5f));
		gameService.checkFinishGame(gamegroup.getId());
		assertEquals(GameState.FINISHED, gamegroup.getState());
		assertEquals(GameResolution.DRAW, gamegroup.getResolution());
		assertThat(gamegroup.getPlayers().get(0).getPoints(), equalTo(1f));
		assertThat(gamegroup.getPlayers().get(1).getPoints(), equalTo(1f));
	}

	@Test
	public void testEloRatingGameGroup() throws Exception
	{
		DateFormat isoDateFormat = Iso8601Format.newDateTimeFormat();

		// 12:00... a gamegroup is created and players join
		GameGroup gamegroup = newGameGroup(null, 2, 2, false, "disabled", null, null, null, null, null, Rules.CHESS, null, null,
				Rating.CHESS_COMPUTER_ELO);
		Date now = isoDateFormat.parse("2004-03-03 12:00:00");
		RequestTime.set(now);
		gameService.joinGame("user1", gamegroup.getId());
		gameService.joinGame("user2", gamegroup.getId());
		Set<SingleGame> childGames = gamegroup.getChildGames();
		assertEquals(GameState.RUNNING, gamegroup.getState());
		assertEquals(2, childGames.size());
		Iterator<SingleGame> i = childGames.iterator();
		SingleGame game1 = i.next();
		SingleGame game2 = i.next();
		assertEquals(GameState.RUNNING, game1.getState());
		assertEquals(GameState.RUNNING, game2.getState());

		// 13:00... game1 is won
		now = isoDateFormat.parse("2004-03-03 13:00:00");
		RequestTime.set(now);
		((GameServiceImpl) gameService).winGame(game1, game1.getActivePlayer());
		assertEquals(GameState.FINISHED, game1.getState());
		assertEquals(GameResolution.WIN, game1.getResolution());
		assertEquals("1516", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1484", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getValue());

		// 14:00... game2 is won
		now = isoDateFormat.parse("2004-03-03 14:00:00");
		RequestTime.set(now);
		((GameServiceImpl) gameService).winGame(game2, game2.getActivePlayer());
		assertEquals(GameState.FINISHED, game2.getState());
		assertEquals(GameResolution.WIN, game2.getResolution());
		assertEquals("1500", findRating(gameService.ratingsForSubject("user1"), Rating.CHESS_COMPUTER_ELO).getValue());
		assertEquals("1500", findRating(gameService.ratingsForSubject("user2"), Rating.CHESS_COMPUTER_ELO).getValue());
	}

	@Test
	public void testCanJoinGame() throws Exception
	{
		User user1 = userDao.read(User.class, "user1");
		User user2 = userDao.read(User.class, "user2");
		SubjectRating rating = new SubjectRating(user2, Rating.CHESS_COMPUTER_ELO, "1500", createdAt);
		subjectRatingDao.create(rating);
		User user3 = userDao.read(User.class, "user3");
		rating = new SubjectRating(user3, Rating.CHESS_COMPUTER_ELO, "2000", createdAt);
		subjectRatingDao.create(rating);

		Game disabled = newGameGroup(null, 3, 7, false, "disabled", null, null, null, null, null, Rules.CHESS, null, null, null);
		Game none = newGameGroup(null, 3, 7, false, "none", Rating.CHESS_COMPUTER_ELO, null, null, null, null, Rules.CHESS, null, null, null);
		Game range = newGameGroup(null, 3, 7, false, "range", Rating.CHESS_COMPUTER_ELO, 1400, 1600, null, null, Rules.CHESS, null, null, null);
		Game noneOrRange = newGameGroup(null, 3, 7, false, "none_or_range", Rating.CHESS_COMPUTER_ELO, 1400, 1600, null, null, Rules.CHESS, null,
				null, null);

		assertTrue(gameService.canJoinGame(user1.getName(), disabled.getId()));
		assertTrue(gameService.canJoinGame(user2.getName(), disabled.getId()));
		assertTrue(gameService.canJoinGame(user3.getName(), disabled.getId()));

		assertTrue(gameService.canJoinGame(user1.getName(), none.getId()));
		assertFalse(gameService.canJoinGame(user2.getName(), none.getId()));
		assertFalse(gameService.canJoinGame(user3.getName(), none.getId()));

		assertFalse(gameService.canJoinGame(user1.getName(), range.getId()));
		assertTrue(gameService.canJoinGame(user2.getName(), range.getId()));
		assertFalse(gameService.canJoinGame(user3.getName(), range.getId()));

		assertTrue(gameService.canJoinGame(user1.getName(), noneOrRange.getId()));
		assertTrue(gameService.canJoinGame(user2.getName(), noneOrRange.getId()));
		assertFalse(gameService.canJoinGame(user3.getName(), noneOrRange.getId()));
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

	private GameGroup newGameGroup(String name, int minPlayers, int maxPlayers, boolean isClosed, String requiredRatingMode, Rating requiredRating,
			Integer requiredRatingMin, Integer requiredRatingMax, Date readyAt, Date startAt, Rules childRules, String childInitialHistory,
			String childClockConstraint, Rating childRating)
	{
		Subject owner = userDao.read("owner");
		GameGroup game = gameDao.newGameGroup(owner, createdAt, childRules, childInitialHistory, Aid.NONE);
		game.setMinPlayers(minPlayers);
		game.setMaxPlayers(maxPlayers);
		game.setRequiredRatingMode(requiredRatingMode);
		game.setRequiredRating(requiredRating);
		game.setRequiredRatingMin(requiredRatingMin);
		game.setRequiredRatingMax(requiredRatingMax);
		game.setReadyAt(readyAt);
		game.setStartedAt(startAt);
		game.setChildRating(childRating);
		gameDao.create(game);
		return game;
	}

	private SubjectRating findRating(List<SubjectRating> ratings, Rating rating)
	{
		for (SubjectRating subjectRating : ratings)
			if (subjectRating.getRating() == rating)
				return subjectRating;

		return null;
	}
}
