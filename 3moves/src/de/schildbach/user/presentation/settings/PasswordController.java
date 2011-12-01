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

package de.schildbach.user.presentation.settings;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.PasswordsValidator;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class PasswordController
{
	private PasswordsValidator passwordsValidator;
	private UserService userService;
	private String view;
	private String successView;

	@Required
	public void setPasswordsValidator(PasswordsValidator passwordsValidator)
	{
		this.passwordsValidator = passwordsValidator;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@Required
	public void setSuccessView(String successView)
	{
		this.successView = successView;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@ModelAttribute("show_old_password")
	public boolean showOldPassword(User user)
	{
		return user.hasPassword();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(ModelMap model)
	{
		model.addAttribute(new Command());
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(User user, @ModelAttribute Command command, BindingResult result, SessionStatus status)
	{
		if (user.hasPassword())
			ValidationUtils.rejectIfEmpty(result, "oldPassword", "missing");

		result.pushNestedPath("passwords");
		passwordsValidator.validate(command.getPasswords(), result);
		result.popNestedPath();

		if (result.hasErrors())
		{
			return view;
		}
		else
		{
			boolean success = userService.setPassword(user.getName(), command.getOldPassword(), command.getPasswords()[0]);

			if (!success)
			{
				result.rejectValue("oldPassword", "incorrect");
				return view;
			}
			else
			{
				status.setComplete();
				return successView;
			}
		}
	}

	public static class Command implements Serializable
	{
		private String oldPassword;
		private String[] passwords = new String[2];

		public String getOldPassword()
		{
			return oldPassword;
		}

		public void setOldPassword(String oldPassword)
		{
			this.oldPassword = oldPassword;
		}

		public String[] getPasswords()
		{
			return passwords;
		}

		public void setPasswords(String[] passwords)
		{
			this.passwords = passwords;
		}
	}
}
