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

import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import de.schildbach.web.CookieHelper;

/**
 * CookieHelper for encrypted cookies.
 * 
 * @author Andreas Schildbach
 * @see {@link CookieHelper}
 * @see {@link EncryptionHelper}
 */
public class EncryptedCookieHelper extends CookieHelper
{
	private EncryptionHelper encryptionHelper;
	private Base64 base64 = new Base64();

	public void setEncryptionHelper(EncryptionHelper encryptionHelper)
	{
		this.encryptionHelper = encryptionHelper;
	}

	public void setEncryptedCookie(HttpServletResponse response, ClientData data, String contextPath) throws GeneralSecurityException
	{
		setCookie(response, new String(base64.encode(encryptionHelper.encryptToClient(data))), contextPath);
	}

	public <T extends ClientData> T getEncryptedCookie(HttpServletRequest request, Class<T> dataClass) throws GeneralSecurityException
	{
		String cookieValue = getCookie(request);

		if (cookieValue == null)
			return null;

		return encryptionHelper.decryptFromClient(base64.decode(cookieValue.getBytes()), dataClass);
	}
}
