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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import de.schildbach.web.crypto.EncryptedCookieHelper;

/**
 * This filter tries to track a user. It determines the time of the first visit, its referer and the
 * resource that is being requested. This is being stored on the client using the supplied
 * EncryptedCookieHelper and can later be associated when the user converts (account or purchase).
 * 
 * <p>
 * In order for the dependency injection into a Servlet filter to work, you need to use Spring's
 * DelegatingFilterProxy or a similar mechanism!
 * </p>
 * 
 * @author Andreas Schildbach
 * @since 1.1
 * @see UserTrackingData
 * @see EncryptedCookieHelper
 * @see org.springframework.web.filter.DelegatingFilterProxy
 */
public class UserTrackingFilter implements Filter
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserTrackingFilter.class);

	private EncryptedCookieHelper encryptedCookieHelper;

	@Required
	public void setEncryptedCookieHelper(EncryptedCookieHelper encryptedCookieHelper)
	{
		this.encryptedCookieHelper = encryptedCookieHelper;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		try
		{
			// is referrer cookie already set?
			if (!encryptedCookieHelper.existsCookie(httpRequest))
			{
				// prepare data object for storage on client
				UserTrackingData data = new UserTrackingData();

				// capture request time, fall-back if necessary
				Date createdAt = RequestTime.get();
				if (createdAt == null)
					createdAt = new Date();
				data.setCreatedAt(createdAt);

				// referred from
				String referrerHeader = (httpRequest).getHeader("Referer"); // misspelled in spec
				if (referrerHeader != null)
				{
					URI referrerFromURI = new URI(referrerHeader);
					URI contextURI = new URI(httpRequest.getScheme(), httpRequest.getServerName(), httpRequest.getContextPath(), null);
					URI relativeReferrerURI = contextURI.relativize(referrerFromURI);

					// referring from outside this application?
					if (relativeReferrerURI.isAbsolute())
					{
						data.setReferredFrom(referrerFromURI.toString());
					}
				}

				// referred to
				StringBuffer referredTo = httpRequest.getRequestURL();
				String queryString = httpRequest.getQueryString();
				if (queryString != null)
					referredTo.append("?").append(queryString);
				data.setReferredTo(referredTo.toString());

				// set cookie
				LOG.debug("setting referrer cookie for " + request.getRemoteAddr() + ": from="
						+ (data.getReferredFrom() != null ? "\"" + data.getReferredFrom() + "\"" : "null") + ", to="
						+ (data.getReferredTo() != null ? "\"" + data.getReferredTo() + "\"" : "null"));
				encryptedCookieHelper.setEncryptedCookie(httpResponse, data, httpRequest.getContextPath());
			}
		}
		catch (URISyntaxException x)
		{
			LOG.warn("could not assemble referrer cookie: " + x);
		}
		catch (GeneralSecurityException x)
		{
			LOG.warn("could not encrypt referrer cookie: " + x);
		}

		// do everything else
		chain.doFilter(request, response);
	}

	public void init(FilterConfig config)
	{
	}

	public void destroy()
	{
	}
}
