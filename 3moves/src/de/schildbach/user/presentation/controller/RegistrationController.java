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

package de.schildbach.user.presentation.controller;

import java.security.Principal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.AddressClientData;
import de.schildbach.user.presentation.registration.RegisterUserController;
import de.schildbach.web.RequestTime;
import de.schildbach.web.crypto.EncryptionHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class RegistrationController extends MultiActionController
{
	private EncryptionHelper addressValidationEncryptionHelper;
	private UserService userService;

	public static final String SESSION_ATTRIBUTE_EMAIL = RegistrationController.class.getName() + ".email";
	public static final String SESSION_ATTRIBUTE_VALIDATE = RegistrationController.class.getName() + ".validate";

	@Required
	public void setAddressValidationEncryptionHelper(EncryptionHelper addressValidationEncryptionHelper)
	{
		this.addressValidationEncryptionHelper = addressValidationEncryptionHelper;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public ModelAndView send_password_reminder(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession(false);
		if (session == null)
			return new ModelAndView("error_session_timeout");

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		String email = request.getParameter("email").trim();
		String password = (String) session.getAttribute(RegisterUserController.SESSION_ATTRIBUTE_PASSWORD);

		if (userService.validateEMailAddress(email))
		{
			userService.sendPasswordReminder(user.getName(), email, password);

			session.removeAttribute(RegisterUserController.SESSION_ATTRIBUTE_PASSWORD);
			session.setAttribute(SESSION_ATTRIBUTE_EMAIL, email);
		}

		return new ModelAndView("home");
	}

	public ModelAndView skip_send_password_reminder(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession(false);
		if (session == null)
			return new ModelAndView("error_session_timeout");

		session.removeAttribute(RegisterUserController.SESSION_ATTRIBUTE_PASSWORD);

		return new ModelAndView("home");
	}

	public ModelAndView add_email_address(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		HttpSession session = request.getSession(false);
		if (session == null)
			return new ModelAndView("error_session_timeout");

		Principal remoteUser = request.getUserPrincipal();
		if (remoteUser == null)
			throw new NotAuthorizedException();

		String email = request.getParameter("email").trim();

		if (email.length() == 0)
			return new ModelAndView("home", "error", "empty");

		if (!userService.validateEMailAddress(email))
			return new ModelAndView("home", "error", "invalid_email");

		Date now = RequestTime.get();
		String username = remoteUser.getName();
		AddressClientData data = new AddressClientData(now, username, "email", email);
		String key = new String(Hex.encodeHex(addressValidationEncryptionHelper.encryptToClient(data)));

		userService.requestEmailValidation(username, email, key);

		session.setAttribute(SESSION_ATTRIBUTE_EMAIL, email);
		session.setAttribute(SESSION_ATTRIBUTE_VALIDATE, Boolean.TRUE);

		return new ModelAndView("home");
	}

	public ModelAndView set_referrer(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		String referrer = request.getParameter("referrer").trim();

		userService.changeReferredFrom(user.getName(), referrer);

		return new ModelAndView("home");
	}

	public ModelAndView skip_set_referrer(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		userService.changeReferredFrom(user.getName(), "-");

		return new ModelAndView("home");
	}
}
