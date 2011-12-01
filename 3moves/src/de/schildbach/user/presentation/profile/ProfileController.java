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

package de.schildbach.user.presentation.profile;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.persistence.user.SubjectVisitor;
import de.schildbach.portal.persistence.user.SystemAccount;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.presentation.DateUtils;
import de.schildbach.presentation.HtmlUtils;
import de.schildbach.user.presentation.PermissionHelper;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ProfileController extends ParameterizableViewController
{
	private UserService userService;
	private PresenceService presenceService;
	private HtmlUtils htmlUtils;
	private String viewNameAsGuest;
	private String viewNameNotFound;
	private String viewNameSystemAccount;

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
	public void setHtmlUtils(HtmlUtils htmlUtils)
	{
		this.htmlUtils = htmlUtils;
	}

	@Required
	public void setViewNameAsGuest(String viewNameAsGuest)
	{
		this.viewNameAsGuest = viewNameAsGuest;
	}

	@Required
	public void setViewNameNotFound(String viewNameNotFound)
	{
		this.viewNameNotFound = viewNameNotFound;
	}

	@Required
	public void setViewNameSystemAccount(String viewNameSystemAccount)
	{
		this.viewNameSystemAccount = viewNameSystemAccount;
	}

	@Override
	protected ModelAndView handleRequestInternal(final HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final Principal remoteUser = request.getUserPrincipal();

		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.PROFILE);

		if (remoteUser != null)
		{
			String targetName = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
			Subject targetSubject = userService.subject(targetName);
			if (targetSubject != null)
			{
				final ModelAndView modelAndView = new ModelAndView(getViewName());

				modelAndView.addObject("name", targetSubject.getName());

				targetSubject.accept(new SubjectVisitor()
				{
					public void visit(SystemAccount systemAccount)
					{
						// viewing system account
						modelAndView.setViewName(viewNameSystemAccount);
					}

					public void visit(User targetUser)
					{
						// viewing user
						boolean isSelf = remoteUser != null && targetUser.equals(remoteUser);
						modelAndView.addObject("user", targetUser);

						if (!isSelf && presenceService.isUserLoggedIn(targetUser.getName()))
							modelAndView.addObject("is_online", true);

						boolean isFriend = false;
						if (!isSelf)
						{
							modelAndView.addObject("show_relation", true);
							SubjectRelation relation = userService.subjectRelation(remoteUser.getName(), targetUser.getName());
							isFriend = relation != null && relation.isFriend() && relation.getConfirmed() != null
									&& relation.getConfirmed().booleanValue();
							if (relation != null)
								modelAndView.addObject("relation", relation);
							SubjectRelation reverseRelation = userService.subjectRelation(targetUser.getName(), remoteUser.getName());
							if (reverseRelation != null)
								modelAndView.addObject("reverse_relation", reverseRelation);
						}

						boolean isAdmin = request.isUserInRole(Role.ADMIN.name());
						modelAndView.addObject("is_admin_role", isAdmin);

						if (targetUser.getGender() != null)
							modelAndView.addObject("gender", targetUser.getGender().name().toLowerCase());

						if (targetUser.getFullName() != null
								&& PermissionHelper.checkPermission(targetUser.getFullNamePermission(), isSelf, true, isFriend))
							modelAndView.addObject("full_name", targetUser.getFullName());

						if (targetUser.getBirthday() != null
								&& PermissionHelper.checkPermission(targetUser.getAgePermission(), isSelf, true, isFriend))
							modelAndView.addObject("age", new Integer(DateUtils.calculateAge(RequestTime.get(), targetUser.getBirthday())));

						if (targetUser.getCountry() != null
								&& PermissionHelper.checkPermission(targetUser.getCountryPermission(), isSelf, true, isFriend))
							modelAndView.addObject("country", targetUser.getCountry());

						if (targetUser.getCity() != null && PermissionHelper.checkPermission(targetUser.getCityPermission(), isSelf, true, isFriend))
							modelAndView.addObject("city", targetUser.getCity());

						if (targetUser.getOccupation() != null
								&& PermissionHelper.checkPermission(targetUser.getOccupationPermission(), isSelf, true, isFriend))
							modelAndView.addObject("occupation", targetUser.getOccupation());

						if (targetUser.getLanguages() != null)
							modelAndView.addObject("languages", targetUser.getLanguages());

						if (targetUser.getTitle() != null)
							modelAndView.addObject("title", targetUser.getTitle());

						String description = targetUser.getDescription();
						if (description != null)
						{
							description = htmlUtils.convertToHtml(description);
							modelAndView.addObject("description", description);
						}

						// statistics
						modelAndView.addObject("created_at", targetUser.getCreatedAt());

						try
						{
							modelAndView.addObject("last_hostname", InetAddress.getByName(targetUser.getLastIP()).getCanonicalHostName());
						}
						catch (UnknownHostException x)
						{
						}

						boolean hasPhoto = userService.hasPhoto(targetUser.getName());

						modelAndView.addObject("can_clear_photo", isSelf && hasPhoto);
						modelAndView.addObject("can_set_photo", isSelf && !hasPhoto);
						modelAndView.addObject("can_edit_profile", isSelf);
						modelAndView.addObject("can_edit_holidays", isSelf);

						Comparator<Role> reverseOrder = Collections.reverseOrder();
						Collection<Role> userRoles = new TreeSet<Role>(reverseOrder);
						for (UserRole userRole : targetUser.getUserRoles())
							userRoles.add(userRole.getRole());
						modelAndView.addObject("user_roles", userRoles);

						if (isAdmin)
						{
							if (targetUser.getReferredFrom() != null)
							{
								modelAndView.addObject("show_referred_from", true);
								if (targetUser.getReferredFrom().startsWith("http:"))
									modelAndView.addObject("show_referred_from_as_link", true);
							}

							modelAndView.addObject("can_remove_user_roles", true);
							List<Role> userRolesToAdd = new LinkedList<Role>(Arrays.asList(Role.values()));
							userRolesToAdd.removeAll(userRoles);
							Collections.sort(userRolesToAdd, reverseOrder);
							modelAndView.addObject("user_roles_to_add", userRolesToAdd);
							modelAndView.addObject("can_turn_into_user", !isSelf);
						}

						modelAndView.addObject("user_holidays", targetUser.getUserHolidays());
					}
				});
				return modelAndView;
			}
			else
			{
				// user not found
				return new ModelAndView(viewNameNotFound, "username", targetName);
			}
		}
		else
		{
			// trying to view as guest
			return new ModelAndView(viewNameAsGuest);
		}
	}
}
