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

package de.schildbach.portal.service.user;

import javax.naming.NamingException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Andreas Schildbach
 */
public class XMPPConnectionFactory
{
	private String host;
	private String username;
	private String password;
	private String resource;
	private XMPPConnection connection;

	@Required
	public void setHost(String host)
	{
		this.host = host;
	}

	@Required
	public void setUsername(String username)
	{
		this.username = username;
	}

	@Required
	public void setPassword(String password)
	{
		this.password = password;
	}

	@Required
	public void setResource(String resource)
	{
		this.resource = resource;
	}
	
	public synchronized XMPPConnection getConnection() throws NamingException, XMPPException
	{
		if (connection != null && connection.isConnected())
			return connection;

		connection = new XMPPConnection(host);
		connection.login(username, password, resource);
/*
		PacketFilter myFilter = new PacketFilter()
		{
			public boolean accept(Packet packet)
			{
				return true;
			}
		};
		PacketListener myListener = new PacketListener()
		{
			public void processPacket(Packet packet)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, packet.toXML());
			}
		};
		connection.addPacketListener(myListener, myFilter);
		
		ConnectionListener myConnectionListener = new ConnectionListener()
		{
			public void connectionClosed()
			{
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, "connection closed");
			}

			public void connectionClosedOnError(Exception x)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, x.toString());
			}
		};
		connection.addConnectionListener(myConnectionListener);
*/
		sleep(5000);
		
		return connection;
	}

	private static void sleep(int millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException x)
		{
		}
	}
}
