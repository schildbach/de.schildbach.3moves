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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;

import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
@Entity
@DiscriminatorValue("single")
public class SingleGame extends Game
{
	public static final String PROPERTY_RULES = "rules";
	public static final String PROPERTY_AID = "aid";
	public static final String PROPERTY_INITIAL_BOARD_NOTATION = "initialBoardNotation";
	public static final String PROPERTY_MARSHALLED_GAME = "marshalledGame";
	public static final String PROPERTY_HISTORY_NOTATION = "historyNotation";
	public static final String PROPERTY_POSITION_NOTATION = "positionNotation";
	public static final String PROPERTY_LAST_MOVE_NOTATION = "lastMoveNotation";
	public static final String PROPERTY_ACTIVE_PLAYER = "activePlayer";
	public static final String PROPERTY_LAST_ACTIVE_AT = "lastActiveAt";
	public static final String PROPERTY_LAST_REMINDER_AT = "lastReminderAt";
	public static final String PROPERTY_PARENT_GAME = "parentGame";

	private Rules rules;
	private String initialHistoryNotation;
	private String clockConstraint;
	private Rating rating;
	private Aid aid;
	private String initialBoardNotation;
	private String marshalledGame;
	private String historyNotation;
	private String positionNotation;
	private String lastMoveNotation;
	private boolean remisOffer;
	private GamePlayer activePlayer;
	private Date lastActiveAt;
	private Date lastReminderAt;
	private GameGroup parentGame;

	protected SingleGame()
	{
	}

	public SingleGame(Subject owner, Date createdAt, Rules rules, String initialHistoryNotation, Rating rating, Aid aid, GameGroup parentGame)
	{
		super(owner, createdAt);
		this.setRules(rules);
		this.setInitialHistoryNotation(initialHistoryNotation);
		this.setRating(rating);
		this.setAid(aid);
		this.setParentGame(parentGame);
	}

	@Override
	public void accept(GameVisitor visitor)
	{
		visitor.visit(this);
	}

	@NotNull
	@Column(name = "rules", updatable = false)
	@Enumerated(EnumType.STRING)
	public Rules getRules()
	{
		return this.rules;
	}

	private void setRules(Rules rules)
	{
		this.rules = rules;
	}

	@Column(name = "initial_history", updatable = false)
	public String getInitialHistoryNotation()
	{
		return this.initialHistoryNotation;
	}

	private void setInitialHistoryNotation(String initialHistoryNotation)
	{
		this.initialHistoryNotation = initialHistoryNotation;
	}

	@Column(name = "clock_constraint")
	public String getClockConstraint()
	{
		return this.clockConstraint;
	}

	public void setClockConstraint(String clockConstraint)
	{
		this.clockConstraint = clockConstraint;
	}

	@Column(name = "rating", updatable = false)
	@Enumerated(EnumType.STRING)
	public Rating getRating()
	{
		return this.rating;
	}

	private void setRating(Rating rating)
	{
		this.rating = rating;
	}

	@NotNull
	@Column(name = "aid", updatable = false)
	@Enumerated(EnumType.STRING)
	public Aid getAid()
	{
		return aid;
	}

	private void setAid(Aid aid)
	{
		this.aid = aid;
	}

	@Column(name = "initial_board_notation")
	public String getInitialBoardNotation()
	{
		return initialBoardNotation;
	}

	public void setInitialBoardNotation(String initialBoardNotation)
	{
		this.initialBoardNotation = initialBoardNotation;
	}

	@Column(name = "marshalled_game")
	public String getMarshalledGame()
	{
		return marshalledGame;
	}

	public void setMarshalledGame(String marshalledGame)
	{
		this.marshalledGame = marshalledGame;
	}

	@Column(name = "history")
	public String getHistoryNotation()
	{
		return this.historyNotation;
	}

	public void setHistoryNotation(String historyNotation)
	{
		this.historyNotation = historyNotation;
	}

	@Column(name = "`position`")
	public String getPositionNotation()
	{
		return this.positionNotation;
	}

	public void setPositionNotation(String positionNotation)
	{
		this.positionNotation = positionNotation;
	}

	@Column(name = "last_move")
	public String getLastMoveNotation()
	{
		return this.lastMoveNotation;
	}

	public void setLastMoveNotation(String lastMoveNotation)
	{
		this.lastMoveNotation = lastMoveNotation;
	}

	@NotNull
	@Column(name = "remis_offer")
	public boolean getRemisOffer()
	{
		return this.remisOffer;
	}

	public void setRemisOffer(boolean remisOffer)
	{
		this.remisOffer = remisOffer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "active_player_id")
	public GamePlayer getActivePlayer()
	{
		return this.activePlayer;
	}

	public void setActivePlayer(GamePlayer activePlayer)
	{
		this.activePlayer = activePlayer;
	}

	@Column(name = "last_active_at", updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastActiveAt()
	{
		return this.lastActiveAt;
	}

	public void setLastActiveAt(Date lastActiveAt)
	{
		this.lastActiveAt = lastActiveAt;
	}

	@Column(name = "last_reminder_at", updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastReminderAt()
	{
		return this.lastReminderAt;
	}

	public void setLastReminderAt(Date lastReminderAt)
	{
		this.lastReminderAt = lastReminderAt;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_game_id", updatable = false)
	public GameGroup getParentGame()
	{
		return this.parentGame;
	}

	private void setParentGame(GameGroup parentGame)
	{
		this.parentGame = parentGame;
	}
}
