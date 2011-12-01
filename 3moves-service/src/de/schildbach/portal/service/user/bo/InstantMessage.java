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
import java.util.Date;

/**
 * @author Andreas Schildbach
 */
public class InstantMessage implements Serializable
{
	private Date createdAt;
	private String sender;
	private String recipient;
	private String text;
	private boolean read = false;
	
	public InstantMessage(Date createdAt, String sender, String recipient, String text)
	{
		this.setCreatedAt(createdAt);
		this.setSender(sender);
		this.setRecipient(recipient);
		this.setText(text);
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}
	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}
	
	public String getSender()
	{
		return sender;
	}
	public void setSender(String sender)
	{
		this.sender = sender;
	}
	
	public String getRecipient()
	{
		return recipient;
	}
	public void setRecipient(String recipient)
	{
		this.recipient = recipient;
	}
	
	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	
	public boolean isRead()
	{
		return read;
	}
	public void setRead(boolean read)
	{
		this.read = read;
	}
	
	@Override
	public String toString()
	{
		return getClass().getName() + "[" + createdAt + "," + sender + "->" + recipient + "," + (read ? "read" : "unread") + "]";
	}
}
