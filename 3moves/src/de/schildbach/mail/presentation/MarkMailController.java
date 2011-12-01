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

package de.schildbach.mail.presentation;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.mail.MailService;
import de.schildbach.presentation.ReferrerFilter;

/**
 * @author Andreas Schildbach
 */
@Controller
public class MarkMailController extends AbstractController
{
	private MailService mailService;
	
	@Required
	public void setMailService(MailService mailService)
	{
		this.mailService = mailService;
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();
		
		int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
		boolean isImportant = ServletRequestUtils.getRequiredBooleanParameter(request, "important");
		
		mailService.setImportant(user.getName(), id, isImportant);
		
		// FIXME
		HttpSession session = request.getSession();
		return new ModelAndView(new RedirectView((String) session.getAttribute(ReferrerFilter.REFERRER)));
	}
}
