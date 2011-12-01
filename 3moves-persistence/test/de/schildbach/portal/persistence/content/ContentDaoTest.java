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

package de.schildbach.portal.persistence.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.schildbach.portal.persistence.AbstractDaoTest;

/**
 * @author Andreas Schildbach
 */
public class ContentDaoTest extends AbstractDaoTest
{
	@Autowired
	private ContentDao contentDao;

	@Test
	public void shouldCreateReadAndDelete() throws UnknownHostException
	{
		Content content = new Content(new Date(), user1, InetAddress.getLocalHost());
		content.setContentType("text/plain");

		int count = countRowsInTable(Content.TABLE_NAME);

		int id = contentDao.create(content);
		flushSession();

		assertEquals(++count, countRowsInTable(Content.TABLE_NAME));
		assertFalse(id == 0);

		content = contentDao.read(Content.class, id);

		contentDao.delete(content);
		flushSession();

		assertEquals(--count, countRowsInTable(Content.TABLE_NAME));
	}
}
