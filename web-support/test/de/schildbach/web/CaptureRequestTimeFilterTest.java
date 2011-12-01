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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class CaptureRequestTimeFilterTest
{
	private CaptureRequestTimeFilter filter;

	@Before
	public void setup()
	{
		filter = new CaptureRequestTimeFilter();
		filter.init(createNiceMock(FilterConfig.class));
	}

	@Test
	public void testFilterChain() throws Exception
	{
		HttpServletRequest request = createNiceMock(HttpServletRequest.class);

		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		FilterChain chain = createMock(FilterChain.class);
		chain.doFilter(request, response);

		replay(chain);

		filter.doFilter(request, response, chain);

		verify(chain);
	}

	@Test
	public void testCaptureRequestTime() throws Exception
	{
		HttpServletRequest request = createNiceMock(HttpServletRequest.class);
		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		Date start = new Date();

		MyFilterChain chain = new MyFilterChain();
		filter.doFilter(request, response, chain);

		Date end = new Date();

		assertNotNull(chain.inbetween);
		assertTrue(start.before(chain.inbetween) || start.equals(chain.inbetween));
		assertTrue(end.after(chain.inbetween) || end.equals(chain.inbetween));

		assertNull(RequestTime.get());
	}

	private static class MyFilterChain implements FilterChain
	{
		public Date inbetween;

		public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
		{
			inbetween = RequestTime.get();
		}
	}
}
