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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This servlet filter allows to add custom HTTP headers to the response. Just define the desired headers as filter
 * init-parameters. Attempts to overwrite specified headers will be blocked.
 * 
 * <p>
 * For Servlet specification 2.4 and above, this filter should be restricted to only execute on
 * &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;.
 * </p>
 * 
 * @author Andreas Schildbach
 */
public class AddResponseHeaderFilter implements Filter
{
	private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";

	private FilterConfig config;

	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// set the provided HTTP response headers
		Set<String> headerNames = new HashSet<String>();
		for (Enumeration<String> e = config.getInitParameterNames(); e.hasMoreElements();)
		{
			String headerName = e.nextElement();
			String headerValue = config.getInitParameter(headerName);

			if (headerName.equals(HEADER_NAME_CONTENT_TYPE))
				httpResponse.setContentType(headerValue);
			else
				httpResponse.addHeader(headerName, headerValue);

			headerNames.add(headerName);
		}

		// pass the request/response on
		chain.doFilter(request, new ResponseWrapper(httpResponse, headerNames));
	}

	private static class ResponseWrapper extends HttpServletResponseWrapper
	{
		private Set<String> headerNames;

		public ResponseWrapper(HttpServletResponse response, Set<String> headerNames)
		{
			super(response);
			this.headerNames = headerNames;
		}

		@Override
		public void setContentType(String type)
		{
			if (!headerNames.contains(HEADER_NAME_CONTENT_TYPE))
				super.setContentType(type);
		}

		@Override
		public void addHeader(String name, String value)
		{
			if (!headerNames.contains(name))
				super.addHeader(name, value);
		}

		@Override
		public void addIntHeader(String name, int value)
		{
			if (!headerNames.contains(name))
				super.addIntHeader(name, value);
		}

		@Override
		public void addDateHeader(String name, long date)
		{
			if (!headerNames.contains(name))
				super.addDateHeader(name, date);
		}
	}

	public void init(FilterConfig config)
	{
		this.config = config;
	}

	public void destroy()
	{
		this.config = null;
	}
}
