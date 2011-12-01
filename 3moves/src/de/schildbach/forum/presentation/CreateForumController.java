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

package de.schildbach.forum.presentation;

import java.io.Serializable;
import java.net.InetAddress;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.service.content.ContentService;
import de.schildbach.portal.service.content.exception.ExistingContentTagException;
import de.schildbach.portal.service.exception.NotAuthorizedException;

/**
 * @author Andreas Schildbach
 */
@Controller
public class CreateForumController extends SimpleFormController
{
	private ContentService contentService;
	
	@Required
	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		return new Command();
	}
	
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;
		
		if(command.getTag() == null)
			errors.rejectValue("tag", "missing");
		if(command.getName() == null)
			errors.rejectValue("name", "missing");
		
		if(!errors.hasErrors())
		{
			try
			{
				InetAddress ip = InetAddress.getByName(request.getRemoteAddr());
				int id = contentService.createForum(user.getName(), command.getTag(), command.getName(), command.getDescription(), ip);
				return new ModelAndView(getSuccessView(), "id", id);
			}
			catch(ExistingContentTagException x)
			{
				errors.rejectValue("tag", "exists");
			}
		}

		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
		private String tag;
		private String name;
		private String description;

		public String getTag()
		{
			return tag;
		}
		public void setTag(String tag)
		{
			this.tag = tag;
		}
		
		public String getDescription()
		{
			return description;
		}
		public void setDescription(String description)
		{
			this.description = description;
		}
	
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
	}
}
