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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;
import de.schildbach.portal.persistence.CollectionUtils;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
@Repository
public class GameDaoHibernateImpl extends GenericDaoHibernateImpl<Game, Integer> implements GameDao
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(GameDaoHibernateImpl.class);

	public SingleGame newSingleGame(Subject owner, Date createdAt, Rules rules, String initialHistoryNotation, Aid aid, boolean isClosed,
			OrderType orderType, Rating rating, String clockConstraintString, Date readyAt, Date startAt, GameGroup parentGame)
	{
		SingleGame game = new SingleGame(owner, createdAt, rules, initialHistoryNotation, rating, aid, parentGame);
		game.setClosed(isClosed);
		game.setOrderType(orderType);
		game.setClockConstraint(clockConstraintString);
		game.setReadyAt(readyAt);
		game.setStartedAt(startAt);
		return game;
	}

	public SingleGame newSingleGame(Subject owner, Date createdAt, Rules rules, String initialHistoryNotation, Aid aid)
	{
		return new SingleGame(owner, createdAt, rules, initialHistoryNotation, null, aid, null);
	}

	public GameGroup newGameGroup(Subject owner, Date createdAt, Rules childRules, String childInitialHistoryNotation, Aid childAid)
	{
		return new GameGroup(owner, createdAt, childRules, childInitialHistoryNotation, childAid);
	}

	public Game findGame(Class<? extends Game> gameClass, int id)
	{
		return (Game) getHibernateTemplate().get(gameClass, new Integer(id));
	}

	@SuppressWarnings("unchecked")
	public List<Game> findGames(GameState state, Subject joinedSubject, Subject invitedSubject, User watchingUser, Subject owner, Boolean isClosed,
			String requiredRatingMode, Date startAt, Date finishedBefore, int maxResults, String orderBy, boolean eagerFetchPlayers)
	{
		if (eagerFetchPlayers && maxResults > 0)
			LOG.warn("ignoring eagerFetchPlayers because maxResults is set");

		DetachedCriteria criteria = DetachedCriteria.forClass(Game.class);

		criteria.setFetchMode(Game.PROPERTY_PLAYERS, eagerFetchPlayers && maxResults == 0 ? FetchMode.JOIN : FetchMode.SELECT);
		if (state != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_STATE, state));
		if (joinedSubject != null)
			criteria.createCriteria(Game.PROPERTY_PLAYERS).add(Restrictions.eq(GamePlayer.PROPERTY_SUBJECT, joinedSubject));
		if (invitedSubject != null)
			criteria.createCriteria(Game.PROPERTY_INVITATIONS).add(Restrictions.eq(GameInvitation.PROPERTY_SUBJECT, invitedSubject));
		if (watchingUser != null)
			criteria.createCriteria(Game.PROPERTY_WATCHES).add(Restrictions.eq(GameWatch.PROPERTY_USER, watchingUser));
		if (owner != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_OWNER, owner));
		if (isClosed != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_CLOSED, isClosed));
		if (requiredRatingMode != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_REQUIRED_RATING_MODE, requiredRatingMode));
		if (startAt != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_STARTED_AT, startAt));
		if (finishedBefore != null)
			criteria.add(Restrictions.le(Game.PROPERTY_FINISHED_AT, finishedBefore));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		List<Game> results = getHibernateTemplate().findByCriteria(criteria, 0, maxResults);

		if (eagerFetchPlayers && maxResults == 0)
			results = CollectionUtils.removeSequentialDuplicates(results);

		return results;
	}

	@SuppressWarnings("unchecked")
	public List<SingleGame> findSingleGames(Rules[] rules, GameState state, Subject activeSubject, Subject player, Subject invitedSubject,
			Subject owner, Boolean isClosed, String requiredRatingMode, Date startAt, Date lastActivityBefore, Date finishedBefore,
			Date finishedAfter, int maxResults, String orderBy, boolean eagerFetchPlayers)
	{
		if (eagerFetchPlayers && maxResults > 0)
			LOG.warn("ignoring eagerFetchPlayers because maxResults is set");

		DetachedCriteria criteria = DetachedCriteria.forClass(SingleGame.class);

		criteria.setFetchMode(Game.PROPERTY_PLAYERS, eagerFetchPlayers && maxResults == 0 ? FetchMode.JOIN : FetchMode.SELECT);
		if (rules != null && rules.length > 0)
			criteria.add(Restrictions.in(SingleGame.PROPERTY_RULES, rules));
		if (state != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_STATE, state));
		if (activeSubject != null)
			criteria.createCriteria(SingleGame.PROPERTY_ACTIVE_PLAYER).add(Restrictions.eq(GamePlayer.PROPERTY_SUBJECT, activeSubject));
		if (player != null)
			criteria.createCriteria(Game.PROPERTY_PLAYERS).add(Restrictions.eq(GamePlayer.PROPERTY_SUBJECT, player));
		if (invitedSubject != null)
			criteria.createCriteria(Game.PROPERTY_INVITATIONS).add(Restrictions.eq(GameInvitation.PROPERTY_SUBJECT, invitedSubject));
		if (owner != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_OWNER, owner));
		if (isClosed != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_CLOSED, isClosed));
		if (requiredRatingMode != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_REQUIRED_RATING_MODE, requiredRatingMode));
		if (startAt != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_STARTED_AT, startAt));
		if (finishedBefore != null)
			criteria.add(Restrictions.le(Game.PROPERTY_FINISHED_AT, finishedBefore));
		if (finishedAfter != null)
			criteria.add(Restrictions.or(Restrictions.isNull(Game.PROPERTY_FINISHED_AT), Restrictions.ge(Game.PROPERTY_FINISHED_AT, finishedAfter)));
		if (lastActivityBefore != null)
			criteria.add(Restrictions.and(Restrictions.lt(SingleGame.PROPERTY_LAST_ACTIVE_AT, lastActivityBefore), Restrictions.or(Restrictions
					.isNull(SingleGame.PROPERTY_LAST_REMINDER_AT), Restrictions.lt(SingleGame.PROPERTY_LAST_REMINDER_AT, lastActivityBefore))));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		List<SingleGame> results = getHibernateTemplate().findByCriteria(criteria, 0, maxResults);

		if (eagerFetchPlayers && maxResults == 0)
			results = CollectionUtils.removeSequentialDuplicates(results);

		return results;
	}

	@SuppressWarnings("unchecked")
	public List<SingleGame> findSingleGames(Rules rules, Aid aid, User player, Set<GameState> states, Boolean hasParent, Date startAfter,
			int maxResults, String orderBy, boolean eagerFetchPlayers)
	{
		if (eagerFetchPlayers && maxResults > 0)
			LOG.warn("ignoring eagerFetchPlayers because maxResults is set");

		DetachedCriteria criteria = DetachedCriteria.forClass(SingleGame.class);

		criteria.setFetchMode(Game.PROPERTY_PLAYERS, eagerFetchPlayers && maxResults == 0 ? FetchMode.JOIN : FetchMode.SELECT);
		if (rules != null)
			criteria.add(Restrictions.eq(SingleGame.PROPERTY_RULES, rules));
		if (player != null)
			criteria.createCriteria(Game.PROPERTY_PLAYERS).add(Restrictions.eq(GamePlayer.PROPERTY_SUBJECT, player));
		if (states != null)
			criteria.add(Restrictions.in(Game.PROPERTY_STATE, states));
		if (aid != null)
			criteria.add(Restrictions.eq(SingleGame.PROPERTY_AID, aid));
		if (hasParent != null)
		{
			if (hasParent)
				criteria.add(Restrictions.isNotNull(SingleGame.PROPERTY_PARENT_GAME));
			else
				criteria.add(Restrictions.isNull(SingleGame.PROPERTY_PARENT_GAME));
		}
		if (startAfter != null)
			criteria.add(Restrictions.ge(Game.PROPERTY_STARTED_AT, startAfter));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		List<SingleGame> results = getHibernateTemplate().findByCriteria(criteria, 0, maxResults);

		if (eagerFetchPlayers && maxResults == 0)
			results = CollectionUtils.removeSequentialDuplicates(results);

		return results;
	}

	@SuppressWarnings("unchecked")
	public List<GameGroup> findGamegroups(GameState state, Subject joinedSubject, Subject owner, Rules childRules, Aid childAid, Boolean isClosed,
			Date startAt, String requiredRatingMode, Date finishedAfter, int maxResults, String orderBy)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(GameGroup.class);

		if (state != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_STATE, state));
		if (joinedSubject != null)
			criteria.createCriteria(Game.PROPERTY_PLAYERS).add(Restrictions.eq(GamePlayer.PROPERTY_SUBJECT, joinedSubject));
		if (owner != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_OWNER, owner));
		if (childRules != null)
			criteria.add(Restrictions.eq(GameGroup.PROPERTY_CHILD_RULES, childRules));
		if (childAid != null)
			criteria.add(Restrictions.eq(GameGroup.PROPERTY_CHILD_AID, childAid));
		if (isClosed != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_CLOSED, isClosed));
		if (startAt != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_STARTED_AT, startAt));
		if (requiredRatingMode != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_REQUIRED_RATING_MODE, requiredRatingMode));
		if (finishedAfter != null)
			criteria.add(Restrictions.or(Restrictions.isNull(Game.PROPERTY_FINISHED_AT), Restrictions.ge(Game.PROPERTY_FINISHED_AT, finishedAfter)));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria, 0, maxResults);
	}

	@SuppressWarnings("unchecked")
	public List<GameGroup> findGamegroups(GameState[] states, Subject owner, Rules childRules, Date finishedAfter, String orderBy)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(GameGroup.class);

		if (states != null)
			criteria.add(Restrictions.in(Game.PROPERTY_STATE, states));
		if (owner != null)
			criteria.add(Restrictions.eq(Game.PROPERTY_OWNER, owner));
		if (childRules != null)
			criteria.add(Restrictions.eq(GameGroup.PROPERTY_CHILD_RULES, childRules));
		if (finishedAfter != null)
			criteria.add(Restrictions.or(Restrictions.isNull(Game.PROPERTY_FINISHED_AT), Restrictions.ge(Game.PROPERTY_FINISHED_AT, finishedAfter)));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> dumpSingleGames(Rules rules, Integer minId)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SingleGame.class);

		criteria.setProjection(Projections.projectionList().add(Projections.property(Game.PROPERTY_ID)).add(
				Projections.property(SingleGame.PROPERTY_RULES)).add(Projections.property(SingleGame.PROPERTY_INITIAL_BOARD_NOTATION)).add(
				Projections.property(SingleGame.PROPERTY_MARSHALLED_GAME)).add(Projections.property(SingleGame.PROPERTY_HISTORY_NOTATION)).add(
				Projections.property(SingleGame.PROPERTY_POSITION_NOTATION)).add(Projections.property(SingleGame.PROPERTY_LAST_MOVE_NOTATION)).add(
				Projections.property(Game.PROPERTY_STATE)).add(Projections.property(Game.PROPERTY_RESOLUTION)));
		criteria.add(Restrictions.in(Game.PROPERTY_STATE, new GameState[] { GameState.RUNNING, GameState.FINISHED }));
		if (rules != null)
			criteria.add(Restrictions.eq(SingleGame.PROPERTY_RULES, rules));
		if (minId != null)
			criteria.add(Restrictions.ge(SingleGame.PROPERTY_ID, minId));

		return getHibernateTemplate().findByCriteria(criteria);
	}

	public List<Subject> getPlayingSubjects(Game game)
	{
		List<Subject> subjects = new LinkedList<Subject>();
		for (GamePlayer player : game.getPlayers())
		{
			subjects.add(player.getSubject());
		}
		return subjects;
	}
}
