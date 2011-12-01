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

package de.schildbach.portal.service.content;

import java.net.InetAddress;
import java.util.List;

import de.schildbach.portal.persistence.content.Content;
import de.schildbach.portal.service.content.exception.ExistingContentTagException;

/**
 * @author Andreas Schildbach
 */
public interface ContentService
{
	Content content(int id);

	Content contentByTag(String tag);

	boolean canReadContent(String userName, int id);

	Content readContent(String userName, int id);

	int postMessage(String userName, int parentId, String subject, String text, InetAddress ip);

	boolean canDeleteMessage(String userName, int id);

	int deleteMessage(String userName, int id);

	int createForum(String userName, String tag, String name, String description, InetAddress ip) throws ExistingContentTagException;

	List<Content> newsFeed(int maxResults);
}
