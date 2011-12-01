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

package de.schildbach.user.presentation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.springframework.beans.factory.annotation.Required;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.Activity;

/**
 * @author Andreas Schildbach
 */
public class AuthenticationHelper
{
	// don't change value, it is to be used all over the views
	private static final String SESSION_ATTRIBUTE_USER_RELATIONS = "relations";

	// don't change value, it is used from external for logging
	private static final String SESSION_ATTRIBUTE_LOGGED_IN_USER_NAME = "logged_in_user_name";

	private UserService userService;
	private PresenceService presenceService;
	private Authentication authentication;
	private int loggedInSessionTimeout; // in minutes
	private int loggedOutSessionTimeout; // in minutes

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

	@Required
	public void setAuthentication(Authentication authentication)
	{
		this.authentication = authentication;
	}

	@Required
	public void setLoggedInSessionTimeout(int loggedInSessionTimeout)
	{
		this.loggedInSessionTimeout = loggedInSessionTimeout;
	}

	@Required
	public void setLoggedOutSessionTimeout(int loggedOutSessionTimeout)
	{
		this.loggedOutSessionTimeout = loggedOutSessionTimeout;
	}

	public void login(String loggedInUser, HttpSession session, Date at, User user, String loggedInFrom, String userAgent)
	{
		// set authentication
		authentication.setUser(user);
		authentication.setAuthenticatedAt(at);

		// support container based logging
		session.setAttribute(SESSION_ATTRIBUTE_LOGGED_IN_USER_NAME, user.getName());

		// those should move away
		refreshTimeZone(session, user.getTimeZone());
		refreshUserRelations(session, userService.userRelations(user.getName()));

		// change session timeout
		session.setMaxInactiveInterval(60 * loggedInSessionTimeout);

		// resolve ip address
		InetAddress loggedInFromAddress = null;
		try
		{
			loggedInFromAddress = InetAddress.getByName(loggedInFrom);
		}
		catch (UnknownHostException e)
		{
			// silently ignore
		}

		// presence
		if (loggedInUser != null)
			presenceService.logout(loggedInUser, session.getId());
		presenceService.login(user, session.getId(), loggedInFromAddress, userAgent);
		presenceService.setLastActivity(user.getName(), Activity.LOGIN);
	}

	public int logout(String username, HttpSession session, Date at)
	{
		Date loginAt = authentication.getAuthenticatedAt();

		// clear authentication
		authentication.clear();

		// clean up session attributes
		session.removeAttribute(SESSION_ATTRIBUTE_LOGGED_IN_USER_NAME);
		session.removeAttribute(SESSION_ATTRIBUTE_USER_RELATIONS);

		// change session timeout
		session.setMaxInactiveInterval(60 * loggedOutSessionTimeout);

		// presence
		presenceService.logout(username, session.getId());

		// calculate online time
		long onlineTime = at.getTime() - loginAt.getTime();
		return (int) (onlineTime / 1000);
	}

	public void refreshLoggedInUser(User user)
	{
		// make sure roles are loaded
		user.getUserRoles().size();

		authentication.setUser(user);
	}

	/** TODO move to something like an EnvironmentHelper */
	public void refreshTimeZone(HttpSession session, TimeZone timeZone)
	{
		if (session != null)
			Config.set(session, Config.FMT_TIME_ZONE, timeZone);
	}

	/** TODO move to something like a ViewHelper */
	public void refreshUserRelations(HttpSession session, Map<String, String> userRelations)
	{
		if (session != null)
			session.setAttribute(SESSION_ATTRIBUTE_USER_RELATIONS, Collections.unmodifiableMap(userRelations));
	}
}
