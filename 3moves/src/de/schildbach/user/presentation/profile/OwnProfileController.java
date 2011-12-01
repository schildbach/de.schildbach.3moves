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

import java.security.Principal;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.persistence.user.Gender;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.OwnProfileCommand;
import de.schildbach.portal.service.user.bo.Permission;

/**
 * @author Andreas Schildbach
 */
@Controller
public class OwnProfileController extends SimpleFormController
{
	private UserService userService;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		return userService.ownProfileForUpdate(user.getName());
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		// TODO take into account TimeZone
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("permissions", Arrays.asList(Permission.values()));
		model.put("genders", Arrays.asList(Gender.values()));
		model.put("standard_languages", userService.standardLanguages());
		return model;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		OwnProfileCommand command = (OwnProfileCommand) commandObj;

		if (command.getLanguages() == null)
			command.setLanguages(new HashSet<String>());

		userService.updateOwnProfile(user.getName(), command);

		return new ModelAndView(getSuccessView());
	}
}
