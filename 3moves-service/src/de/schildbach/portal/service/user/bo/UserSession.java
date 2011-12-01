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

package de.schildbach.portal.service.user.bo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

public class UserSession implements Serializable
{
	String sessionId;
	InetAddress loggedInFrom;
	Date loggedInAt;
	String userAgent;

	public UserSession(String sessionId)
	{
		setSessionId(sessionId);
	}

	private void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setLoggedInFrom(InetAddress loggedInFrom)
	{
		this.loggedInFrom = loggedInFrom;
	}

	public InetAddress getLoggedInFrom()
	{
		return loggedInFrom;
	}

	public void setLoggedInAt(Date loggedInAt)
	{
		this.loggedInAt = loggedInAt;
	}

	public Date getLoggedInAt()
	{
		return loggedInAt;
	}

	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	public String getUserAgent()
	{
		return userAgent;
	}

	@Override
	public boolean equals(Object obj)
	{
		UserSession other = (UserSession) obj;
		return this.sessionId.equals(other.sessionId);
	}

	@Override
	public int hashCode()
	{
		return this.sessionId.hashCode();
	}
}
