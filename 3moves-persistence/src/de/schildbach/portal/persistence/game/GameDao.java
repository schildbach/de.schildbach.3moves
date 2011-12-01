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
import java.util.List;
import java.util.Set;

import de.schildbach.persistence.GenericDao;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
public interface GameDao extends GenericDao<Game, Integer>
{
	SingleGame newSingleGame(Subject owner, Date createdAt, Rules rules, String initialHistoryNotation, Aid aid, boolean isClosed,
			OrderType orderType, Rating rating, String clockConstraintString, Date readyAt, Date startAt, GameGroup parentGame);

	SingleGame newSingleGame(Subject owner, Date createdAt, Rules rules, String initialHistoryNotation, Aid aid);

	GameGroup newGameGroup(Subject owner, Date createdAt, Rules childRules, String childInitialHistoryNotation, Aid childAid);

	Game findGame(Class<? extends Game> gameClass, int id);

	List<Game> findGames(GameState state, Subject joinedSubject, Subject invitedSubject, User watchingUser, Subject owner, Boolean isClosed,
			String requiredRatingMode, Date startAt, Date finishedBefore, int maxResults, String orderBy, boolean eagerFetchPlayers);

	List<SingleGame> findSingleGames(Rules[] rules, GameState state, Subject activeSubject, Subject player, Subject invitedSubject, Subject owner,
			Boolean isClosed, String requiredRatingMode, Date startAt, Date lastActivityBefore, Date finishedBefore, Date finishedAfter,
			int maxResults, String orderBy, boolean eagerFetchPlayers);

	List<SingleGame> findSingleGames(Rules rules, Aid aid, User player, Set<GameState> states, Boolean hasParent, Date startedAfter, int maxResults,
			String orderBy, boolean eagerFetchPlayers);

	List<GameGroup> findGamegroups(GameState state, Subject joinedSubject, Subject owner, Rules childRules, Aid childAid, Boolean isClosed,
			Date startAt, String requiredRatingMode, Date finishedAfter, int maxResults, String orderBy);

	List<GameGroup> findGamegroups(GameState[] states, Subject owner, Rules childRules, Date finishedAfter, String orderBy);

	List<Object[]> dumpSingleGames(Rules rules, Integer minId);

	List<Subject> getPlayingSubjects(Game game);
}
