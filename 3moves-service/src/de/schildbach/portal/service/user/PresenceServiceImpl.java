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

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.portal.service.user.bo.InstantMessage;
import de.schildbach.portal.service.user.bo.UserSession;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Service
public class PresenceServiceImpl implements PresenceService
{
	private Map<String, Entry> users = Collections.synchronizedMap(new HashMap<String, Entry>());

	private class Entry
	{
		private Entry(User user)
		{
			this.user = user;
		}

		private User user;
		private Activity lastActivity = Activity.NONE;
		private Date lastAccessedAt = RequestTime.get();
		private Map<String, UserSession> sessions = Collections.synchronizedMap(new HashMap<String, UserSession>());
		private List<InstantMessage> messages = Collections.synchronizedList(new LinkedList<InstantMessage>());
	}

	public synchronized void login(User user, String sessionId, InetAddress loggedInFrom, String userAgent)
	{
		Entry entry = users.get(user.getName());

		if (entry == null)
		{
			entry = new Entry(user);
			users.put(user.getName(), entry);
		}

		UserSession session = new UserSession(sessionId);
		session.setLoggedInFrom(loggedInFrom);
		session.setLoggedInAt(RequestTime.get());
		session.setUserAgent(userAgent);
		entry.sessions.put(sessionId, session);
	}

	public synchronized void logout(String username, String sessionId)
	{
		Entry entry = users.get(username);

		if (entry == null)
			return;

		entry.sessions.remove(sessionId);

		if (entry.sessions.size() == 0)
			users.remove(username);
	}

	public boolean isUserLoggedIn(String username)
	{
		return numberOfSimultaneousLogins(username) > 0;
	}

	public Set<User> loggedInUsers()
	{
		Set<User> loggedInUsers = new HashSet<User>();

		for (Entry entry : users.values())
		{
			loggedInUsers.add(entry.user);
		}

		return Collections.unmodifiableSet(loggedInUsers);
	}

	public Set<UserSession> sessionsOfUser(String username)
	{
		Entry entry = users.get(username);

		return new HashSet<UserSession>(entry.sessions.values());
	}

	public int numberOfLoggedInUsers(boolean countMultipleLogins)
	{
		if (countMultipleLogins)
		{
			int num = 0;
			for (Entry entry : users.values())
			{
				num += entry.sessions.size();
			}
			return num;
		}
		else
		{
			return users.size();
		}
	}

	public int numberOfSimultaneousLogins(String username)
	{
		Entry entry = users.get(username);
		if (entry != null)
		{
			return entry.sessions.size();
		}
		else
		{
			return 0;
		}
	}

	public void setLastActivity(String username, Activity lastActivity)
	{
		if (username == null)
			return;

		Entry entry = users.get(username);

		// be graceful if state is inconsistent
		if (entry == null)
			return;

		entry.lastActivity = lastActivity;
		entry.lastAccessedAt = RequestTime.get();
	}

	public Map<String, Activity> lastActivities()
	{
		Map<String, Activity> lastActivities = new HashMap<String, Activity>();

		for (Entry entry : users.values())
		{
			if (entry.lastActivity != Activity.NONE)
				lastActivities.put(entry.user.getName(), entry.lastActivity);
		}

		return Collections.unmodifiableMap(lastActivities);
	}

	public Map<String, Date> lastAccessedAt()
	{
		Map<String, Date> lastAccessedAt = new HashMap<String, Date>();

		for (Entry entry : users.values())
		{
			lastAccessedAt.put(entry.user.getName(), entry.lastAccessedAt);
		}

		return Collections.unmodifiableMap(lastAccessedAt);
	}

	public boolean sendInstantMessage(String senderName, String recipientName, String text)
	{
		Entry senderEntry = lookup(senderName);
		Entry recipientEntry = users.get(recipientName);
		if (recipientEntry == null)
			return false;

		InstantMessage message = new InstantMessage(RequestTime.get(), senderName, recipientName, text);

		recipientEntry.messages.add(message);
		senderEntry.messages.add(message);

		return true;
	}

	public List<InstantMessage> instantMessages(String username)
	{
		Entry entry = users.get(username);

		// be graceful if state is inconsistent
		if (entry == null)
			return Collections.emptyList();

		return Collections.unmodifiableList(entry.messages);
	}

	private Entry lookup(String username)
	{
		Entry entry = users.get(username);

		if (entry == null)
			throw new IllegalStateException("user \"" + username + "\" not logged in");

		return entry;
	}
}
