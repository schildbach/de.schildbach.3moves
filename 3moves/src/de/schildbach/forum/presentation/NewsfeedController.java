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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.content.Content;
import de.schildbach.portal.service.content.ContentService;
import de.schildbach.presentation.HtmlUtils;

/**
 * @author Andreas Schildbach
 */
@Controller
public class NewsfeedController extends ParameterizableViewController
{
	private int pageLength;
	private ContentService contentService;
	private HtmlUtils htmlUtils;

	@Required
	public void setPageLength(int pageLength)
	{
		this.pageLength = pageLength;
	}

	@Required
	public void setContentService(ContentService communityService)
	{
		this.contentService = communityService;
	}

	@Required
	public void setHtmlUtils(HtmlUtils htmlUtils)
	{
		this.htmlUtils = htmlUtils;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		List<Content> entries = contentService.newsFeed(pageLength);

		Date lastUpdated = null;
		Map<Content, String> bodies = new HashMap<Content, String>();
		for (Content entry : entries)
		{
			// determine last updated
			if (lastUpdated == null || lastUpdated.before(entry.getCreatedAt()))
				lastUpdated = entry.getCreatedAt();

			// prepare bodies
			String body = new String(entry.getContent(), "UTF-8");
			body = htmlUtils.convertToHtml(body);
			body = htmlUtils.linkUrls(body);
			body = htmlUtils.linkGames(body, request.getContextPath());
			bodies.put(entry, body);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("entries", entries);
		model.put("bodies", bodies);
		model.put("last_updated", lastUpdated);
		return new ModelAndView(getViewName(), model);
	}
}
