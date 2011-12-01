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
public class NumberOfPlayers implements Serializable
{
	private Integer min;
	private Integer max;

	public NumberOfPlayers()
	{
	}

	public NumberOfPlayers(Integer min, Integer max)
	{
		this.setMin(min);
		this.setMax(max);
	}

	public Integer getMin()
	{
		return min;
	}

	public void setMin(Integer min)
	{
		this.min = min;
	}

	public Integer getMax()
	{
		return max;
	}

	public void setMax(Integer max)
	{
		this.max = max;
	}

	@Override
	public String toString()
	{
		return "[min=" + min + ",max=" + max + "]";
	}
}
