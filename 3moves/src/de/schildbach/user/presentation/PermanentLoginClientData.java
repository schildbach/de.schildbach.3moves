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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Date;

import de.schildbach.web.crypto.ClientData;

/**
 * @author Andreas Schildbach
 */
public class PermanentLoginClientData extends ClientData
{
	private String username;

	public PermanentLoginClientData()
	{
	}

	public PermanentLoginClientData(Date createdAt, String username)
	{
		setCreatedAt(createdAt);
		setUsername(username);
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		out.writeByte(1); // version
		out.writeLong(getCreatedAt().getTime());
		out.writeUTF(getUsername());
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		switch (in.readByte())
		{
			case 1:
				setCreatedAt(new Date(in.readLong()));
				setUsername(in.readUTF());
				break;

			default:
				throw new NotSerializableException("unknown version");
		}
	}
}
