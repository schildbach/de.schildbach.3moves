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
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class DisableUrlRewritingFilterTest
{
	private static final String URL = "http://schildbach.de";

	private DisableUrlRewritingFilter filter;

	@Before
	public void setup()
	{
		filter = new DisableUrlRewritingFilter();
		filter.init(createNiceMock(FilterConfig.class));
	}

	@Test
	public void testFilterChain() throws Exception
	{
		HttpServletRequest request = createNiceMock(HttpServletRequest.class);

		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		FilterChain chain = createMock(FilterChain.class);
		chain.doFilter(eq(request), isA(HttpServletResponseWrapper.class));

		replay(chain);

		filter.doFilter(request, response, chain);

		verify(chain);
	}

	@Test
	public void testEncodeURL() throws Exception
	{
		HttpServletRequest request = createNiceMock(HttpServletRequest.class);

		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		FilterChain chain = new FilterChain()
		{
			@SuppressWarnings("deprecation")
			public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
			{
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				assertEquals(URL, httpResponse.encodeURL(URL));
				assertEquals(URL, httpResponse.encodeRedirectURL(URL));
				assertEquals(URL, httpResponse.encodeUrl(URL));
				assertEquals(URL, httpResponse.encodeRedirectUrl(URL));
			}
		};

		filter.doFilter(request, response, chain);
	}

	@Test
	public void testInvalidateSession() throws Exception
	{
		HttpSession session = createMock(HttpSession.class);
		session.invalidate();

		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.isRequestedSessionIdFromURL()).andReturn(true);
		expect(request.getSession()).andReturn(session).anyTimes();

		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		FilterChain chain = createMock(FilterChain.class);

		replay(session, request);

		filter.doFilter(request, response, chain);

		verify(session, request);
	}

	@Test
	public void testDontInvalidateSession() throws Exception
	{
		HttpSession session = createMock(HttpSession.class);

		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.isRequestedSessionIdFromURL()).andReturn(false);
		expect(request.getSession()).andReturn(session).anyTimes();

		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		FilterChain chain = createMock(FilterChain.class);

		replay(session, request);

		filter.doFilter(request, response, chain);

		verify(session, request);
	}
}
