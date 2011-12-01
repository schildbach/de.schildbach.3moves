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

package de.schildbach.user.presentation.settings;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;

/**
 * @author Andreas Schildbach
 */
@Controller
public class SettingsController
{
	public static final String MESSAGE_ATTRIBUTE = "add_address_message";

	private String view;

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@RequestMapping
	public String settings(User user, Model model)
	{
		if (user == null)
			throw new NotAuthorizedException();

		model.addAttribute("user_name", user.getName());
		model.addAttribute("has_password", user.hasPassword());
		if (user.getOpenId() != null)
			model.addAttribute("open_id", user.getOpenId());
		if (user.getEmail() != null)
			model.addAttribute("email", user.getEmail());
		if (user.getXmpp() != null)
			model.addAttribute("xmpp", user.getXmpp());
		model.addAttribute("locale", user.getLocale().toString());
		model.addAttribute("timezone", user.getTimeZone());
		if (user.getScreenResolution() != null)
			model.addAttribute("screen_resolution", user.getScreenResolution());

		return view;
	}
}
