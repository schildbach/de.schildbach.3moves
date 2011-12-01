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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.util.UrlPathHelper;

import de.schildbach.portal.service.exception.NotAuthorizedException;

/**
 * @author Andreas Schildbach
 */
public class RequestOnlyExceptionResolver extends SimpleMappingExceptionResolver
{
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception x)
	{
		if (request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE) == null)
		{
			ModelAndView modelAndView = super.resolveException(request, response, handler, x);

			if (modelAndView != null)
				log(x);

			return modelAndView;
		}
		else
		{
			return null;
		}
	}

	private void log(Exception x)
	{
		// reduce log file flooding
		if (x instanceof NotAuthorizedException)
			x.setStackTrace(new StackTraceElement[] { x.getStackTrace()[0] });

		// log
		logger.error("handled exception", x);
	}
}
