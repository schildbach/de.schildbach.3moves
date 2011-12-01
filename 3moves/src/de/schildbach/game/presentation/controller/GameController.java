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

package de.schildbach.game.presentation.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class GameController extends MultiActionController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(GameController.class);

	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	public ModelAndView remind_active_player_form(HttpServletRequest request, HttpServletResponse response)
	{
		return new ModelAndView("remind_active_player.jspx", "game_id", request.getParameter("game_id"));
	}

	public ModelAndView remind_active_player(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		int gameId = Integer.parseInt(request.getParameter("game_id"));
		String text = request.getParameter("text");

		gameService.remindActivePlayer(user.getName(), gameId, text);

		model.put("id", new Integer(gameId));

		return new ModelAndView("game", model);
	}
}
