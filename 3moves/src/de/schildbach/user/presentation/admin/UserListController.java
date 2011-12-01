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

package de.schildbach.user.presentation.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UserListController extends SimpleFormController
{
	private static final int MAX_ROWS = 50;

	private UserService userService;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	private boolean isSubmitNameFilter(HttpServletRequest request)
	{
		return request.getParameter("submitNameFilter") != null;
	}

	private boolean isClearNameFilter(HttpServletRequest request)
	{
		return request.getParameter("clearNameFilter") != null;
	}

	private boolean isRoleFilter(HttpServletRequest request)
	{
		return request.getParameter("roleFilter") != null;
	}

	private boolean isOrderBy(HttpServletRequest request)
	{
		return request.getParameter("orderBy") != null;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		if (!request.isUserInRole(Role.ADMIN.name()))
			throw new NotAuthorizedException();

		return isSubmitNameFilter(request) || isClearNameFilter(request) || isRoleFilter(request) || isOrderBy(request);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		return new Command("!lastLoginAt");
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Command command = (Command) commandObj;

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("roles", Role.values());
		model.put("users", userService.search(command.getNameFilter(), command.getRoleFilter(), command.getOrderBy(), MAX_ROWS));
		return model;
	}

	@Override
	protected boolean isFormChangeRequest(HttpServletRequest request)
	{
		return isRoleFilter(request) || isOrderBy(request);
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Command command = (Command) commandObj;

		if (isClearNameFilter(request))
			command.setNameFilter(null);

		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
		private String nameFilter;
		private Role roleFilter;
		private String orderBy;

		public Command(String orderBy)
		{
			setOrderBy(orderBy);
		}

		public String getNameFilter()
		{
			return nameFilter;
		}

		public void setNameFilter(String nameFilter)
		{
			this.nameFilter = nameFilter;
		}

		public Role getRoleFilter()
		{
			return roleFilter;
		}

		public void setRoleFilter(Role roleFilter)
		{
			this.roleFilter = roleFilter;
		}

		public String getOrderBy()
		{
			return orderBy;
		}

		public void setOrderBy(String orderBy)
		{
			this.orderBy = orderBy;
		}
	}
}
