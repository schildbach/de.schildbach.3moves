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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
public class ViewMessageController extends ParameterizableViewController
{
	private ContentService contentService;
	private HtmlUtils htmlUtils;
	
	@Required
	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}

	@Required
	public void setHtmlUtils(HtmlUtils htmlUtils)
	{
		this.htmlUtils = htmlUtils;
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Content parentContent = (Content) request.getAttribute(ViewNodeController.REQUEST_ATTRIBUTE_CONTENT);
		
		List<char[]> tree = new LinkedList<char[]>();
		List<Content> contentNodes = new LinkedList<Content>();
		BitSet bits = new BitSet();
		for(Iterator<Content> iChildNode = parentContent.getPath().get(1).getDepthFirstIterator(); iChildNode.hasNext();)
		{
			Content childNode = iChildNode.next();
		
			int depth = childNode.getDepth()-1;
			char[] images = new char[depth];
			for(int i = 0; i < depth; i++)
			{
				if(i < depth-1)
				{
					if(bits.get(i))
						images[i] = 'l'; // vertical connection
					else
						images[i] = 'n'; // empty
				}
				else
				{
					if(!childNode.isLastSibling())
					{
						images[i] = 'x'; // normal node
						bits.set(i);
					}
					else
					{
						images[i] = 'e'; // end node
						bits.clear(i);
					}
				}
			}
			contentNodes.add(childNode);
			tree.add(images);
		}
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("content_nodes", contentNodes);
		model.put("tree", tree);
		model.put("content", parentContent);
		if(parentContent.getContent() != null)
		{
			String body = new String(parentContent.getContent(), "UTF-8");
			body = htmlUtils.convertToHtml(body);
			body = htmlUtils.linkUrls(body);
			body = htmlUtils.linkGames(body, request.getContextPath());
			model.put("body", body);
		}
		boolean isIpLogged = parentContent.getCreatedByIP() != null;
		model.put("is_ip_logged", isIpLogged);
		if(isIpLogged && parentContent.getCreatedBy().equals(request.getUserPrincipal()))
			model.put("created_by_ip", parentContent.getCreatedByIP().getHostAddress());
		model.put("can_delete_message", contentService.canDeleteMessage(request.getRemoteUser(), parentContent.getId()));
		return new ModelAndView(getViewName(), model);
	}
}
