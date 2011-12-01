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

package de.schildbach.portal.service.user;

import java.util.Collection;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

import de.schildbach.portal.service.user.UserEventService;
import de.schildbach.portal.service.user.event.UserMessageEvent;

/**
 * @author Andreas Schildbach
 */
public class UserEventServiceMock implements UserEventService
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserEventServiceMock.class);

	private MultiMap messages = new MultiHashMap();

	public void onApplicationEvent(ApplicationEvent event)
	{
		if (event instanceof UserMessageEvent)
		{
			UserMessageEvent userMessageEvent = (UserMessageEvent) event;
			LOG.debug("would send message " + event);
			messages.put(userMessageEvent.username, userMessageEvent.subject + "\n" + userMessageEvent.text);
		}
	}

	public void reset()
	{
		messages.clear();
	}

	public int eventCount(String username)
	{
		Collection<?> collection = (Collection<?>) messages.get(username);
		if (collection == null)
			return 0;
		else
			return collection.size();
	}
}
