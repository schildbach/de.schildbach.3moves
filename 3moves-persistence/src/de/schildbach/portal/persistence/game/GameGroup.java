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

package de.schildbach.portal.persistence.game;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.validator.NotNull;

import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
@Entity
@DiscriminatorValue("gamegroup")
public class GameGroup extends Game
{
	public static final String PROPERTY_CHILD_RULES = "childRules";
	public static final String PROPERTY_CHILD_AID = "childAid";

	private Rules childRules;
	private String childInitialHistory;
	private String childClockConstraint;
	private Rating childRating;
	private Aid childAid;
	private int minPlayers;
	private int maxPlayers;

	private Set<SingleGame> childGames;

	protected GameGroup()
	{
	}

	public GameGroup(Subject owner, Date createdAt, Rules childRules, String childInitialHistory, Aid childAid)
	{
		super(owner, createdAt);
		this.setChildRules(childRules);
		this.setChildInitialHistory(childInitialHistory);
		this.setChildAid(childAid);

		// defaults
		this.setChildGames(new TreeSet<SingleGame>());
	}

	@Override
	public void accept(GameVisitor visitor)
	{
		visitor.visit(this);
	}

	@NotNull
	@Column(name = "child_rules", updatable = false)
	@Enumerated(EnumType.STRING)
	public Rules getChildRules()
	{
		return this.childRules;
	}

	public void setChildRules(Rules childRules)
	{
		this.childRules = childRules;
	}

	@Column(name = "child_initial_history", updatable = false)
	public String getChildInitialHistory()
	{
		return this.childInitialHistory;
	}

	public void setChildInitialHistory(String childInitialHistory)
	{
		this.childInitialHistory = childInitialHistory;
	}

	@Column(name = "child_clock_constraint", updatable = false)
	public String getChildClockConstraint()
	{
		return this.childClockConstraint;
	}

	public void setChildClockConstraint(String childClockConstraint)
	{
		this.childClockConstraint = childClockConstraint;
	}

	@Column(name = "child_rating", updatable = false)
	@Enumerated(EnumType.STRING)
	public Rating getChildRating()
	{
		return this.childRating;
	}

	public void setChildRating(Rating childRating)
	{
		this.childRating = childRating;
	}

	@NotNull
	@Column(name = "child_aid", updatable = false)
	@Enumerated(EnumType.STRING)
	public Aid getChildAid()
	{
		return childAid;
	}

	private void setChildAid(Aid childAid)
	{
		this.childAid = childAid;
	}

	@Column(name = "min_players")
	public int getMinPlayers()
	{
		return this.minPlayers;
	}

	public void setMinPlayers(int minPlayers)
	{
		this.minPlayers = minPlayers;
	}

	@Column(name = "max_players")
	public int getMaxPlayers()
	{
		return this.maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers)
	{
		this.maxPlayers = maxPlayers;
	}

	@OneToMany(mappedBy = SingleGame.PROPERTY_PARENT_GAME, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Sort(type = SortType.NATURAL)
	public Set<SingleGame> getChildGames()
	{
		return this.childGames;
	}

	private void setChildGames(Set<SingleGame> childGames)
	{
		this.childGames = childGames;
	}
}
