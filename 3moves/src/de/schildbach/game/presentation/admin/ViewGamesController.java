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

package de.schildbach.game.presentation.admin;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ViewGamesController extends ParameterizableViewController
{
	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		if (!request.isUserInRole(Role.ADMIN.name()))
			throw new NotAuthorizedException();

		String orderBy = ServletRequestUtils.getStringParameter(request, "order_by", "!" + Game.PROPERTY_ID);

		List<? extends Game> games = gameService.allGames(200, orderBy);

		return new ModelAndView(getViewName(), "games", games);
	}
}
