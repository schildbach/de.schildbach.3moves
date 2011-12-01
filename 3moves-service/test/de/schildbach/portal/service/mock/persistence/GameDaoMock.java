package de.schildbach.portal.service.mock.persistence;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.schildbach.persistence.test.GenericDaoMockImpl;
import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitorAdapter;
import de.schildbach.portal.persistence.game.OrderType;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
public class GameDaoMock extends GenericDaoMockImpl<Game, Integer> implements GameDao
{
	private int id = 0;

	@Override
	protected Integer generateId(Game game)
	{
		int newId = id++;
		game.setId(newId);
		return newId;
	}

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
		return read(id);
	}

	public List<Game> findGames(GameState state, Subject joinedSubject, Subject invitedSubject, User watchingUser, Subject owner, Boolean isClosed,
			String requiredRatingMode, Date startAt, Date finishedBefore, int limit, String orderBy, boolean eagerFetchPlayers)
	{
		List<Game> result = new LinkedList<Game>();
		for (Game game : data())
		{
			if (state != null && !game.getState().equals(state))
				continue;
			if (finishedBefore != null && game.getFinishedAt().after(finishedBefore))
				continue;
			// TODO all other checks
			result.add(game);
		}
		return result;
	}

	public List<SingleGame> findSingleGames(Rules[] rules, final GameState state, Subject activeSubject, Subject player, Subject invitedSubject,
			Subject owner, Boolean isClosed, String requiredRatingMode, Date startAt, final Date lastActivityBefore, final Date finishedBefore,
			Date finishedAfter, int limit, String orderBy, boolean eagerFetchPlayers)
	{
		final List<SingleGame> result = new LinkedList<SingleGame>();
		for (Game game : data())
		{
			game.accept(new GameVisitorAdapter()
			{
				@Override
				public void visit(SingleGame game)
				{
					if (state != null && !game.getState().equals(state))
						return;
					if (finishedBefore != null && game.getFinishedAt().after(finishedBefore))
						return;
					if (lastActivityBefore != null
							&& !(beforeOrEquals(game.getLastActiveAt(), lastActivityBefore) && beforeOrEqualsWithNull(game.getLastReminderAt(),
									lastActivityBefore)))
						return;
					// TODO all other checks
					result.add((SingleGame) game);
				}

				private boolean beforeOrEquals(Date d1, Date d2)
				{
					return !d1.after(d2);
				}

				private boolean beforeOrEqualsWithNull(Date d1, Date d2)
				{
					if (d1 == null)
						return true;
					else if (d2 == null)
						return false;
					else
						return !d1.after(d2);
				}
			});
		}
		return result;
	}

	public List<SingleGame> findSingleGames(Rules rules, Aid aid, User player, Set<GameState> states, Boolean hasParent, Date startAfter,
			int maxResults, String orderBy, boolean eagerFetchPlayers)
	{
		throw new UnsupportedOperationException();
	}

	public List<GameGroup> findGamegroups(GameState state, Subject joinedSubject, Subject owner, Rules childRules, Aid childAid, Boolean isClosed,
			Date startAt, String requiredRatingMode, Date finishedAfter, int maxResults, String orderBy)
	{
		throw new UnsupportedOperationException();
	}

	public List<GameGroup> findGamegroups(GameState[] states, Subject owner, Rules childRules, Date finishedAfter, String orderBy)
	{
		throw new UnsupportedOperationException();
	}

	public List<Object[]> dumpSingleGames(Rules rules, Integer minId)
	{
		throw new UnsupportedOperationException();
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
