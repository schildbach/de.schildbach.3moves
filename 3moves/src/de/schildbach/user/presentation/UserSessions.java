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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserAuthenticationService;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
public class UserSessions implements HttpSessionListener
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserSessions.class);

	private static final String SESSION_ACTIVATION_LISTENER = UserSessions.class.getName() + ".session_activation_listener";
	private static int sessionCount = 0;

	private UserAuthenticationService userAuthenticationService;
	private AuthenticationHelper authenticationHelper;
	private Authentication authentication;

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

	@Required
	public void setAuthentication(Authentication authentication)
	{
		this.authentication = authentication;
	}

	public void sessionCreated(HttpSessionEvent sessionEvent)
	{
		HttpSession session = sessionEvent.getSession();

		session.setAttribute(SESSION_ACTIVATION_LISTENER, new MySessionActivationListener());

		sessionCount++;
	}

	public void sessionDestroyed(HttpSessionEvent sessionEvent)
	{
		HttpSession session = sessionEvent.getSession();

		session.removeAttribute(SESSION_ACTIVATION_LISTENER);

		// this workaround is needed because the servlet specification does not specify if
		// session listener callbacks may be called from a request handling thread or not

		// save context
		Date enclosingNow = RequestTime.get();
		RequestAttributes enclosingRequestAttributes = RequestContextHolder.getRequestAttributes();

		try
		{
			// set temporary now
			RequestTime.set(new Date());

			// set session context for session scoped beans
			MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
			mockHttpServletRequest.setSession(session);
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

			// handle event
			handleSessionDestroyed(sessionEvent);
		}
		finally
		{
			// restore context
			RequestTime.set(enclosingNow);
			RequestContextHolder.setRequestAttributes(enclosingRequestAttributes);
		}
	}

	private void handleSessionDestroyed(HttpSessionEvent sessionEvent)
	{
		HttpSession session = sessionEvent.getSession();

		User user = authentication.getUser();
		if (user != null)
		{
			String username = user.getName();
			Date lastAccessedTime = new Date(session.getLastAccessedTime());
			LOG.info("user \"" + username + "\": session timed out (last accessed: " + lastAccessedTime + ")");

			// logout
			synchronized (session)
			{
				int onlineTime = authenticationHelper.logout(username, session, RequestTime.get());
				userAuthenticationService.logoutAutomatically(username, onlineTime);
			}
		}
		else
		{
			LOG.info("guest session timed out");
		}

		sessionCount--;
		LOG.info("number of monitored sessions: " + sessionCount);

		Enumeration<?> attributes = session.getAttributeNames();
		if (attributes.hasMoreElements())
			LOG.info("attributes at session destroy: " + Collections.list(attributes));
	}

	private static class MySessionActivationListener implements HttpSessionActivationListener, Serializable
	{
		public void sessionDidActivate(HttpSessionEvent sessionEvent)
		{
			sessionCount++;
		}

		public void sessionWillPassivate(HttpSessionEvent sessionEvent)
		{
			sessionCount--;
		}
	}

	public static int numberOfSessions()
	{
		return sessionCount;
	}
}
