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
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This servlet filter works around a shortcoming of the Servlet specification up to at least
 * version 2.5: There is no way to get the 'official' time the request entered the servlet
 * container. The usual workaround of retrieving the current time by using new Date() as needed has
 * the disadvantage of getting a slightly different value on each invocation.
 * 
 * <p>
 * This filter serves as a single entry point for web requests. It is responsible for capturing the
 * current time once per request and attach it to the current thread as a timestamp.
 * </p>
 * 
 * <p>
 * This filter should execute very early in the filter chain, if not as the first filter. For
 * Servlet specification 2.4 and above, it must be restricted to only execute on
 * &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;! Better yet, use CaptureRequestTimeListener instead
 * </p>
 * 
 * @author Andreas Schildbach
 * @see RequestTime
 * @see CaptureRequestTimeListener
 */
public class CaptureRequestTimeFilter extends CaptureRequestTimeHelper implements Filter
{
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		// get current time
		Date now = currentTime();

		// set request time
		RequestTime.set(now);

		try
		{
			// pass the request/response on
			chain.doFilter(request, response);
		}
		finally
		{
			// clear request time
			RequestTime.clear();
		}
	}

	public void init(FilterConfig config)
	{
	}

	public void destroy()
	{
	}
}
