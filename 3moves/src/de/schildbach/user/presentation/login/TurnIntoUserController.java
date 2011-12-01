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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserAuthenticationService;
import de.schildbach.presentation.ReferrerFilter;
import de.schildbach.user.presentation.AuthenticationHelper;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class TurnIntoUserController extends ParameterizableViewController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(TurnIntoUserController.class);

	private UserAuthenticationService userAuthenticationService;
	private AuthenticationHelper authenticationHelper;

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
		HttpSession session = request.getSession();

		if (!request.isUserInRole(Role.ADMIN.name()))
			throw new NotAuthorizedException();

		Date now = RequestTime.get();
		String userAgent = request.getHeader("user-agent");
		String ip = request.getRemoteAddr();

		String newUserName = ServletRequestUtils.getRequiredStringParameter(request, "user");

		User newUser = userAuthenticationService.turnIntoUser(request.getRemoteUser(), newUserName, ip);

		authenticationHelper.login(request.getRemoteUser(), session, now, newUser, ip, userAgent);

		String internalReferrer = (String) session.getAttribute(ReferrerFilter.REFERRER);
		LOG.debug("got " + ReferrerFilter.REFERRER + " attribute \"" + internalReferrer + "\" from session " + session.getId());
		if (internalReferrer != null)
			return new ModelAndView(new RedirectView(internalReferrer));
		else
			return new ModelAndView(getViewName());
	}
}
