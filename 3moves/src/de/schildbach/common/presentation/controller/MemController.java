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

package de.schildbach.common.presentation.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Andreas Schildbach
 */
@Controller
public class MemController extends AbstractController
{
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
		while(rootGroup.getParent() != null)
			rootGroup = rootGroup.getParent();
		int numActiveThreads = rootGroup.activeCount(); 
		Runtime runtime = Runtime.getRuntime();
		response.getWriter().write(runtime.freeMemory() + " " + runtime.totalMemory() + " " + runtime.maxMemory() + " " + numActiveThreads);
		return null;
	}
}
