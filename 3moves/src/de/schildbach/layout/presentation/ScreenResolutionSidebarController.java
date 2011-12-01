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

package de.schildbach.layout.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.util.HtmlUtils;

import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ScreenResolutionSidebarController extends ParameterizableViewController
{
	private List<Integer> screenResolutions;
	private String linkSet;
	private Environment environment;

	@Required
	public void setScreenResolutions(List<Integer> screenResolutions)
	{
		this.screenResolutions = screenResolutions;
	}

	@Required
	public void setLinkSet(String linkSet)
	{
		this.linkSet = linkSet;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("screen_resolutions", screenResolutions);
		model.put("selected_resolution", environment.getScreenResolution());
		model.put("link_set", request.getContextPath() + HtmlUtils.htmlEscape(linkSet));
		return new ModelAndView(getViewName(), model);
	}
}
