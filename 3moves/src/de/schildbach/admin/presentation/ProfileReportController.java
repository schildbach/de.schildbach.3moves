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

package de.schildbach.admin.presentation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.util.CompareUtils;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ProfileReportController extends ParameterizableViewController
{
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		if (!request.isUserInRole(Role.ADMIN.name()))
			throw new NotAuthorizedException();

		// get monitors
		List<Monitor> monitors = monitors();

		// sort monitors
		String order = request.getParameter("order");
		if ("label".equals(order))
			Collections.sort(monitors, new LabelComparator());
		else if ("hits".equals(order))
			Collections.sort(monitors, new HitsComparator());
		else if ("avg".equals(order))
			Collections.sort(monitors, new AvgComparator());
		else if ("total".equals(order))
			Collections.sort(monitors, new TotalComparator());
		else if ("stdDev".equals(order))
			Collections.sort(monitors, new StdDevComparator());
		else if ("lastVal".equals(order))
			Collections.sort(monitors, new LastValueComparator());
		else if ("min".equals(order))
			Collections.sort(monitors, new MinComparator());
		else if ("max".equals(order))
			Collections.sort(monitors, new MaxComparator());
		else if ("actv".equals(order))
			Collections.sort(monitors, new ActiveComparator());
		else if ("avgActv".equals(order))
			Collections.sort(monitors, new AvgActiveComparator());
		else if ("maxActv".equals(order))
			Collections.sort(monitors, new MaxActiveComparator());
		else
			// default
			Collections.sort(monitors, new TotalComparator());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("monitors", monitors);
		model.put("row_classes", new String[] { "dark", "light" });
		return new ModelAndView(getViewName(), model);
	}

	@SuppressWarnings("unchecked")
	private List<Monitor> monitors()
	{
		List<Monitor> monitors = new LinkedList<Monitor>();
		for (Iterator<Monitor> i = MonitorFactory.getFactory().iterator(); i.hasNext();)
			monitors.add(i.next());
		return monitors;
	}

	private class LabelComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return m1.getLabel().compareTo(m2.getLabel());
		}
	}

	private class HitsComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getHits(), m2.getHits());
		}
	}

	private class AvgComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getAvg(), m2.getAvg());
		}
	}

	private class TotalComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getTotal(), m2.getTotal());
		}
	}

	private class StdDevComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getStdDev(), m2.getStdDev());
		}
	}

	private class LastValueComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getLastValue(), m2.getLastValue());
		}
	}

	private class MinComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getMin(), m2.getMin());
		}
	}

	private class MaxComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getMax(), m2.getMax());
		}
	}

	private class ActiveComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getActive(), m2.getActive());
		}
	}

	private class AvgActiveComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getAvgActive(), m2.getAvgActive());
		}
	}

	private class MaxActiveComparator implements Comparator<Monitor>
	{
		public int compare(Monitor m1, Monitor m2)
		{
			return -CompareUtils.compare(m1.getMaxActive(), m2.getMaxActive());
		}
	}
}
