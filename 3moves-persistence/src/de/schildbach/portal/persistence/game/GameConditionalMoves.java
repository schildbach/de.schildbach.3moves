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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NaturalId;

import de.schildbach.persistence.DomainObject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = GameConditionalMoves.TABLE_NAME)
public class GameConditionalMoves extends DomainObject
{
	public static final String PROPERTY_GAME_PLAYER = "gamePlayer";

	public static final String TABLE_NAME = "game_conditional_moves";

	private int id;
	private GamePlayer gamePlayer;
	private String marshalledMoves;
	private String moves;
	private Date createdAt;

	protected GameConditionalMoves()
	{
	}

	public GameConditionalMoves(GamePlayer gamePlayer, String marshalledMoves, String moves, Date createdAt)
	{
		this.setGamePlayer(gamePlayer);
		this.setMarshalledMoves(marshalledMoves);
		this.setMoves(moves);
		this.setCreatedAt(createdAt);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof GameConditionalMoves))
			return false;
		final GameConditionalMoves other = (GameConditionalMoves) o;
		if (!other.getGamePlayer().equals(this.getGamePlayer()))
			return false;
		if (!other.getMarshalledMoves().equals(this.getMarshalledMoves()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hashCode = this.getGamePlayer().hashCode();
		hashCode *= 29;
		hashCode += this.getMarshalledMoves().hashCode();
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
	@JoinColumn(name = "game_player_id", nullable = false, updatable = false)
	public GamePlayer getGamePlayer()
	{
		return gamePlayer;
	}

	private void setGamePlayer(GamePlayer gamePlayer)
	{
		this.gamePlayer = gamePlayer;
	}

	@NaturalId
	@Column(name = "marshalled_moves", nullable = false, updatable = false)
	public String getMarshalledMoves()
	{
		return marshalledMoves;
	}

	private void setMarshalledMoves(String marshalledMoves)
	{
		this.marshalledMoves = marshalledMoves;
	}

	@Column(name = "moves", nullable = false, updatable = false)
	public String getMoves()
	{
		return moves;
	}

	private void setMoves(String moves)
	{
		this.moves = moves;
	}

	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedAt()
	{
		return createdAt;
	}

	private void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}
}
