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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import de.schildbach.portal.persistence.content.Content;
import de.schildbach.portal.service.content.ContentService;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.presentation.HtmlUtils;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ViewNodeController extends AbstractController
{
	protected static final String REQUEST_ATTRIBUTE_CONTENT = ViewNodeController.class.getName() + ".content";

	private ContentService contentService;
	private PresenceService presenceService;
	private String forumView;
	private String messageView;
	private HtmlUtils htmlUtils;

	@Required
	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Required
	public void setForumView(String forumView)
	{
		this.forumView = forumView;
	}

	@Required
	public void setMessageView(String messageView)
	{
		this.messageView = messageView;
	}

	@Required
	public void setHtmlUtils(HtmlUtils htmlUtils)
	{
		this.htmlUtils = htmlUtils;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();

		String userName = request.getRemoteUser();

		Content content;
		if (request.getParameter("tag") != null)
		{
			content = contentService.contentByTag(ServletRequestUtils.getRequiredStringParameter(request, "tag"));
		}
		else
		{
			int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
			if (!contentService.canReadContent(userName, id))
				throw new NotAuthorizedException();
			content = contentService.readContent(userName, id);
		}

		// update last user activity
		presenceService.setLastActivity(userName, Activity.FORUM);

		request.setAttribute(REQUEST_ATTRIBUTE_CONTENT, content);
		model.put("content", content);

		if (content.getContent() != null)
		{
			String description = new String(content.getContent(), "UTF-8");
			description = htmlUtils.convertToHtml(description);
			description = htmlUtils.linkUrls(description);
			model.put("description", description);
		}

		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DATE, -3);
		model.put("reference_date", calendar.getTime());

		model.put("can_post_message", request.getUserPrincipal() != null);
		if (content.getCreatedBy() != null)
			model.put("can_private_reply", !content.getCreatedBy().equals(request.getUserPrincipal()));

		if (content.isRoot())
			return new ModelAndView(forumView, model);
		else
			return new ModelAndView(messageView, model);
	}
}
