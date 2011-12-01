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

package de.schildbach.presentation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * @author Andreas Schildbach
 */
public class ReferrerFilter implements Filter
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(ReferrerFilter.class);

	private List<Pattern> ignorePathPatterns;

	public static final String REFERRER = "referrer";

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		try
		{
			// do everything else
			chain.doFilter(request, response);
		}
		finally
		{
			HttpServletRequest httpRequest = (HttpServletRequest) request;

			// check ignore list
			if (ignorePathPatterns != null)
			{
				String pathInfo = httpRequest.getPathInfo();
				String path = httpRequest.getServletPath() + (pathInfo != null ? pathInfo : "");
				for (Pattern pattern : ignorePathPatterns)
					if (pattern.matcher(path).matches())
						return;
			}

			// set "internal" referrer
			HttpSession session = httpRequest.getSession(false);
			if (session != null)
			{
				StringBuffer url = httpRequest.getRequestURL();
				if (httpRequest.getQueryString() != null)
					url.append("?").append(httpRequest.getQueryString());

				LOG.debug("setting " + REFERRER + " attribute \"" + url.toString() + "\" on session " + session.getId());
				session.setAttribute(REFERRER, url.toString());
			}
		}
	}

	public void init(FilterConfig config)
	{
		String param = config.getInitParameter("ignorePaths");
		if (param != null)
		{
			String[] ignorePaths = StringUtils.tokenizeToStringArray(param, ",; \t\n");
			ignorePathPatterns = new LinkedList<Pattern>();
			for (String ignorePath : ignorePaths)
				ignorePathPatterns.add(Pattern.compile(ignorePath));
		}
	}

	public void destroy()
	{
	}
}
