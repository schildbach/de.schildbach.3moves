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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.mail.Mail;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.mail.MailService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.presentation.HtmlUtils;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ViewMailController extends ParameterizableViewController
{
	private MailService mailService;
	private PresenceService presenceService;
	private HtmlUtils htmlUtils;
	
	@Required
	public void setMailService(MailService mailService)
	{
		this.mailService = mailService;
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
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();

		int id = ServletRequestUtils.getRequiredIntParameter(request, "id");

		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.MAIL);

		Mail mail = mailService.readMail(user.getName(), id);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("mail", mail);
		if(mail.getContent() != null)
		{
			String body = new String(mail.getContent(), "UTF-8");
			body = htmlUtils.convertToHtml(body);
			body = htmlUtils.linkUrls(body);
			body = htmlUtils.linkGames(body, request.getContextPath());
			body = htmlUtils.linkEMail(body);
			model.put("body", body);
		}
		model.put("can_reply", user.equals(mail.getRecipient()));
		model.put("show_flags", user.equals(mail.getRecipient()) && (mail.isImportant() || mail.isRepliedTo()));
		return new ModelAndView(getViewName(), model);
	}
}
