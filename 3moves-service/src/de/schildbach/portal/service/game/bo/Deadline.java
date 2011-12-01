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
import java.util.Date;

/**
 * @author Andreas Schildbach
 */
public class Deadline implements Serializable
{
	private DeadlineOption option;
	private Date date;
	private int hour;

	public DeadlineOption getOption()
	{
		return option;
	}

	public void setOption(DeadlineOption option)
	{
		this.option = option;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public int getHour()
	{
		return hour;
	}

	public void setHour(int hour)
	{
		this.hour = hour;
	}

	@Override
	public String toString()
	{
		return "[option=" + option + ",date=" + date + ",hour=" + hour + "]";
	}
}
