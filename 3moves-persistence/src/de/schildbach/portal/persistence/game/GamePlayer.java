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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import de.schildbach.persistence.DomainObject;
import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = GamePlayer.TABLE_NAME)
public class GamePlayer extends DomainObject
{
	public static final String PROPERTY_SUBJECT = "subject";
	public static final String PROPERTY_GAME = "game";
	public static final String PROPERTY_PARENT_PLAYER = "parentPlayer";

	public static final String TABLE_NAME = "game_players";

	private int id;
	private Subject subject;
	private Game game;
	private int position;
	private Long clock;
	private Float points;
	private String comment;
	private Date lastSystemReminderAt;
	private String ratingAtStart;
	private String ratingAtFinish;
	private Date joinedAt;

	private Set<GamePlayer> childPlayers;
	private GamePlayer parentPlayer;
	private Set<GameConditionalMoves> conditionalMoves;

	protected GamePlayer()
	{
	}

	public GamePlayer(Game game, Subject subject, Date joinedAt)
	{
		this.setGame(game);
		this.setSubject(subject);
		this.setJoinedAt(joinedAt);

		// defaults
		this.setChildPlayers(new TreeSet<GamePlayer>());
		this.setConditionalMoves(new HashSet<GameConditionalMoves>());
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof GamePlayer))
			return false;
		final GamePlayer other = (GamePlayer) o;
		if (!other.getSubject().equals(this.getSubject()))
			return false;
		if (!other.getGame().equals(this.getGame()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hashCode = this.getSubject().hashCode();
		hashCode *= 29;
		hashCode += this.getGame().hashCode();
		return hashCode;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	public int getId()
	{
		return this.id;
	}

	@SuppressWarnings("unused")
	private void setId(int id)
	{
		this.id = id;
	}

	@NaturalId
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_name", nullable = false, updatable = false)
	public Subject getSubject()
	{
		return this.subject;
	}

	private void setSubject(Subject subject)
	{
		this.subject = subject;
	}

	@NaturalId
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false, updatable = false)
	public Game getGame()
	{
		return this.game;
	}

	private void setGame(Game game)
	{
		this.game = game;
	}

	@Column(name = "`position`", nullable = false)
	public int getPosition()
	{
		return this.position;
	}

	protected void setPosition(int position)
	{
		this.position = position;
	}

	@Column(name = "clock", nullable = true)
	public Long getClock()
	{
		return this.clock;
	}

	public void setClock(Long clock)
	{
		this.clock = clock;
	}

	@Column(name = "points", nullable = true)
	public Float getPoints()
	{
		return this.points;
	}

	public void setPoints(Float points)
	{
		this.points = points;
	}

	@Column(name = "comment", nullable = true)
	public String getComment()
	{
		return this.comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	@Column(name = "last_system_reminder_at", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastSystemReminderAt()
	{
		return this.lastSystemReminderAt;
	}

	public void setLastSystemReminderAt(Date lastSystemReminderAt)
	{
		this.lastSystemReminderAt = lastSystemReminderAt;
	}

	@Column(name = "rating_at_start", nullable = true)
	public String getRatingAtStart()
	{
		return this.ratingAtStart;
	}

	public void setRatingAtStart(String ratingAtStart)
	{
		this.ratingAtStart = ratingAtStart;
	}

	@Column(name = "rating_at_finish", nullable = true)
	public String getRatingAtFinish()
	{
		return this.ratingAtFinish;
	}

	public void setRatingAtFinish(String ratingAtFinish)
	{
		this.ratingAtFinish = ratingAtFinish;
	}

	@Column(name = "joined_at", nullable = true)
	// actually false, but there is legacy data without...
	@Temporal(TemporalType.TIMESTAMP)
	public Date getJoinedAt()
	{
		return joinedAt;
	}

	public void setJoinedAt(Date joinedAt)
	{
		this.joinedAt = joinedAt;
	}

	@OneToMany(mappedBy = GamePlayer.PROPERTY_PARENT_PLAYER, fetch = FetchType.LAZY)
	@Sort(type = SortType.NATURAL)
	public Set<GamePlayer> getChildPlayers()
	{
		return this.childPlayers;
	}

	public void setChildPlayers(Set<GamePlayer> childPlayers)
	{
		this.childPlayers = childPlayers;
	}

	public void addChildPlayer(GamePlayer childPlayer)
	{
		childPlayer.setParentPlayer(this);
		getChildPlayers().add(childPlayer);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_player_id", nullable = true)
	public GamePlayer getParentPlayer()
	{
		return this.parentPlayer;
	}

	public void setParentPlayer(GamePlayer parentPlayer)
	{
		this.parentPlayer = parentPlayer;
	}

	@OneToMany(mappedBy = GameConditionalMoves.PROPERTY_GAME_PLAYER, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public Set<GameConditionalMoves> getConditionalMoves()
	{
		return this.conditionalMoves;
	}

	private void setConditionalMoves(Set<GameConditionalMoves> conditionalMoves)
	{
		this.conditionalMoves = conditionalMoves;
	}
}
