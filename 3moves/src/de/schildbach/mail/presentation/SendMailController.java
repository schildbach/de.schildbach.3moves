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

package de.schildbach.mail.presentation;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.mail.MailService;
import de.schildbach.portal.service.mail.SendMailStatus;
import de.schildbach.portal.service.mail.exception.InvalidRecipientException;

/**
 * @author Andreas Schildbach
 */
@Controller
public class SendMailController extends SimpleFormController
{
	private MailService mailService;

	@Required
	public void setMailService(MailService mailService)
	{
		this.mailService = mailService;
	}
	
	public SendMailController()
	{
		setBindOnNewForm(true);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		Command command = new Command();
		command.setReplyTo(ServletRequestUtils.getIntParameter(request, "reply_to"));
		return command;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.setAllowedFields(new String[]
		{ "recipient", "subject", "text" });
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;

		if (command.getRecipient() != null)
		{
			SendMailStatus status = mailService.checkSendMail(user.getName(), command.getRecipient());
			if (status == SendMailStatus.INVALID_RECIPIENT)
				errors.rejectValue("recipient", "recipient_invalid");
			else if (status == SendMailStatus.BANNED_SENDER)
				errors.rejectValue("recipient", "sender_banned");
			else if (status == SendMailStatus.OK_BUT_MISSPELLED_RECIPIENT)
				command.setRecipient(mailService.correctUsername(command.getRecipient()));
		}

		return null;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;
		if (command.getRecipient() == null)
			errors.rejectValue("recipient", "recipient_missing");
		if (command.getSubject() == null)
			errors.rejectValue("subject", "subject_missing");
		if (command.getText() == null)
			errors.rejectValue("text", "text_missing");

		if (!errors.hasErrors())
		{
			try
			{
				mailService.sendMail(user.getName(), command.getRecipient(), command.getSubject(), command.getText(), command.getReplyTo());
				return new ModelAndView(getSuccessView());
			}
			catch (InvalidRecipientException x)
			{
			}
		}

		errors.reject("failed");

		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
		private String recipient;

		private String subject;

		private String text;

		private Integer replyTo;

		public String getRecipient()
		{
			return recipient;
		}

		public void setRecipient(String recipient)
		{
			this.recipient = recipient;
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

		public Integer getReplyTo()
		{
			return replyTo;
		}

		public void setReplyTo(Integer replyTo)
		{
			this.replyTo = replyTo;
		}
	}
}
