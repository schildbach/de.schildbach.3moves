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

package de.schildbach.user.presentation.login;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.service.user.UserAuthenticationService;
import de.schildbach.user.presentation.AuthenticationHelper;
import de.schildbach.user.presentation.registration.RegisterUserController;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class LogoutController extends ParameterizableViewController
{
	private UserAuthenticationService userAuthenticationService;
	private AuthenticationHelper authenticationHelper;

	public static final String LEAVING_USER_PARAMETER_NAME = "leaving";

	@Required
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService)
	{
		this.userAuthenticationService = userAuthenticationService;
	}

	@Required
	public void setAuthenticationHelper(AuthenticationHelper authenticationHelper)
	{
		this.authenticationHelper = authenticationHelper;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		HttpSession session = request.getSession(false);

		if (session != null)
		{
			session.removeAttribute(RegisterUserController.SESSION_ATTRIBUTE_PASSWORD);

			Principal user = request.getUserPrincipal();
			if (user != null)
			{
				Date now = RequestTime.get();
				int onlineTime = authenticationHelper.logout(user.getName(), session, now);
				userAuthenticationService.logoutManually(user.getName(), onlineTime);

				Map<String, Object> model = new HashMap<String, Object>();
				model.put(LEAVING_USER_PARAMETER_NAME, user.getName());
				return new ModelAndView(getViewName(), model);
			}
		}

		return new ModelAndView(getViewName());
	}
}
