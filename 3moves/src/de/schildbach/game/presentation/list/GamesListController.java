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

package de.schildbach.game.presentation.list;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameHelper;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.GameVisitorAdapter;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.presentation.DateUtils;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class GamesListController extends ParameterizableViewController
{
	@Override
	protected ModelAndView handleRequestInternal(final HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final Date now = RequestTime.get();
		final Principal user = request.getUserPrincipal();

		Collection<Game> games = (Collection<Game>) request.getAttribute("games");
		String columns = request.getParameter("columns");

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("columns", columns);
		model.put("show_header", ServletRequestUtils.getBooleanParameter(request, "header", true));

		// always available
		final Map<Game, String> gameClass = new HashMap<Game, String>();
		final Map<Game, Rules> rules = new HashMap<Game, Rules>();
		final Map<Game, Aid> aids = new HashMap<Game, Aid>();
		final Map<Game, String> rating = new HashMap<Game, String>();
		final Map<Game, String> clock = new HashMap<Game, String>();

		for (Game game : games)
		{
			game.accept(new GameVisitor()
			{
				public void visit(SingleGame singleGame)
				{
					gameClass.put(singleGame, "single_game");
					rules.put(singleGame, singleGame.getRules());
					aids.put(singleGame, singleGame.getAid());
					if (singleGame.getRating() != null && singleGame.getRating().ratingClass().equals("ELO"))
						rating.put(singleGame, "elo");
					if (singleGame.getClockConstraint() != null)
						clock.put(singleGame, singleGame.getClockConstraint());
				}

				public void visit(GameGroup gameGroup)
				{
					gameClass.put(gameGroup, "gamegroup");
					rules.put(gameGroup, gameGroup.getChildRules());
					aids.put(gameGroup, gameGroup.getChildAid());
					if (gameGroup.getChildRating() != null && gameGroup.getChildRating().ratingClass().equals("ELO"))
						rating.put(gameGroup, "elo");
					if (gameGroup.getChildClockConstraint() != null)
						clock.put(gameGroup, gameGroup.getChildClockConstraint());
				}
			});
		}

		model.put("class", gameClass);
		model.put("clock", clock);
		model.put("rules", rules);
		model.put("aids", aids);
		model.put("rating", rating);

		// available for specific columns
		for (StringTokenizer t = new StringTokenizer(columns, ","); t.hasMoreTokens();)
		{
			String column = t.nextToken();

			if (column.equals("players") || column.equals("players-turn"))
			{
				Map<Game, Boolean> canTakeMorePlayersMap = new HashMap<Game, Boolean>();
				Map<Game, Boolean> displayWithVs = new HashMap<Game, Boolean>();
				for (Game game : games)
				{
					int numPlayers = game.getPlayers().size();
					boolean canTakeMorePlayers = game.getState() == GameState.FORMING && numPlayers < GameRulesHelper.maxPlayers(game);
					canTakeMorePlayersMap.put(game, canTakeMorePlayers);
					displayWithVs.put(game, !canTakeMorePlayers && numPlayers == 2);
				}
				model.put("can_take_more_players", canTakeMorePlayersMap);
				model.put("display_with_vs", displayWithVs);
			}
			else if (column.equals("last_active_at"))
			{
				final List<String> lastActiveAt = new LinkedList<String>();
				for (Game game : games)
				{
					game.accept(new GameVisitor()
					{
						public void visit(SingleGame game)
						{
							if (game.getLastActiveAt() != null && game.getState() != GameState.FINISHED)
								lastActiveAt.add(DateUtils.dateDiffShort(game.getLastActiveAt(), now, request.getLocale(), false));
							else
								lastActiveAt.add("");
						}

						public void visit(GameGroup group)
						{
							lastActiveAt.add(null);
						}
					});
				}
				model.put("last_active_at", lastActiveAt);
			}
			else if (column.equals("remaining_clock"))
			{
				final Map<Game, String> remainingClock = new HashMap<Game, String>();
				for (final Game game : games)
				{
					game.accept(new GameVisitorAdapter()
					{
						@Override
						public void visit(SingleGame singleGame)
						{
							// find "myself"
							GamePlayer self = null;
							for (GamePlayer player : singleGame.getPlayers())
							{
								if (player.getSubject().equals(user))
								{
									self = player;
									break;
								}
							}

							// calculate remaining clock
							if (self != null && self.getClock() != null)
							{
								long elapsed = (self.equals(singleGame.getActivePlayer())) ? now.getTime() - singleGame.getLastActiveAt().getTime()
										: 0;
								long remaining = self.getClock().longValue() - elapsed;
								remainingClock.put(singleGame, DateUtils.dateDiffShort(Math.max(remaining, 0), request.getLocale(), false));
							}
						}
					});
				}
				model.put("remaining_clock", remainingClock);
			}
			else if (column.equals("active_player") || column.equals("full_state"))
			{
				final Map<Game, Boolean> activePlayerYou = new HashMap<Game, Boolean>();
				for (Game game : games)
				{
					game.accept(new GameVisitorAdapter()
					{
						@Override
						public void visit(SingleGame singleGame)
						{
							GamePlayer activePlayer = singleGame.getActivePlayer();
							activePlayerYou.put(singleGame, activePlayer != null && user != null && activePlayer.getSubject().equals(user));
						}
					});
				}
				model.put("active_player_you", activePlayerYou);
			}
			else if (column.equals("resolution"))
			{
				final Map<Game, Boolean> winnerYou = new HashMap<Game, Boolean>();
				for (Game game : games)
				{
					if (game.getResolution() != null)
					{
						GamePlayer winner = game.getWinner();
						winnerYou.put(game, winner != null && user != null && winner.getSubject().equals(user));
					}
				}
				model.put("winner_you", winnerYou);
			}
			else if (column.equals("required_rating"))
			{
				final Map<Game, String> requiredRating = new HashMap<Game, String>();
				for (Game game : games)
				{
					String mode = game.getRequiredRatingMode();
					if (mode.equals("none"))
						requiredRating.put(game, "none");
					else if (mode.equals("range") && game.getRequiredRatingMin() != null && game.getRequiredRatingMax() != null)
						requiredRating.put(game, "exists");
					else if (mode.equals("range") || mode.equals("none_or_range"))
					{
						if (game.getRequiredRating().ratingClass().equals("ELO"))
							requiredRating.put(game, "elo");
					}
				}
				model.put("required_rating", requiredRating);
			}
		}

		List<Boolean> canJoins = (List<Boolean>) request.getAttribute("can_join");

		if (request.getParameter("row_class") != null)
		{
			Collection<String> rowClasses = new LinkedList<String>();
			int i = 0;
			for (Game game : games)
			{
				StringBuilder builder = new StringBuilder();
				for (StringTokenizer t = new StringTokenizer(request.getParameter("row_class"), ","); t.hasMoreTokens();)
				{
					String rowClass = t.nextToken();
					if (rowClass.equals("light_dark"))
					{
						builder.append(i % 2 == 0 ? "dark " : "light ");
					}
					else if (rowClass.equals("active"))
					{
						GameHelper gameHelper = new GameHelper(game);
						if (user != null && game.getState() == GameState.RUNNING && gameHelper.isSingleGame())
						{
							GamePlayer activePlayer = gameHelper.getSingleGame().getActivePlayer();
							if (activePlayer != null && activePlayer.getSubject().equals(user))
								builder.append("active ");
						}
					}
					else if (rowClass.equals("can_join") && canJoins != null)
					{
						if (canJoins.get(i))
							builder.append("can_join ");
					}
				}
				rowClasses.add(builder.toString());
				i++;
			}
			model.put("row_classes", rowClasses);
		}

		return new ModelAndView(getViewName(), model);
	}
}
