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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
public class StandardLayoutController extends ParameterizableViewController
{
	private String linkHome;
	private int defaultResolution;
	private int rightSidebarResolutionThreshold;
	private int leftSidebarResolutionThreshold;
	private Environment environment;

	@Required
	public void setLinkHome(String linkHome)
	{
		this.linkHome = linkHome;
	}

	@Required
	public void setDefaultResolution(int defaultResolution)
	{
		this.defaultResolution = defaultResolution;
	}

	@Required
	public void setRightSidebarResolutionThreshold(int rightSidebarResolutionThreshold)
	{
		this.rightSidebarResolutionThreshold = rightSidebarResolutionThreshold;
	}

	@Required
	public void setLeftSidebarResolutionThreshold(int leftSidebarResolutionThreshold)
	{
		this.leftSidebarResolutionThreshold = leftSidebarResolutionThreshold;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		boolean isMinimal = request.getParameter("minimal") != null;

		int screenResolution = defaultResolution;
		if (environment.getScreenResolution() != null)
			screenResolution = environment.getScreenResolution();

		boolean showRightSidebar = !isMinimal && screenResolution >= rightSidebarResolutionThreshold;
		boolean showLeftSidebar = screenResolution >= leftSidebarResolutionThreshold;
		boolean bigNorth = screenResolution >= rightSidebarResolutionThreshold;

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("link_home", request.getContextPath() + linkHome);
		model.put("show_right_sidebar", showRightSidebar);
		model.put("show_left_sidebar", showLeftSidebar);
		model.put("big_north", bigNorth);
		model.put("north_navigation", !showLeftSidebar);
		return new ModelAndView(getViewName(), model);
	}
}
