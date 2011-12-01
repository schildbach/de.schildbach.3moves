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

package de.schildbach.user.presentation.list;

import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.InstantMessage;

/**
 * @author Andreas Schildbach
 */
@Controller
public class OnlineUserListSidebarController extends ParameterizableViewController
{
	private UserService userService;
	private PresenceService presenceService;
	private String linkDonations;
	private String linkChat;
	private String linkMore;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Required
	public void setLinkDonations(String linkDonations)
	{
		this.linkDonations = linkDonations;
	}

	@Required
	public void setLinkChat(String linkChat)
	{
		this.linkChat = linkChat;
	}

	@Required
	public void setLinkMore(String linkMore)
	{
		this.linkMore = linkMore;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		int max = ServletRequestUtils.getIntParameter(request, "max", 15);

		SortedSet<User> users = new TreeSet<User>(new LastLoginAtComparator());
		users.addAll(presenceService.loggedInUsers());

		Date isNewDate = userService.isNewCutoffDate();

		List<Row> rows = new LinkedList<Row>();
		for (User user : users)
		{
			Row row = new Row(user.getName());
			TreeSet<UserRole> userRoles = new TreeSet<UserRole>(user.getUserRoles());
			if (userRoles != null && !userRoles.isEmpty())
				row.setRole(userRoles.last().getRole());
			row.setNew(isNewDate.before(user.getCreatedAt()));

			rows.add(row);

			if (rows.size() >= max)
				break;
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("rows", rows);

		// chat requests
		Principal loggedInUser = request.getUserPrincipal();
		if (loggedInUser != null)
		{
			Map<String, Boolean> chatRequests = new HashMap<String, Boolean>();
			for (InstantMessage message : presenceService.instantMessages(loggedInUser.getName()))
			{
				if (!message.isRead() && message.getRecipient().equals(loggedInUser.getName()))
				{
					chatRequests.put(message.getSender(), true);
				}
			}
			model.put("chat_requests", chatRequests);
		}

		model.put("show_more", loggedInUser != null);
		model.put("more_num", rows.size() - users.size());
		model.put("link_donations", request.getContextPath() + linkDonations);
		model.put("link_chat", request.getContextPath() + linkChat);
		model.put("link_more", request.getContextPath() + linkMore);
		return new ModelAndView(getViewName(), model);
	}

	public static final class Row
	{
		private String username;
		private Role role;
		private boolean isNew;
		private boolean chatRequest;

		public Row(String username)
		{
			setUsername(username);
		}

		public final boolean isNew()
		{
			return isNew;
		}

		public final void setNew(boolean isNew)
		{
			this.isNew = isNew;
		}

		public final Role getRole()
		{
			return role;
		}

		public final void setRole(Role role)
		{
			this.role = role;
		}

		public final String getUsername()
		{
			return username;
		}

		public final void setUsername(String username)
		{
			this.username = username;
		}

		public boolean isChatRequest()
		{
			return chatRequest;
		}

		public void setChatRequest(boolean chatRequest)
		{
			this.chatRequest = chatRequest;
		}
	}

	private static final class LastLoginAtComparator implements Comparator<User>
	{
		public int compare(User o1, User o2)
		{
			return -o1.getLastLoginAt().compareTo(o2.getLastLoginAt());
		}
	}
}
