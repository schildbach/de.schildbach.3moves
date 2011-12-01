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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.persistence.user.UserTitle;
import de.schildbach.portal.persistence.user.UserTitleVisitor;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.presentation.GenericMappingPropertyEditor;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class TitleController
{
	private UserService userService;
	private String view;
	private String successView;

	private static final String RATING_PREFIX = "rating_";
	private static final String ROLE_PREFIX = "role_";

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setView(String viewName)
	{
		this.view = viewName;
	}

	@Required
	public void setSuccessView(String successView)
	{
		this.successView = successView;
	}

	@InitBinder
	public void initBinder(User user, WebDataBinder binder)
	{
		binder.registerCustomEditor(UserTitle.class, new GenericMappingPropertyEditor<UserTitle>(availableTitles(user)));
	}

	@ModelAttribute("available_titles")
	public Map<String, UserTitle> availableTitles(User user)
	{
		List<UserTitle> titles = userService.userTitles(user.getName());

		Map<String, UserTitle> availableTitles = new LinkedHashMap<String, UserTitle>();
		availableTitles.put("none", null);
		for (UserTitle title : titles)
			availableTitles.put(titleToString(title), title);

		return availableTitles;
	}

	private String titleToString(UserTitle title)
	{
		final StringBuilder text = new StringBuilder();

		title.accept(new UserTitleVisitor()
		{
			public void visit(UserRole userRole)
			{
				text.append(ROLE_PREFIX);
				text.append(userRole.getRole());
			}

			public void visit(SubjectRating subjectRating)
			{
				text.append(RATING_PREFIX);
				text.append(subjectRating.getRating());
			}
		});

		return text.toString();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, ModelMap model)
	{
		Command command = new Command();
		command.setTitle(user.getTitle());

		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(User user, @ModelAttribute Command command, SessionStatus status)
	{
		userService.setTitle(user.getName(), command.getTitle());
		status.setComplete();
		return successView;
	}

	public static class Command implements Serializable
	{
		private UserTitle title;

		public UserTitle getTitle()
		{
			return title;
		}

		public void setTitle(UserTitle title)
		{
			this.title = title;
		}
	}
}
