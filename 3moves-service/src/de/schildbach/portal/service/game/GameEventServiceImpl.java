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

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.SubjectVisitorAdapter;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;
import de.schildbach.portal.service.game.event.GameEvent;
import de.schildbach.portal.service.game.event.GamePlayerActiveEvent;
import de.schildbach.portal.service.game.event.GamePlayerEvent;
import de.schildbach.portal.service.game.event.GamePlayerInactiveDisqualificationEvent;
import de.schildbach.portal.service.game.event.GamePlayerInactiveSystemReminderEvent;
import de.schildbach.portal.service.game.event.GameStateChangedEvent;
import de.schildbach.portal.service.user.event.UserMessageEvent;
import de.schildbach.util.CompareUtils;

/**
 * @author Andreas Schildbach
 */
@Transactional
@Service
public class GameEventServiceImpl implements GameEventService, ApplicationContextAware
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(GameEventServiceImpl.class);

	private UserDao userDao;
	private GameDao gameDao;
	private ApplicationContext ctx;

	@Required
	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	@Required
	public void setGameDao(GameDao gameDao)
	{
		this.gameDao = gameDao;
	}

	@Required
	public void setApplicationContext(ApplicationContext ctx) throws BeansException
	{
		this.ctx = ctx;
	}

	public void onApplicationEvent(ApplicationEvent event)
	{
		if (event instanceof GameEvent)
		{
			handleGameEvent((GameEvent) event);
		}
	}

	/*
	 * Collects all events in a ThreadLocal, because they should be handled at the end of the request.
	 */
	private void handleGameEvent(GameEvent event)
	{
		if (LOG.isDebugEnabled())
			LOG.debug("handling " + event);

		GameEvents.add(event);
	}

	/*
	 * This method is called at the end of each request. Here, all collected requests are being handled.
	 */
	public void processCollectedGameEvents()
	{
		if (!GameEvents.hasEvents())
			return;

		List<GamePlayerInactiveSystemReminderEvent> gamePlayerSystemReminderEvents = new LinkedList<GamePlayerInactiveSystemReminderEvent>();
		for (GameEvent event : GameEvents.events())
		{
			if (event instanceof GamePlayerInactiveSystemReminderEvent)
				gamePlayerSystemReminderEvents.add((GamePlayerInactiveSystemReminderEvent) event);
			else if (event instanceof GamePlayerInactiveDisqualificationEvent)
				handleGamePlayerInactiveDisqualificationEvent((GamePlayerInactiveDisqualificationEvent) event);
			else if (event instanceof GamePlayerActiveEvent)
				handleGamePlayerActiveEvent((GamePlayerActiveEvent) event);
			else if (event instanceof GameStateChangedEvent)
				handleGameStateChangedEvent((GameStateChangedEvent) event);
		}

		GameEvents.clear();

		if (!gamePlayerSystemReminderEvents.isEmpty())
			handleGamePlayerSystemReminderEvents(gamePlayerSystemReminderEvents);
	}

	private void handleGamePlayerActiveEvent(final GamePlayerActiveEvent event)
	{
		final String prefix = "game_player_active.";

		Subject hSubject = userDao.read(event.getSubjectName());
		hSubject.accept(new SubjectVisitorAdapter()
		{
			@Override
			public void visit(User user)
			{
				if (user.getIsActiveNotification())
				{
					SingleGame game = gameDao.read(SingleGame.class, event.getGameId());
					Locale locale = user.getLocale();

					String subject = ctx.getMessage(prefix + "subject", new Object[] { ctx.getMessage(game.getRules().name(), null, locale),
							game.getId() }, locale);
					String text = ctx.getMessage(prefix + "text", new Object[] { user.getName(), game.getLastMoveNotation(),
							ctx.getMessage("url.view_game", new Object[] { game.getId() }, locale), game.getHistoryNotation() }, locale);
					ctx.publishEvent(new UserMessageEvent(this, user.getName(), subject, text));
				}
			}
		});
	}

	private void handleGamePlayerSystemReminderEvents(List<GamePlayerInactiveSystemReminderEvent> events)
	{
		final String prefix = "game_player_inactive_system_reminder.";

		Map<String, List<GamePlayerInactiveSystemReminderEvent>> map = separateByUser(events);
		for (Map.Entry<String, List<GamePlayerInactiveSystemReminderEvent>> entry : map.entrySet())
		{
			User user = userDao.read(User.class, entry.getKey());
			Locale locale = user.getLocale();

			SortedSet<GamePlayerInactiveSystemReminderEvent> sortedSet = new TreeSet<GamePlayerInactiveSystemReminderEvent>(
					new Comparator<GamePlayerInactiveSystemReminderEvent>()
					{
						public int compare(GamePlayerInactiveSystemReminderEvent o1, GamePlayerInactiveSystemReminderEvent o2)
						{
							return CompareUtils.compare(o1.getGameId(), o2.getGameId());
						}
					});
			sortedSet.addAll(entry.getValue());
			StringBuilder gameList = new StringBuilder();
			for (GamePlayerInactiveSystemReminderEvent event : sortedSet)
			{
				gameList.append(ctx.getMessage("url.view_game", new Object[] { new Integer(event.getGameId()) }, locale));
				gameList.append("\n");
			}

			String subject = ctx.getMessage(prefix + "subject", new Object[] { new Integer(sortedSet.size()) }, locale);
			String text = ctx
					.getMessage(prefix + "text", new Object[] { user.getName(), new Integer(sortedSet.size()), gameList.toString() }, locale);
			ctx.publishEvent(new UserMessageEvent(this, user.getName(), subject, text));
		}
	}

	private void handleGamePlayerInactiveDisqualificationEvent(GamePlayerInactiveDisqualificationEvent event)
	{
		Game game = gameDao.read(event.getGameId());

		for (GamePlayer player : game.getPlayers())
		{
			User user = (User) player.getSubject();
			Locale locale = user.getLocale();

			String prefix = "game_player_inactive_disqualification.";
			String gameUrl = ctx.getMessage("url.view_game", new Object[] { new Integer(event.getGameId()) }, locale);
			String subject = ctx.getMessage(prefix + "subject", new Object[] { new Integer(event.getGameId()) }, locale);
			String text = ctx.getMessage(prefix + "text", new Object[] { user.getName(), gameUrl }, locale);

			ctx.publishEvent(new UserMessageEvent(this, user.getName(), subject, text));
		}
	}

	private MultiMap separateByUser(Collection<? extends GamePlayerEvent> events)
	{
		MultiMap map = new MultiHashMap();
		for (GamePlayerEvent event : events)
		{
			map.put(event.getSubjectName(), event);
		}
		return map;
	}

	private void handleGameStateChangedEvent(GameStateChangedEvent event)
	{
		Game game = gameDao.read(event.getGameId());
		if (game instanceof SingleGame)
			notifySingleGameStateChange((SingleGame) game, event.getGameState());
		else if (game instanceof GameGroup)
			notifyGameGroupStateChange((GameGroup) game, event.getGameState());
	}

	private void notifySingleGameStateChange(final SingleGame game, final GameState gameState)
	{
		final String prefix = "singlegame_state_changed.";

		for (final GamePlayer player : game.getPlayers())
		{
			player.getSubject().accept(new SubjectVisitorAdapter()
			{
				@Override
				public void visit(User user)
				{
					Locale locale = user.getLocale();

					String firstActivePlayerText = null;
					if (game.getActivePlayer() != null)
					{
						if (game.getActivePlayer().equals(player))
						{
							firstActivePlayerText = ctx.getMessage(prefix + "first_active_player_you", null, locale) + " ";
						}
						else
						{
							firstActivePlayerText = ctx.getMessage(prefix + "first_active_player", new Object[] { game.getActivePlayer().getSubject()
									.getName() }, locale)
									+ " ";
						}
					}

					String rulesMessage = ctx.getMessage(game.getRules().name(), null, locale);

					String gameUrl = ctx.getMessage("url.view_game", new Object[] { new Integer(game.getId()) }, locale);

					String subject = ctx.getMessage(prefix + gameState + ".subject", new Object[] { rulesMessage, game.getId() }, locale);

					String text = ctx.getMessage(prefix + gameState + ".text", new Object[] { user.getName(), rulesMessage, game.getStartedAt(),
							firstActivePlayerText, gameUrl }, locale);

					ctx.publishEvent(new UserMessageEvent(this, user.getName(), subject, text));
				}
			});
		}
	}

	private void notifyGameGroupStateChange(final GameGroup group, final GameState gameState)
	{
		final String prefix = "gamegroup_state_changed.";

		for (final GamePlayer player : group.getPlayers())
		{
			player.getSubject().accept(new SubjectVisitorAdapter()
			{
				@Override
				public void visit(User user)
				{
					Locale locale = user.getLocale();

					StringBuilder players = new StringBuilder();
					for (GamePlayer gamePlayer : group.getPlayers())
					{
						players.append(gamePlayer.getSubject().getName());
						players.append("\n");
					}

					StringBuilder games = new StringBuilder();
					for (SingleGame game : group.getChildGames())
					{
						List<Subject> subjects = gameDao.getPlayingSubjects(game);
						if (subjects.contains(user))
						{
							subjects.remove(user);
							games.append(ctx.getMessage("url.view_game", new Object[] { new Integer(game.getId()) }, locale));
							games.append(" (");
							games.append(subjects.get(0).getName());
							games.append(")\n");
						}
					}

					String rulesMessage = ctx.getMessage(group.getChildRules().name(), null, locale);

					String gameUrl = ctx.getMessage("url.view_game", new Object[] { new Integer(group.getId()) }, locale);

					String subject = ctx.getMessage(prefix + gameState + ".subject", new Object[] { rulesMessage, group.getId() }, locale);

					String text = ctx.getMessage(prefix + gameState + ".text", new Object[] { user.getName(), rulesMessage, group.getId(),
							group.getStartedAt(), gameUrl, players.toString(), games.toString() }, locale);

					ctx.publishEvent(new UserMessageEvent(this, user.getName(), subject, text));
				}
			});
		}
	}
}
