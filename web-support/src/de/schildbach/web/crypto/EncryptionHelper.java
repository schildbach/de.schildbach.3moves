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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Required;

/**
 * This helper is useful for encrypting and decrypting sensitive data that needs to be held in a web
 * client (like conversational state).
 * 
 * @author Andreas Schildbach
 * @see {@link ClientData}
 */
public class EncryptionHelper
{
	private String algorithmName;
	private String encryptionKey;

	private SecretKeySpec secretKeySpec;
	private Cipher cipher;

	/**
	 * Sets the name of the symmetric algorithm to use for encryption.
	 * 
	 * @param algorithmName
	 *            name of the algorithm
	 * @throws GeneralSecurityException
	 */
	@Required
	public void setAlgorithmName(String algorithmName) throws GeneralSecurityException
	{
		this.algorithmName = algorithmName;
		this.cipher = Cipher.getInstance(algorithmName);
		trySetSecretKeySpec();
	}

	/**
	 * Sets a string representation of the secret key to use for encryption.
	 * 
	 * @param encryptionKey
	 *            string representation of secret key
	 */
	@Required
	public void setEncryptionKey(String encryptionKey)
	{
		this.encryptionKey = encryptionKey;
		trySetSecretKeySpec();
	}

	private void trySetSecretKeySpec()
	{
		if (algorithmName != null && encryptionKey != null)
			secretKeySpec = new SecretKeySpec(encryptionKey.getBytes(), algorithmName);
	}

	/**
	 * Encrypt data that is about to be sent to the web client.
	 * 
	 * @param data
	 *            plain text object to encrypt
	 * @return encrypted ciphertext
	 * @throws GeneralSecurityException
	 */
	public byte[] encryptToClient(ClientData data) throws GeneralSecurityException
	{
		synchronized (cipher)
		{
			try
			{
				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				CipherOutputStream cos = new CipherOutputStream(baos, cipher);
				DataOutputStream oos = new DataOutputStream(cos);
				data.writeData(oos);
				oos.close();
				return baos.toByteArray();
			}
			catch (IOException x)
			{
				throw new RuntimeException(x);
			}
		}
	}

	/**
	 * Decrypts ciphertext that has just been received from the web client.
	 * 
	 * @param cipherText
	 *            ciphertext to decrypt
	 * @param dataClass
	 *            class of the plain text object
	 * @return plain text object
	 * @throws GeneralSecurityException
	 */
	public <T extends ClientData> T decryptFromClient(byte[] cipherText, Class<T> dataClass) throws GeneralSecurityException
	{
		synchronized (cipher)
		{
			try
			{
				T data = dataClass.newInstance();
				cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
				ByteArrayInputStream bais = new ByteArrayInputStream(cipherText);
				CipherInputStream cis = new CipherInputStream(bais, cipher);
				DataInputStream ois = new DataInputStream(cis);
				data.readData(ois);
				ois.close();
				return data;
			}
			catch (IOException x)
			{
				throw new RuntimeException(x);
			}
			catch (InstantiationException x)
			{
				throw new RuntimeException(x);
			}
			catch (IllegalAccessException x)
			{
				throw new RuntimeException(x);
			}
		}
	}
}
