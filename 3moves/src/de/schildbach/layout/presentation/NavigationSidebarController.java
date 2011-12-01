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

package de.schildbach.layout.presentation;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.user.Role;

/**
 * @author Andreas Schildbach
 */
@Controller
public class NavigationSidebarController extends ParameterizableViewController
{
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("is_logged_in", user != null);

		Map<String, Boolean> auth = new HashMap<String, Boolean>();
		auth.put("", true);
		auth.put("none", user == null);
		auth.put("user", request.isUserInRole(Role.USER.name()));
		auth.put("sponsor", request.isUserInRole(Role.SPONSOR.name()));
		auth.put("betatester", request.isUserInRole(Role.BETATESTER.name()));
		auth.put("game_admin", request.isUserInRole(Role.GAME_ADMIN.name()));
		auth.put("admin", request.isUserInRole(Role.ADMIN.name()));
		model.put("auth", auth);

		model.put("language", request.getLocale().getLanguage());

		return new ModelAndView(getViewName(), model);
	}
}
