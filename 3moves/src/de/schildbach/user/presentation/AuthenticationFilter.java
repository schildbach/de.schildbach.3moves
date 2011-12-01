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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.user.UserAuthenticationService;
import de.schildbach.user.presentation.login.LogoutController;
import de.schildbach.web.RequestTime;
import de.schildbach.web.crypto.EncryptedCookieHelper;

/**
 * This filter maintains authentication, either by session, HTTP simple authentication or by "automatic login" via
 * cookie.
 * 
 * @author Andreas Schildbach
 */
public class AuthenticationFilter implements Filter
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(AuthenticationFilter.class);

	private UserAuthenticationService userAuthenticationService;
	private AuthenticationHelper authenticationHelper;
	private EncryptedCookieHelper permanentLoginCookieHelper;
	private Environment environment;
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
	public void setPermanentLoginCookieHelper(EncryptedCookieHelper permanentLoginCookieHelper)
	{
		this.permanentLoginCookieHelper = permanentLoginCookieHelper;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Required
	public void setAuthentication(Authentication authentication)
	{
		this.authentication = authentication;
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		boolean isLeaving = request.getParameter(LogoutController.LEAVING_USER_PARAMETER_NAME) != null;

		// is user already logged in?
		User user = authentication.getUser();

		if (!isLeaving)
		{
			// basic authentication
			if (user == null)
				user = tryBasicAuthentication(request);

			// authentication cookie
			if (user == null)
				user = tryAuthenticationCookie(request);
		}

		// call next filter
		if (user != null)
			chain.doFilter(new OverrideRemoteUserRequestWrapper(request, user), response);
		else
			chain.doFilter(request, response);
	}

	private User tryAuthenticationCookie(HttpServletRequest request)
	{
		try
		{
			String username = null;

			// get login cookie
			String loginCookie = permanentLoginCookieHelper.getCookie(request);
			if (loginCookie != null)
			{
				LOG.debug("got login cookie");

				// try new cookie
				try
				{
					PermanentLoginClientData cookie = permanentLoginCookieHelper.getEncryptedCookie(request, PermanentLoginClientData.class);
					if (cookie == null)
						return null;

					username = cookie.getUsername();
				}
				catch (GeneralSecurityException x)
				{
					throw new ApplicationException("could not decrypt login cookie", x);
				}
			}
			else
			{
				return null;
			}

			// force session and login
			String useragent = request.getHeader("user-agent");
			String ip = request.getRemoteAddr();
			User user = userAuthenticationService.loginAutomatically(username, ip, useragent);

			if (user.getLocale() != null)
				environment.setLocale(user.getLocale());
			if (user.getScreenResolution() != null)
				environment.setScreenResolution(user.getScreenResolution());

			HttpSession session = request.getSession();
			synchronized (session)
			{
				authenticationHelper.login(request.getRemoteUser(), session, RequestTime.get(), user, ip, useragent);
			}

			return user;
		}
		catch (Exception x)
		{
			LOG.warn("caught exception while trying automatically login user", x);
			return null;
		}
	}

	private User tryBasicAuthentication(HttpServletRequest request)
	{
		// check header
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null)
			return null;

		// check prefix
		if (!authorizationHeader.startsWith("Basic"))
			return null;

		// cut prefix
		authorizationHeader = authorizationHeader.substring(6);

		// decode
		authorizationHeader = new String(Base64.decodeBase64(authorizationHeader.getBytes()));

		// split user and password
		String[] authorization = authorizationHeader.split(":");

		// check password
		String userAgent = request.getHeader("user-agent");
		User user = userAuthenticationService.loginByName(authorization[0], authorization[1], request.getRemoteAddr(), userAgent);
		if (user == null)
			return null;

		return user;
	}

	private static class OverrideRemoteUserRequestWrapper extends HttpServletRequestWrapper
	{
		private User user;
		private Set<String> roles;

		public OverrideRemoteUserRequestWrapper(HttpServletRequest request, User user)
		{
			super(request);

			if (user == null)
				throw new IllegalArgumentException("user must not be null");

			this.user = user;
			this.roles = prepareRoles(user.getUserRoles());
		}

		private Set<String> prepareRoles(Set<UserRole> userRoles)
		{
			Set<String> roles = new HashSet<String>(userRoles.size());
			for (UserRole role : userRoles)
				roles.add(role.getRole().name());
			return Collections.unmodifiableSet(roles);
		}

		@Override
		public Principal getUserPrincipal()
		{
			return user;
		}

		@Override
		public String getRemoteUser()
		{
			return user.getName();
		}

		@Override
		public boolean isUserInRole(String rolename)
		{
			return roles.contains(rolename);
		}
	}

	public void init(FilterConfig config)
	{
	}

	public void destroy()
	{
	}
}
