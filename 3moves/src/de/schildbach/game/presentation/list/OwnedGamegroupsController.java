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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;

/**
 * @author Andreas Schildbach
 */
@Controller
public class OwnedGamegroupsController extends ParameterizableViewController
{
	private GameService gameService;
	private PresenceService presenceService;
	private List<Rules> rulesOptions;

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
	public void setRulesOptions(List<Rules> rulesOptions)
	{
		this.rulesOptions = rulesOptions;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_LIST);

		Rules rules = Rules.valueOf(ServletRequestUtils.getStringParameter(request, "rules", "CHESS"));

		List<GameGroup> games = gameService.ownedGamegroups(user.getName(), rules);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("show_create", request.isUserInRole(Role.GAME_ADMIN.name()));
		model.put("games", games);
		model.put("rules", rules);
		model.put("rules_options", rulesOptions);
		return new ModelAndView(getViewName(), model);
	}
}
