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

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class GameOptionsController
{
	private UserService userService;
	private String view;
	private String successView;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
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

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, ModelMap model)
	{
		Command command = new Command();
		command.setAutoMove(user.isAutoMove());

		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(User user, @ModelAttribute Command command, SessionStatus status)
	{
		userService.setGameOptions(user.getName(), command.isAutoMove());

		status.setComplete();

		return successView;
	}

	public static class Command implements Serializable
	{
		private boolean autoMove;

		public boolean isAutoMove()
		{
			return autoMove;
		}

		public void setAutoMove(boolean autoMove)
		{
			this.autoMove = autoMove;
		}
	}
}
