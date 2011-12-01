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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class CookieHelperTest
{
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String CONTEXT_PATH = "/context_path";
	private static final int MAX_AGE = 1000;

	private CookieHelper cookieHelper;

	@Before
	public void setup()
	{
		cookieHelper = new CookieHelper();
		cookieHelper.setCookieName(NAME);
		cookieHelper.setCookieMaxAge(MAX_AGE);
	}

	@Test
	public void testSetCookie()
	{
		Cookie cookie = new Cookie(NAME, VALUE);
		cookie.setMaxAge(MAX_AGE);

		HttpServletResponse response = createMock(HttpServletResponse.class);
		response.addCookie(eqCookie(cookie));

		replay(response);

		cookieHelper.setCookie(response, VALUE, CONTEXT_PATH);

		verify(response);
	}

	@Test
	public void testClearCookie()
	{
		Cookie cookie = new Cookie(NAME, "");
		cookie.setMaxAge(0);

		HttpServletResponse response = createMock(HttpServletResponse.class);
		response.addCookie(eqCookie(cookie));

		replay(response);

		cookieHelper.clearCookie(response, CONTEXT_PATH);

		verify(response);
	}

	@Test
	public void testGetCookie()
	{
		HttpServletRequest request = createMock(HttpServletRequest.class);
		Cookie[] cookies = new Cookie[] { new Cookie("bad1", "bad1"), new Cookie(NAME, VALUE), new Cookie("bad2", "bad2") };
		expect(request.getCookies()).andReturn(cookies);

		replay(request);

		assertEquals(VALUE, cookieHelper.getCookie(request));

		verify(request);
	}

	@Test
	public void testExistsCookie()
	{
		HttpServletRequest request = createMock(HttpServletRequest.class);
		Cookie[] cookies = new Cookie[] { new Cookie("bad1", "bad1"), new Cookie(NAME, VALUE), new Cookie("bad2", "bad2") };
		expect(request.getCookies()).andReturn(cookies);

		replay(request);

		assertTrue(cookieHelper.existsCookie(request));

		verify(request);
	}

	@Test
	public void testDoesNotExistCookie()
	{
		HttpServletRequest request = createMock(HttpServletRequest.class);
		Cookie[] cookies = new Cookie[] { new Cookie("bad1", "bad1"), new Cookie("bad2", "bad2") };
		expect(request.getCookies()).andReturn(cookies);

		replay(request);

		assertFalse(cookieHelper.existsCookie(request));

		verify(request);
	}

	private static Cookie eqCookie(Cookie expected)
	{
		EasyMock.reportMatcher(new CookieEquals(expected));
		return null;
	}

	private static class CookieEquals implements IArgumentMatcher
	{
		private Cookie expected;

		public CookieEquals(Cookie expected)
		{
			this.expected = expected;
		}

		public boolean matches(Object actual)
		{
			if (!(actual instanceof Cookie))
				return false;

			Cookie actualCookie = (Cookie) actual;

			if (!actualCookie.getName().equals(expected.getName()))
				return false;

			if (!actualCookie.getValue().equals(expected.getValue()))
				return false;

			if (actualCookie.getMaxAge() != expected.getMaxAge())
				return false;

			return true;
		}

		public void appendTo(StringBuffer buffer)
		{
			buffer.append("eqCookie(");
			buffer.append(expected.getClass().getName());
			buffer.append(" with name=\"");
			buffer.append(expected.getName());
			buffer.append("\", value=\"");
			buffer.append(expected.getValue());
			buffer.append("\", maxAge=");
			buffer.append(expected.getMaxAge());
			buffer.append(")");
		}
	}
}
