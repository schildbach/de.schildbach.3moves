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

package de.schildbach.game.presentation.rating;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
public class RatingListsController extends ParameterizableViewController
{
	private PresenceService presenceService;
	private Map<Integer, Integer> resolutionThresholds;
	private int defaultNumberOfColumns;
	private Environment environment;

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Required
	public void setResolutionThresholds(Map<Integer, Integer> resolutionThresholds)
	{
		this.resolutionThresholds = resolutionThresholds;
	}

	@Required
	public void setDefaultNumberOfColumns(int defaultNumberOfColumns)
	{
		this.defaultNumberOfColumns = defaultNumberOfColumns;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	{
		// update last user activity
		presenceService.setLastActivity(request.getRemoteUser(), Activity.RATINGS);

		// determine number of columns
		int columns;
		if (environment.getScreenResolution() != null)
		{
			columns = 1;
			for (Map.Entry<Integer, Integer> threshold : resolutionThresholds.entrySet())
			{
				if (threshold.getKey() <= environment.getScreenResolution() && threshold.getValue() > columns)
					columns = threshold.getValue();
			}
		}
		else
		{
			columns = defaultNumberOfColumns;
		}

		// determine number of rows
		int numRatings = Rating.values().length;
		int rows = (int) Math.ceil((double) numRatings / columns);

		// build table
		int ratingsPtr = 0;
		Rating[][] ratingsTable = new Rating[rows][];
		for (int row = 0; row < rows; row++)
		{
			int actualColumns = Math.min(columns, numRatings - ratingsPtr);
			ratingsTable[row] = new Rating[actualColumns];
			for (int column = 0; column < actualColumns; column++)
			{
				ratingsTable[row][column] = Rating.values()[ratingsPtr++];
			}
		}

		// return model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("ratings_table", ratingsTable);
		return new ModelAndView(getViewName(), model);
	}
}
