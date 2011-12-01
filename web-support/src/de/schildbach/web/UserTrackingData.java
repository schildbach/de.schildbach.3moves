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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Date;

import de.schildbach.web.crypto.ClientData;

/**
 * This is the value object for user tracking data.
 * 
 * @author Andreas Schildbach
 * @since 1.1
 * @see UserTrackingFilter
 */
public class UserTrackingData extends ClientData
{
	private String referredFrom;
	private String referredTo;

	public UserTrackingData()
	{
	}

	public UserTrackingData(Date createdAt, String referredFrom, String referredTo)
	{
		setCreatedAt(createdAt);
		setReferredFrom(referredFrom);
		setReferredTo(referredTo);
	}

	public String getReferredFrom()
	{
		return referredFrom;
	}

	public void setReferredFrom(String referrer)
	{
		this.referredFrom = referrer;
	}

	public String getReferredTo()
	{
		return referredTo;
	}

	public void setReferredTo(String referredTo)
	{
		this.referredTo = referredTo;
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		out.writeByte(2);
		out.writeLong(getCreatedAt().getTime());
		out.writeBoolean(getReferredFrom() != null);
		if (getReferredFrom() != null)
			out.writeUTF(getReferredFrom());
		out.writeBoolean(getReferredTo() != null);
		if (getReferredTo() != null)
			out.writeUTF(getReferredTo());
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		switch (in.readByte())
		{
			case 1:
				setCreatedAt(new Date(in.readLong()));
				setReferredFrom(in.readUTF());
				break;

			case 2:
				setCreatedAt(new Date(in.readLong()));
				setReferredFrom(null);
				if (in.readBoolean())
					setReferredFrom(in.readUTF());
				setReferredTo(null);
				if (in.readBoolean())
					setReferredTo(in.readUTF());
				break;

			default:
				throw new NotSerializableException("unknown version");
		}
	}
}
