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

package de.schildbach.portal.service.game;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import de.schildbach.game.GameMove;
import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.persistence.game.SubjectRatingHistory;
import de.schildbach.portal.service.game.GameServiceImpl.Count;
import de.schildbach.portal.service.game.bo.CreateGameCommand;
import de.schildbach.portal.service.game.bo.CreateMiniTournamentCommand;
import de.schildbach.portal.service.game.bo.CreateTournamentCommand;
import de.schildbach.portal.service.game.bo.RequiredRating;
import de.schildbach.portal.service.game.exception.IllegalReadyAtDateException;
import de.schildbach.portal.service.game.exception.IllegalRequiredRatingException;
import de.schildbach.portal.service.game.exception.IllegalStartAtDateException;
import de.schildbach.portal.service.game.exception.InvalidTargetSubjectException;

/**
 * @author Andreas Schildbach
 */
public interface GameService
{
	CreateGameCommand createGameDefaults();

	int createGame(String username, CreateGameCommand command) throws IllegalStartAtDateException, IllegalRequiredRatingException;

	CreateMiniTournamentCommand createMiniTournamentDefaults();

	boolean canCreateMiniTournament(String username);

	int createMiniTournament(String username, CreateMiniTournamentCommand command) throws IllegalStartAtDateException, IllegalRequiredRatingException;

	CreateTournamentCommand createTournamentDefaults();

	List<RequiredRating> createTournamentRequiredRatingDefaults(String username, Rules rules, Aid aid);

	int[] createTournament(String username, CreateTournamentCommand createGameCommand) throws IllegalStartAtDateException,
			IllegalReadyAtDateException, IllegalRequiredRatingException;

	boolean canCreateSecondLeg(String username, int gameId);

	int createSecondLeg(String username, int gameId);

	Game game(int gameId);

	List<? extends Game> allGames(int maxResults, String orderBy);

	List<GameGroup> runningGameGroups();

	List<GameGroup> selectedGameGroups(int maxResults);

	List<SingleGame> selectedSingleGames(int maxResults);

	List<Game> joinedGames(String subjectName, Class<? extends Game> gameClass, String rules, String stateFilter);

	List<GameGroup> ownedGamegroups(String subjectName, Rules childRules);

	List<SingleGame> activeSingleGames(String subjectName, int maxResults);

	List<Game> openGameInvitations(String subjectName, Class<? extends Game> gameClass, String rules, int maxResults);

	List<Game> personalGameInvitations(String subjectName);

	List<SingleGame> search(Rules rules, Aid aid, String playerName, Set<GameState> states, Boolean hasParent, Integer windowAfterStart);

	boolean canInviteToGame(String username, int gameId);

	String[] invitationText(Locale locale, int gameId);

	void inviteSubjectToGame(String username, int gameId, String targetSubjectname) throws InvalidTargetSubjectException;

	void inviteEMailToGame(String username, int gameId, String fromName, String fromAddr, String toAddr, String subject, String text, String key);

	Game inviteSubjectByKey(String subjectName, int gameId);

	boolean isInvitedToGame(String username, int gameId);

	boolean canRemoveInvitationFromGame(String username, int gameId, String targetSubjectname);

	void removeInvitationFromGame(String username, int gameId, String targetSubjectname);

	boolean canOpenGame(String username, int gameId);

	void openGame(String username, int gameId);

	boolean canGameBeJoined(int gameId);

	boolean canJoinGame(String username, int gameId);

	void joinGame(String username, int gameId);

	boolean canUnjoinGame(String username, int gameId);

	void unjoinGame(String username, int gameId);

	boolean canKickPlayerFromGame(String username, int gameId);

	void kickPlayerFromGame(String username, int gameId, String playername);

	GamePlayer player(String subjectName, int gameId);

	boolean canReadyGame(String username, int gameId);

	void readyGame(String username, int gameId);

	boolean canUnaccomplishGame(String username, int gameId);

	void unaccomplishGame(String username, int gameId);

	boolean canDeleteGame(String username, int gameId);

	void deleteGame(String username, int gameId);

	boolean canCommitMove(String username, int gameId);

	void commitMove(String username, int gameId, GameMove move, boolean offerRemis);

	void addConditionalMoves(String username, int gameId, String movesNotation);

	void removeConditionalMoves(String username, int gameId, int conditionalMovesId);

	boolean canRemisBeClaimed(String username, int gameId);

	void claimRemis(String username, int gameId);

	boolean canRemisBeAccepted(String username, int gameId);

	void acceptRemis(String username, int gameId);

	void resignGame(String username, int gameId);

	boolean canRemindActivePlayer(String username, int gameId);

	void remindActivePlayer(String username, int gameId, String customText);

	boolean canDisqualifyActivePlayer(String username, int gameId);

	void disqualifyActivePlayer(String username, int gameId);

	boolean checkFinishGame(int gameId);

	boolean canReactivateGame(String username, int gameId);

	void reactivateGame(String username, int gameId);

	// ratings

	int updateRatingIndex(Rating rating);

	List<SubjectRating> ratingsToplist(Rating rating, int maxResults);

	List<SubjectRating> ratingsToplist(Rating rating, int topResults, String subjectName, int nearResults);

	List<SubjectRating> ratingsForSubject(String subjectName);

	List<SubjectRatingHistory> ratingHistory(String subjectName, Rating rating);

	// notes & comments

	boolean canAccessPrivatePlayerNotes(String userName, int gameId);

	void setPrivatePlayerNotes(String userName, int gameId, String notes);

	boolean canReadGameComments(String username, int gameId);

	boolean canAddGameComment(String username, int gameId);

	void addGameComment(String username, int gameId, String comment);

	Game viewGame(int gameId);

	int checkGamesWithState(GameState state);

	int checkInactiveSystemReminder();

	int checkInactiveDisqualify();

	int checkClockTimeout();

	SortedMap<Rules, Map<String, Count>> gameStatisticsForSubject(String subjectName, String opponentName, int windowAfterFinish);

	boolean canWatchGame(String userName, int gameId);

	void watchGame(String userName, int gameId);

	boolean canUnwatchGame(String userName, int gameId);

	void unwatchGame(String userName, int gameId);

	List<Game> watchedGames(String name);

	int checkExistsBeginnerTournaments();
}
