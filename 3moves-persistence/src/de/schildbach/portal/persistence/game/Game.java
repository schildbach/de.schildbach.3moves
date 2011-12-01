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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;
import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = Game.TABLE_NAME)
@DiscriminatorColumn(name = "class", discriminatorType = DiscriminatorType.STRING)
@Proxy(lazy = false)
public abstract class Game extends DomainObject implements Comparable<Game>
{
	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_TURN = "turn";
	public static final String PROPERTY_STATE = "state";
	public static final String PROPERTY_RESOLUTION = "resolution";
	public static final String PROPERTY_CLOSED = "closed";
	public static final String PROPERTY_REQUIRED_RATING_MODE = "requiredRatingMode";
	public static final String PROPERTY_OWNER = "owner";
	public static final String PROPERTY_STARTED_AT = "startedAt";
	public static final String PROPERTY_FINISHED_AT = "finishedAt";
	public static final String PROPERTY_PLAYERS = "players";
	public static final String PROPERTY_INVITATIONS = "invitations";
	public static final String PROPERTY_WATCHES = "watches";

	public static final String TABLE_NAME = "games";

	private int id;
	private String name;
	private Integer turn;
	private GameState state;
	private GameResolution resolution;
	private GamePlayer winner;
	private boolean isClosed;
	private String requiredRatingMode;
	private Rating requiredRating;
	private Integer requiredRatingMin;
	private Integer requiredRatingMax;
	private OrderType orderType;
	private Subject owner;
	private Date createdAt;
	private Date readyAt;
	private Date startedAt;
	private Date finishedAt;
	private String comments;

	private List<GamePlayer> players;
	private Set<GameInvitation> invitations;
	private Set<GameWatch> watches;

	protected Game()
	{
	}

	protected Game(Subject owner, Date createdAt)
	{
		this.setOwner(owner);
		this.setCreatedAt(createdAt);
		this.setState(GameState.FORMING);
		this.setRequiredRatingMode("disabled");

		// defaults
		this.setPlayers(new LinkedList<GamePlayer>());
		this.setInvitations(new HashSet<GameInvitation>());
		this.setWatches(new HashSet<GameWatch>());
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof Game))
			return false;
		final Game other = (Game) o;
		return this.getId() == other.getId();
	}

	@Override
	public int hashCode()
	{
		return this.getId();
	}

	public int compareTo(Game other)
	{
		int c = this.getCreatedAt().compareTo(other.getCreatedAt());
		if (c != 0)
			return c;
		if (this.getId() > other.getId())
			return 1;
		if (this.getId() < other.getId())
			return -1;
		return 0;
	}

	public void accept(GameVisitor visitor)
	{
		throw new UnsupportedOperationException();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	public int getId()
	{
		return this.id;
	}

	public void setId(int id) // FIXME shouldn't be public
	{
		this.id = id;
	}

	@Column(name = "name", nullable = true)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "turn", nullable = true)
	public Integer getTurn()
	{
		return this.turn;
	}

	public void setTurn(Integer turn)
	{
		this.turn = turn;
	}

	@NotNull
	@Column(name = "state", nullable = false)
	@Enumerated(EnumType.STRING)
	public GameState getState()
	{
		return this.state;
	}

	public void setState(GameState state)
	{
		this.state = state;
	}

	@Column(name = "resolution", nullable = true)
	@Enumerated(EnumType.STRING)
	public GameResolution getResolution()
	{
		return this.resolution;
	}

	public void setResolution(GameResolution resolution)
	{
		this.resolution = resolution;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winner_player_id", nullable = true)
	public GamePlayer getWinner()
	{
		return this.winner;
	}

	public void setWinner(GamePlayer winner)
	{
		this.winner = winner;
	}

	@NotNull
	@Column(name = "is_closed", nullable = false)
	public boolean isClosed()
	{
		return this.isClosed;
	}

	public void setClosed(boolean isClosed)
	{
		this.isClosed = isClosed;
	}

	@NotNull
	@Column(name = "required_rating_mode", nullable = false)
	public String getRequiredRatingMode()
	{
		return this.requiredRatingMode;
	}

	public void setRequiredRatingMode(String requiredRatingMode)
	{
		this.requiredRatingMode = requiredRatingMode;
	}

	@Column(name = "required_rating", nullable = true)
	@Enumerated(EnumType.STRING)
	public Rating getRequiredRating()
	{
		return this.requiredRating;
	}

	public void setRequiredRating(Rating requiredRating)
	{
		this.requiredRating = requiredRating;
	}

	@Column(name = "required_rating_min", nullable = true)
	public Integer getRequiredRatingMin()
	{
		return this.requiredRatingMin;
	}

	public void setRequiredRatingMin(Integer requiredRatingMin)
	{
		this.requiredRatingMin = requiredRatingMin;
	}

	@Column(name = "required_rating_max", nullable = true)
	public Integer getRequiredRatingMax()
	{
		return this.requiredRatingMax;
	}

	public void setRequiredRatingMax(Integer requiredRatingMax)
	{
		this.requiredRatingMax = requiredRatingMax;
	}

	@Column(name = "order_type", nullable = true)
	@Enumerated(EnumType.STRING)
	public OrderType getOrderType()
	{
		return this.orderType;
	}

	public void setOrderType(OrderType orderType)
	{
		this.orderType = orderType;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", nullable = false, updatable = false)
	public Subject getOwner()
	{
		return this.owner;
	}

	private void setOwner(Subject owner)
	{
		this.owner = owner;
	}

	@NotNull
	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedAt()
	{
		return this.createdAt;
	}

	private void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	@Column(name = "ready_at", nullable = true, updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getReadyAt()
	{
		return this.readyAt;
	}

	public void setReadyAt(Date readyAt)
	{
		this.readyAt = readyAt;
	}

	@Column(name = "started_at", nullable = true, updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getStartedAt()
	{
		return this.startedAt;
	}

	public void setStartedAt(Date startedAt)
	{
		this.startedAt = startedAt;
	}

	@Column(name = "finished_at", nullable = true, updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFinishedAt()
	{
		return this.finishedAt;
	}

	public void setFinishedAt(Date finishedAt)
	{
		this.finishedAt = finishedAt;
	}

	@Column(name = "comments", nullable = true)
	public String getComments()
	{
		return this.comments;
	}

	public void setComments(String comments)
	{
		this.comments = comments;
	}

	@OneToMany(mappedBy = GamePlayer.PROPERTY_GAME, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@IndexColumn(name = "`position`", base = 0)
	public List<GamePlayer> getPlayers()
	{
		return this.players;
	}

	private void setPlayers(List<GamePlayer> players)
	{
		this.players = players;
	}

	public void addPlayer(GamePlayer player)
	{
		assert player != null;
		List<GamePlayer> players = getPlayers();
		player.setPosition(players.size());
		players.add(player);
	}

	public void addPlayer(int index, GamePlayer player)
	{
		assert player != null;
		List<GamePlayer> players = getPlayers();
		players.add(index, player);
		refreshPlayerPositions();
	}

	public void removePlayer(GamePlayer player)
	{
		assert player != null;
		List<GamePlayer> players = getPlayers();
		if (players.remove(player))
			refreshPlayerPositions();
	}

	public void shufflePlayers()
	{
		List<GamePlayer> players = getPlayers();
		Collections.shuffle(players);
		refreshPlayerPositions();
	}

	private void refreshPlayerPositions()
	{
		List<GamePlayer> players = getPlayers();
		for (int i = 0; i < players.size(); i++)
			players.get(i).setPosition(i);
	}

	@OneToMany(mappedBy = GameInvitation.PROPERTY_GAME, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public Set<GameInvitation> getInvitations()
	{
		return this.invitations;
	}

	private void setInvitations(Set<GameInvitation> invitations)
	{
		this.invitations = invitations;
	}

	public void addInvitation(GameInvitation invitation)
	{
		getInvitations().add(invitation);
	}

	public void removeInvitation(GameInvitation invitation)
	{
		getInvitations().remove(invitation);
	}

	@OneToMany(mappedBy = GameWatch.PROPERTY_GAME, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public Set<GameWatch> getWatches()
	{
		return this.watches;
	}

	private void setWatches(Set<GameWatch> watches)
	{
		this.watches = watches;
	}
}
