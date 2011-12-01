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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;

/**
 * @author Andreas Schildbach
 */
@Controller
public class OpenGameInvitationsController extends ParameterizableViewController
{
	private GameService gameService;
	private PresenceService presenceService;
	private String viewNameGamegroup;
	private String linkCreateGame;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Required
	public void setViewNameGamegroup(String viewNameGamegroups)
	{
		this.viewNameGamegroup = viewNameGamegroups;
	}

	@Required
	public void setLinkCreateGame(String linkCreateGame)
	{
		this.linkCreateGame = linkCreateGame;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();

		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_LIST);

		if (user != null)
			model.put("is_logged_in", true);

		String rules = request.getParameter("rules");
		if ("".equals(rules))
			rules = null;
		model.put("rules", rules);

		Class<? extends Game> gameClass = null;
		String viewName = null;
		if ("gamegroup".equals(rules))
		{
			gameClass = GameGroup.class;
			rules = null;
			viewName = viewNameGamegroup;
		}
		else
		{
			gameClass = SingleGame.class;
			viewName = getViewName();
		}

		List<Game> games = gameService.openGameInvitations(user != null ? user.getName() : null, gameClass, rules, 0);
		model.put("games", games);

		if (user != null)
		{
			List<Boolean> canJoin = new LinkedList<Boolean>();
			for (Game game : games)
			{
				canJoin.add(gameService.canJoinGame(user.getName(), game.getId()));
			}
			model.put("can_join", canJoin);
		}

		model.put("link_create_game", request.getContextPath() + linkCreateGame);

		return new ModelAndView(viewName, model);
	}
}
