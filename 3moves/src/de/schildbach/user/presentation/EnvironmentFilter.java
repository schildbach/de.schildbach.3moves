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
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import de.schildbach.wurflapi.ObjectsManager;
import de.schildbach.wurflapi.WurflSource;

/**
 * Servlet filter to initialize the environment to sane default values.
 * 
 * @author Andreas Schildbach
 */
public class EnvironmentFilter implements Filter
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(EnvironmentFilter.class);

	private static final String WURFL_PATH = "/WEB-INF/wurfl.xml";

	private static final String HEADER_ACCEPT_LANGUAGE = "accept-language";
	private static final String HEADER_USER_AGENT = "user-agent";
	private static final String CAPABILITY_RESOLUTION_WIDTH = "resolution_width";

	private static final int DEFAULT_SCREEN_RESOLUTION = 1280;

	private Environment environment;

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	public void init(final FilterConfig config)
	{
		ObjectsManager.initMyWay(new WurflSource()
		{
			public InputStream getWurflInputStream()
			{
				return config.getServletContext().getResourceAsStream(WURFL_PATH);
			}

			public InputStream getWurflPatchInputStream()
			{
				return null;
			}
		});
	}

	public void destroy()
	{
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) servletRequest;

		if (environment.getLocale() == null)
			environment.setLocale(determineLocale(request));

		if (environment.getScreenResolution() == null)
			environment.setScreenResolution(determineScreenResolution(request));

		// call next filter
		if (environment.getLocale() != null)
			chain.doFilter(new OverrideLocaleRequestWrapper(request, environment.getLocale()), response);
		else
			chain.doFilter(request, response);
	}

	private Locale determineLocale(ServletRequest request)
	{
		// try request
		Locale locale = request.getLocale();
		if (LOG.isDebugEnabled())
			LOG.debug("locale from request: \"" + locale + "\"");

		// default
		if (locale == null)
			locale = Locale.ENGLISH;

		return locale;
	}

	private Integer determineScreenResolution(HttpServletRequest request)
	{
		// try user agent
		String useragent = request.getHeader(HEADER_USER_AGENT);
		String deviceId = ObjectsManager.getUAManagerInstance().getDeviceIDFromUA(useragent);
		if (LOG.isDebugEnabled())
			LOG.debug("deviceId \"" + deviceId + "\" from UA \"" + useragent + "\"");
		if (deviceId.equals("generic"))
			return DEFAULT_SCREEN_RESOLUTION;

		String resolutionWidth = ObjectsManager.getCapabilityMatrixInstance().getCapabilityForDevice(deviceId, CAPABILITY_RESOLUTION_WIDTH);
		if (LOG.isDebugEnabled())
			LOG.debug(CAPABILITY_RESOLUTION_WIDTH + ": " + resolutionWidth);
		if (resolutionWidth == null)
			return DEFAULT_SCREEN_RESOLUTION;
		return Integer.parseInt(resolutionWidth);
	}

	private static class OverrideLocaleRequestWrapper extends HttpServletRequestWrapper
	{
		private Locale locale;

		public OverrideLocaleRequestWrapper(HttpServletRequest request, Locale locale)
		{
			super(request);

			if (locale == null)
				throw new IllegalArgumentException("locale must not be null");

			this.locale = locale;
		}

		@Override
		public Locale getLocale()
		{
			return locale;
		}

		@Override
		public Enumeration<Locale> getLocales()
		{
			List<Locale> list = new LinkedList<Locale>();
			list.add(locale);
			return Collections.enumeration(list);
		}

		@Override
		public String getHeader(String name)
		{
			if (name.equalsIgnoreCase(HEADER_ACCEPT_LANGUAGE))
			{
				return locale.getLanguage();
			}
			else
			{
				return super.getHeader(name);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public Enumeration<String> getHeaders(String name)
		{
			if (name.equalsIgnoreCase(HEADER_ACCEPT_LANGUAGE))
			{
				List<String> list = new LinkedList<String>();
				list.add(locale.getLanguage());
				return Collections.enumeration(list);
			}
			else
			{
				return super.getHeaders(name);
			}
		}
	}
}
