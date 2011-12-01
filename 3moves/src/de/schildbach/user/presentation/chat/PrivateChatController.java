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

package de.schildbach.user.presentation.chat;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.portal.service.user.bo.InstantMessage;

/**
 * @author Andreas Schildbach
 */
@Controller
public class PrivateChatController extends SimpleFormController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(PrivateChatController.class);

	private PresenceService presenceService;

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.CHAT);

		return super.isFormSubmission(request);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		Command command = new Command();
		command.setPartner(ServletRequestUtils.getRequiredStringParameter(request, "user"));
		return command;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal loggedInUser = request.getUserPrincipal();
		if (loggedInUser == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;

		String loggedInUserName = loggedInUser.getName();
		String partnerName = command.getPartner();

		List<InstantMessage> allMessages = presenceService.instantMessages(loggedInUser.getName());

		model.put("recipient", command.getPartner());
		List<Row> messages = new LinkedList<Row>();
		for (InstantMessage message : allMessages)
		{
			String senderName = message.getSender();
			String recipientName = message.getRecipient();
			boolean fromPartnerToMe = senderName.equals(partnerName) && recipientName.equals(loggedInUserName);
			boolean fromMeToPartner = senderName.equals(loggedInUserName) && recipientName.equals(partnerName);

			if (fromPartnerToMe || fromMeToPartner)
			{
				Row row = new Row(senderName, message.getText());
				if (!message.isRead() && fromPartnerToMe)
				{
					row.setHighlighted(true);
					message.setRead(true);
					LOG.info("user \"" + loggedInUserName + "\" has read chat message from \"" + senderName + "\"");
				}
				messages.add(row);
			}
		}
		model.put("messages", messages);

		return model;
	}

	@Override
	protected boolean isFormChangeRequest(HttpServletRequest request)
	{
		return true;
	}

	@Override
	protected void onBind(HttpServletRequest request, Object commandObj, BindException errors) throws Exception
	{
		Principal loggedInUser = request.getUserPrincipal();
		Command command = (Command) commandObj;
		if (command.getText() != null)
		{
			boolean success = presenceService.sendInstantMessage(loggedInUser.getName(), command.getPartner(), command.getText());
			if (success)
			{
				command.setText(null);
				LOG.info("user \"" + loggedInUser.getName() + "\" sent chat message to \"" + command.getPartner() + "\"");
			}
			else
			{
				errors.reject("cannot_send", "");
				LOG.info("user \"" + loggedInUser.getName() + "\" could not send chat message to \"" + command.getPartner() + "\"");
			}
		}
	}

	public static class Command implements Serializable
	{
		private String partner;
		private String text;

		public String getPartner()
		{
			return partner;
		}

		public void setPartner(String partner)
		{
			this.partner = partner;
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

	public static class Row
	{
		private String sender;
		private String text;
		private boolean highlighted;

		public Row(String sender, String text)
		{
			setSender(sender);
			setText(text);
		}

		public String getSender()
		{
			return sender;
		}

		public void setSender(String sender)
		{
			this.sender = sender;
		}

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}

		public boolean isHighlighted()
		{
			return highlighted;
		}

		public void setHighlighted(boolean highlighted)
		{
			this.highlighted = highlighted;
		}
	}
}
