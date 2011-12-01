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
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import de.schildbach.portal.persistence.content.Content;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.content.ContentService;
import de.schildbach.presentation.HtmlUtils;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class PostMessageController
{
	private ContentService contentService;
	private HtmlUtils htmlUtils;
	private String view;
	private String successView;

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

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@Required
	public void setSuccessView(String successView)
	{
		this.successView = successView;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, @RequestParam("parent_id") int parentId, @RequestParam(value = "subject", required = false) String subject,
			HttpServletRequest request, Model model) throws UnsupportedEncodingException
	{
		Content content = contentService.content(parentId);
		model.addAttribute("parent_content", content);
		if (content.getContent() != null)
		{
			String body = new String(content.getContent(), "UTF-8");
			body = htmlUtils.convertToHtml(body);
			body = htmlUtils.linkUrls(body);
			body = htmlUtils.linkGames(body, request.getContextPath());
			model.addAttribute("parent_body", body);
		}

		model.addAttribute("created_by", user.getName());

		Command command = new Command();
		command.setSubject(subject != null ? subject : content.getName());
		command.setParentId(parentId);

		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView processSubmit(User user, @ModelAttribute Command command, BindingResult result, HttpServletRequest request,
			SessionStatus status) throws UnknownHostException
	{
		// post message
		InetAddress ip = InetAddress.getByName(request.getRemoteAddr());
		int id = contentService.postMessage(user.getName(), command.getParentId(), command.getSubject(), command.getText(), ip);

		status.setComplete();

		return new ModelAndView(successView, "id", id);
	}

	public static class Command implements Serializable
	{
		private String subject;
		private String text;
		private int parentId;

		public String getSubject()
		{
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}

		public int getParentId()
		{
			return parentId;
		}

		public void setParentId(int parentId)
		{
			this.parentId = parentId;
		}
	}
}
