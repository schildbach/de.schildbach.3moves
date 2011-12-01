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

package de.schildbach.game.presentation.forming;

import java.beans.PropertyEditorSupport;

import de.schildbach.portal.service.game.ClockConstraint;

/**
 * @author Andreas Schildbach
 */
public class ClockConstraintEditor extends PropertyEditorSupport
{
	@Override
	public String getAsText()
	{
		ClockConstraint clockConstraint = (ClockConstraint) getValue();
		if (clockConstraint != null)
			return clockConstraint.getId();
		else
			return "";
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException
	{
		ClockConstraint clockConstraint = null;

		if (!text.equals(""))
		{
			clockConstraint = new ClockConstraint(text);

			if (!ClockConstraint.getAvailableClockConstraints().contains(clockConstraint))
				throw new IllegalArgumentException(text);
		}

		setValue(clockConstraint);
	}
}
