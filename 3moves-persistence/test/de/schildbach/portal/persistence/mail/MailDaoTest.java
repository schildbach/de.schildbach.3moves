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

package de.schildbach.portal.persistence.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.schildbach.portal.persistence.AbstractDaoTest;

/**
 * @author Andreas Schildbach
 */
public class MailDaoTest extends AbstractDaoTest
{
	private static final String CONTENT_TYPE = "text/plain";

	@Autowired
	private MailDao mailDao;

	@Test
	public void shouldCreateReadAndDelete()
	{
		Mail mail = new Mail(new Date(), user1, user2);
		mail.setContentType(CONTENT_TYPE);

		int count = countRowsInTable(Mail.TABLE_NAME);

		int id = mailDao.create(mail);
		flushSession();

		assertEquals(++count, countRowsInTable(Mail.TABLE_NAME));
		assertFalse(id == 0);

		mail = mailDao.read(Mail.class, id);

		mailDao.delete(mail);
		flushSession();

		assertEquals(--count, countRowsInTable(Mail.TABLE_NAME));
	}

	@Test
	public void countUnreadMail()
	{
		// create 2 unread mails to user2
		Mail mail = new Mail(new Date(), user1, user2);
		mail.setContentType(CONTENT_TYPE);
		mailDao.create(mail);
		mail = new Mail(new Date(), user1, user2);
		mail.setContentType(CONTENT_TYPE);
		mailDao.create(mail);

		// create 1 read mails to user2
		mail = new Mail(new Date(), user1, user2);
		mail.setContentType(CONTENT_TYPE);
		mail.setRead(true);
		mailDao.create(mail);

		// asserts
		assertEquals(0, mailDao.countUnreadMail(user1));
		assertEquals(2, mailDao.countUnreadMail(user2));
	}
}
