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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class AddResponseHeaderFilterTest
{
	private static final String COMMON_HEADER_NAME = "Cache-Control";
	private static final String COMMON_HEADER_VALUE = "max-age=600";
	private static final String CONTENT_TYPE_HEADER_VALUE = "text/html";
	private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

	private AddResponseHeaderFilter filter;

	@Before
	public void setup()
	{
		filter = new AddResponseHeaderFilter();
	}

	private FilterConfig mockFilterConfig(Map<String, String> headers)
	{
		FilterConfig config = createNiceMock(FilterConfig.class);
		expect(config.getInitParameterNames()).andReturn(Collections.enumeration(headers.keySet()));
		for (Map.Entry<String, String> header : headers.entrySet())
			expect(config.getInitParameter(header.getKey())).andReturn(header.getValue());
		replay(config);
		return config;
	}

	@Test
	public void testFilterChain() throws Exception
	{
		// initialize filter
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(COMMON_HEADER_NAME, COMMON_HEADER_VALUE);
		filter.init(mockFilterConfig(headers));

		HttpServletRequest request = createNiceMock(HttpServletRequest.class);

		HttpServletResponse response = createNiceMock(HttpServletResponse.class);

		FilterChain chain = createMock(FilterChain.class);
		chain.doFilter(eq(request), isA(HttpServletResponseWrapper.class));

		replay(chain);

		filter.doFilter(request, response, chain);

		verify(chain);
	}

	@Test
	public void testSetResponseHeader() throws Exception
	{
		// initialize filter
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(COMMON_HEADER_NAME, COMMON_HEADER_VALUE);
		filter.init(mockFilterConfig(headers));

		HttpServletRequest request = createNiceMock(HttpServletRequest.class);

		HttpServletResponse response = createMock(HttpServletResponse.class);
		response.addHeader(COMMON_HEADER_NAME, COMMON_HEADER_VALUE);

		FilterChain chain = createNiceMock(FilterChain.class);

		replay(response);

		filter.doFilter(request, response, chain);

		verify(response);
	}

	@Test
	public void testSetContentType() throws Exception
	{
		// initialize filter
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
		filter.init(mockFilterConfig(headers));

		HttpServletRequest request = createNiceMock(HttpServletRequest.class);

		HttpServletResponse response = createMock(HttpServletResponse.class);
		response.setContentType(CONTENT_TYPE_HEADER_VALUE);

		FilterChain chain = createNiceMock(FilterChain.class);

		replay(response);

		filter.doFilter(request, response, chain);

		verify(response);
	}
}
