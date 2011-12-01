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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.Activity;

/**
 * @author Andreas Schildbach
 */
@Controller
public class JoinedGamesController extends AbstractController
{
	private String viewNameSingleGames;
	private String viewNameGamegroups;
	private String viewNameNotAllowed;
	private GameService gameService;
	private UserService userService;
	private PresenceService presenceService;

	@Required
	public void setViewNameSingleGames(String viewNameSingleGames)
	{
		this.viewNameSingleGames = viewNameSingleGames;
	}

	@Required
	public void setViewNameGamegroups(String viewNameGamegroups)
	{
		this.viewNameGamegroups = viewNameGamegroups;
	}

	@Required
	public void setViewNameNotAllowed(String viewNameNotAllowed)
	{
		this.viewNameNotAllowed = viewNameNotAllowed;
	}

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_LIST);

		String target = request.getParameter("subject");
		if (target == null)
			target = user.getName();
		model.put("target", target);

		String rules = request.getParameter("rules");
		if ("".equals(rules))
			rules = null;
		model.put("rules", rules);

		boolean isSelf = target.equals(user.getName());
		model.put("self", isSelf);

		SubjectRelation relation = userService.subjectRelation(target, user.getName());
		if (isSelf || (relation != null && relation.isFriend() && relation.getConfirmed() != null && relation.getConfirmed().booleanValue())
				|| request.isUserInRole(Role.ADMIN.name()))
		{
			Class<? extends Game> gameClass = null;
			String viewName = null;
			if ("gamegroup".equals(rules))
			{
				gameClass = GameGroup.class;
				viewName = viewNameGamegroups;
				rules = null;
			}
			else
			{
				gameClass = SingleGame.class;
				viewName = viewNameSingleGames;
			}

			List<Game> games = gameService.joinedGames(target, gameClass, rules, null);
			List<Game> gamesWaiting = new LinkedList<Game>();
			List<Game> gamesRunning = new LinkedList<Game>();
			List<Game> gamesFinished = new LinkedList<Game>();
			for (Game game : games)
			{
				GameState state = game.getState();
				if (state == GameState.FORMING || state == GameState.READY || state == GameState.UNACCOMPLISHED)
					gamesWaiting.add(game);
				else if (state == GameState.RUNNING)
					gamesRunning.add(game);
				else if (state == GameState.FINISHED)
					gamesFinished.add(game);
			}

			Collections.sort(gamesWaiting, new Comparator<Game>()
			{
				public int compare(Game g1, Game g2)
				{
					Date startAt1 = g1.getStartedAt();
					Date startAt2 = g2.getStartedAt();
					if (startAt1 == null && startAt2 == null)
						return 0;
					if (startAt1 == null && startAt2 != null)
						return -1;
					if (startAt1 != null && startAt2 == null)
						return 1;
					return startAt1.compareTo(startAt2);
				}
			});

			Collections.sort(gamesFinished, new Comparator<Game>()
			{
				public int compare(Game g1, Game g2)
				{
					Date finishAt1 = g1.getFinishedAt();
					Date finishAt2 = g2.getFinishedAt();
					if (finishAt1 == null && finishAt2 == null)
						return 0;
					if (finishAt1 == null && finishAt2 != null)
						return 1;
					if (finishAt1 != null && finishAt2 == null)
						return -1;
					return -finishAt1.compareTo(finishAt2);
				}
			});

			if (isSelf)
				model.put("games_waiting", gamesWaiting);
			model.put("games_running", gamesRunning);
			model.put("games_finished", gamesFinished);

			return new ModelAndView(viewName, model);
		}
		else
		{
			return new ModelAndView(viewNameNotAllowed, model);
		}
	}
}
