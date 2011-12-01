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

import de.schildbach.game.Game;
import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.service.game.ClockConstraint;

/**
 * @author Andreas Schildbach
 */
public class CreateMiniTournamentCommand implements Serializable
{
	private Rules rules;
	private InvitationType invitationType;
	private int numberOfPlayers;
	private Deadline startAt = new Deadline();
	private ClockConstraint clockConstraint;
	private Aid aid;
	private RequiredRating requiredRating = new RequiredRating();
	private String name;
	private Game opening;

	public Rules getRules()
	{
		return rules;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public InvitationType getInvitationType()
	{
		return invitationType;
	}

	public void setInvitationType(InvitationType invitationType)
	{
		this.invitationType = invitationType;
	}

	public int getNumberOfPlayers()
	{
		return numberOfPlayers;
	}

	public void setNumberOfPlayers(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
	}

	public ClockConstraint getClockConstraint()
	{
		return clockConstraint;
	}

	public void setClockConstraint(ClockConstraint clockConstraint)
	{
		this.clockConstraint = clockConstraint;
	}

	public Aid getAid()
	{
		return aid;
	}

	public void setAid(Aid aid)
	{
		this.aid = aid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Game getOpening()
	{
		return opening;
	}

	public void setOpening(Game opening)
	{
		this.opening = opening;
	}

	public RequiredRating getRequiredRating()
	{
		return requiredRating;
	}

	public void setRequiredRating(RequiredRating requiredRating)
	{
		this.requiredRating = requiredRating;
	}

	public Deadline getStartAt()
	{
		return startAt;
	}

	public void setStartAt(Deadline startAt)
	{
		this.startAt = startAt;
	}
}
