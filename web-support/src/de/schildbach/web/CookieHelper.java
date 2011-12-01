/*
 * Copyright 2007 the original author or authors.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.schildbach.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * This helper is useful for setting, getting and clearing HTTP cookies to/from remote HTTP clients. Cookie parameters
 * are only configured once per helper instance, which can then be used to access the same cookie unlimited times.
 * 
 * @author Andreas Schildbach
 * @see {@link Cookie}
 */
public class CookieHelper
{
	@SuppressWarnings("unused")
	protected static final Log LOG = LogFactory.getLog(CookieHelper.class);

	private String cookieName;
	private String cookieComment;
	private String cookieDomain;
	private int cookieMaxAge = 60 * 60 * 24 * 365; // 1 year
	private boolean cookieSecure = false;

	@Required
	public void setCookieName(String cookieName)
	{
		this.cookieName = cookieName;
	}

	public void setCookieComment(String cookieComment)
	{
		this.cookieComment = cookieComment;
	}

	public void setCookieDomain(String cookieDomain)
	{
		this.cookieDomain = cookieDomain;
	}

	public void setCookieMaxAge(int cookieMaxAge)
	{
		this.cookieMaxAge = cookieMaxAge;
	}

	public void setCookieSecure(boolean cookieSecure)
	{
		this.cookieSecure = cookieSecure;
	}

	/**
	 * Sets cookie to remote HTTP client.
	 * 
	 * @param response
	 *            HTTP response that goes to the client
	 * @param value
	 *            cookie value to be set
	 * @param contextPath
	 *            context path that should be used, or null if none
	 */
	public void setCookie(HttpServletResponse response, String value, String contextPath)
	{
		value = urlEncode(value);

		Cookie cookie = new Cookie(cookieName, value);
		if (cookieComment != null)
			cookie.setComment(cookieComment);
		if (cookieDomain != null)
			cookie.setDomain(cookieDomain);
		cookie.setMaxAge(cookieMaxAge);
		if (contextPath != null)
			cookie.setPath(adaptContextPath(contextPath));
		cookie.setSecure(cookieSecure);

		LOG.debug("setting cookie \"" + cookie.getName() + "\" to value \"" + cookie.getValue() + "\" for domain "
				+ (cookie.getDomain() != null ? "\"" + cookie.getDomain() + "\"" : "NULL") + " and path "
				+ (cookie.getPath() != null ? "\"" + cookie.getPath() + "\"" : "NULL"));

		response.addCookie(cookie);
	}

	/**
	 * Clears cookie from the remote HTTP client.
	 * 
	 * @param response
	 *            HTTP response that goes to the client
	 * @param contextPath
	 *            context path that should be used, or null if none
	 */
	public void clearCookie(HttpServletResponse response, String contextPath)
	{
		Cookie cookie = new Cookie(cookieName, "");
		cookie.setMaxAge(0);
		if (contextPath != null)
			cookie.setPath(adaptContextPath(contextPath));

		LOG.debug("clearing cookie \"" + cookie.getName() + "\" for path \"" + cookie.getPath() + "\"");

		response.addCookie(cookie);
	}

	/**
	 * Gets a cookie from the remote HTTP client.
	 * 
	 * @param request
	 *            HTTP request that comes from the client
	 * @return cookie value, or null if the cookie does not exist
	 */
	public String getCookie(HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();

		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				if (cookie.getName().equals(cookieName))
				{
					String value = cookie.getValue();

					value = urlDecode(value);
					LOG.debug("got cookie \"" + cookie.getName() + "\" with value \"" + value + "\"");

					return value;
				}
			}
		}

		return null;
	}

	/**
	 * Convenience method for checking if a cookie exists on the remote HTTP client.
	 * 
	 * @param request
	 *            HTTP request that comes from the client
	 * @return true if the cookie exists
	 */
	public boolean existsCookie(HttpServletRequest request)
	{
		return getCookie(request) != null;
	}

	private String adaptContextPath(String contextPath)
	{
		if (contextPath.equals(""))
			return "/";
		else
			return contextPath;
	}

	private String urlEncode(String value)
	{
		try
		{
			return URLEncoder.encode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// cannot happen
			throw new RuntimeException(e);
		}
	}

	private String urlDecode(String value)
	{
		try
		{
			return URLDecoder.decode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// cannot happen
			throw new RuntimeException(e);
		}
	}
}
