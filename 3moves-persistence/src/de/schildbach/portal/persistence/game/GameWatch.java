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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import de.schildbach.persistence.DomainObject;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = GameWatch.TABLE_NAME)
public class GameWatch extends DomainObject
{
	public static final String PROPERTY_GAME = "game";
	public static final String PROPERTY_USER = "user";

	public static final String TABLE_NAME = "game_watches";

	private int id;
	private Game game;
	private User user;

	protected GameWatch()
	{
	}

	public GameWatch(Game game, User user)
	{
		this.setGame(game);
		this.setUser(user);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof GameWatch))
			return false;
		final GameWatch other = (GameWatch) o;
		if (!this.getGame().equals(other.getGame()))
			return false;
		if (!this.getUser().equals(other.getUser()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return getGame().hashCode() + 29 * getUser().hashCode();
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
	@JoinColumn(name = "user_name", nullable = false, updatable = false)
	public User getUser()
	{
		return this.user;
	}

	private void setUser(User user)
	{
		this.user = user;
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
}
