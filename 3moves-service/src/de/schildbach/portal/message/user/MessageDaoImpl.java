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

package de.schildbach.portal.message.user;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Repository;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.XMPPConnectionFactory;
import de.schildbach.util.TextWrapper;

/**
 * @author Andreas Schildbach
 */
@Repository
public class MessageDaoImpl implements MessageDao
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(MessageDaoImpl.class);

	private MailSender mailSender;
	private MailMessage mailMessage;
	private XMPPConnectionFactory xmppConnectionFactory;

	@Required
	public void setMailSender(MailSender mailSender)
	{
		this.mailSender = mailSender;
	}

	@Required
	public void setMailMessage(MailMessage mailMessage)
	{
		this.mailMessage = mailMessage;
	}

	@Required
	public void setXmppConnectionFactory(XMPPConnectionFactory xmppConnectionFactory)
	{
		this.xmppConnectionFactory = xmppConnectionFactory;
	}

	public void sendEmail(String fromName, String fromAddr, String toAddr, String subject, String text)
	{
		SimpleMailMessage msg = new SimpleMailMessage((SimpleMailMessage) this.mailMessage);
		if (fromName != null && fromAddr != null)
			msg.setFrom(fromName + " <" + fromAddr + ">");
		msg.setTo(toAddr);
		msg.setSubject(subject);
		msg.setText(text);
		try
		{
			mailSender.send(msg);
		}
		catch (MailException x)
		{
			LOG.warn("exception while sending mail", x);
		}
	}

	public void sendInstantMessage(String type, String address, String subject, String text)
	{
		try
		{
			if (type.equals("xmpp"))
			{
				XMPPConnection connection = xmppConnectionFactory.getConnection();
				Chat chat = connection.createChat(address);
				Message message = chat.createMessage();
				message.setSubject(subject);
				message.setBody(text);
				chat.sendMessage(message);
			}
			else if (type.equals("icq"))
			{
				XMPPConnection connection = xmppConnectionFactory.getConnection();
				Chat chat = connection.createChat(address + "@" + "icq.amessage.info");
				Message message = chat.createMessage();
				message.setSubject(subject);
				message.setBody(text);
				chat.sendMessage(message);
			}
			else if (type.equals("msn"))
			{
				XMPPConnection connection = xmppConnectionFactory.getConnection();
				address = address.replace('@', '%') + "@msn.gate.amessage.de";
				Chat chat = connection.createChat(address);
				Message message = chat.createMessage();
				message.setSubject(subject);
				message.setBody(text);
				chat.sendMessage(message);
			}
			else
			{
				throw new IllegalArgumentException(type);
			}
		}
		catch (NamingException x)
		{
			x.printStackTrace();
		}
		catch (XMPPException x)
		{
			x.printStackTrace();
		}
	}

	public void sendMessageAllChannels(User user, String subject, String text)
	{
		if (user.getEmail() != null)
		{
			if (LOG.isInfoEnabled())
				LOG.info("sending message to " + user.getName() + " via email: " + subject);
			sendEmail(null, null, user.getEmail(), subject, TextWrapper.wrap(text, 70));
		}

		if (user.getXmpp() != null)
		{
			if (LOG.isInfoEnabled())
				LOG.info("sending message to " + user.getName() + " via xmpp: " + subject);
			sendInstantMessage("xmpp", user.getXmpp(), subject, text);
		}
	}
}
