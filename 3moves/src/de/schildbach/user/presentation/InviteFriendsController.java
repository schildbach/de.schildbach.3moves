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

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.util.TextWrapper;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class InviteFriendsController
{
	private EmailValidator emailValidator;
	private UserService userService;
	private String view;
	private String successView;

	@Required
	public void setEmailValidator(EmailValidator emailValidator)
	{
		this.emailValidator = emailValidator;
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

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, Model model)
	{
		if (user == null)
			throw new NotAuthorizedException();

		Command command = new Command();
		command.setFromName(user.getFullName());
		command.setFromAddr(user.getEmail());

		String[] text = userService.inviteFriendText(user.getName());
		command.setSubject(text[0]);
		command.setText(text[1]);

		model.addAttribute(command);
		model.addAttribute("static_text", TextWrapper.wrap(text[2], 80));

		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(User user, @ModelAttribute Command command, BindingResult result, SessionStatus status)
	{
		if (user == null)
			throw new NotAuthorizedException();

		String fromName = command.getFromName();
		if (fromName == null)
			result.rejectValue("fromName", "missing");

		String fromAddr = command.getFromAddr();
		if (fromAddr == null)
		{
			result.rejectValue("fromAddr", "missing");
		}
		else
		{
			result.pushNestedPath("fromAddr");
			emailValidator.validate(fromAddr, result);
			result.popNestedPath();
		}

		String[] toAddr = command.getToAddr();
		boolean anyToAddr = false;
		for (int i = 0; i < toAddr.length; i++)
		{
			if (toAddr[i] != null)
			{
				result.pushNestedPath("toAddr[" + i + "]");
				emailValidator.validate(toAddr[i], result);
				result.popNestedPath();

				anyToAddr = true;
			}
		}

		if (!anyToAddr)
			result.reject("missing_to_addr");

		String subject = command.getSubject();

		String text = command.getText();

		if (result.hasErrors())
		{
			return view;
		}
		else
		{
			for (int i = 0; i < toAddr.length; i++)
				if (toAddr[i] != null)
					userService.inviteFriend(user.getName(), fromName, fromAddr, toAddr[i], subject, text);

			status.setComplete();
			return successView;
		}
	}

	public static class Command implements Serializable
	{
		private String fromName;
		private String fromAddr;
		private String[] toAddr = new String[3];
		private String subject;
		private String text;

		public String getFromName()
		{
			return fromName;
		}

		public void setFromName(String fromName)
		{
			this.fromName = fromName;
		}

		public String getFromAddr()
		{
			return fromAddr;
		}

		public void setFromAddr(String fromAddr)
		{
			this.fromAddr = fromAddr;
		}

		public String[] getToAddr()
		{
			return toAddr;
		}

		public void setToAddr(String[] toAddr)
		{
			this.toAddr = toAddr;
		}

		public String getSubject()
		{
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}
	}
}
