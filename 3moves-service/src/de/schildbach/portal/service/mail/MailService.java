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

import java.util.List;

import de.schildbach.portal.persistence.mail.Mail;
import de.schildbach.portal.service.mail.exception.InvalidRecipientException;

/**
 * @author Andreas Schildbach
 */
public interface MailService
{
	SendMailStatus checkSendMail(String username, String recipientName);

	String correctUsername(String username);

	int sendMail(String username, String recipientName, String subject, String text, Integer replyTo) throws InvalidRecipientException;

	Mail readMail(String username, int id);

	void deleteMail(String username, int id);

	List<Mail> outbox(String username);

	List<Mail> inbox(String username);

	int countUnreadMail(String username);

	void setImportant(String username, int id, boolean isImportant);
}
