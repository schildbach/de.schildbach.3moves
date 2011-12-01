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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.portal.service.user.bo.InstantMessage;
import de.schildbach.portal.service.user.bo.UserSession;

/**
 * @author Andreas Schildbach
 */
public interface PresenceService
{
	void login(User user, String sessionId, InetAddress loggedInFrom, String userAgent);

	void logout(String username, String sessionId);

	boolean isUserLoggedIn(String username);

	Set<User> loggedInUsers();

	Set<UserSession> sessionsOfUser(String username);

	int numberOfLoggedInUsers(boolean countMultipleLogins);

	int numberOfSimultaneousLogins(String username);

	void setLastActivity(String username, Activity lastActivity);

	Map<String, Activity> lastActivities();

	Map<String, Date> lastAccessedAt();

	boolean sendInstantMessage(String senderName, String recipientName, String text);

	List<InstantMessage> instantMessages(String username);
}
