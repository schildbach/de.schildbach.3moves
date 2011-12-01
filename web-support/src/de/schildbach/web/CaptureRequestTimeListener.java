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

import java.util.Date;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * This listener works around a shortcoming of the Servlet specification up to at least version 2.5:
 * There is no way to get the 'official' time the request entered the servlet container. The usual
 * workaround of retrieving the current time by using new Date() as needed has the disadvantage of
 * getting a slightly different value on each invocation.
 * 
 * @author Andreas Schildbach
 * @see RequestTime
 * @see CaptureRequestTimeFilter
 * @since 1.1
 */
public class CaptureRequestTimeListener extends CaptureRequestTimeHelper implements ServletRequestListener
{
	public void requestInitialized(ServletRequestEvent event)
	{
		// get current time
		Date now = currentTime();

		// set request time
		RequestTime.set(now);
	}

	public void requestDestroyed(ServletRequestEvent event)
	{
		// clear request time
		RequestTime.clear();
	}
}
