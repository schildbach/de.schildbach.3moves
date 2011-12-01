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

/**
 * @author Andreas Schildbach
 */
public class GameHelper implements GameVisitor
{
	private SingleGame singleGame;
	private GameGroup gameGroup;

	public GameHelper(Game game)
	{
		game.accept(this);
	}

	public void visit(SingleGame singleGame)
	{
		this.singleGame = singleGame;
	}

	public void visit(GameGroup gameGroup)
	{
		this.gameGroup = gameGroup;
	}

	public boolean isSingleGame()
	{
		return this.singleGame != null;
	}

	public boolean isGameGroup()
	{
		return this.gameGroup != null;
	}

	public SingleGame getSingleGame()
	{
		return singleGame;
	}

	public GameGroup getGameGroup()
	{
		return gameGroup;
	}
}
