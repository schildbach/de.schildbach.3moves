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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schildbach.portal.persistence.content.Content;
import de.schildbach.portal.persistence.content.ContentDao;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;
import de.schildbach.portal.service.content.exception.ExistingContentTagException;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Transactional
@Service
public class ContentServiceImpl implements ContentService
{
	private static final String TRASH_NODE_TAG = "trash";

	private UserDao userDao;
	private ContentDao contentDao;

	@Required
	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	@Required
	public void setContentDao(ContentDao contentDao)
	{
		this.contentDao = contentDao;
	}

	public Content content(int id)
	{
		return contentDao.read(id);
	}

	public Content contentByTag(String tag)
	{
		return contentDao.findContentByTag(tag);
	}

	public boolean canReadContent(String userName, int id)
	{
		// always when logged in
		if (userName != null)
			return true;

		// always when written by admin
		Content content = content(id);
		if (content.getCreatedBy().isUserInRole(Role.ADMIN))
			return true;

		return false;
	}

	public Content readContent(String userName, int id)
	{
		if (!canReadContent(userName, id))
			throw new NotAuthorizedException();

		Content content = content(id);
		content.setReadCount(content.getReadCount() + 1);
		return content;
	}

	public int postMessage(String userName, int parentId, String subject, String text, InetAddress ip)
	{
		Date now = RequestTime.get();

		try
		{
			// load objects
			User user = userDao.read(User.class, userName);
			Content parent = contentDao.read(parentId);

			Content content = new Content(now, user, ip);
			content.setName(subject);
			content.setContentType("text/plain");
			content.setContent(text.getBytes("UTF-8"));
			contentDao.create(content);

			parent.addChild(content);

			pullupChange(content, now, user);

			return content.getId();
		}
		catch (UnsupportedEncodingException x)
		{
			throw new ApplicationException(x);
		}
	}

	public boolean canDeleteMessage(String userName, int id)
	{
		// never by guests
		if (userName == null)
			return false;

		// is admin?
		User user = userDao.read(User.class, userName);
		if (!user.isUserInRole(Role.ADMIN))
			return false;

		// is root content node?
		Content content = contentDao.read(id);
		if (content.isRoot())
			return false;

		return true;
	}

	public int deleteMessage(String userName, int id)
	{
		if (!canDeleteMessage(userName, id))
			throw new NotAuthorizedException();

		// load objects
		Content content = contentDao.read(id);
		Content parent = content.getParent();
		Content trash = contentDao.findContentByTag(TRASH_NODE_TAG);

		// move content to trash
		parent.removeChild(content);
		trash.addChild(content);

		return parent.getId();
	}

	private void pullupChange(Content content, Date changedAt, User changedBy)
	{
		content.setInheritedChangedAt(changedAt);
		content.setInheritedChangedBy(changedBy);
		Content parent = content.getParent();
		if (parent != null)
			pullupChange(parent, changedAt, changedBy);
	}

	private boolean canCreateForum(String userName)
	{
		// never by guests
		if (userName == null)
			return false;

		// is admin?
		User user = userDao.read(User.class, userName);
		if (!user.isUserInRole(Role.ADMIN))
			return false;

		return true;
	}

	public int createForum(String userName, String tag, String name, String description, InetAddress ip) throws ExistingContentTagException
	{
		if (!canCreateForum(userName))
			throw new NotAuthorizedException();

		Date now = RequestTime.get();

		if (contentDao.findContentByTag(tag) != null)
			throw new ExistingContentTagException(tag);

		try
		{
			// load objects
			User user = userDao.read(User.class, userName);

			Content content = new Content(now, user, ip);
			content.setRoot(true);
			content.setTag(tag);
			content.setName(name);
			content.setContentType("text/plain");
			content.setContent(description.getBytes("UTF-8"));
			contentDao.create(content);

			return content.getId();
		}
		catch (UnsupportedEncodingException x)
		{
			throw new ApplicationException(x);
		}
	}

	public List<Content> newsFeed(int maxResults)
	{
		Content parent = contentDao.findContentByTag("allgemein");
		User createdBy = userDao.loadUser("goonie", false);
		String nameStart = "Update";

		return contentDao.findContents(parent, createdBy, nameStart, "!" + Content.PROPERTY_CREATED_AT, maxResults);
	}
}
