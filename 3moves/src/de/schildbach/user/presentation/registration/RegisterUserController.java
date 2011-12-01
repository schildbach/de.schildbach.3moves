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

package de.schildbach.user.presentation.registration;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.persistence.user.Gender;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserAuthenticationService;
import de.schildbach.portal.service.user.exception.ExistingUserException;
import de.schildbach.user.presentation.AuthenticationHelper;
import de.schildbach.user.presentation.PasswordsValidator;
import de.schildbach.user.presentation.UserNameValidator;
import de.schildbach.web.RequestTime;
import de.schildbach.web.UserTrackingData;
import de.schildbach.web.crypto.EncryptedCookieHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class RegisterUserController extends SimpleFormController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(RegisterUserController.class);

	private UserNameValidator userNameValidator;
	private PasswordsValidator passwordsValidator;
	private UserAuthenticationService userAuthenticationService;
	private AuthenticationHelper authenticationHelper;
	private EncryptedCookieHelper encryptedCookieHelper;

	public static final String SESSION_ATTRIBUTE_PASSWORD = RegisterUserController.class.getName() + ".password";

	@Required
	public void setUserNameValidator(UserNameValidator userNameValidator)
	{
		this.userNameValidator = userNameValidator;
	}

	@Required
	public void setPasswordsValidator(PasswordsValidator passwordsValidator)
	{
		this.passwordsValidator = passwordsValidator;
	}

	@Required
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService)
	{
		this.userAuthenticationService = userAuthenticationService;
	}

	@Required
	public void setAuthenticationHelper(AuthenticationHelper authenticationHelper)
	{
		this.authenticationHelper = authenticationHelper;
	}

	@Required
	public void setEncryptedCookieHelper(EncryptedCookieHelper encryptedCookieHelper)
	{
		this.encryptedCookieHelper = encryptedCookieHelper;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		return new Command();
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("genders", Arrays.asList(Gender.values()));
		return model;
	}

	@Override
	protected void onBindAndValidate(HttpServletRequest request, Object commandObj, BindException errors) throws Exception
	{
		Command command = (Command) commandObj;

		errors.pushNestedPath("name");
		userNameValidator.validate(command.getName(), errors);
		errors.popNestedPath();

		errors.pushNestedPath("passwords");
		passwordsValidator.validate(command.getPasswords(), errors);
		errors.popNestedPath();
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Command command = (Command) commandObj;

		// extract referrer
		Date referrerCreatedAt = null;
		String referredFrom = null;
		String referredTo = null;

		try
		{
			UserTrackingData referrerData = encryptedCookieHelper.getEncryptedCookie(request, UserTrackingData.class);
			if (referrerData != null)
			{
				referrerCreatedAt = referrerData.getCreatedAt();
				referredFrom = referrerData.getReferredFrom();
				referredTo = referrerData.getReferredTo();
			}
		}
		catch (GeneralSecurityException x)
		{
			LOG.warn("cannot decrypt referrer cookie", x);
		}

		String password = command.getPasswords()[0];

		try
		{
			// TimeZone timeZone = null;
			// if (command.getTimezoneOffset() != null)
			// {
			// String id = MessageFormat.format("GMT{0,number,+00;-00}", -(command.getTimezoneOffset() / 60));
			// timeZone = TimeZone.getTimeZone(id);
			// }

			userAuthenticationService.registerUser(command.getName(), password, command.getGender(), request.getLocale(), null, referrerCreatedAt,
					referredFrom, referredTo);
		}
		catch (ExistingUserException x)
		{
			errors.rejectValue("name", "exists");
			return showForm(request, response, errors);
		}

		HttpSession session = request.getSession();
		String userAgent = request.getHeader("user-agent");
		String ip = request.getRemoteAddr();

		User user = userAuthenticationService.loginByName(command.getName(), password, ip, userAgent);
		authenticationHelper.login(request.getRemoteUser(), session, RequestTime.get(), user, ip, userAgent);

		session.setAttribute(SESSION_ATTRIBUTE_PASSWORD, password);

		return new ModelAndView(getSuccessView());
	}

	public static class Command implements Serializable
	{
		private String name;
		private String[] passwords = new String[2];
		private Gender gender;
		private Integer timezoneOffset;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String[] getPasswords()
		{
			return passwords;
		}

		public void setPasswords(String[] passwords)
		{
			this.passwords = passwords;
		}

		public Gender getGender()
		{
			return gender;
		}

		public void setGender(Gender gender)
		{
			this.gender = gender;
		}

		public void setTimezoneOffset(Integer timezoneOffset)
		{
			this.timezoneOffset = timezoneOffset;
		}

		public Integer getTimezoneOffset()
		{
			return timezoneOffset;
		}
	}
}
