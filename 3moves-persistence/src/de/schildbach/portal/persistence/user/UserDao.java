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

package de.schildbach.portal.persistence.user;

import java.util.List;

import de.schildbach.persistence.GenericDao;

/**
 * @author Andreas Schildbach
 */
public interface UserDao extends GenericDao<Subject, String>
{
	User loadUser(String name, boolean fetchRoles);

	Subject findSubject(Class<? extends Subject> subjectClass, String name);

	User findUserCaseInsensitive(String userName, boolean fetchRoles);

	User findUserByOpenId(String openId);

	User findUserByEmail(String email);

	List<User> findUsers(String nameFilter, Role role, String orderBy, int maxResults);

	List<String> findUserNames(String nameStart, int maxResults);
}
