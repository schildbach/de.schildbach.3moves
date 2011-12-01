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

package de.schildbach.game.presentation.forming;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.game.GameRules;
import de.schildbach.game.presentation.GameViewHelper;
import de.schildbach.game.presentation.ViewGameController;
import de.schildbach.game.presentation.board.BaseBoardController;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameInvitation;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.PermissionHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class FormGameController extends SimpleFormController
{
	private static final String REQUEST_PARAMETER_JOIN_GAME = "join_game";
	private static final String REQUEST_PARAMETER_UNJOIN_GAME = "unjoin_game";
	private static final String REQUEST_PARAMETER_REMOVE_PLAYER = "remove_player";
	private static final String REQUEST_PARAMETER_REMOVE_INVITATION = "remove_invitation";
	private static final String REQUEST_PARAMETER_UNACCOMPLISH_GAME = "unaccomplish_game";
	private static final String REQUEST_PARAMETER_READY_GAME = "ready_game";
	private static final String REQUEST_PARAMETER_OPEN_GAME = "open_game";
	private static final String VALUE_CONFIRM = "confirm";

	private GameViewHelper gameViewHelper;
	private GameService gameService;
	private UserService userService;

	@Required
	public void setGameViewHelper(GameViewHelper gameViewHelper)
	{
		this.gameViewHelper = gameViewHelper;
	}

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		return request.getParameter(ViewGameController.REQUEST_PARAMETER_ID) == null;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		return new Command();
	}

	@Override
	protected Map<String, Object> referenceData(final HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		final Map<String, Object> model = new HashMap<String, Object>();

		// remote user
		Principal user = request.getUserPrincipal();
		String username = request.getRemoteUser();

		// game in focus
		Game game = gameViewHelper.getGame(request);

		int gameId = game.getId();

		model.put("game", game);

		Map<GamePlayer, String> locations = new HashMap<GamePlayer, String>();
		for (GamePlayer player : game.getPlayers())
		{
			User playerUser = (User) player.getSubject();
			boolean isSelf = playerUser.equals(user);
			boolean isFriend = false;
			if (user != null)
			{
				SubjectRelation relation = userService.subjectRelation(username, user.getName());
				isFriend = relation != null && relation.isFriend() && relation.getConfirmed() != null && relation.getConfirmed().booleanValue();
			}

			StringBuilder location = new StringBuilder();
			if (playerUser.getCity() != null && PermissionHelper.checkPermission(playerUser.getCityPermission(), isSelf, user != null, isFriend))
				location.append(playerUser.getCity()).append(", ");
			if (playerUser.getCountry() != null
					&& PermissionHelper.checkPermission(playerUser.getCountryPermission(), isSelf, user != null, isFriend))
				location.append(playerUser.getCountry()).append(", ");
			if (location.length() > 0)
				location.setLength(location.length() - 2);
			locations.put(player, location.toString());
		}
		model.put("location", locations);

		boolean isJoined = gameService.player(username, gameId) != null;
		boolean canJoin = gameService.canJoinGame(username, gameId);
		boolean canInvite = gameService.canInviteToGame(username, gameId);
		boolean canOpen = gameService.canOpenGame(username, gameId);

		boolean showJoinGameConfirm = request.getParameter(REQUEST_PARAMETER_JOIN_GAME) != null
				&& !request.getParameter(REQUEST_PARAMETER_JOIN_GAME).equalsIgnoreCase(VALUE_CONFIRM);
		boolean showOpenGameConfirm = request.getParameter(REQUEST_PARAMETER_OPEN_GAME) != null
				&& !request.getParameter(REQUEST_PARAMETER_OPEN_GAME).equalsIgnoreCase(VALUE_CONFIRM);
		boolean showUnaccomplishGameConfirm = request.getParameter(REQUEST_PARAMETER_UNACCOMPLISH_GAME) != null
				&& !request.getParameter(REQUEST_PARAMETER_UNACCOMPLISH_GAME).equalsIgnoreCase(VALUE_CONFIRM);
		boolean showReadyGameConfirm = request.getParameter(REQUEST_PARAMETER_READY_GAME) != null
				&& !request.getParameter(REQUEST_PARAMETER_READY_GAME).equalsIgnoreCase(VALUE_CONFIRM);
		boolean isConfirmShowing = showJoinGameConfirm || showOpenGameConfirm || showUnaccomplishGameConfirm || showReadyGameConfirm;

		model.put("show_join_game_confirm", showJoinGameConfirm);
		model.put("show_open_game_confirm", showOpenGameConfirm);
		model.put("show_unaccomplish_game_confirm", showUnaccomplishGameConfirm);
		model.put("show_ready_game_confirm", showReadyGameConfirm);

		model.put("show_registration_form", user == null);
		model.put("show_instruction_can_join", !isConfirmShowing && canJoin);
		model.put("show_instruction_is_invited", !isConfirmShowing && gameService.isInvitedToGame(username, gameId) && !isJoined);
		model.put("show_instruction_should_invite", !isConfirmShowing && game.isClosed() && canInvite);

		model.put("can_invite", canInvite);
		model.put("can_open", canOpen);
		model.put("can_join", canJoin);
		boolean canUnjoin = gameService.canUnjoinGame(username, gameId);
		model.put("can_unjoin", canUnjoin);
		Map<GamePlayer, Boolean> canUnjoinMap = new HashMap<GamePlayer, Boolean>();
		for (GamePlayer player : game.getPlayers())
			canUnjoinMap.put(player, canUnjoin && player.getSubject().equals(user));
		model.put("can_unjoin_map", canUnjoinMap);
		model.put("can_ready", gameService.canReadyGame(username, gameId));
		model.put("can_unaccomplish", gameService.canUnaccomplishGame(username, gameId));
		model.put("can_remove_player", gameService.canKickPlayerFromGame(username, gameId));
		Map<GameInvitation, Boolean> canRemoveInviation = new HashMap<GameInvitation, Boolean>();
		for (GameInvitation invitation : game.getInvitations())
			canRemoveInviation.put(invitation, canInvite || invitation.getSubject().equals(user));
		model.put("can_remove_invitation", canRemoveInviation);

		// initial history
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame singleGame)
			{
				if (singleGame.getInitialHistoryNotation() != null)
				{
					GameRules rules = GameRulesHelper.rules(singleGame);
					de.schildbach.game.Game initialHistoryGame = GameRulesHelper.gameFromInitialHistory(singleGame);
					model.put("opening_notation", rules.formatGame(initialHistoryGame, request.getLocale()));
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, initialHistoryGame.getActualPosition().getBoard());
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, rules.getBoardGeometry());
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());
					model.put("show_beta_warning", GameRulesHelper.isBeta(singleGame));
				}
			}

			public void visit(GameGroup gameGroup)
			{
				if (gameGroup.getChildInitialHistory() != null)
				{
					GameRules rules = GameRulesHelper.rulesFromChildRules(gameGroup);
					de.schildbach.game.Game initialHistoryGame = GameRulesHelper.gameFromChildInitialHistory(gameGroup);
					model.put("opening_notation", rules.formatGame(initialHistoryGame, request.getLocale()));
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, initialHistoryGame.getActualPosition().getBoard());
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, rules.getBoardGeometry());
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());
					model.put("show_beta_warning", GameRulesHelper.isBeta(gameGroup));
					model.put("show_gamegroup_warning", true);
					model.put("max_players", gameGroup.getMaxPlayers());
					model.put("num_single_games", (gameGroup.getMaxPlayers() - 1) * 2);
				}
			}
		});

		return model;
	}

	private boolean isJoinGame(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_JOIN_GAME) != null;
	}

	private boolean isUnjoinGame(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_UNJOIN_GAME) != null;
	}

	private boolean isRemovePlayer(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_REMOVE_PLAYER) != null;
	}

	private boolean isRemoveInvitation(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_REMOVE_INVITATION) != null;
	}

	private boolean isOpenGame(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_OPEN_GAME) != null;
	}

	private boolean isReadyGame(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_READY_GAME) != null;
	}

	private boolean isUnaccomplishGame(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_UNACCOMPLISH_GAME) != null;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Game game = gameViewHelper.getGame(request);
		int gameId = game.getId();
		String userName = request.getRemoteUser();

		if (userName == null)
			throw new NotAuthorizedException();

		if (isJoinGame(request))
		{
			if (request.getParameter(REQUEST_PARAMETER_JOIN_GAME).equalsIgnoreCase(VALUE_CONFIRM))
				gameService.joinGame(userName, gameId);
		}
		else if (isUnjoinGame(request))
		{
			gameService.unjoinGame(userName, gameId);
		}
		else if (isRemovePlayer(request))
		{
			String playername = request.getParameter(REQUEST_PARAMETER_REMOVE_PLAYER);
			gameService.kickPlayerFromGame(userName, gameId, playername);
		}
		else if (isOpenGame(request))
		{
			if (request.getParameter(REQUEST_PARAMETER_OPEN_GAME).equalsIgnoreCase(VALUE_CONFIRM))
				gameService.openGame(userName, gameId);
		}
		else if (isReadyGame(request))
		{
			if (request.getParameter(REQUEST_PARAMETER_READY_GAME).equalsIgnoreCase(VALUE_CONFIRM))
				gameService.readyGame(userName, gameId);
		}
		else if (isUnaccomplishGame(request))
		{
			if (request.getParameter(REQUEST_PARAMETER_UNACCOMPLISH_GAME).equalsIgnoreCase(VALUE_CONFIRM))
				gameService.unaccomplishGame(userName, gameId);
		}
		else if (isRemoveInvitation(request))
		{
			String subjectName = request.getParameter(REQUEST_PARAMETER_REMOVE_INVITATION);
			gameService.removeInvitationFromGame(userName, gameId, subjectName);
		}

		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
	}
}
