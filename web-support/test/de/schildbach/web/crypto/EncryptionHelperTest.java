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

package de.schildbach.web.crypto;

import static org.junit.Assert.assertEquals;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class EncryptionHelperTest
{
	private static final String STR = "test";

	private EncryptionHelper encryptionHelper;

	@Before
	public void setup() throws Exception
	{
		encryptionHelper = new EncryptionHelper();
		encryptionHelper.setAlgorithmName("DES");
		encryptionHelper.setEncryptionKey("01234567");
	}

	@Test
	public void test() throws Exception
	{
		ClientTestData data = new ClientTestData();
		data.setStr(STR);
		byte[] cipherText = encryptionHelper.encryptToClient(data);
		ClientTestData returnedData = encryptionHelper.decryptFromClient(cipherText, ClientTestData.class);
		assertEquals(data, returnedData);
	}

	protected static class ClientTestData extends ClientData
	{
		private String str;

		public void setStr(String str)
		{
			this.str = str;
		}

		public String getStr()
		{
			return str;
		}

		@Override
		public void writeData(DataOutput out) throws IOException
		{
			out.writeUTF(str);
		}

		@Override
		public void readData(DataInput in) throws IOException
		{
			str = in.readUTF();
		}

		@Override
		public boolean equals(Object obj)
		{
			return str.equals(((ClientTestData) obj).str);
		}

		@Override
		public int hashCode()
		{
			return str.hashCode();
		}
	}
}
