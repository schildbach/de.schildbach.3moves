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

package de.schildbach.portal.service.mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schildbach.portal.persistence.mail.Mail;
import de.schildbach.portal.persistence.mail.MailDao;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.mail.exception.InvalidRecipientException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Transactional
@Service
public class MailServiceImpl implements MailService
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(MailServiceImpl.class);

	private UserDao userDao;
	private UserService userService;
	private MailDao mailDao;

	@Required
	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setMailDao(MailDao mailDao)
	{
		this.mailDao = mailDao;
	}

	public SendMailStatus checkSendMail(String username, String recipientName)
	{
		if (username == null)
			return SendMailStatus.NOT_AUTHORIZED;

		if (recipientName == null)
			return SendMailStatus.INVALID_RECIPIENT;

		Subject recipient = userDao.findUserCaseInsensitive(recipientName, false);
		if (recipient == null)
			return SendMailStatus.INVALID_RECIPIENT;

		if (userService.isBannedBy(username, recipient.getName()))
			return SendMailStatus.BANNED_SENDER;

		if (recipient.getName().equalsIgnoreCase(recipientName) && !recipient.getName().equals(recipientName))
			return SendMailStatus.OK_BUT_MISSPELLED_RECIPIENT;

		return SendMailStatus.OK;
	}

	public String correctUsername(String username)
	{
		Subject user = userDao.findUserCaseInsensitive(username, false);
		return user.getName();
	}

	private boolean canSendMail(String username, String recipientName)
	{
		SendMailStatus status = checkSendMail(username, recipientName);
		if (status == SendMailStatus.OK || status == SendMailStatus.OK_BUT_MISSPELLED_RECIPIENT)
			return true;

		return false;
	}

	public int sendMail(String username, String recipientName, String subject, String text, Integer replyTo) throws InvalidRecipientException
	{
		Date now = RequestTime.get();

		if (!canSendMail(username, recipientName))
			throw new InvalidRecipientException();

		// load objects
		User user = userDao.read(User.class, username);
		Subject recipient = userDao.findUserCaseInsensitive(recipientName, false);

		// send mail
		Mail mail = new Mail(now, user, recipient);
		mail.setSubject(subject);
		mail.setContentType("text/plain");
		try
		{
			mail.setContent(text.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException x)
		{
			throw new ApplicationException(x);
		}

		mailDao.create(mail);
		LOG.info("user \"" + user.getName() + "\" sent mail to \"" + recipient.getName() + "\"");

		// set replied to
		if (replyTo != null)
		{
			Mail repliedMail = mailDao.read(replyTo.intValue());
			repliedMail.setRepliedTo(true);
		}

		return mail.getId();
	}

	public Mail readMail(String username, int id)
	{
		// load objects
		User user = userDao.read(User.class, username);
		Mail mail = mailDao.read(id);

		// can user read mail?
		if (!mail.getSender().equals(user) && !mail.getRecipient().equals(user))
			throw new NotAuthorizedException("mail");

		// read mail
		if (mail.getRecipient().equals(user))
			mail.setRead(true);

		return mail;
	}

	public void deleteMail(String username, int id)
	{
		// load objects
		User user = userDao.read(User.class, username);
		Mail mail = mailDao.read(id);

		// delete mail
		if (mail.getSender().equals(user))
			mail.setDeletedBySender(true);
		if (mail.getRecipient().equals(user))
			mail.setDeletedByRecipient(true);
		if (mail.isDeletedByRecipient() && mail.isDeletedBySender())
			mailDao.delete(mail);
	}

	public List<Mail> inbox(String username)
	{
		// load user
		User user = userDao.read(User.class, username);

		// get inbox
		return mailDao.findMails(null, user, null, Boolean.FALSE, "!" + Mail.PROPERTY_CREATED_AT);
	}

	public List<Mail> outbox(String username)
	{
		// load user
		User user = userDao.read(User.class, username);

		// get outbox
		return mailDao.findMails(user, null, Boolean.FALSE, null, "!" + Mail.PROPERTY_CREATED_AT);
	}

	public int countUnreadMail(String username)
	{
		// load user
		User user = userDao.read(User.class, username);

		// count unread mail
		return mailDao.countUnreadMail(user);
	}

	public void setImportant(String username, int id, boolean isImportant)
	{
		// load objects
		User user = userDao.read(User.class, username);
		Mail mail = mailDao.read(id);

		// can user set important?
		if (!mail.getRecipient().equals(user))
			throw new NotAuthorizedException();

		// set important
		mail.setImportant(isImportant);
	}
}
