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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

/**
 * This servlet filter works around a shortcoming of the Servlet specification up to at least
 * version 2.5: Session tracking by URL Rewriting cannot be disabled. You may want to do that
 * because of security concerns (e.g. Session hijacking) or just to keep URLs tidy.
 * 
 * <p>
 * If you add this class to the servlet filter chain, all calls to
 * {@link javax.servlet.http.HttpServletResponse#encodeURL(String)},
 * {@link javax.servlet.http.HttpServletResponse#encodeRedirectURL(String)} and their deprecated
 * counterparts are overridden so that the URL is returned unchanged. Also, if it is detected that a
 * session id is passed in by URL ("jsessionid"), the creation of a valid session is prevented for
 * the current request.
 * </p>
 * 
 * <p>
 * This filter should execute early in the chain; certainly before any other filter has the chance
 * to call any of the overridden methods.
 * </p>
 * 
 * @author Andreas Schildbach
 */
public class DisableUrlRewritingFilter implements Filter
{
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// invalidate session that comes from url-encoded session id
		if (httpRequest.isRequestedSessionIdFromURL())
		{
			HttpSession session = httpRequest.getSession();
			if (session != null)
				session.invalidate();
		}

		// invoke next filter in the chain
		chain.doFilter(request, new ResponseWrapper(httpResponse));
	}

	private static class ResponseWrapper extends HttpServletResponseWrapper
	{
		public ResponseWrapper(HttpServletResponse response)
		{
			super(response);
		}

		@Override
		public String encodeUrl(String url)
		{
			return url;
		}

		@Override
		public String encodeURL(String url)
		{
			return url;
		}

		@Override
		public String encodeRedirectUrl(String url)
		{
			return url;
		}

		@Override
		public String encodeRedirectURL(String url)
		{
			return url;
		}
	}

	public void init(FilterConfig config)
	{
	}

	public void destroy()
	{
	}
}
