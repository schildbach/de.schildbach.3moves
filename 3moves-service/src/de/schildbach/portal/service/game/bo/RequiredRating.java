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

package de.schildbach.portal.service.game.bo;

import java.io.Serializable;

/**
 * @author Andreas Schildbach
 */
public class RequiredRating implements Serializable
{
	private String min;
	private String max;

	public RequiredRating()
	{
	}

	public RequiredRating(String min, String max)
	{
		setMin(min);
		setMax(max);
	}

	public boolean isDefined()
	{
		return min != null || max != null;
	}
	
	public final String getMin()
	{
		return min;
	}
	public final void setMin(String min)
	{
		this.min = min;
	}
	
	public final String getMax()
	{
		return max;
	}
	public final void setMax(String max)
	{
		this.max = max;
	}
	
	@Override
	public String toString()
	{
		return
		"[min=" + min + 
		",max=" + max + "]";
	}
}
