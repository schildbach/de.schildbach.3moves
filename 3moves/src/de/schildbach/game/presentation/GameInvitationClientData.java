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

package de.schildbach.game.presentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Date;

import de.schildbach.web.crypto.ClientData;

/**
 * @author Andreas Schildbach
 */
public class GameInvitationClientData extends ClientData
{
	private String invitingUsername;
	private int gameId;

	public GameInvitationClientData()
	{
	}

	public GameInvitationClientData(Date createdAt, String invitingUsername, int gameId)
	{
		setCreatedAt(createdAt);
		setInvitingUsername(invitingUsername);
		setGameId(gameId);
	}

	public String getInvitingUsername()
	{
		return invitingUsername;
	}

	public void setInvitingUsername(String invitingUsername)
	{
		this.invitingUsername = invitingUsername;
	}

	public int getGameId()
	{
		return gameId;
	}

	public void setGameId(int gameId)
	{
		this.gameId = gameId;
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		out.writeByte(1); // version
		out.writeLong(getCreatedAt().getTime());
		out.writeUTF(getInvitingUsername());
		out.writeInt(getGameId());
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		switch (in.readByte())
		{
			case 1:
				setCreatedAt(new Date(in.readLong()));
				setInvitingUsername(in.readUTF());
				setGameId(in.readInt());
				break;

			default:
				throw new NotSerializableException("unknown version");
		}
	}
}
