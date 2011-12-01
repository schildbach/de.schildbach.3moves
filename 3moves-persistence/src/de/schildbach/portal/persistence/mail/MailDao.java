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

import java.util.List;

import de.schildbach.persistence.GenericDao;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
public interface MailDao extends GenericDao<Mail, Integer>
{
	List<Mail> findMails(User sender, User recipient, Boolean deletedBySender, Boolean deletedByRecipient, String orderBy);

	int countUnreadMail(User user);
}
