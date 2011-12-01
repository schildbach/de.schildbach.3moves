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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.presentation.DateUtils;
import de.schildbach.user.presentation.PermissionHelper;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UsersListController extends ParameterizableViewController
{
	private UserService userService;
	private PresenceService presenceService;

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

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Date now = RequestTime.get();

		Principal remoteUser = request.getUserPrincipal();

		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.USERS_ONLINE);

		Collection<User> users = (Collection<User>) request.getAttribute("users");
		model.put("users", users);

		List<Role> roles = new LinkedList<Role>();
		List<Boolean> isNew = new LinkedList<Boolean>();
		Date isNewDate = userService.isNewCutoffDate();
		for (User user : users)
		{
			TreeSet<UserRole> userRoles = new TreeSet<UserRole>(user.getUserRoles());
			if (!userRoles.isEmpty())
				roles.add(userRoles.last().getRole());
			else
				roles.add(null);

			isNew.add(Boolean.valueOf(isNewDate.before(user.getCreatedAt())));
		}
		model.put("roles", roles);
		model.put("is_new", isNew);

		for (StringTokenizer t = new StringTokenizer(request.getParameter("columns"), ","); t.hasMoreTokens();)
		{
			String column = t.nextToken();
			if (column.equals("age"))
			{
				List<Integer> ages = new LinkedList<Integer>();
				for (User user : users)
				{
					Integer age = null;
					if (user.getBirthday() != null
							&& PermissionHelper.checkPermission(user.getAgePermission(), user.equals(remoteUser), remoteUser != null, false))
						age = new Integer(DateUtils.calculateAge(now, user.getBirthday()));
					ages.add(age);
				}
				model.put("ages", ages);
			}
			else if (column.equals("location"))
			{
				List<String> locations = new LinkedList<String>();
				for (User user : users)
				{
					StringBuilder location = new StringBuilder();
					boolean isSelf = remoteUser != null && user.equals(remoteUser);
					if (user.getCity() != null && PermissionHelper.checkPermission(user.getCityPermission(), isSelf, remoteUser != null, false))
						location.append(user.getCity()).append(", ");
					if (user.getCountry() != null && PermissionHelper.checkPermission(user.getCountryPermission(), isSelf, remoteUser != null, false))
						location.append(user.getCountry()).append(", ");
					if (location.length() > 0)
						location.setLength(location.length() - 2);
					locations.add(location.toString());
				}
				model.put("locations", locations);
			}
			else if (column.equals("last_activity"))
			{
				List<String> lastAccessedAt = new LinkedList<String>();
				Map<String, Date> lastAccessedAtInput = (Map<String, Date>) request.getAttribute("last_accessed_at");
				for (User user : users)
				{
					lastAccessedAt.add(DateUtils.dateDiffShort(lastAccessedAtInput.get(user.getName()), now, request.getLocale(), false));
				}
				model.put("last_activity_at", lastAccessedAt);
				model.put("last_activities", presenceService.lastActivities());
			}
		}

		model.put("row_classes", new String[] { "dark", "light" });

		return new ModelAndView(getViewName(), model);
	}
}
