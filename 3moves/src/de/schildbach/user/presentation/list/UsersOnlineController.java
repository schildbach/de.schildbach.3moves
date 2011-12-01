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

package de.schildbach.user.presentation.list;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UsersOnlineController extends ParameterizableViewController
{
	private UserService userService;
	private PresenceService presenceService;

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
		Collection<User> users = presenceService.loggedInUsers();

		SortedSet<User> result = new TreeSet<User>();
		for (User user : users)
			result.add(userService.user(user.getName()));

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("users", result);
		model.put("last_accessed_at", presenceService.lastAccessedAt());
		return new ModelAndView(getViewName(), model);
	}
}
