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

package de.schildbach.common.presentation.controller;

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
import org.springframework.web.servlet.mvc.AbstractController;

import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.user.presentation.Environment;
import de.schildbach.user.presentation.login.LogoutController;

/**
 * @author Andreas Schildbach
 */
@Controller
public class IndexController extends AbstractController
{
	private int showChessboardThreshold;
	private Environment environment;
	private List<Rules> rulesOptions;
	private String viewNameUser;
	private String viewNameArrivingGuest;
	private String viewNameLeavingGuest;

	@Required
	public void setShowChessboardThreshold(int showChessboardThreshold)
	{
		this.showChessboardThreshold = showChessboardThreshold;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Required
	public void setRulesOptions(List<Rules> rulesOptions)
	{
		this.rulesOptions = rulesOptions;
	}

	@Required
	public void setViewNameUser(String viewNameUser)
	{
		this.viewNameUser = viewNameUser;
	}

	@Required
	public void setViewNameArrivingGuest(String viewNameArrivingGuest)
	{
		this.viewNameArrivingGuest = viewNameArrivingGuest;
	}

	@Required
	public void setViewNameLeavingGuest(String viewNameLeavingGuest)
	{
		this.viewNameLeavingGuest = viewNameLeavingGuest;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String leavingUserName = request.getParameter(LogoutController.LEAVING_USER_PARAMETER_NAME);
		Principal user = request.getUserPrincipal();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("leaving_user_name", leavingUserName);
		model.put("show_chessboard", environment.getScreenResolution() >= showChessboardThreshold);

		if (user != null)
		{
			return new ModelAndView(viewNameUser, model);
		}
		else if (leavingUserName == null)
		{
			String referer = request.getHeader("referer"); // misspelled in spec
			if (referer != null)
				referer = referer.toLowerCase();

			boolean dragonchess = referer != null && referer.contains("dragon");
			boolean antikingchess = referer != null && referer.contains("anti") && referer.contains("king");
			boolean checkers = referer != null && (referer.contains("checkers") || referer.contains("dame"));
			boolean suicide = referer != null && (referer.contains("suicide") || referer.contains("räuber") || referer.contains("raeuber"));

			List<Rules> optimizedRulesOptions;
			if (dragonchess || antikingchess || checkers || suicide)
				optimizedRulesOptions = new LinkedList<Rules>(rulesOptions);
			else
				optimizedRulesOptions = rulesOptions;

			if (dragonchess)
			{
				optimizedRulesOptions.remove(Rules.DRAGONCHESS);
				optimizedRulesOptions.add(0, Rules.DRAGONCHESS);
			}

			if (antikingchess)
			{
				optimizedRulesOptions.remove(Rules.CHESS_ANTIKING);
				optimizedRulesOptions.add(0, Rules.CHESS_ANTIKING);
			}

			if (checkers)
			{
				// optimizedRulesOptions.remove(Rules.CHECKERS_SUICIDE);
				// optimizedRulesOptions.add(0, Rules.CHECKERS_SUICIDE);
				optimizedRulesOptions.remove(Rules.CHECKERS);
				optimizedRulesOptions.add(0, Rules.CHECKERS);
			}

			if (suicide)
			{
				// optimizedRulesOptions.remove(Rules.CHECKERS_SUICIDE);
				// optimizedRulesOptions.add(0, Rules.CHECKERS_SUICIDE);
				optimizedRulesOptions.remove(Rules.CHESS_SUICIDE);
				optimizedRulesOptions.add(0, Rules.CHESS_SUICIDE);
			}

			model.put("rules_options", optimizedRulesOptions);
			return new ModelAndView(viewNameArrivingGuest, model);
		}
		else
		{
			return new ModelAndView(viewNameLeavingGuest, model);
		}
	}
}
