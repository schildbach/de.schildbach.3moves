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

package de.schildbach.user.presentation.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import de.schildbach.portal.persistence.user.RelationType;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.AuthenticationHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UserRelationController extends MultiActionController
{
	private UserService userService;
	private AuthenticationHelper authenticationHelper;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setAuthenticationHelper(AuthenticationHelper authenticationHelper)
	{
		this.authenticationHelper = authenticationHelper;
	}

	public ModelAndView user_relations(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("friends", userService.friendsOfSubject(user.getName()));
		model.put("fans", userService.fansOfSubject(user.getName()));
		model.put("banned", userService.bannedBySubject(user.getName()));

		return new ModelAndView("user_relations.jspx", model);
	}

	public ModelAndView add_user_relation(HttpServletRequest request, HttpServletResponse response)
	{
		Principal remoteUser = request.getUserPrincipal();
		if (remoteUser == null)
			throw new NotAuthorizedException();

		HttpSession session = request.getSession();

		String target = request.getParameter("target");
		RelationType type = Enum.valueOf(RelationType.class, request.getParameter("type").toUpperCase());

		try
		{
			if (type == RelationType.FRIEND)
				userService.becomeFanOf(remoteUser.getName(), target);
			else if (type == RelationType.BANNED)
				userService.ban(remoteUser.getName(), target);

			authenticationHelper.refreshUserRelations(session, userService.userRelations(remoteUser.getName()));
		}
		catch (ApplicationException x)
		{
			session.setAttribute("message_view_friends", "message_" + x.getMessage());
		}

		return new ModelAndView("user_relations.html");
	}

	public ModelAndView remove_user_relation(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		HttpSession session = request.getSession();

		String target = request.getParameter("target");

		userService.removeSubjectRelation(user.getName(), target);

		authenticationHelper.refreshUserRelations(session, userService.userRelations(user.getName()));

		return new ModelAndView("user_relations.html");
	}

	public ModelAndView set_friend_confirm(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		String source = request.getParameter("source");
		boolean confirm = request.getParameter("confirm").equals("true");

		userService.setFriendConfirm(user.getName(), source, confirm);

		return new ModelAndView("user_relations.html");
	}
}
