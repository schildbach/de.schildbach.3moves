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

package de.schildbach.presentation;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.RequestHandledEvent;

import de.schildbach.portal.service.game.GameEventService;

/**
 * @author Andreas Schildbach
 */
public class ApplicationContextListener implements ApplicationListener
{
	private GameEventService gameEventService;

	@Required
	public void setGameEventService(GameEventService gameEventService)
	{
		this.gameEventService = gameEventService;
	}

	public void onApplicationEvent(ApplicationEvent event)
	{
		if (event instanceof RequestHandledEvent)
		{
			gameEventService.processCollectedGameEvents();
		}
	}
}
