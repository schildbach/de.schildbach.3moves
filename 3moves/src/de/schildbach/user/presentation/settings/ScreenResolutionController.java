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

package de.schildbach.user.presentation.settings;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.AuthenticationHelper;
import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class ScreenResolutionController
{
	private UserService userService;
	private AuthenticationHelper authenticationHelper;
	private String view;
	private String successView;
	private List<Integer> availableResolutions;
	private Environment environment;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setAuthenticationHelper(AuthenticationHelper authenticationHelper)
	{
		this.authenticationHelper = authenticationHelper;
	}

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@Required
	public void setSuccessView(String successView)
	{
		this.successView = successView;
	}

	@Required
	public void setAvailableResolutions(List<Integer> availableResolutions)
	{
		this.availableResolutions = availableResolutions;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@ModelAttribute("available_resolutions")
	public List<Integer> availableResolutions() throws Exception
	{
		return availableResolutions;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, ModelMap model)
	{
		Command command = new Command();
		command.setScreenResolution(user.getScreenResolution());

		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(User user, @ModelAttribute Command command, SessionStatus status)
	{
		// change screen resolution
		userService.setScreenResolution(user.getName(), command.getScreenResolution());
		environment.setScreenResolution(command.getScreenResolution());

		// refresh user
		authenticationHelper.refreshLoggedInUser(userService.user(user.getName()));

		status.setComplete();

		return successView;
	}

	public static class Command implements Serializable
	{
		private int screenResolution;

		public void setScreenResolution(int screenResolution)
		{
			this.screenResolution = screenResolution;
		}

		public int getScreenResolution()
		{
			return screenResolution;
		}
	}
}
