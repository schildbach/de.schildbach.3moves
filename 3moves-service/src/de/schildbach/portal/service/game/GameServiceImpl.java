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

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schildbach.game.GameMove;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.exception.IllegalMoveException;
import de.schildbach.game.exception.ParseException;
import de.schildbach.portal.message.user.MessageDao;
import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameConditionalMoves;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameHelper;
import de.schildbach.portal.persistence.game.GameInvitation;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameResolution;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.GameVisitorAdapter;
import de.schildbach.portal.persistence.game.GameWatch;
import de.schildbach.portal.persistence.game.OrderType;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.persistence.game.SubjectRatingDao;
import de.schildbach.portal.persistence.game.SubjectRatingHistory;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.SubjectHelper;
import de.schildbach.portal.persistence.user.SubjectVisitorAdapter;
import de.schildbach.portal.persistence.user.SystemAccount;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;
import de.schildbach.portal.persistence.user.UserTitle;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.bo.CreateGameCommand;
import de.schildbach.portal.service.game.bo.CreateMiniTournamentCommand;
import de.schildbach.portal.service.game.bo.CreateTournamentCommand;
import de.schildbach.portal.service.game.bo.DeadlineOption;
import de.schildbach.portal.service.game.bo.InvitationType;
import de.schildbach.portal.service.game.bo.RequiredRating;
import de.schildbach.portal.service.game.event.GamePlayerActiveEvent;
import de.schildbach.portal.service.game.event.GamePlayerInactiveDisqualificationEvent;
import de.schildbach.portal.service.game.event.GamePlayerInactiveSystemReminderEvent;
import de.schildbach.portal.service.game.event.GameStateChangedEvent;
import de.schildbach.portal.service.game.exception.IllegalReadyAtDateException;
import de.schildbach.portal.service.game.exception.IllegalRequiredRatingException;
import de.schildbach.portal.service.game.exception.IllegalStartAtDateException;
import de.schildbach.portal.service.game.exception.InvalidTargetSubjectException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.UserTitleParticipant;
import de.schildbach.portal.service.user.event.UserMessageEvent;
import de.schildbach.util.TextWrapper;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Transactional
@Service
public class GameServiceImpl implements GameService, UserTitleParticipant, InitializingBean, ApplicationContextAware
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(GameServiceImpl.class);

	// time a game has to be finished disappear from certain lists
	private static final int OLD_DELAY_FIELD = Calendar.MONTH;
	private static final int OLD_DELAY_AMOUNT = 6;

	// time a game has to be unaccomplished before being deleted
	private static final int DELETE_DELAY_FIELD = Calendar.MONTH;
	private static final int DELETE_DELAY_AMOUNT = 1;

	// inactivity time after which the players of a game are started to be
	// notified of a threatening disqualificaton
	private static final int INACTIVE_SYSTEM_REMINDER_AFTER_FIELD = Calendar.MONTH;
	private static final int INACTIVE_SYSTEM_REMINDER_AFTER_AMOUNT = 2;

	// time after a notification where no further notification will be sent
	private static final int INACTIVE_SYSTEM_REMINDER_IMMUNITY_FIELD = Calendar.DAY_OF_WEEK;
	private static final int INACTIVE_SYSTEM_REMINDER_IMMUNITY_AMOUNT = 6;

	// inactivity time after which a game is completely disqualified
	private static final int INACTIVE_DISQUALIFY_AFTER_FIELD = Calendar.MONTH;
	private static final int INACTIVE_DISQUALIFY_AFTER_AMOUNT = 3;

	private static final int MAX_COMMENT_LENGTH = 80;
	private static final int MAX_COMMENTS_PER_GAME = 5;

	private static final long REMINDER_TIMEOUT = 1000 * 60 * 60 * 24 * 5;

	private static final int TOURNAMENT_MINIMUM_DAYS_IN_ADVANCE = 2;

	private static final int WAITING_FOR_PLAYERS_WEEKS = 3;

	private static final Aid DEFAULT_AID = Aid.NONE;

	private UserDao userDao;
	private UserService userService;
	private GameDao gameDao;
	private SubjectRatingDao subjectRatingDao;
	private MessageDao messageDao;
	private RatingActivity ratingActivity;
	private ApplicationContext ctx;

	@Required
	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setGameDao(GameDao gameDao)
	{
		this.gameDao = gameDao;
	}

	@Required
	public void setSubjectRatingDao(SubjectRatingDao subjectRatingDao)
	{
		this.subjectRatingDao = subjectRatingDao;
	}

	@Required
	public void setMessageDao(MessageDao messageDao)
	{
		this.messageDao = messageDao;
	}

	@Required
	public void setRatingActivity(RatingActivity ratingActivity)
	{
		this.ratingActivity = ratingActivity;
	}

	public void afterPropertiesSet() throws Exception
	{
		userService.addUserTitleParticipant(this);
	}

	@Required
	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.ctx = applicationContext;
	}

	public CreateGameCommand createGameDefaults()
	{
		// prepare calendar (set to tomorrow)
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		// initialize defaults
		CreateGameCommand command = new CreateGameCommand();
		command.setRules(Rules.CHESS);
		command.setAid(DEFAULT_AID);
		command.setInvitationType(InvitationType.OPEN);
		command.setOrderType(OrderType.RANDOM);
		command.getStartAt().setOption(DeadlineOption.ASAP);
		command.getStartAt().setDate(calendar.getTime());
		command.getStartAt().setHour(8);

		return command;
	}

	public int createGame(String username, CreateGameCommand command) throws IllegalStartAtDateException, IllegalRequiredRatingException
	{
		Date now = RequestTime.get();

		GameRules gameRules = GameRulesHelper.rules(command.getRules());
		String initialHistoryNotation = command.getOpening() != null ? gameRules.formatGame(command.getOpening(), Locale.ENGLISH) : null;

		// load user
		User user = userDao.read(User.class, username);

		// create game
		SingleGame game = gameDao.newSingleGame(user, now, command.getRules(), initialHistoryNotation, command.getAid());

		// simple parameters
		game.setName(command.getName());
		game.setClosed(command.getInvitationType() == InvitationType.CLOSED);
		game.setOrderType(command.getOrderType());
		game.setClockConstraint(command.getClockConstraint() != null ? command.getClockConstraint().getId() : null);

		// required rating parameter
		if (command.getRequiredRating().isDefined())
		{
			// TODO other rating types than ELO
			game.setRequiredRating(ratingActivity.assembleRating(command.getRules(), RatingClass.ELO, command.getAid()));
			game.setRequiredRatingMode("none_or_range");

			// check format and parse
			try
			{
				if (command.getRequiredRating().getMin() != null)
				{
					int min = Integer.parseInt(command.getRequiredRating().getMin());
					if (min < 0 || min > 3000)
						throw new IllegalRequiredRatingException("range");
					if (min > 0)
						game.setRequiredRatingMin(min);
				}

				if (command.getRequiredRating().getMax() != null)
				{
					int max = Integer.parseInt(command.getRequiredRating().getMax());
					if (max < 0 || max > 3000)
						throw new IllegalRequiredRatingException("range");
					game.setRequiredRatingMax(max);
				}
			}
			catch (NumberFormatException x)
			{
				throw new IllegalRequiredRatingException("format");
			}

			// check for minimum span
			if (game.getRequiredRatingMin() != null && game.getRequiredRatingMax() != null
					&& game.getRequiredRatingMax() - game.getRequiredRatingMin() < 50)
				throw new IllegalRequiredRatingException("span");
		}

		// start at parameter
		if (command.getStartAt().getOption() == DeadlineOption.TIME)
		{
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(command.getStartAt().getDate());
			calendar.set(Calendar.HOUR_OF_DAY, command.getStartAt().getHour());
			game.setStartedAt(calendar.getTime());

			if (game.getStartedAt().before(now))
				throw new IllegalStartAtDateException("too_early");

			calendar.setTime(now);
			calendar.add(Calendar.WEEK_OF_YEAR, WAITING_FOR_PLAYERS_WEEKS);
			if (game.getStartedAt().after(calendar.getTime()))
				throw new IllegalStartAtDateException("too_late");
		}

		// persist game
		gameDao.create(game);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" created " + game.getRules() + " game " + game.getId() + " (closed=" + game.isClosed()
					+ ",orderType=" + game.getOrderType() + ",clockConstraint=" + game.getClockConstraint() + ",aid=" + game.getAid() + ")");

		// join the game if possible
		if (canJoinGame(username, game.getId()))
			joinGame(username, game.getId());

		// invite user
		if (command.getInviteUser() != null)
		{
			Subject inviteUser = userDao.findUserCaseInsensitive(command.getInviteUser(), false);
			internalInviteSubjectToGame(username, game, inviteUser);
		}

		return game.getId();
	}

	public CreateMiniTournamentCommand createMiniTournamentDefaults()
	{
		// prepare calendar (set to tomorrow)
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		// initialize defaults
		CreateMiniTournamentCommand command = new CreateMiniTournamentCommand();
		command.setRules(Rules.CHESS);
		command.setAid(DEFAULT_AID);
		command.setInvitationType(InvitationType.OPEN);
		command.setNumberOfPlayers(3);
		command.getStartAt().setOption(DeadlineOption.ASAP);
		command.getStartAt().setDate(calendar.getTime());
		command.getStartAt().setHour(8);
		command.setClockConstraint(new ClockConstraint("5d+1d*:16d"));
		return command;
	}

	public boolean canCreateMiniTournament(String username)
	{
		// never by guests
		if (username == null)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// always by admin
		if (user.isUserInRole(Role.ADMIN))
			return true;

		// all owned tournaments
		List<GameGroup> ownedTournaments = gameDao.findGamegroups(null, null, user, null, null, null, null, null, null, 0, null);

		// remove finished and not accomplished
		for (Iterator<GameGroup> i = ownedTournaments.iterator(); i.hasNext();)
		{
			GameGroup game = i.next();
			if (game.getState() != GameState.FORMING && game.getState() != GameState.READY && game.getState() != GameState.RUNNING)
				i.remove();
		}

		// number exceeded?
		if (ownedTournaments.size() >= 1)
			return false;

		return true;
	}

	public int createMiniTournament(String username, CreateMiniTournamentCommand command) throws IllegalStartAtDateException,
			IllegalRequiredRatingException
	{
		Date now = RequestTime.get();

		GameRules gameRules = GameRulesHelper.rules(command.getRules());
		String childInitialHistory = command.getOpening() != null ? gameRules.formatGame(command.getOpening(), Locale.ENGLISH) : null;

		// allowed?
		if (!canCreateMiniTournament(username))
			throw new NotAuthorizedException();

		if (command.getNumberOfPlayers() < 3 || command.getNumberOfPlayers() > 5)
			throw new IllegalArgumentException("illegal number of players: " + command.getNumberOfPlayers());

		// load user
		User user = userDao.read(User.class, username);

		// create tournament
		GameGroup game = gameDao.newGameGroup(user, now, command.getRules(), childInitialHistory, command.getAid());

		// simple parameters
		game.setMinPlayers(command.getNumberOfPlayers());
		game.setMaxPlayers(command.getNumberOfPlayers());
		game.setClosed(command.getInvitationType() == InvitationType.CLOSED);
		game.setChildClockConstraint(command.getClockConstraint() != null ? command.getClockConstraint().getId() : null);
		game.setName(command.getName());

		// start at parameter
		if (command.getStartAt().getOption() == DeadlineOption.TIME)
		{
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(command.getStartAt().getDate());
			calendar.set(Calendar.HOUR_OF_DAY, command.getStartAt().getHour());
			game.setStartedAt(calendar.getTime());

			if (game.getStartedAt().before(now))
				throw new IllegalStartAtDateException("too_early");

			calendar.setTime(now);
			calendar.add(Calendar.WEEK_OF_YEAR, WAITING_FOR_PLAYERS_WEEKS);
			if (game.getStartedAt().after(calendar.getTime()))
				throw new IllegalStartAtDateException("too_late");

			if (game.getReadyAt() != null && game.getStartedAt().compareTo(game.getReadyAt()) < 0)
				throw new IllegalStartAtDateException("before_ready");
		}

		// required rating parameter
		if (command.getRequiredRating().isDefined())
		{
			// TODO other rating types than ELO
			game.setRequiredRating(ratingActivity.assembleRating(command.getRules(), RatingClass.ELO, command.getAid()));
			game.setRequiredRatingMode("none_or_range");

			// check format and parse
			try
			{
				if (command.getRequiredRating().getMin() != null)
				{
					int min = Integer.parseInt(command.getRequiredRating().getMin());
					if (min < 0 || min > 3000)
						throw new IllegalRequiredRatingException("range");
					if (min > 0)
						game.setRequiredRatingMin(min);
				}

				if (command.getRequiredRating().getMax() != null)
				{
					int max = Integer.parseInt(command.getRequiredRating().getMax());
					if (max < 0 || max > 3000)
						throw new IllegalRequiredRatingException("range");
					game.setRequiredRatingMax(max);
				}
			}
			catch (NumberFormatException x)
			{
				throw new IllegalRequiredRatingException("format");
			}

			// check for minimum span
			if (game.getRequiredRatingMin() != null && game.getRequiredRatingMax() != null
					&& game.getRequiredRatingMax() - game.getRequiredRatingMin() < 50)
				throw new IllegalRequiredRatingException("span");
		}

		// persist game
		gameDao.create(game);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" created " + game.getChildRules() + " mini tournament " + game.getId() + " (closed="
					+ game.isClosed() + ",clockConstraint=" + game.getChildClockConstraint() + ",aid=" + game.getChildAid() + ")");

		return game.getId();
	}

	public CreateTournamentCommand createTournamentDefaults()
	{
		// prepare calendar, next big tournament
		Calendar calendar = new GregorianCalendar();
		calendar.setFirstDayOfWeek(Calendar.SATURDAY);

		// minimum 2 days in advance
		calendar.add(Calendar.DAY_OF_MONTH, TOURNAMENT_MINIMUM_DAYS_IN_ADVANCE);

		// next saturday
		calendar.add(Calendar.WEEK_OF_YEAR, 1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Date startTime = calendar.getTime();
		boolean big = true;

		// prepare calendar, next small tournament
		calendar = new GregorianCalendar();
		calendar.setFirstDayOfWeek(Calendar.WEDNESDAY);

		// minimum 2 days in advance
		calendar.add(Calendar.DAY_OF_MONTH, TOURNAMENT_MINIMUM_DAYS_IN_ADVANCE);

		// next wednesday
		calendar.add(Calendar.WEEK_OF_YEAR, 1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (calendar.getTime().before(startTime))
		{
			startTime = calendar.getTime();
			big = false;
		}

		// initialize defaults
		CreateTournamentCommand command = new CreateTournamentCommand();
		command.setRules(Rules.CHESS);
		command.setAid(DEFAULT_AID);
		command.getNumPlayers().setMin(3);
		command.getNumPlayers().setMax(big ? 7 : 5);
		command.setInvitationType(InvitationType.OPEN);
		command.getReadyAt().setOption(DeadlineOption.TIME);
		command.getReadyAt().setDate(startTime);
		command.getReadyAt().setHour(0);
		command.getStartAt().setOption(DeadlineOption.TIME);
		command.getStartAt().setDate(startTime);
		command.getStartAt().setHour(8);
		command.setClockConstraint(new ClockConstraint("5d+1d*:16d"));
		command.setRated(true);
		command.getRequiredRatings().add(new RequiredRating());
		return command;
	}

	public List<RequiredRating> createTournamentRequiredRatingDefaults(String username, Rules rules, Aid aid)
	{
		List<RequiredRating> requiredRatings = new LinkedList<RequiredRating>();

		// load user
		User user = userDao.read(User.class, username);

		// find latest start at
		List<GameGroup> latestGamegroups = gameDao.findGamegroups(null, null, user, rules, aid, false, null, null, null, 1, "!"
				+ Game.PROPERTY_STARTED_AT);
		if (!latestGamegroups.isEmpty())
		{
			Date latestStartAt = latestGamegroups.get(0).getStartedAt();

			// find all gamegroups with that date
			List<GameGroup> groups = gameDao.findGamegroups(null, null, user, rules, aid, false, latestStartAt, null, null, 0, Game.PROPERTY_ID);

			// extract required ratings from gamegroups
			for (Game group : groups)
			{
				String min = group.getRequiredRatingMin() != null ? group.getRequiredRatingMin().toString() : null;
				String max = group.getRequiredRatingMax() != null ? group.getRequiredRatingMax().toString() : null;
				requiredRatings.add(new RequiredRating(min, max));
			}
		}
		else
		{
			requiredRatings.add(new RequiredRating());
		}

		return requiredRatings;
	}

	public int[] createTournament(String username, CreateTournamentCommand command) throws IllegalStartAtDateException, IllegalReadyAtDateException,
			IllegalRequiredRatingException
	{
		Date now = RequestTime.get();

		GameRules gameRules = GameRulesHelper.rules(command.getRules());
		String childInitialHistory = command.getOpening() != null ? gameRules.formatGame(command.getOpening(), Locale.ENGLISH) : null;

		// allowed?
		if (command.getNumPlayers().getMin() > command.getNumPlayers().getMax())
			throw new IllegalArgumentException("minPlayers > maxPlayers");

		if (command.getNumPlayers().getMin() < 2 || command.getNumPlayers().getMin() > 10)
			throw new IllegalArgumentException("minPlayers must be between 2 and 10");

		if (command.getNumPlayers().getMax() < 2 || command.getNumPlayers().getMax() > 10)
			throw new IllegalArgumentException("maxPlayers must be between 2 and 10");

		// load user
		User user = userDao.read(User.class, username);

		List<RequiredRating> requiredRatings = command.getRequiredRatings();
		int[] gameIds = new int[requiredRatings.size()];
		for (int i = 0; i < requiredRatings.size(); i++)
		{
			RequiredRating requiredRating = requiredRatings.get(i);

			// create tournament
			GameGroup game = gameDao.newGameGroup(user, now, command.getRules(), childInitialHistory, command.getAid());

			// simple parameters
			game.setMinPlayers(command.getNumPlayers().getMin());
			game.setMaxPlayers(command.getNumPlayers().getMax());
			game.setClosed(command.getInvitationType() == InvitationType.CLOSED);
			game.setChildClockConstraint(command.getClockConstraint() != null ? command.getClockConstraint().getId() : null);
			game.setName(command.getName());

			// ready at parameter
			if (command.getReadyAt().getOption() == DeadlineOption.TIME)
			{
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(command.getReadyAt().getDate());
				calendar.set(Calendar.HOUR_OF_DAY, command.getReadyAt().getHour());
				game.setReadyAt(calendar.getTime());

				if (game.getReadyAt().before(now))
					throw new IllegalReadyAtDateException("too_early");

				calendar.setTime(now);
				calendar.add(Calendar.WEEK_OF_YEAR, WAITING_FOR_PLAYERS_WEEKS);
				if (game.getReadyAt().after(calendar.getTime()))
					throw new IllegalReadyAtDateException("too_late");
			}

			// start at parameter
			if (command.getStartAt().getOption() == DeadlineOption.TIME)
			{
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(command.getStartAt().getDate());
				calendar.set(Calendar.HOUR_OF_DAY, command.getStartAt().getHour());
				game.setStartedAt(calendar.getTime());

				if (game.getStartedAt().before(now))
					throw new IllegalStartAtDateException("too_early");

				calendar.setTime(now);
				calendar.add(Calendar.WEEK_OF_YEAR, WAITING_FOR_PLAYERS_WEEKS);
				if (game.getStartedAt().after(calendar.getTime()))
					throw new IllegalStartAtDateException("too_late");

				if (game.getReadyAt() != null && game.getStartedAt().compareTo(game.getReadyAt()) < 0)
					throw new IllegalStartAtDateException("before_ready");
			}

			// rating
			if (command.isRated())
			{
				// TODO other rating types than ELO
				game.setChildRating(ratingActivity.assembleRating(command.getRules(), RatingClass.ELO, command.getAid()));
			}

			// required rating parameter
			if (!requiredRating.isDefined())
			{
				game.setRequiredRatingMode("disabled");
			}
			else
			{
				// TODO other rating types than ELO
				game.setRequiredRating(ratingActivity.assembleRating(command.getRules(), RatingClass.ELO, command.getAid()));
				game.setRequiredRatingMode("none_or_range");

				// check format and parse
				try
				{
					if (requiredRating.getMin() != null)
					{
						int min = Integer.parseInt(requiredRating.getMin());
						if (min < 0 || min > 3000)
							throw new IllegalRequiredRatingException("range");
						if (min > 0)
							game.setRequiredRatingMin(min);
					}

					if (requiredRating.getMax() != null)
					{
						int max = Integer.parseInt(requiredRating.getMax());
						if (max < 0 || max > 3000)
							throw new IllegalRequiredRatingException("range");
						game.setRequiredRatingMax(max);
					}
				}
				catch (NumberFormatException x)
				{
					throw new IllegalRequiredRatingException("format");
				}

				// check for minimum span
				if (game.getRequiredRatingMin() != null && game.getRequiredRatingMax() != null
						&& game.getRequiredRatingMax() - game.getRequiredRatingMin() < 50)
					throw new IllegalRequiredRatingException("span");
			}

			// persist game
			gameDao.create(game);

			// log
			if (LOG.isInfoEnabled())
				LOG.info("user \"" + user.getName() + "\" created " + game.getChildRules() + " tournament " + game.getId() + " (closed="
						+ game.isClosed() + ",clockConstraint=" + game.getChildClockConstraint() + ",aid=" + game.getChildAid() + ",rating="
						+ game.getChildRating() + ")");

			gameIds[i] = game.getId();
		}

		return gameIds;
	}

	public boolean canCreateSecondLeg(String username, int gameId)
	{
		// never by guests
		if (username == null)
			return false;

		// load game
		SingleGame game = gameDao.read(SingleGame.class, gameId);

		// is game finished?
		if (game.getState() != GameState.FINISHED)
			return false;

		// did it end with a disqualification?
		if (game.getResolution() == GameResolution.DISQUALIFY)
			return false;

		// is it a game that can be easily "second legged" (2 player game)?
		if (GameRulesHelper.minPlayers(game) != 2 || GameRulesHelper.maxPlayers(game) != 2)
			return false;

		// is it part of a gamegroup?
		if (game.getParentGame() != null)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// was user player of game?
		if (!isSubjectPlayerOfGame(user, game))
			return false;

		return true;
	}

	private boolean isSubjectPlayerOfGame(Subject subject, Game game)
	{
		return findPlayer(subject, game) != null;
	}

	private GamePlayer findPlayer(Subject subject, Game game)
	{
		for (GamePlayer player : game.getPlayers())
			if (player.getSubject().equals(subject))
				return player;

		return null;
	}

	public int createSecondLeg(String username, int gameId)
	{
		Date now = RequestTime.get();

		try
		{
			if (!canCreateSecondLeg(username, gameId))
				throw new NotAuthorizedException();

			SingleGame game = gameDao.read(SingleGame.class, gameId);

			final User user = userDao.read(User.class, username);

			GamePlayer opponentPlayer = findOpponent(findPlayer(user, game));
			Subject opponent = opponentPlayer.getSubject();

			OrderType orderType = opponentPlayer.getPosition() == 0 ? OrderType.FORWARD : OrderType.REVERSE;

			// create game
			final Game secondLeg = gameDao.newSingleGame(user, now, game.getRules(), game.getInitialHistoryNotation(), game.getAid(), true,
					orderType, null, game.getClockConstraint(), null, null, null);
			gameDao.create(secondLeg);

			// join the game
			joinGame(username, secondLeg.getId());

			// invite subject
			GameInvitation invitation = new GameInvitation(secondLeg, opponent);
			game.addInvitation(invitation);

			opponent.accept(new SubjectVisitorAdapter()
			{
				@Override
				public void visit(User opponent)
				{
					notifyInvitationByName(user.getName(), opponent, secondLeg);
				}
			});

			return secondLeg.getId();
		}
		catch (Exception x)
		{
			throw new ApplicationException(x);
		}
	}

	public Game game(int gameId)
	{
		return gameDao.findGame(Game.class, gameId);
	}

	public List<? extends Game> allGames(int maxResults, String orderBy)
	{
		return gameDao.findGames(null, null, null, null, null, null, null, null, null, maxResults, orderBy, false);
	}

	public List<GameGroup> runningGameGroups()
	{
		return gameDao.findGamegroups(GameState.RUNNING, null, null, null, null, false, null, null, null, 0, Game.PROPERTY_ID);
	}

	public List<GameGroup> selectedGameGroups(int maxResults)
	{
		List<GameGroup> groups = new LinkedList<GameGroup>();

		for (Rules rules : Rules.values())
		{
			groups.addAll(gameDao.findGamegroups(GameState.RUNNING, null, null, rules, null, false, null, null, null, 1, Game.PROPERTY_ID));

			// maxResults reached?
			if (groups.size() >= maxResults)
				break;
		}

		return groups;
	}

	public List<SingleGame> selectedSingleGames(int maxResults)
	{
		List<SingleGame> games = new LinkedList<SingleGame>();

		for (Rules rules : Rules.values())
		{
			games.addAll(gameDao.findSingleGames(new Rules[] { rules }, GameState.RUNNING, null, null, null, null, null, null, null, null, null,
					null, 1, "!" + Game.PROPERTY_TURN, true));

			// maxResults reached?
			if (games.size() >= maxResults)
				break;
		}

		return games;
	}

	public List<Game> joinedGames(String subjectName, Class<? extends Game> gameClass, String rules, String stateFilter)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		// strip old games
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(RequestTime.get());
		calendar.add(OLD_DELAY_FIELD, -OLD_DELAY_AMOUNT);
		Date finishedAfter = calendar.getTime();

		List<Game> games;
		if (gameClass == SingleGame.class)
		{
			games = new LinkedList<Game>(gameDao.findSingleGames(Rules.fromMajorId(rules), null, null, subject, null, null, null, null, null, null,
					null, finishedAfter, 0, Game.PROPERTY_ID, true));
		}
		else if (gameClass == GameGroup.class)
		{
			games = new LinkedList<Game>(gameDao
					.findGamegroups(null, subject, null, null, null, null, null, null, finishedAfter, 0, Game.PROPERTY_ID));
		}
		else
		{
			throw new IllegalArgumentException("" + gameClass);
		}

		for (Iterator<Game> i = games.iterator(); i.hasNext();)
		{
			Game game = i.next();
			GameState state = game.getState();
			if (("waiting".equals(stateFilter) && state != GameState.FORMING && state != GameState.READY)
					|| ("running".equals(stateFilter) && state != GameState.RUNNING)
					|| ("finished".equals(stateFilter) && state != GameState.FINISHED)
					|| ("not_accomplished".equals(stateFilter) && state != GameState.UNACCOMPLISHED))
				i.remove();
		}

		return games;
	}

	public List<GameGroup> ownedGamegroups(String subjectName, Rules childRules)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		// strip old games
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(RequestTime.get());
		calendar.add(OLD_DELAY_FIELD, -OLD_DELAY_AMOUNT);
		Date finishedAfter = calendar.getTime();

		GameState[] states = new GameState[] { GameState.FORMING, GameState.READY, GameState.RUNNING, GameState.FINISHED, GameState.UNACCOMPLISHED };
		List<GameGroup> results = gameDao.findGamegroups(states, subject, childRules, finishedAfter, "!" + Game.PROPERTY_STARTED_AT);

		return results;
	}

	public List<SingleGame> activeSingleGames(String subjectName, int maxResults)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		// find active games
		return gameDao.findSingleGames(null, GameState.RUNNING, subject, null, null, null, null, null, null, null, null, null, maxResults,
				SingleGame.PROPERTY_LAST_ACTIVE_AT, false);
	}

	public List<Game> openGameInvitations(String subjectName, Class<? extends Game> gameClass, String rules, int maxResults)
	{
		// TODO if subject is given, maxResults may not be accurate!

		// find all open invitations
		List<Game> games;
		if (gameClass == SingleGame.class)
		{
			games = new LinkedList<Game>(gameDao.findSingleGames(Rules.fromMajorId(rules), GameState.FORMING, null, null, null, null, Boolean.FALSE,
					null, null, null, null, null, maxResults, Game.PROPERTY_ID, false));
		}
		else if (gameClass == GameGroup.class)
		{
			games = new LinkedList<Game>(gameDao.findGamegroups(GameState.FORMING, null, null, null, null, Boolean.FALSE, null, null, null,
					maxResults, Game.PROPERTY_ID));
		}
		else
		{
			throw new IllegalArgumentException("" + gameClass);
		}

		// if subject is given filter out all games which subject joined
		if (subjectName != null)
		{
			Subject subject = userDao.read(subjectName);
			for (Iterator<Game> i = games.iterator(); i.hasNext();)
			{
				Game game = i.next();
				if (isSubjectPlayerOfGame(subject, game))
					i.remove();
			}
		}

		// filter out all games where max players reached
		for (Iterator<Game> i = games.iterator(); i.hasNext();)
		{
			Game game = i.next();
			if (game.getPlayers().size() >= GameRulesHelper.maxPlayers(game))
				i.remove();
		}

		return games;
	}

	public List<Game> personalGameInvitations(String subjectName)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		List<Game> games = gameDao.findGames(GameState.FORMING, null, subject, null, null, null, null, null, null, 0, Game.PROPERTY_ID, true);
		for (Iterator<Game> i = games.iterator(); i.hasNext();)
		{
			Game game = i.next();
			if (isSubjectPlayerOfGame(subject, game))
				i.remove();
		}
		return games;
	}

	public List<SingleGame> search(Rules rules, Aid aid, String playerName, Set<GameState> states, Boolean hasParent, Integer windowAfterStart)
	{
		User player = playerName != null ? userDao.findUserCaseInsensitive(playerName, false) : null;

		// calculate window
		Date startedAfter = null;
		if (windowAfterStart != null)
		{
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(RequestTime.get());
			calendar.add(Calendar.MONTH, -windowAfterStart);
			startedAfter = calendar.getTime();
		}

		List<SingleGame> games = gameDao.findSingleGames(rules, aid, player, states, hasParent, startedAfter, 100, Game.PROPERTY_ID, true);

		LOG.info("search for " + rules + " returned " + games.size() + " results");

		return games;
	}

	private boolean canGameBeInvitedTo(int gameId)
	{
		// load game
		Game game = gameDao.read(gameId);

		// is game still forming?
		if (game.getState() != GameState.FORMING)
			return false;

		return true;
	}

	public boolean canInviteToGame(String username, int gameId)
	{
		if (username == null)
			return false;

		if (!canGameBeInvitedTo(gameId))
			return false;

		// load objects
		User user = userDao.read(User.class, username);
		Game game = gameDao.read(gameId);

		// is owner?
		if (!user.equals(game.getOwner()))
			return false;

		return true;
	}

	private boolean canSubjectBeInvited(String username, Subject targetSubject)
	{
		// is user not inviting himself?
		User user = userDao.read(User.class, username);
		if (targetSubject.equals(user))
			return false;

		return true;
	}

	private boolean canInviteSubjectToGame(String username, int gameId, Subject targetSubject)
	{
		if (!canInviteToGame(username, gameId))
			return false;

		if (!canSubjectBeInvited(username, targetSubject))
			return false;

		// load objects
		Game game = gameDao.read(gameId);

		// is subject not invited yet?
		if (isSubjectInvitedToGame(targetSubject, game))
			return false;

		return true;
	}

	private boolean isSubjectInvitedToGame(Subject subject, Game game)
	{
		return findGameInvitation(subject, game) != null;
	}

	private GameInvitation findGameInvitation(Subject subject, Game game)
	{
		for (GameInvitation invitation : game.getInvitations())
			if (invitation.getSubject().equals(subject))
				return invitation;

		return null;
	}

	public void inviteSubjectToGame(final String username, int gameId, String targetSubjectname) throws InvalidTargetSubjectException
	{
		Subject targetSubject = userDao.findUserCaseInsensitive(targetSubjectname, false);
		if (targetSubject == null)
			throw new InvalidTargetSubjectException();

		if (!canInviteSubjectToGame(username, gameId, targetSubject))
			throw new NotAuthorizedException();

		// load game
		Game game = gameDao.read(gameId);

		// invite
		internalInviteSubjectToGame(username, game, targetSubject);
	}

	private void internalInviteSubjectToGame(final String username, final Game game, Subject targetSubject)
	{
		// invite subject
		GameInvitation invitation = new GameInvitation(game, targetSubject);
		game.addInvitation(invitation);

		// notify user
		targetSubject.accept(new SubjectVisitorAdapter()
		{
			@Override
			public void visit(User targetUser)
			{
				notifyInvitationByName(username, targetUser, game);
			}
		});
	}

	public String[] invitationText(final Locale locale, int gameId)
	{
		// load objects
		Game game = gameDao.read(gameId);

		// locate resources
		final String validateUrl = ctx.getMessage("url.validate_game_invitation_key", new Object[] { "xxx" }, locale);
		final String registerUrl = ctx.getMessage("url.register_user", null, locale);

		// provide mail subject and text
		final String[] text = new String[3];
		text[1] = ctx.getMessage("game_invitation_by_email.text", null, locale);
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame singleGame)
			{
				String prefix = "singlegame_invitation_by_email.";
				String rulesMessage = ctx.getMessage(singleGame.getRules().name(), null, locale);

				text[0] = ctx.getMessage(prefix + "subject", new Object[] { rulesMessage }, locale);
				text[2] = ctx.getMessage(prefix + "statictext", new Object[] { rulesMessage, registerUrl, validateUrl }, locale);
			}

			public void visit(GameGroup gameGroup)
			{
				String prefix = "gamegroup_invitation_by_email.";
				String rulesMessage = ctx.getMessage(gameGroup.getChildRules().name(), null, locale);

				text[0] = ctx.getMessage(prefix + "subject", new Object[] { rulesMessage }, locale);
				text[2] = ctx.getMessage(prefix + "statictext", new Object[] { rulesMessage, registerUrl, validateUrl }, locale);
			}
		});

		return text;
	}

	public void inviteEMailToGame(String username, int gameId, String fromName, String fromAddr, String toAddr, String subject, String personalText,
			String key)
	{
		if (!canInviteToGame(username, gameId))
			throw new NotAuthorizedException();

		// load objects
		User user = userDao.read(User.class, username);
		Game game = gameDao.read(gameId);

		// locate resources
		final Locale locale = user.getLocale();
		final String validateUrl = ctx.getMessage("url.validate_game_invitation_key", new Object[] { key }, locale);
		final String registerUrl = ctx.getMessage("url.register_user", null, locale);

		// compose text
		final StringBuilder text = new StringBuilder(personalText + "\n" + "=====================================================================\n"
				+ "\n");
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				String prefix = "singlegame_invitation_by_email.";
				String rulesMessage = ctx.getMessage(game.getRules().name(), null, locale);

				text.append(TextWrapper.wrap(ctx.getMessage(prefix + "statictext", new Object[] { rulesMessage, registerUrl, validateUrl }, locale),
						70));
			}

			public void visit(GameGroup group)
			{
				String prefix = "gamegroup_invitation_by_email.";
				String rulesMessage = ctx.getMessage(group.getChildRules().name(), null, locale);

				text.append(TextWrapper.wrap(ctx.getMessage(prefix + "statictext", new Object[] { rulesMessage, registerUrl, validateUrl }, locale),
						70));
			}
		});

		// send email
		if (LOG.isInfoEnabled())
			LOG.info("sending email to " + toAddr + ": " + subject);
		messageDao.sendEmail(fromName, fromAddr, toAddr, subject, text.toString());
	}

	public Game inviteSubjectByKey(String subjectName, int gameId)
	{
		// can game accept invitations?
		if (!canGameBeInvitedTo(gameId))
			throw new NotAuthorizedException();

		// load objects
		Game game = gameDao.read(gameId);
		Subject subject = userDao.read(subjectName);

		// invite subject
		GameInvitation invitation = new GameInvitation(game, subject);
		game.addInvitation(invitation);

		return game;
	}

	public boolean isInvitedToGame(String username, int gameId)
	{
		// guests never
		if (username == null)
			return false;

		// load objects
		Game game = gameDao.read(gameId);
		User user = userDao.read(User.class, username);

		// is invited to game?
		return isSubjectInvitedToGame(user, game);
	}

	public boolean canRemoveInvitationFromGame(String username, int gameId, String targetSubjectname)
	{
		// guests never
		if (username == null)
			return false;

		// load objects
		Game game = gameDao.read(gameId);
		Subject subject = userDao.read(targetSubjectname);

		// is subject invited to game?
		if (!isSubjectInvitedToGame(subject, game))
			return false;

		// is subject removing himself?
		if (username.equals(targetSubjectname))
			return true;

		// same rules as for inviting apply
		if (!canInviteToGame(username, gameId))
			return false;

		return true;
	}

	public void removeInvitationFromGame(String username, int gameId, String targetSubjectname)
	{
		if (!canRemoveInvitationFromGame(username, gameId, targetSubjectname))
			throw new NotAuthorizedException();

		// load objects
		Subject targetSubject = userDao.read(targetSubjectname);
		Game game = gameDao.read(gameId);

		// find invitation
		GameInvitation invitation = findGameInvitation(targetSubject, game);
		if (invitation == null)
			throw new ApplicationException("subject not invited to game");

		// remove invitation
		game.removeInvitation(invitation);
	}

	public boolean canOpenGame(String username, int gameId)
	{
		if (!canInviteToGame(username, gameId))
			return false;

		// load game
		Game game = gameDao.read(gameId);

		if (!game.isClosed())
			return false;

		return true;
	}

	public void openGame(String username, int gameId)
	{
		if (!canOpenGame(username, gameId))
			throw new NotAuthorizedException();

		// load game
		Game game = gameDao.read(gameId);

		// open game
		game.setClosed(false);
	}

	public boolean canGameBeJoined(int gameId)
	{
		// load game
		Game game = gameDao.read(gameId);

		// has match already started?
		if (game.getState() != GameState.FORMING)
			return false;

		// maximum players reached?
		if (game.getPlayers().size() >= GameRulesHelper.maxPlayers(game))
			return false;

		return true;

	}

	public boolean canJoinGame(String username, int gameId)
	{
		// never joinable by guests
		if (username == null)
			return false;

		// can game be joined?
		if (!canGameBeJoined(gameId))
			return false;

		// load objects
		final User user = userDao.read(User.class, username);
		Game game = gameDao.read(gameId);
		GameHelper gameHelper = new GameHelper(game);

		// is user already joined?
		if (isSubjectPlayerOfGame(user, game))
			return false;

		// is user banned from rated games/tournaments?
		if ((gameHelper.isSingleGame() && gameHelper.getSingleGame().getRating() != null)
				|| (gameHelper.isGameGroup() && gameHelper.getGameGroup().getChildRating() != null))
		{
			if (user.isUserInRole(Role.UNRATED))
				return false;
		}

		// required rating
		String requiredRatingMode = game.getRequiredRatingMode();

		if (!requiredRatingMode.equals("disabled"))
		{
			SubjectRating rating = subjectRatingDao.findRating(user, game.getRequiredRating());
			// FIXME this is directly dependant on Elo atm
			RatingClass ratingClass = RatingClass.ELO;

			if (requiredRatingMode.equals("none"))
			{
				if (rating != null)
					return false;
			}
			else if (requiredRatingMode.equals("range"))
			{
				if (rating == null)
					return false;

				if (game.getRequiredRatingMin() != null
						&& ratingActivity.compare(ratingClass, rating.getValue(), game.getRequiredRatingMin().toString()) < 0)
					return false;
				if (game.getRequiredRatingMax() != null
						&& ratingActivity.compare(ratingClass, rating.getValue(), game.getRequiredRatingMax().toString()) > 0)
					return false;
			}
			else if (requiredRatingMode.equals("none_or_range"))
			{
				if (rating == null)
				{
					String initialRating = ratingActivity.initialRating(ratingClass);
					if (game.getRequiredRatingMin() != null
							&& ratingActivity.compare(ratingClass, initialRating, game.getRequiredRatingMin().toString()) < 0)
						return false;
					if (game.getRequiredRatingMax() != null
							&& ratingActivity.compare(ratingClass, initialRating, game.getRequiredRatingMax().toString()) > 0)
						return false;
				}
				else
				{
					if (game.getRequiredRatingMin() != null
							&& ratingActivity.compare(ratingClass, rating.getValue(), game.getRequiredRatingMin().toString()) < 0)
						return false;
					if (game.getRequiredRatingMax() != null
							&& ratingActivity.compare(ratingClass, rating.getValue(), game.getRequiredRatingMax().toString()) > 0)
						return false;
				}
			}
		}

		// is invited?
		if (isSubjectInvitedToGame(user, game))
			return true;

		// is user banned by owner?
		if (game.getOwner() != null)
		{
			if (userService.isBannedBy(user.getName(), game.getOwner().getName()))
				return false;
		}

		// is match open?
		if (!game.isClosed())
			return true;

		// is owner?
		if (user.equals(game.getOwner()))
			return true;

		return false;
	}

	public void joinGame(String username, int gameId)
	{
		Date now = RequestTime.get();

		if (!canJoinGame(username, gameId))
			throw new NotAuthorizedException();

		// load objects
		User user = userDao.read(User.class, username);
		Game game = gameDao.read(gameId);

		// execute join
		internalJoinGame(game, user, now);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" joined game " + game.getId());

		// move ready date if late
		if (game.getReadyAt() != null && now.compareTo(game.getReadyAt()) > 0)
			game.setReadyAt(now);

		// take whatever action follow
		internalCheckGameState(now, game, true);
	}

	private GamePlayer internalJoinGame(Game game, Subject subject, Date at)
	{
		GamePlayer player = new GamePlayer(game, subject, at);

		if (game.getOrderType() == OrderType.REVERSE)
			game.addPlayer(0, player);
		else
			game.addPlayer(player);

		return player;
	}

	public boolean canUnjoinGame(String username, int gameId)
	{
		// guests never
		if (username == null)
			return false;

		// load game
		Game game = gameDao.read(gameId);

		// has match already started?
		if (game.getState() != GameState.FORMING)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// is joined?
		if (!isSubjectPlayerOfGame(user, game))
			return false;

		return true;
	}

	public void unjoinGame(String username, int gameId)
	{
		if (!canUnjoinGame(username, gameId))
			throw new NotAuthorizedException();

		// load objects
		User user = userDao.read(User.class, username);
		Game game = gameDao.read(gameId);

		// find player
		GamePlayer player = findPlayer(user, game);

		// remove player
		game.removePlayer(player);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" unjoined game " + game.getId());
	}

	public boolean canKickPlayerFromGame(String username, int gameId)
	{
		// guests never
		if (username == null)
			return false;

		Game game = gameDao.read(gameId);

		if (game.getState() != GameState.FORMING)
			return false;

		User user = userDao.read(User.class, username);
		if (user.isUserInRole(Role.ADMIN))
			return true;

		if (user.equals(game.getOwner()))
			return true;

		return false;
	}

	public void kickPlayerFromGame(String username, int gameId, String playername)
	{
		if (!canKickPlayerFromGame(username, gameId))
			throw new NotAuthorizedException();

		Game game = gameDao.read(gameId);

		Subject playerSubject = userDao.read(playername);

		GamePlayer player = findPlayer(playerSubject, game);

		// remove player
		game.removePlayer(player);
	}

	public GamePlayer player(String subjectName, int gameId)
	{
		// guests cannot be players
		if (subjectName == null)
			return null;

		// load objects
		Subject subject = userDao.read(subjectName);
		Game game = gameDao.read(gameId);

		// find player
		return findPlayer(subject, game);
	}

	public boolean canAccessPrivatePlayerNotes(String userName, int gameId)
	{
		// guests never
		if (userName == null)
			return false;

		// load game
		Game game = gameDao.read(gameId);

		// is game in correct state?
		if (game.getState() == GameState.FORMING || game.getState() == GameState.UNACCOMPLISHED)
			return false;

		// load user
		Subject subject = userDao.read(userName);

		// is user player of game?
		GamePlayer player = findPlayer(subject, game);
		if (player == null)
			return false;

		return true;
	}

	public void setPrivatePlayerNotes(String userName, int gameId, String notes)
	{
		if (!canAccessPrivatePlayerNotes(userName, gameId))
			throw new NotAuthorizedException();

		// load objects
		Subject user = userDao.read(userName);
		Game game = gameDao.read(gameId);

		// find player
		GamePlayer player = findPlayer(user, game);

		// set comment
		player.setComment(notes);

		// log action
		LOG.info(userName + " has set his private notes of game " + gameId);
	}

	public boolean canGameBeReadied(int gameId)
	{
		Date now = RequestTime.get();
		return internalCanGameBeReadied(gameId, now);
	}

	private boolean internalCanGameBeReadied(int gameId, Date at)
	{
		// load game
		Game game = gameDao.read(gameId);

		// check state
		if (game.getState() != GameState.FORMING)
			return false;

		// min/max players?
		int numPlayers = game.getPlayers().size();
		if (numPlayers < GameRulesHelper.minPlayers(game) || numPlayers > GameRulesHelper.maxPlayers(game))
			return false;

		// ready at?
		if (game.getReadyAt() != null && at.compareTo(game.getReadyAt()) < 0)
			return false;

		return true;
	}

	public boolean canReadyGame(String username, int gameId)
	{
		if (username == null)
			return false;

		if (!canGameBeReadied(gameId))
			return false;

		// load objects
		Game game = gameDao.read(gameId);
		User user = userDao.read(User.class, username);

		// is owner?
		if (user.equals(game.getOwner()))
			return true;

		// is admin?
		if (user.isUserInRole(Role.ADMIN))
			return true;

		return false;
	}

	public void readyGame(String username, int gameId)
	{
		Date now = RequestTime.get();

		if (!canReadyGame(username, gameId))
			throw new NotAuthorizedException();

		// load game
		Game game = gameDao.read(gameId);

		internalReadyGame(game, now);

		notifyStateChange(game);
	}

	private boolean canGameBeUnaccomplished(int gameId)
	{
		// load game
		Game game = gameDao.read(gameId);

		// is game forming?
		if (game.getState() != GameState.FORMING)
			return false;

		return true;
	}

	public boolean canUnaccomplishGame(String username, int gameId)
	{
		// never by guests
		if (username == null)
			return false;

		// load game
		Game game = gameDao.read(gameId);

		// check state
		if (game.getState() != GameState.FORMING)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// is owner?
		if (user.equals(game.getOwner()))
			return true;

		// is admin?
		if (user.isUserInRole(Role.ADMIN))
			return true;

		return false;
	}

	public void unaccomplishGame(int gameId)
	{
		Date now = RequestTime.get();

		// can game be unaccomplished?
		if (!canGameBeUnaccomplished(gameId))
			throw new NotAuthorizedException();

		// unaccomplish game
		internalUnaccomplishGame(gameId, now);
	}

	public void unaccomplishGame(String username, int gameId)
	{
		Date now = RequestTime.get();

		// can user unaccomplish game?
		if (!canUnaccomplishGame(username, gameId))
			throw new NotAuthorizedException();

		// unaccomplish game
		internalUnaccomplishGame(gameId, now);
	}

	private void internalUnaccomplishGame(int gameId, Date at)
	{
		// load game
		Game game = gameDao.read(gameId);

		// unaccomplish game
		game.setState(GameState.UNACCOMPLISHED);
		game.setFinishedAt(at);

		// remove all invitations
		game.getInvitations().clear();
	}

	public boolean canDeleteGame(String username, int gameId)
	{
		// never by guests
		if (username == null)
			return false;

		// load game
		Game game = gameDao.read(gameId);

		// check state
		if (game.getState() != GameState.FORMING && game.getState() != GameState.FINISHED)
			return false;

		// game is not part of a gamegroup?
		GameHelper gameHelper = new GameHelper(game);
		if (gameHelper.isSingleGame() && gameHelper.getSingleGame().getParentGame() != null)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// is admin?
		if (user.isUserInRole(Role.GAME_ADMIN))
			return true;

		return false;
	}

	public void deleteGame(String username, int gameId)
	{
		if (!canDeleteGame(username, gameId))
			throw new NotAuthorizedException();

		// load game
		Game game = gameDao.read(gameId);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + username + "\" deleted game " + game.getId());

		// delete game
		gameDao.delete(game);
	}

	private boolean canReallyCommitMove(String username, int gameId)
	{
		if (!canCommitMove(username, gameId))
			return false;

		// load objects
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);
		User user = userDao.read(User.class, username);

		// is user active from the view of the game history? (just to be sure...)
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);
		int activePlayerIndex = game.getActualPosition().getActivePlayerIndex();
		if (!singleGame.getPlayers().get(activePlayerIndex).getSubject().equals(user))
			return false;

		return true;
	}

	public boolean canCommitMove(String username, int gameId)
	{
		// guests never
		if (username == null)
			return false;

		// load game
		SingleGame game = gameDao.read(SingleGame.class, gameId);

		// is game running?
		if (game.getState() != GameState.RUNNING)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// is user active?
		if (!game.getActivePlayer().getSubject().equals(user))
			return false;

		// is user player of game? (just to be sure...)
		if (!isSubjectPlayerOfGame(user, game))
			return false;

		return true;
	}

	public void commitMove(String username, int gameId, GameMove move, boolean offerRemis)
	{
		Date now = RequestTime.get();

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		internalCheckGameState(now, singleGame, true);

		if (!canReallyCommitMove(username, gameId))
			throw new NotAuthorizedException();

		// verify user name
		// FIXME this will most likely not work, just return a proxy
		userDao.read(User.class, username);

		// commit move
		internalCommitMove(now, singleGame, move, offerRemis);

		// auto-commit moves
		while (true)
		{
			GameMove conditionalMove = checkAutoCommitMoves(singleGame);

			if (conditionalMove == null)
				break;

			internalCommitMove(now, singleGame, conditionalMove, offerRemis);
		}

		// clean up conditional moves
		for (GamePlayer player : singleGame.getPlayers())
			cleanupConditionalMoves(player);
	}

	private void internalCommitMove(Date now, SingleGame singleGame, GameMove move, boolean remisOffer)
	{
		try
		{
			// unmarshal game
			GameRules rules = GameRulesHelper.rules(singleGame);
			de.schildbach.game.Game game = GameRulesHelper.game(singleGame);

			// sanity check: check against acn constructed game
			if (!game.equals(GameRulesHelper.game(singleGame, singleGame.getHistoryNotation())))
				throw new IllegalStateException("game " + singleGame.getId());

			// sanity check: is move allowed?
			if (!rules.allowedMoves(game).contains(move))
				throw new IllegalMoveException(move);

			// execute move
			rules.executeMove(game, move);

			// update game
			GameRulesHelper.update(singleGame, game);
			singleGame.setRemisOffer(remisOffer);

			// log
			if (LOG.isInfoEnabled())
			{
				LOG.info("user \"" + singleGame.getActivePlayer().getSubject().getName() + "\" committed move \"" + rules.formatMove(move) + "\" ("
						+ singleGame.getTurn() + ".) on " + singleGame.getRules() + " game " + singleGame.getId() + " (remisOffer=" + remisOffer
						+ ")");
			}

			// handle claim remis after move
			if (singleGame.getRemisOffer() && rules.canDrawBeClaimed(game))
			{
				internalFinishGame(now, singleGame, GameResolution.DRAW, null, rules.pointsForDraw(), now);
				return;
			}

			// finish game if rules dictate
			if (rules.isFinished(game))
			{
				float[] points = rules.points(game);
				GamePlayer winner = determineWinner(singleGame, points);
				internalFinishGame(now, singleGame, winner != null ? GameResolution.WIN : GameResolution.DRAW, winner, points, now);
				return;
			}

			// activate next player
			activatePlayer(singleGame.getId(), game.getActualPosition().getActivePlayerIndex(), game.getActualPosition().getFullmoveNumber(), now);
		}
		catch (IllegalMoveException x)
		{
			throw new ApplicationException(x);
		}
		catch (ParseException x)
		{
			throw new ApplicationException(x);
		}
	}

	private GameMove checkAutoCommitMoves(SingleGame singleGame)
	{
		// is game still running?
		if (singleGame.getState() != GameState.RUNNING)
			return null;

		// has there been a remis offer?
		if (singleGame.getRemisOffer())
			return null;

		// unmarshal game
		GameRules rules = GameRulesHelper.rules(singleGame);
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);

		// check for only move allowed
		if (singleGame.getActivePlayer().getSubject().isAutoMove())
		{
			Collection<? extends GameMove> allowedMoves = rules.allowedMoves(game);
			if (allowedMoves.size() == 1)
			{
				GameMove move = allowedMoves.iterator().next();
				if (LOG.isInfoEnabled())
					LOG.info("detected only possible move \"" + rules.formatMove(move) + "\" for \""
							+ singleGame.getActivePlayer().getSubject().getName() + "\" in game " + singleGame.getId());
				return move;
			}
		}

		// check for first matching conditional move
		for (GameConditionalMoves conditionalMoves : singleGame.getActivePlayer().getConditionalMoves())
		{
			de.schildbach.game.Game conditionalGame = GameRulesHelper.game(conditionalMoves);

			if (conditionalGame.startsWith(game) && !conditionalGame.equals(game))
			{
				GameMove move = conditionalGame.getMove(game.getSize());
				if (LOG.isInfoEnabled())
					LOG.info("detected conditional move \"" + rules.formatMove(move) + "\" for \""
							+ singleGame.getActivePlayer().getSubject().getName() + "\" in game " + singleGame.getId());
				return move;
			}
		}

		// nothing found
		return null;
	}

	private void cleanupConditionalMoves(GamePlayer player)
	{
		SingleGame singleGame = (SingleGame) player.getGame();
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);

		for (Iterator<GameConditionalMoves> i = player.getConditionalMoves().iterator(); i.hasNext();)
		{
			GameConditionalMoves conditionalMoves = i.next();
			de.schildbach.game.Game conditionalGame = GameRulesHelper.game(conditionalMoves);

			if (conditionalGame.equals(game))
			{
				if (LOG.isInfoEnabled())
					LOG.info("conditional moves \"" + conditionalMoves.getMoves() + "\" from \"" + player.getSubject().getName() + "\" for game "
							+ singleGame.getId() + " out of date, removing");
				i.remove();
			}
			else if (!conditionalGame.startsWith(game))
			{
				if (LOG.isInfoEnabled())
					LOG.info("conditional moves \"" + conditionalMoves.getMoves() + "\" from \"" + player.getSubject().getName() + "\" for game "
							+ singleGame.getId() + " do not match, removing");
				i.remove();
			}
		}
	}

	private boolean canSetConditionalMoves(String userName, int gameId)
	{
		// guests never
		if (userName == null)
			return false;

		// load game
		SingleGame game = gameDao.read(SingleGame.class, gameId);

		// is game running?
		if (game.getState() != GameState.RUNNING)
			return false;

		// load user
		User user = userDao.read(User.class, userName);

		// is user player of game?
		if (!isSubjectPlayerOfGame(user, game))
			return false;

		return true;
	}

	public void addConditionalMoves(String username, int gameId, String moves)
	{
		Date now = RequestTime.get();

		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		internalCheckGameState(now, singleGame, true);

		if (!canSetConditionalMoves(username, gameId))
			throw new NotAuthorizedException();

		// load objects
		User hUser = userDao.read(User.class, username);
		GamePlayer hPlayer = findPlayer(hUser, singleGame);

		// normalize input
		GameRules rules = GameRulesHelper.rules(singleGame);
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame, moves);

		// strip last moves if they have no consequence
		if (!game.isEmpty())
		{
			while ((game.getSize() - 1) % rules.getNumberOfPlayers() != hPlayer.getPosition())
				rules.undoLastMove(game);
		}

		// add conditional moves if there is anything left of it
		if (!game.isEmpty())
		{
			moves = rules.formatGame(game, Locale.ENGLISH);
			String marshalledMoves = rules.marshal(game);

			if (LOG.isInfoEnabled())
				LOG.info("setting conditional move \"" + moves + "\" for \"" + hPlayer.getSubject().getName() + "\" in game " + singleGame.getId());

			hPlayer.getConditionalMoves().add(new GameConditionalMoves(hPlayer, marshalledMoves, moves, now));
		}
	}

	public void removeConditionalMoves(String username, int gameId, int conditionalMovesId)
	{
		Date now = RequestTime.get();

		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		internalCheckGameState(now, singleGame, true);

		if (!canSetConditionalMoves(username, gameId))
			throw new NotAuthorizedException();

		// load objects
		User hUser = userDao.read(User.class, username);
		GamePlayer hPlayer = findPlayer(hUser, singleGame);

		// remove conditional moves
		for (Iterator<GameConditionalMoves> i = hPlayer.getConditionalMoves().iterator(); i.hasNext();)
		{
			GameConditionalMoves conditionalMoves = i.next();
			if (conditionalMoves.getId() == conditionalMovesId)
			{
				if (LOG.isInfoEnabled())
					LOG.info("removing conditional moves for \"" + hPlayer.getSubject().getName() + "\" in game " + singleGame.getId());

				i.remove();
			}
		}
	}

	public void resignGame(String username, int gameId)
	{
		Date now = RequestTime.get();

		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		internalCheckGameState(now, singleGame, true);

		if (!canReallyCommitMove(username, gameId))
			throw new NotAuthorizedException();

		GameRules rules = GameRulesHelper.rules(singleGame);

		// load objects
		User hUser = userDao.read(User.class, username);

		// determine winner
		GamePlayer winner = findOpponent(findPlayer(hUser, singleGame));

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + hUser.getName() + "\" resigns game " + singleGame.getId() + " (winner: \"" + winner.getSubject().getName() + "\")");

		// resign game
		internalFinishGame(now, singleGame, GameResolution.RESIGN, winner, rules.pointsForWin(winner.getPosition()), now);
	}

	private boolean canRemisBeReallyAccepted(String userName, int gameId)
	{
		// can commit move at all?
		if (!canReallyCommitMove(userName, gameId))
			return false;

		if (!canRemisBeAccepted(userName, gameId))
			return false;

		return true;
	}

	public boolean canRemisBeAccepted(String userName, int gameId)
	{
		// can commit move at all?
		if (!canCommitMove(userName, gameId))
			return false;

		// load game
		SingleGame game = gameDao.read(SingleGame.class, gameId);

		// is there a remis offer?
		if (!game.getRemisOffer())
			return false;

		return true;
	}

	public void acceptRemis(String username, int gameId)
	{
		Date now = RequestTime.get();

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		internalCheckGameState(now, singleGame, true);

		if (!canRemisBeReallyAccepted(username, gameId))
			throw new NotAuthorizedException();

		GameRules rules = GameRulesHelper.rules(singleGame);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + username + "\" accepts remis in game " + gameId);

		// accept remis
		internalFinishGame(now, singleGame, GameResolution.DRAW, null, rules.pointsForDraw(), now);
	}

	private boolean canRemisBeReallyClaimed(String username, int gameId)
	{
		// can commit move at all?
		if (!canReallyCommitMove(username, gameId))
			return false;

		if (!canRemisBeClaimed(username, gameId))
			return false;

		return true;
	}

	public boolean canRemisBeClaimed(String username, int gameId)
	{
		// can commit move at all?
		if (!canCommitMove(username, gameId))
			return false;

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		// can remis be claimed?
		GameRules rules = GameRulesHelper.rules(singleGame);
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);

		return rules.canDrawBeClaimed(game);
	}

	public void claimRemis(String username, int gameId)
	{
		Date now = RequestTime.get();

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		internalCheckGameState(now, singleGame, true);

		if (!canRemisBeReallyClaimed(username, gameId))
			throw new NotAuthorizedException();

		GameRules rules = GameRulesHelper.rules(singleGame);

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + username + "\" claims remis in game " + gameId);

		// claim remis
		internalFinishGame(now, singleGame, GameResolution.DRAW, null, rules.pointsForDraw(), now);
	}

	private void internalReadyGame(Game game, final Date readyAt)
	{
		// set ready at
		game.setReadyAt(readyAt);

		// remove all invitations
		game.getInvitations().clear();

		// shuffle players because order is random
		if (game.getOrderType() != null && game.getOrderType() == OrderType.RANDOM)
		{
			game.shufflePlayers();
		}

		// move start at when game is late
		if (game.getStartedAt() != null && readyAt.compareTo(game.getStartedAt()) > 0)
		{
			game.setStartedAt(readyAt);
		}

		// state
		game.setState(GameState.READY);

		// ready
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				internalReadySingleGame(game, readyAt);
			}

			public void visit(GameGroup group)
			{
				internalReadyGameGroup(group, readyAt);
			}
		});
	}

	private void internalReadyGameGroup(GameGroup group, Date at)
	{
		// create all child games
		for (GamePlayer p1 : group.getPlayers())
		{
			for (GamePlayer p2 : group.getPlayers())
			{
				if (!p1.equals(p2))
				{
					SingleGame childGame = gameDao.newSingleGame(group.getOwner(), at, group.getChildRules(), group.getChildInitialHistory(), group
							.getChildAid(), true, OrderType.FORWARD, group.getChildRating(), group.getChildClockConstraint(), null, group
							.getStartedAt(), group);
					gameDao.create(childGame);

					group.getChildGames().add(childGame);

					GamePlayer cp1 = internalJoinGame(childGame, p1.getSubject(), p1.getJoinedAt());
					cp1.setParentPlayer(p1);
					GamePlayer cp2 = internalJoinGame(childGame, p2.getSubject(), p2.getJoinedAt());
					cp2.setParentPlayer(p2);

					internalReadyGame(childGame, at);
				}
			}
		}
	}

	private void internalReadySingleGame(SingleGame singleGame, Date at)
	{
		// setup clocks
		if (singleGame.getClockConstraint() != null && singleGame.getClockConstraint().length() > 0)
		{
			for (GamePlayer player : singleGame.getPlayers())
			{
				player.setClock(0l);
			}
		}

		de.schildbach.game.Game game = GameRulesHelper.newGame(singleGame.getRules(), singleGame.getInitialHistoryNotation());
		GameRulesHelper.update(singleGame, game);
		if (GameRulesHelper.rulesForceInitialBoard(singleGame.getRules()))
		{
			GameRules rules = GameRulesHelper.rules(singleGame.getRules());
			singleGame.setInitialBoardNotation(rules.formatBoard(game.getInitialPosition().getBoard()));
		}

		// setup turn number
		GamePosition position = game.getActualPosition();
		internalSetTurnNumber(singleGame, position.getFullmoveNumber(), position.getActivePlayerIndex());
	}

	public boolean canGameBeStarted(int gameId)
	{
		Date now = RequestTime.get();
		return internalCanGameBeStarted(gameId, now);
	}

	private boolean internalCanGameBeStarted(int gameId, Date at)
	{
		// load game
		Game game = gameDao.read(gameId);

		// is ready?
		if (game.getState() != GameState.READY)
			return false;

		// is child game?
		GameHelper helper = new GameHelper(game);
		if (helper.isSingleGame() && helper.getSingleGame().getParentGame() != null)
			return false;

		// start asap?
		if (game.getStartedAt() == null)
			return true;

		// reached start time?
		if (at.compareTo(game.getStartedAt()) >= 0)
			return true;

		return false;
	}

	public void startGame(int gameId)
	{
		Date now = RequestTime.get();

		// can game be started?
		if (!canGameBeStarted(gameId))
			throw new NotAuthorizedException();

		// load game
		Game game = gameDao.read(gameId);

		// start game
		internalStartGame(game, now);
	}

	private void internalStartGame(Game game, final Date at)
	{
		game.setState(GameState.RUNNING);
		game.setStartedAt(at);

		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				internalStartSingleGame(game, at);
			}

			public void visit(GameGroup group)
			{
				for (Game childGame : group.getChildGames())
				{
					internalStartGame(childGame, at);
				}
			}
		});
	}

	private void internalStartSingleGame(SingleGame singleGame, Date at)
	{
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);
		int activePlayerIndex = game.getActualPosition().getActivePlayerIndex();
		internalSetPlayerActive(singleGame.getPlayers().get(activePlayerIndex), at);

		// record ratings at start
		if (singleGame.getRating() != null)
		{
			for (GamePlayer player : singleGame.getPlayers())
			{
				SubjectRating rating = subjectRatingDao.findRating(player.getSubject(), singleGame.getRating());
				if (rating != null)
					player.setRatingAtStart(rating.getValue());
			}
		}
	}

	private void activatePlayer(int gameId, int playerIndex, int turnNumber, Date activateAt)
	{
		// load game
		SingleGame game = gameDao.read(SingleGame.class, gameId);
		List<GamePlayer> players = game.getPlayers();

		assert playerIndex >= 0 && playerIndex < players.size() : "playerIndex " + playerIndex + " not in bounds 0-" + (players.size() - 1)
				+ " in game " + game + ", players=" + players;

		GamePlayer player = players.get(playerIndex);

		// set turn number and activate player
		internalSetPlayerInactive(game, activateAt);
		internalSetTurnNumber(game, turnNumber, playerIndex);
		internalSetPlayerActive(player, activateAt);

		// notification
		ctx.publishEvent(new GamePlayerActiveEvent(this, game.getId(), player.getSubject().getName()));
	}

	private void internalSetPlayerInactive(SingleGame game, Date inactiveAt)
	{
		if (game.getState() != GameState.RUNNING)
			throw new IllegalStateException("game " + game.getId() + " is not running");

		GamePlayer activePlayer = game.getActivePlayer();
		if (activePlayer == null)
			throw new IllegalStateException("no player active");

		if (inactiveAt.before(game.getLastActiveAt()))
			throw new IllegalArgumentException("inactive before active");

		// substract active time from clock
		if (activePlayer.getClock() != null)
		{
			long elapsed = inactiveAt.getTime() - game.getLastActiveAt().getTime();
			long oldRemaining = activePlayer.getClock();
			long newRemaining = oldRemaining - elapsed;
			activePlayer.setClock(newRemaining);
			LOG.info("adjusted clock of player " + activePlayer.getId() + " (" + activePlayer.getPosition() + "), game " + game.getId() + ": old="
					+ oldRemaining + "ms, new=" + newRemaining + "ms");
		}

		// set player inactive
		game.setActivePlayer(null);
	}

	private void internalSetPlayerActive(GamePlayer player, Date activeAt)
	{
		GameHelper helper = new GameHelper(player.getGame());
		SingleGame game = helper.getSingleGame();

		if (game.getState() != GameState.RUNNING)
			throw new IllegalStateException("game " + game.getId() + " is not running");

		GamePlayer activePlayer = game.getActivePlayer();
		if (activePlayer != null)
			throw new IllegalStateException("game " + game.getId() + " has unexpected active player " + activePlayer.getId() + " (lastActiveAt="
					+ game.getLastActiveAt() + ")");

		// set player active
		game.setActivePlayer(player);
		game.setLastActiveAt(activeAt);

		// clear last reminder
		game.setLastReminderAt(null);
	}

	private void internalSetTurnNumber(SingleGame singleGame, int turnNumber, int playerIndex)
	{
		// set turn number
		singleGame.setTurn(turnNumber);

		if (singleGame.getClockConstraint() != null && singleGame.getClockConstraint().length() > 0)
		{
			int halfMoveNumber = (turnNumber - 1) * 2 + playerIndex;

			// for the chess clock, substract moves from initial history
			if (singleGame.getInitialHistoryNotation() != null)
			{
				de.schildbach.game.Game initialHistory = GameRulesHelper.gameFromInitialHistory(singleGame);
				halfMoveNumber -= (initialHistory.getActualPosition().getFullmoveNumber() - 1) * 2
						+ (initialHistory.getActualPosition().getActivePlayerIndex());
			}

			if (halfMoveNumber % 2 == 0)
			{
				// update clocks
				ClockConstraint clockConstraint = new ClockConstraint(singleGame.getClockConstraint());
				for (GamePlayer player : singleGame.getPlayers())
				{
					int turn = (halfMoveNumber / 2) + 1;
					long oldRemaining = player.getClock();
					long newRemaining = clockConstraint.incrementClock(turn, oldRemaining);
					player.setClock(newRemaining);
					if (oldRemaining != newRemaining)
						LOG.info("adjusted clock of player " + player.getId() + " (" + player.getPosition() + "), game " + singleGame.getId() + ", "
								+ clockConstraint.getId() + ", turn " + turn + "): old=" + oldRemaining + "ms, new=" + newRemaining + "ms");
				}
			}
		}
	}

	private boolean canActivePlayerBeReminded(int gameId)
	{
		Date now = RequestTime.get();

		// find single game
		SingleGame game = (SingleGame) gameDao.findGame(SingleGame.class, gameId);

		// is the game existing?
		if (game == null)
			return false;

		// is the game running?
		if (game.getState() != GameState.RUNNING)
			return false;

		// has player already been reminded?
		if (game.getLastReminderAt() != null)
			return false;

		// 48 hours since last move?
		if ((now.getTime() - game.getLastActiveAt().getTime()) < REMINDER_TIMEOUT)
			return false;

		// only users can be notified atm
		SubjectHelper helper = new SubjectHelper(game.getActivePlayer().getSubject());
		if (!helper.isUser())
			return false;

		// is there a contact to notify?
		if (helper.getUser().getEmail() == null && helper.getUser().getXmpp() == null)
			return false;

		// is user in holidays?
		if (userService.isUserInHolidays(game.getActivePlayer().getSubject().getName()))
			return false;

		return true;
	}

	public boolean canRemindActivePlayer(String username, int gameId)
	{
		// guests never
		if (username == null)
			return false;

		// can active player be reminded?
		if (!canActivePlayerBeReminded(gameId))
			return false;

		// load objects
		SingleGame game = gameDao.read(SingleGame.class, gameId);
		User user = userDao.read(User.class, username);

		// is reminding user player of game?
		if (!isSubjectPlayerOfGame(user, game))
			return false;

		// can't remind yourself
		if (user.equals(game.getActivePlayer().getSubject()))
			return false;

		return true;
	}

	public void remindActivePlayer(String username, int gameId, String customText)
	{
		Date now = RequestTime.get();

		// can player be reminded?
		if (!canRemindActivePlayer(username, gameId))
			throw new NotAuthorizedException();

		// load game
		SingleGame game = gameDao.read(SingleGame.class, gameId);

		game.setLastReminderAt(now);

		SubjectHelper helper = new SubjectHelper(game.getActivePlayer().getSubject());

		// locate resources
		Locale locale = helper.getUser().getLocale();
		String gameUrl = ctx.getMessage("url.view_game", new Object[] { gameId }, locale);
		String rulesMessage = ctx.getMessage(game.getRules().name(), null, locale);
		String prefix = "remind_active_player.";

		// compose text
		String subject = ctx.getMessage(prefix + "subject", new Object[] { rulesMessage, game.getId(), username }, locale);
		String text = customText
				+ "\n"
				+ ctx.getMessage(prefix + "text", new Object[] { game.getId(), game.getLastActiveAt(), gameUrl,
						game.getActivePlayer().getSubject().getName() }, locale);

		// send message
		ctx.publishEvent(new UserMessageEvent(this, helper.getUser().getName(), subject.toString(), text.toString()));
	}

	public boolean canDisqualifyActivePlayer(String username, int gameId)
	{
		Date now = RequestTime.get();

		// guests never
		if (username == null)
			return false;

		// find single game
		SingleGame game = (SingleGame) gameDao.findGame(SingleGame.class, gameId);

		// is the game existing?
		if (game == null)
			return false;

		// is the game running?
		if (game.getState() != GameState.RUNNING)
			return false;

		// is the time-out over?
		if ((now.getTime() - (game.getLastReminderAt() != null ? game.getLastReminderAt() : game.getLastActiveAt()).getTime()) < REMINDER_TIMEOUT)
			return false;

		// can active player be reminded?
		if (canActivePlayerBeReminded(gameId))
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// is disqualifying user player of game?
		if (!isSubjectPlayerOfGame(user, game))
			return false;

		// can't disqualify yourself
		if (user.equals(game.getActivePlayer().getSubject()))
			return false;

		// is user in holidays?
		if (userService.isUserInHolidays(game.getActivePlayer().getSubject().getName()))
			return false;

		return true;
	}

	public void disqualifyActivePlayer(String username, int gameId)
	{
		Date now = RequestTime.get();

		// can player be disqualified?
		if (!canDisqualifyActivePlayer(username, gameId))
			throw new NotAuthorizedException();

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);
		GameRules rules = GameRulesHelper.rules(singleGame);

		// determine winner
		GamePlayer winner = findOpponent(singleGame.getActivePlayer());

		// disqualify active player
		internalFinishGame(now, singleGame, GameResolution.DISQUALIFY, winner, rules.pointsForWin(winner.getPosition()), now);
	}

	public boolean checkFinishGame(int gameId)
	{
		Date now = RequestTime.get();
		return internalCheckFinishGame(gameId, now);
	}

	private static class BooleanHolder
	{
		private boolean value;

		public void setValue(boolean bool)
		{
			this.value = bool;
		}

		public boolean getValue()
		{
			return value;
		}
	}

	private boolean internalCheckFinishGame(int gameId, final Date now)
	{
		// load game
		Game game = gameDao.read(gameId);

		final BooleanHolder booleanHolder = new BooleanHolder();
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				booleanHolder.setValue(internalCheckFinish(game, now));
			}

			public void visit(GameGroup group)
			{
				booleanHolder.setValue(internalCheckFinish(group, now));
			}
		});
		return booleanHolder.getValue();
	}

	private boolean internalCheckFinish(SingleGame singleGame, Date now)
	{
		// is game running?
		if (singleGame.getState() != GameState.RUNNING)
			return false;

		// has it got any clock constraints?
		if (singleGame.getClockConstraint() == null || singleGame.getClockConstraint().length() == 0)
			return false;

		GamePlayer activePlayer = singleGame.getActivePlayer();
		if (activePlayer == null)
			throw new IllegalStateException("no active player in single game " + singleGame.getId());
		if (activePlayer.getClock() == null)
			throw new IllegalStateException("player " + activePlayer.getId() + " has not got any clock");
		if (singleGame.getLastActiveAt() == null)
			throw new IllegalStateException("last active at not set for game " + singleGame.getId());

		long remaining = activePlayer.getClock();
		long elapsed = now.getTime() - singleGame.getLastActiveAt().getTime();

		if (elapsed < 0)
			throw new IllegalStateException("elapsed time < 0");

		// has time on the clock elapsed?
		if (remaining >= elapsed)
			return false;

		// log timeout
		LOG.info("clock of active player " + activePlayer.getId() + " timed out: remaining=" + remaining + "ms, elapsed=" + elapsed + "ms");

		// determine winner
		GamePlayer winner = findOpponent(activePlayer);

		// time out game
		GameRules rules = GameRulesHelper.rules(singleGame);
		Date finishAt = new Date(singleGame.getLastActiveAt().getTime() + remaining);
		internalFinishGame(now, singleGame, GameResolution.TIMEOUT, winner, rules.pointsForWin(winner.getPosition()), finishAt);

		return true;
	}

	private boolean internalCheckFinish(GameGroup group, Date now)
	{
		// game needs correct initial state
		if (group.getState() != GameState.RUNNING)
			return false;

		// determine if game group is finished and when it finished
		Date groupFinishedAt = group.getStartedAt();
		boolean groupFinished = true;

		for (Game childGame : group.getChildGames())
		{
			if (childGame.getState() == GameState.FINISHED)
			{
				if (childGame.getFinishedAt().after(groupFinishedAt))
					groupFinishedAt = childGame.getFinishedAt();
			}
			else
			{
				groupFinished = false;
			}
		}

		// action
		if (groupFinished)
		{
			assert (groupFinishedAt != null);

			float[] points = determineGameGroupPoints(group);
			GamePlayer winner = determineWinner(group, points);
			GameResolution resolution = winner != null ? GameResolution.WIN : GameResolution.DRAW;

			internalFinishGame(now, group, resolution, winner, points, groupFinishedAt);
		}

		return groupFinished;
	}

	public boolean canReactivateGame(String username, int gameId)
	{
		// never by guests
		if (username == null)
			return false;

		// is admin?
		User user = userDao.read(User.class, username);
		if (!user.isUserInRole(Role.ADMIN))
			return false;

		// load game
		Game hGame = gameDao.read(gameId);
		GameHelper gameHelper = new GameHelper(hGame);

		// is single game?
		if (!gameHelper.isSingleGame())
			return false;

		// is game finished?
		if (hGame.getState() != GameState.FINISHED)
			return false;

		// is parent game still running?
		SingleGame singleGame = gameHelper.getSingleGame();
		if (singleGame.getParentGame() != null && singleGame.getParentGame().getState() != GameState.RUNNING)
			return false;

		// did game finish regularly?
		GameRules rules = GameRulesHelper.rules(singleGame);
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);
		if (rules.isFinished(game))
			return false;

		return true;
	}

	public void reactivateGame(String username, int gameId)
	{
		// can game be reactivated?
		if (!canReactivateGame(username, gameId))
			throw new NotAuthorizedException();

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		// reactivate game
		singleGame.setState(GameState.RUNNING);
		singleGame.setResolution(null);
		singleGame.setWinner(null);

		// reactivate player
		de.schildbach.game.Game game = GameRulesHelper.game(singleGame);
		int activePlayerIndex = game.getActualPosition().getActivePlayerIndex();
		GamePlayer activePlayer = singleGame.getPlayers().get(activePlayerIndex);
		internalSetPlayerActive(activePlayer, RequestTime.get());

		// TODO adjust clock of active player
	}

	private GamePlayer determineWinner(Game game, float points[])
	{
		GamePlayer winner = null;
		float winnerPoints = -1;

		for (GamePlayer player : game.getPlayers())
		{
			if (winnerPoints == -1)
			{
				winner = player;
				winnerPoints = points[winner.getPosition()];
			}
			else if (points[player.getPosition()] > winnerPoints)
			{
				winner = player;
				winnerPoints = points[winner.getPosition()];
			}
			else if (points[player.getPosition()] == winnerPoints)
			{
				winner = null;
			}
		}

		return winner;
	}

	private float[] determineGameGroupPoints(GameGroup group)
	{
		float[] points = new float[group.getPlayers().size()];

		// sum points
		for (Game child : group.getChildGames())
		{
			for (GamePlayer player : child.getPlayers())
			{
				GamePlayer parentPlayer = player.getParentPlayer();
				assert parentPlayer != null;
				if (player.getPoints() != null)
					points[parentPlayer.getPosition()] += player.getPoints();
			}
		}

		return points;
	}

	protected void winGame(SingleGame singleGame, GamePlayer winner)
	{
		Date now = RequestTime.get();
		GameRules rules = GameRulesHelper.rules(singleGame);
		internalFinishGame(now, singleGame, GameResolution.WIN, winner, rules.pointsForWin(winner.getPosition()), now);
	}

	public void drawGame(int gameId)
	{
		Date now = RequestTime.get();

		// load game
		SingleGame singleGame = gameDao.read(SingleGame.class, gameId);

		GameRules rules = GameRulesHelper.rules(singleGame);
		internalFinishGame(now, singleGame, GameResolution.DRAW, null, rules.pointsForDraw(), now);
	}

	private void persistPoints(Game game, float[] points)
	{
		// transfer points into persistant objects
		for (int i = 0; i < points.length; i++)
			game.getPlayers().get(i).setPoints(points[i]);
	}

	private void internalFinishGame(final Date now, Game game, GameResolution resolution, final GamePlayer winner, float[] points, final Date finishAt)
	{
		// check preconditions
		if (game.getState() != GameState.RUNNING)
			throw new ApplicationException("game not running");

		if (winner != null && !(game.getPlayers().contains(winner)))
			throw new ApplicationException("pretended winner is no player");

		// set active player inactive
		game.accept(new GameVisitorAdapter()
		{
			@Override
			public void visit(SingleGame game)
			{
				internalSetPlayerInactive(game, finishAt);
			}
		});

		// persist points
		persistPoints(game, points);

		// remove conditional moves from all players
		for (GamePlayer player : game.getPlayers())
			player.getConditionalMoves().clear();

		// finish game
		game.setState(GameState.FINISHED);
		game.setResolution(resolution);
		game.setWinner(winner);
		game.setFinishedAt(finishAt);

		game.accept(new GameVisitorAdapter()
		{
			@Override
			public void visit(SingleGame game)
			{
				internalAfterFinishSingleGame(now, game, winner, finishAt);
			}
		});
	}

	private void internalAfterFinishSingleGame(Date now, SingleGame game, GamePlayer winner, Date finishAt)
	{
		// pre-condition
		if (game.getPlayers().size() != 2)
			throw new IllegalStateException("can only handle games with 2 players");

		// adjust rating
		if (game.getRating() != null)
		{
			// TODO do away with ELO dependancy!
			RatingClass ratingClass = RatingClass.ELO;

			GamePlayer player0 = game.getPlayers().get(0);
			GamePlayer player1 = game.getPlayers().get(1);

			SubjectRating elo0 = internalGetRating(finishAt, player0.getSubject(), game.getRating());
			SubjectRating elo1 = internalGetRating(finishAt, player1.getSubject(), game.getRating());

			// adjust
			SubjectRating[] ratings = new SubjectRating[] { elo0, elo1 };
			String[] baseRatings = new String[] {
					player0.getRatingAtStart() != null ? player0.getRatingAtStart() : ratingActivity.initialRating(ratingClass),
					player1.getRatingAtStart() != null ? player1.getRatingAtStart() : ratingActivity.initialRating(ratingClass) };

			float[] points = new float[] { player0.getPoints(), player1.getPoints() };
			ratingActivity.adjustRatings(finishAt, ratingClass, ratings, baseRatings, points);

			// record
			for (GamePlayer player : game.getPlayers())
			{
				// record into player
				SubjectRating rating = subjectRatingDao.findRating(player.getSubject(), game.getRating());
				if (rating != null)
					player.setRatingAtFinish(rating.getValue());

				// record to history
				SubjectRatingHistory history = new SubjectRatingHistory(now, player.getSubject(), game.getRating(), finishAt, rating.getValue(),
						player);
				subjectRatingDao.save(history);
			}
		}

		// TODO kludge!
		if (game.getParentGame() != null)
		{
			game.getParentGame().accept(new GameVisitorAdapter()
			{
				@Override
				public void visit(GameGroup group)
				{
					float[] points = determineGameGroupPoints(group);
					persistPoints(group, points);
				}
			});
		}
	}

	private SubjectRating internalGetRating(Date at, Subject subject, Rating rating)
	{
		// get former rating
		SubjectRating subjectRating = subjectRatingDao.findRating(subject, rating);

		// return rating if already present
		if (subjectRating != null)
			return subjectRating;

		// FIXME this directly dependant on elo atm
		String value = ratingActivity.initialRating(RatingClass.ELO);

		// new rating
		subjectRating = new SubjectRating(subject, rating, value, at);
		subjectRating.setLastValue(value);
		subjectRatingDao.create(subjectRating);
		return subjectRating;
	}

	private class Sequence
	{
		private int value = 0;

		public int nextValue()
		{
			return value++;
		}
	}

	public int updateRatingIndex(Rating rating)
	{
		List<SubjectRating> ratings = subjectRatingDao.findRatings(rating, null, null, null);
		ratingActivity.sortRatings(RatingClass.ELO, ratings);

		final Sequence sequence = new Sequence();

		for (final SubjectRating subjectRating : ratings)
		{
			subjectRating.setIndex(null);
			SubjectVisitorAdapter visitor = new SubjectVisitorAdapter()
			{
				@Override
				public void visit(User user)
				{
					if (userService.isUserActive(user.getName()) && !user.isUserInRole(Role.UNRATED))
						subjectRating.setIndex(sequence.nextValue());
				}
			};
			subjectRating.getSubject().accept(visitor);
		}
		return sequence.nextValue();
	}

	public List<SubjectRating> ratingsToplist(Rating rating, int maxResults)
	{
		return subjectRatingDao.findRatings(rating, 0, maxResults - 1, "index");
	}

	public List<SubjectRating> ratingsToplist(Rating rating, int topResults, String subjectName, int nearResults)
	{
		List<SubjectRating> ratings = subjectRatingDao.findRatings(rating, 0, topResults - 1, "index");
		if (subjectName != null)
		{
			Subject subject = userDao.read(subjectName);
			SubjectRating anchorRating = subjectRatingDao.findRating(subject, rating);
			if (anchorRating != null && anchorRating.getIndex() != null)
			{
				int min = anchorRating.getIndex() - nearResults / 2;
				if (min < topResults)
					min = topResults;
				int max = min + nearResults - 1;
				ratings.addAll(subjectRatingDao.findRatings(rating, min, max, "index"));
			}
		}
		return ratings;
	}

	public List<SubjectRating> ratingsForSubject(String subjectName)
	{
		Subject subject = userDao.read(subjectName);
		List<SubjectRating> ratings = subjectRatingDao.findRatingsForSubject(subject);
		Collections.sort(ratings);
		return ratings;
	}

	public List<SubjectRatingHistory> ratingHistory(String subjectName, Rating rating)
	{
		Subject subject = userDao.read(subjectName);
		return subjectRatingDao.findRatingHistory(subject, rating, SubjectRatingHistory.PROPERTY_CREATED_AT);
	}

	public boolean canReadGameComments(String username, int gameId)
	{
		return canAddGameComment(username, gameId);
	}

	public boolean canAddGameComment(String username, int gameId)
	{
		// guests never
		if (username == null)
			return false;

		// load objects
		User user = userDao.read(User.class, username);
		Game game = gameDao.read(gameId);

		// is user player of game?
		if (isSubjectPlayerOfGame(user, game))
			return true;

		// is user owner of game?
		if (user.equals(game.getOwner()))
			return true;

		return false;
	}

	public void addGameComment(String username, int gameId, String comment)
	{
		// can user add public comment?
		if (!canAddGameComment(username, gameId))
			throw new NotAuthorizedException();

		if (comment.length() > MAX_COMMENT_LENGTH)
			throw new ApplicationException("comment exceeds length " + MAX_COMMENT_LENGTH);

		// load game
		Game game = gameDao.read(gameId);

		// add public comment
		String[] comments = game.getComments() != null ? game.getComments().split("\\n") : new String[] {};
		int numComments = comments.length;
		StringBuilder c2 = new StringBuilder();
		for (int i = numComments < MAX_COMMENTS_PER_GAME ? 0 : 1; i < numComments; i++)
		{
			c2.append(comments[i]).append("\n");
		}
		c2.append(username).append(": ").append(comment).append("\n");
		game.setComments(c2.toString());

		// log
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + username + "\" has added a player comment to game " + gameId);
	}

	public Game viewGame(int gameId)
	{
		Date now = RequestTime.get();

		Game game = gameDao.findGame(Game.class, gameId);

		if (game != null)
			internalCheckGameState(now, game, true);

		return game;
	}

	private boolean shouldGameBeUnaccomplishedBySystem(int gameId, Date at)
	{
		// load game
		Game game = gameDao.read(gameId);

		// can game be unaccomplished?
		if (!canGameBeUnaccomplished(gameId))
			return false;

		if (game.getStartedAt() == null)
		{
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(game.getCreatedAt());
			calendar.add(Calendar.MONTH, 1);
			return !at.before(calendar.getTime());
		}
		else
		{
			return at.after(game.getStartedAt());
		}
	}

	private boolean shouldGameBeDeletedBySystem(Game game, Date at)
	{
		// is game unaccomplished?
		if (game.getState() != GameState.UNACCOMPLISHED)
			return false;

		// is game without parent and childs?
		GameHelper gameHelper = new GameHelper(game);
		if (gameHelper.isSingleGame() && gameHelper.getSingleGame().getParentGame() != null)
			return false;
		if (gameHelper.isGameGroup() && !gameHelper.getGameGroup().getChildGames().isEmpty())
			return false;

		// delay
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(at);
		calendar.add(DELETE_DELAY_FIELD, -DELETE_DELAY_AMOUNT);
		if (game.getFinishedAt() != null && game.getFinishedAt().after(calendar.getTime()))
			return false;

		// ok to delete
		return true;
	}

	private boolean internalCheckGameState(Date now, Game game, boolean shouldNotify)
	{
		int gameId = game.getId();

		try
		{
			boolean readied = internalCanGameBeReadied(gameId, now)
					&& (game.getReadyAt() != null || game.getPlayers().size() >= GameRulesHelper.maxPlayers(game));
			if (readied)
			{
				internalReadyGame(game, game.getReadyAt() != null ? game.getReadyAt() : now);
				LOG.info("readied game " + gameId);
			}

			boolean unaccomplished = shouldGameBeUnaccomplishedBySystem(gameId, now);
			if (unaccomplished)
			{
				internalUnaccomplishGame(gameId, now);
				LOG.info("unaccomplished game " + gameId);
			}

			boolean deleted = shouldGameBeDeletedBySystem(game, now);
			if (deleted)
			{
				gameDao.delete(game);
				LOG.info("deleted game " + gameId);
				return true; // no further checks
			}

			boolean started = internalCanGameBeStarted(gameId, now);
			if (started)
			{
				internalStartGame(game, game.getStartedAt() != null ? game.getStartedAt() : now);
				LOG.info("started game " + gameId);
			}

			boolean finished = internalCheckFinishGame(gameId, now);
			if (finished)
			{
				LOG.info("finished game " + gameId);
			}

			if (readied || started || finished || unaccomplished)
			{
				if (shouldNotify)
					notifyStateChange(game);
			}

			return (readied || started || finished || unaccomplished);
		}
		catch (RuntimeException x)
		{
			throw x;
		}
		catch (Exception x)
		{
			throw new ApplicationException(x);
		}
	}

	public int checkGamesWithState(GameState state)
	{
		Date now = RequestTime.get();

		int count = 0;

		for (Game game : gameDao.findGames(state, null, null, null, null, null, null, null, null, 0, null, true))
		{
			if (internalCheckGameState(now, game, true))
				count++;
		}

		return count;
	}

	private class Counter
	{
		private int value = 0;

		public void increment()
		{
			value++;
		}

		public int getValue()
		{
			return value;
		}
	}

	public int checkInactiveSystemReminder()
	{
		final Date now = RequestTime.get();

		final Counter reminderCount = new Counter();

		// calculate dates
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.add(INACTIVE_SYSTEM_REMINDER_AFTER_FIELD, -INACTIVE_SYSTEM_REMINDER_AFTER_AMOUNT);
		Date inactiveSystemReminderAfter = calendar.getTime();
		calendar.setTime(now);
		calendar.add(INACTIVE_SYSTEM_REMINDER_IMMUNITY_FIELD, -INACTIVE_SYSTEM_REMINDER_IMMUNITY_AMOUNT);
		Date inactiveSystemReminderImmunity = calendar.getTime();

		// iterate through a coarse selection of games
		List<SingleGame> games = gameDao.findSingleGames(null, GameState.RUNNING, null, null, null, null, null, null, null,
				inactiveSystemReminderAfter, null, null, 0, null, true);
		if (LOG.isDebugEnabled())
			LOG.debug("gameDao.findGames() returned " + games.size() + " games");

		for (final SingleGame game : games)
		{
			if (game.getClockConstraint() == null)
			{
				for (final GamePlayer player : game.getPlayers())
				{
					Date lastSystemReminderAt = player.getLastSystemReminderAt();

					if ((lastSystemReminderAt == null || lastSystemReminderAt.before(inactiveSystemReminderImmunity)))
					{
						player.getSubject().accept(new SubjectVisitorAdapter()
						{
							@Override
							public void visit(User user)
							{
								if (user.getEmail() != null || user.getXmpp() != null)
								{
									ctx.publishEvent(new GamePlayerInactiveSystemReminderEvent(this, game.getId(), user.getName()));
									player.setLastSystemReminderAt(now);
									reminderCount.increment();
								}
							}
						});
					}
				}
			}
		}

		return reminderCount.getValue();
	}

	public int checkInactiveDisqualify()
	{
		Date now = RequestTime.get();

		int count = 0;

		// calculate dates
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.add(INACTIVE_DISQUALIFY_AFTER_FIELD, -INACTIVE_DISQUALIFY_AFTER_AMOUNT);
		Date inactiveDisqualifyAfter = calendar.getTime();

		// iterate through a coarse selection of games
		List<SingleGame> games = gameDao.findSingleGames(null, GameState.RUNNING, null, null, null, null, null, null, null, inactiveDisqualifyAfter,
				null, null, 0, null, true);
		if (LOG.isDebugEnabled())
			LOG.debug("gameDao.findGames() returned " + games.size() + " games");
		for (SingleGame singleGame : games)
		{
			if (singleGame.getClockConstraint() == null && !inactiveDisqualifyAfter.before(singleGame.getLastActiveAt())
					&& (singleGame.getLastReminderAt() == null || !inactiveDisqualifyAfter.before(singleGame.getLastReminderAt())))
			{
				// notification
				ctx.publishEvent(new GamePlayerInactiveDisqualificationEvent(this, singleGame.getId()));

				// disqualify
				GameRules rules = GameRulesHelper.rules(singleGame);
				internalFinishGame(now, singleGame, GameResolution.DISQUALIFY, null, rules.pointsForDraw(), now);

				count++;
			}
		}

		return count;
	}

	public int checkClockTimeout()
	{
		Date now = RequestTime.get();

		int count = 0;

		List<SingleGame> games = gameDao.findSingleGames(null, GameState.RUNNING, null, null, null, null, null, null, null, null, null, null, 0,
				null, true);
		// TODO lieber gleich via hibernate filtern
		for (SingleGame game : games)
		{
			if (game.getClockConstraint() != null)
			{
				// TODO genauerer check
				if (internalCheckGameState(now, game, true))
					count++;
			}
		}

		return count;
	}

	public static class Count extends Number
	{
		int count = 0;

		public void increment()
		{
			count++;
		}

		@Override
		public double doubleValue()
		{
			return count;
		}

		@Override
		public float floatValue()
		{
			return count;
		}

		@Override
		public int intValue()
		{
			return count;
		}

		@Override
		public long longValue()
		{
			return count;
		}

		@Override
		public byte byteValue()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString()
		{
			return Integer.toString(count);
		}
	}

	public SortedMap<Rules, Map<String, Count>> gameStatisticsForSubject(String subjectName, String opponentName, int windowAfterFinish)
	{
		SortedMap<Rules, Map<String, Count>> stats = new TreeMap<Rules, Map<String, Count>>();

		// load subjects
		Subject subject = userDao.read(subjectName);
		Subject opponent = opponentName != null ? userDao.read(opponentName) : null;

		// calculate window
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(RequestTime.get());
		calendar.add(Calendar.MONTH, -windowAfterFinish);
		Date finishedAfter = calendar.getTime();

		// iterate games
		for (SingleGame game : gameDao.findSingleGames(null, null, null, subject, null, null, null, null, null, null, null, finishedAfter, 0, null,
				true))
		{
			Rules key = game.getRules();

			Map<String, Count> gameStats = stats.get(key);
			if (gameStats == null)
			{
				gameStats = new HashMap<String, Count>();
				gameStats.put("total", new Count());

				gameStats.put("forming", new Count());
				gameStats.put("unaccomplished", new Count());
				gameStats.put("ready", new Count());
				gameStats.put("running", new Count());
				gameStats.put("finished", new Count());

				gameStats.put("draws", new Count());

				gameStats.put("wins", new Count());
				gameStats.put("wins_regular", new Count());
				gameStats.put("wins_resign", new Count());
				gameStats.put("wins_timeout", new Count());
				gameStats.put("wins_disqualification", new Count());

				gameStats.put("losses", new Count());
				gameStats.put("losses_regular", new Count());
				gameStats.put("losses_resign", new Count());
				gameStats.put("losses_timeout", new Count());
				gameStats.put("losses_disqualification", new Count());

				stats.put(key, gameStats);
			}

			if (opponent != null)
			{
				List<Subject> subjects = new LinkedList<Subject>(gameDao.getPlayingSubjects(game));
				if (!subjects.contains(opponent))
					continue;
			}

			gameStats.get("total").increment();

			if (game.getState() == GameState.FORMING)
				gameStats.get("forming").increment();
			else if (game.getState() == GameState.UNACCOMPLISHED)
				gameStats.get("unaccomplished").increment();
			else if (game.getState() == GameState.READY)
				gameStats.get("ready").increment();
			else if (game.getState() == GameState.RUNNING)
				gameStats.get("running").increment();
			else if (game.getState() == GameState.FINISHED)
			{
				gameStats.get("finished").increment();

				if (game.getResolution() == GameResolution.DRAW)
				{
					gameStats.get("draws").increment();
				}
				else if (game.getResolution() == GameResolution.WIN)
					if (game.getWinner().getSubject().equals(subject))
					{
						gameStats.get("wins").increment();
						gameStats.get("wins_regular").increment();
					}
					else
					{
						gameStats.get("losses").increment();
						gameStats.get("losses_regular").increment();
					}
				else if (game.getResolution() == GameResolution.RESIGN)
					if (game.getWinner().getSubject().equals(subject))
					{
						gameStats.get("wins").increment();
						gameStats.get("wins_resign").increment();
					}
					else
					{
						gameStats.get("losses").increment();
						gameStats.get("losses_resign").increment();
					}
				else if (game.getResolution() == GameResolution.TIMEOUT)
					if (game.getWinner().getSubject().equals(subject))
					{
						gameStats.get("wins").increment();
						gameStats.get("wins_timeout").increment();
					}
					else
					{
						gameStats.get("losses").increment();
						gameStats.get("losses_timeout").increment();
					}
				else if (game.getResolution() == GameResolution.DISQUALIFY)
					if (game.getWinner() == null)
					{
						// TODO
					}
					else if (game.getWinner().getSubject().equals(subject))
					{
						gameStats.get("wins").increment();
						gameStats.get("wins_disqualification").increment();
					}
					else
					{
						gameStats.get("losses").increment();
						gameStats.get("losses_disqualification").increment();
					}
			}
			else
				LOG.warn("uncounted game " + game.getId());
		}

		return stats;
	}

	private void notifyInvitationByName(final String remoteUserName, final User user, Game game)
	{
		// locate resources
		final Locale locale = user.getLocale();
		final String gameUrl = ctx.getMessage("url.view_game", new Object[] { new Integer(game.getId()) }, locale);

		// build string
		final StringBuilder subject = new StringBuilder();
		final StringBuilder text = new StringBuilder();
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				String prefix = "singlegame_invitation_by_name.";
				String rulesMessage = ctx.getMessage(game.getRules().name(), null, locale);

				subject.append(ctx.getMessage(prefix + "subject", new Object[] { rulesMessage }, locale));
				text.append(ctx.getMessage(prefix + "text", new Object[] { user.getName(), rulesMessage, remoteUserName, gameUrl }, locale));
			}

			public void visit(GameGroup group)
			{
				String prefix = "gamegroup_invitation_by_name.";
				String rulesMessage = ctx.getMessage(group.getChildRules().name(), null, locale);

				subject.append(ctx.getMessage(prefix + "subject", new Object[] { rulesMessage }, locale));
				text.append(ctx.getMessage(prefix + "text", new Object[] { user.getName(), rulesMessage, remoteUserName, gameUrl }, locale));
			}
		});

		// send message
		ctx.publishEvent(new UserMessageEvent(this, user.getName(), subject.toString(), text.toString()));
	}

	private void notifyStateChange(Game game)
	{
		// dispatch
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				// don't notify if there is a parent game (parent game will notify)
				if (game.getParentGame() != null)
					return;

				// notification
				ctx.publishEvent(new GameStateChangedEvent(this, game.getId(), game.getState()));
			}

			public void visit(GameGroup group)
			{
				// notification
				ctx.publishEvent(new GameStateChangedEvent(this, group.getId(), group.getState()));
			}
		});
	}

	private List<GamePlayer> findOpponents(GamePlayer player)
	{
		List<GamePlayer> opponents = new LinkedList<GamePlayer>(player.getGame().getPlayers());
		opponents.remove(player);
		return opponents;
	}

	private GamePlayer findOpponent(GamePlayer player)
	{
		List<GamePlayer> opponents = findOpponents(player);
		if (opponents.size() > 2)
			throw new ApplicationException("more than 1 opponent");
		return opponents.get(0);
	}

	public boolean canWatchGame(String userName, int gameId)
	{
		// guests never
		if (userName == null)
			return false;

		// load objects
		Game game = gameDao.read(gameId);
		User user = userDao.read(User.class, userName);

		// already watching?
		if (isUserWatchingGame(user, game))
			return false;

		return true;
	}

	public void watchGame(String userName, int gameId)
	{
		// can watch game?
		if (!canWatchGame(userName, gameId))
			throw new NotAuthorizedException();

		// load objects
		Game game = gameDao.read(gameId);
		User user = userDao.read(User.class, userName);

		// add watch
		GameWatch watch = new GameWatch(game, user);
		game.getWatches().add(watch);
	}

	public boolean canUnwatchGame(String userName, int gameId)
	{
		// guests never
		if (userName == null)
			return false;

		// load objects
		Game game = gameDao.read(gameId);
		User user = userDao.read(User.class, userName);

		// watching?
		if (!isUserWatchingGame(user, game))
			return false;

		return true;
	}

	public void unwatchGame(String userName, int gameId)
	{
		// can unwatch game?
		if (!canUnwatchGame(userName, gameId))
			throw new NotAuthorizedException();

		// load objects
		Game game = gameDao.read(gameId);
		User user = userDao.read(User.class, userName);

		// find watch
		GameWatch watch = findGameWatch(user, game);
		if (watch == null)
			throw new ApplicationException("not watching");

		// unwatch game
		game.getWatches().remove(watch);
	}

	private boolean isUserWatchingGame(User user, Game game)
	{
		return findGameWatch(user, game) != null;
	}

	private GameWatch findGameWatch(User user, Game game)
	{
		for (GameWatch watch : game.getWatches())
			if (watch.getUser().equals(user))
				return watch;

		return null;
	}

	public List<Game> watchedGames(String userName)
	{
		// load user
		User user = userDao.read(User.class, userName);

		// find watched games
		return gameDao.findGames(null, null, null, user, null, null, null, null, null, 0, Game.PROPERTY_ID, true);
	}

	public int checkExistsBeginnerTournaments()
	{
		int count = 0;

		if (internalCheckExistsBeginnerTournament(Rules.CHESS))
			count++;
		if (internalCheckExistsBeginnerTournament(Rules.CHESS_SUICIDE))
			count++;
		if (internalCheckExistsBeginnerTournament(Rules.CHECKERS))
			count++;

		return count;
	}

	private boolean internalCheckExistsBeginnerTournament(Rules rules)
	{
		if (!gameDao.findGamegroups(GameState.FORMING, null, null, rules, null, Boolean.FALSE, null, "none", null, 0, null).isEmpty())
			return false;

		Rating rating = ratingActivity.assembleRating(rules, RatingClass.ELO, DEFAULT_AID);

		SystemAccount owner = userDao.read(SystemAccount.class, "3moves.net");
		GameGroup game = gameDao.newGameGroup(owner, RequestTime.get(), rules, null, DEFAULT_AID);
		game.setMinPlayers(4);
		game.setMaxPlayers(4);
		game.setClosed(false);
		game.setRequiredRatingMode("none");
		game.setRequiredRating(rating);
		game.setChildClockConstraint("5d+1d*:16d");
		game.setChildRating(rating);
		gameDao.create(game);
		return true;
	}

	public List<UserTitle> userTitles(User user)
	{
		LinkedList<UserTitle> titles = new LinkedList<UserTitle>();
		titles.addAll(subjectRatingDao.findRatingsForSubject(user));
		return titles;
	}
}
