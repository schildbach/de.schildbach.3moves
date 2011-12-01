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

/**
 * This is a holder class for the time of the request. The time is stored in a thread local.
 * 
 * @author Andreas Schildbach
 * @see {@link CaptureRequestTimeFilter}
 */
public class RequestTime
{
	private static ThreadLocal<Date> holder = new ThreadLocal<Date>();

	/**
	 * Sets the time of the request. Should only be used by one single entry point, e.g.
	 * CaptureRequestTimeFilter for web requests.
	 * 
	 * @param date
	 *            request time to set
	 */
	public static void set(Date date)
	{
		holder.set(date);
	}

	/**
	 * Convenience method for clearing the request time. This is equivalent to {@link #set(null)}.
	 * Should only be used by one single entry point, e.g. CaptureRequestTimeFilter for web
	 * requests.
	 */
	public static void clear()
	{
		holder.set(null);
	}

	/**
	 * Gets the time of the request. May be used by any request handling component or component
	 * being called by such. (It is debatable wether lower layer comoponents like services or DAOs
	 * should be allowed to rely on a ThreadLocal being set in the calling component, or if the time
	 * should be part of the call parameters.)
	 * 
	 * <p>
	 * The returned Date object must not be modified!
	 * </p>
	 * 
	 * @return request time
	 */
	public static Date get()
	{
		return holder.get();
	}
}
