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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.Environment;
import de.schildbach.user.presentation.controller.RegistrationController;
import de.schildbach.user.presentation.registration.RegisterUserController;

/**
 * @author Andreas Schildbach
 */
@Controller
public class IndexUserController extends ParameterizableViewController
{
	private int showMenuThreshold;
	private Environment environment;
	private UserService userService;

	@Required
	public void setShowMenuThreshold(int showMenuThreshold)
	{
		this.showMenuThreshold = showMenuThreshold;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		// refresh user
		User hUser = userService.user(user.getName());

		HttpSession session = request.getSession(false);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user_name", user.getName());
		model.put("show_reminder", session != null && session.getAttribute(RegisterUserController.SESSION_ATTRIBUTE_PASSWORD) != null);
		String email = (String) session.getAttribute(RegistrationController.SESSION_ATTRIBUTE_EMAIL);
		if (email != null)
			model.put("email", email);
		model.put("validate", session.getAttribute(RegistrationController.SESSION_ATTRIBUTE_VALIDATE));
		model.put("show_contact", hUser.getEmail() == null && hUser.getXmpp() == null);
		model.put("show_referrer", hUser.getReferredFrom() == null);
		model.put("show_personal_details", hUser.getFullName() == null && hUser.getCity() == null && hUser.getBirthday() == null);
		model.put("show_menu", environment.getScreenResolution() >= showMenuThreshold);
		return new ModelAndView(getViewName(), model);
	}
}
