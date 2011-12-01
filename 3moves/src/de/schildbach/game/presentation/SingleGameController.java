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

package de.schildbach.game.presentation;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.game.BoardGeometry;
import de.schildbach.game.Game;
import de.schildbach.game.GameMove;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.PieceCapturing;
import de.schildbach.game.GameRules.FormatGameArrayElement;
import de.schildbach.game.presentation.board.BaseBoardController;
import de.schildbach.game.presentation.board.CapturedPiecesRowController;
import de.schildbach.game.presentation.board.RuleBasedBoardUIState;
import de.schildbach.portal.persistence.game.GameConditionalMoves;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.user.presentation.Environment;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class SingleGameController extends SimpleFormController
{
	@SuppressWarnings("unused")
	protected static final Log LOG = LogFactory.getLog(SingleGameController.class);

	private static final String CLICK_ACTION = "s";
	private static final String SET_CURSOR_ACTION = "cursor";
	private static final String DISQUALIFY_ACTIVE_PLAYER_ACTION = "disqualify_active_player";
	private static final String REMOVE_CONDITIONAL_MOVES_ACTION = "remove_conditional_moves";
	private static final String VALUE_CONFIRM = "confirm";

	private int showHintThreshold;
	private Environment environment;
	private GameViewHelper gameViewHelper;
	private GameService gameService;
	private PresenceService presenceService;

	@Required
	public void setShowHintThreshold(int showHintThreshold)
	{
		this.showHintThreshold = showHintThreshold;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

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
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		return request.getParameter(ViewGameController.REQUEST_PARAMETER_ID) == null;
	}

	private boolean isClick(HttpServletRequest request)
	{
		return request.getParameter(CLICK_ACTION) != null;
	}

	private boolean isSetMove(HttpServletRequest request)
	{
		return request.getParameter("set_move") != null;
	}

	private boolean isClearMove(HttpServletRequest request)
	{
		return request.getParameter("clear_move") != null;
	}

	private boolean isUndoMove(HttpServletRequest request)
	{
		return request.getParameter("undo_move") != null;
	}

	private boolean isUndoIrreal(HttpServletRequest request)
	{
		return request.getParameter("undo_irreal") != null;
	}

	private boolean isUndoAll(HttpServletRequest request)
	{
		return request.getParameter("undo_all") != null;
	}

	private boolean isCommitMove(HttpServletRequest request)
	{
		return request.getParameter("commit_move") != null;
	}

	private boolean isSetCursor(HttpServletRequest request)
	{
		return request.getParameter(SET_CURSOR_ACTION) != null;
	}

	private boolean isBackFromHistory(HttpServletRequest request)
	{
		return request.getParameter("back_from_history") != null;
	}

	private boolean isResignGame(HttpServletRequest request)
	{
		return request.getParameter("resign_game") != null;
	}

	private boolean isRemis(HttpServletRequest request)
	{
		return request.getParameter("remis") != null;
	}

	private boolean isFlipBoard(HttpServletRequest request)
	{
		return request.getParameter("flip_board") != null;
	}

	private boolean isAddConditionalMoves(HttpServletRequest request)
	{
		return request.getParameter("add_conditional_moves") != null;
	}

	private boolean isRemoveConditionalMoves(HttpServletRequest request)
	{
		return request.getParameter(REMOVE_CONDITIONAL_MOVES_ACTION) != null;
	}

	private boolean isDisqualifyActivePlayer(HttpServletRequest request)
	{
		return request.getParameter(DISQUALIFY_ACTIVE_PLAYER_ACTION) != null;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		SingleGame singleGame = (SingleGame) gameViewHelper.getGame(request);
		int gameId = singleGame.getId();
		GameRules rules = GameRulesHelper.rules(singleGame);
		Game game = GameRulesHelper.game(singleGame);
		autoExecuteSubsequentMoves(game, rules);
		RuleBasedBoardUIState state = RuleBasedBoardUIState.getInstance(rules, game.getActualPosition(), game.getInitialPosition().getBoard());
		state.setMove(game.getLastMove());
		Command command = new Command(game, state);
		command.setRealGame(GameRulesHelper.game(singleGame));
		command.setRealGameLastActiveAt(singleGame.getLastActiveAt());
		GamePlayer gamePlayer = gameService.player(request.getRemoteUser(), gameId);
		command.setFlip(gamePlayer != null && gamePlayer.getPosition() != 0);
		return command;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	private void autoExecuteSubsequentMoves(Game game, GameRules gameRules)
	{
		Collection<? extends GameMove> allowedMoves = gameRules.allowedMoves(game);
		while (allowedMoves.size() == 1)
		{
			GameMove move = allowedMoves.iterator().next();
			gameRules.executeMove(game, move);
			allowedMoves = gameRules.allowedMoves(game);
		}
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Command command = (Command) commandObj;

		Map<String, Object> model = new HashMap<String, Object>();

		Date now = RequestTime.get();

		// persistant objects
		SingleGame hGame = (SingleGame) gameViewHelper.getGame(request);
		int gameId = hGame.getId();
		GamePlayer hGamePlayer = gameService.player(request.getRemoteUser(), gameId);
		GamePlayer activePlayer = hGame.getActivePlayer();
		GameRules rules = GameRulesHelper.rules(hGame);

		// update real game
		if (!ObjectUtils.equals(command.getRealGameLastActiveAt(), hGame.getLastActiveAt()))
		{
			command.setRealGame(GameRulesHelper.game(hGame));
			command.setRealGameLastActiveAt(hGame.getLastActiveAt());
		}

		// business objects
		Game realGame = command.getRealGame();
		BoardGeometry geometry = rules.getBoardGeometry();
		RuleBasedBoardUIState ui = command.getUi();
		Game game = command.getGame();

		// conditions
		boolean isPlayer = hGamePlayer != null;
		boolean isActive = ObjectUtils.equals(activePlayer, hGamePlayer);
		boolean isGameStartReal = game.startsWith(realGame);
		boolean isGameReal = isGameStartReal && game.getSize() == realGame.getSize();
		boolean isGameIrreal = !isGameStartReal && !realGame.startsWith(game);
		boolean isInHistory = command.getCursorIndex() != null;

		// update last user activity; overwrite activity determined in ViewGameController
		if (isInHistory)
			presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_HISTORY);
		else if (game.getSize() > realGame.getSize() + 1)
			presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_ANALYZE);

		// attributes for included view
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, ui.getGameBoard());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, geometry);
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS, ui.getCursors());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CLICKABLES, ui.getClickables());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_MARKERS, ui.getMarkers());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_CLICK_ACTION, "?" + CLICK_ACTION + "={0}");
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_FLIP, command.isFlip());

		GamePosition position = !isInHistory ? game.getActualPosition() : game.getPosition(command.getCursorIndex());
		if (position instanceof PieceCapturing)
		{
			model.put(CapturedPiecesRowController.REQUEST_ATTRIBUTE_CAPTURED_PIECES_BY_PLAYER, ((PieceCapturing) position)
					.getCapturedPiecesByPlayer());
			model.put(CapturedPiecesRowController.REQUEST_ATTRIBUTE_PIECESET, rules.getPieceSet());
		}

		// attributes for this view
		model.put("game_id", gameId);
		model.put("flip_board", command.isFlip());
		model.put("players", hGame.getPlayers());
		model.put("active_player", hGame.getActivePlayer());
		model.put("color", rules.getPieceSet().getColorTags());
		model.put("special_attributes", GameRulesHelper.specialAttributes(game, rules));
		model.put("remis_offer", hGame.getRemisOffer());
		FormatGameArrayElement[] notationArray = rules.formatGameArray(game, request.getLocale());
		FormatGameArrayElement[] realNotationArray = rules.formatGameArray(realGame, request.getLocale());
		model.put("notation_array", notationArray);
		model.put("notation_array_class", determineNotationArrayClass(notationArray, realNotationArray, isActive, isPlayer ? new Integer(hGamePlayer
				.getPosition()) : null));
		model.put("notation_array_cursor_index", command.getCursorIndex());

		if (hGame.getClockConstraint() != null)
		{
			long[] clocks = new long[hGame.getPlayers().size()];
			for (GamePlayer player : hGame.getPlayers())
			{
				long clock = player.getClock();
				if (player.equals(hGame.getActivePlayer()))
					clock -= now.getTime() - hGame.getLastActiveAt().getTime();
				clocks[player.getPosition()] = clock;
			}
			model.put("clock", clocks);
		}

		if (isInHistory)
		{
			model.put("show_history_controls", true);
			if (command.getCursorIndex() > 0)
				model.put("history_back_index", command.getCursorIndex() - 1);
			if (command.getCursorIndex() < game.getSize() - 1)
				model.put("history_forward_index", command.getCursorIndex() + 1);
		}

		boolean showDisqualifyActivePlayerConfirm = request.getParameter(DISQUALIFY_ACTIVE_PLAYER_ACTION) != null
				&& !request.getParameter(DISQUALIFY_ACTIVE_PLAYER_ACTION).equalsIgnoreCase(VALUE_CONFIRM);
		model.put("show_disqualify_active_player_confirm", showDisqualifyActivePlayerConfirm);

		if (environment.getScreenResolution() == null || environment.getScreenResolution() >= showHintThreshold)
		{
			if (!showDisqualifyActivePlayerConfirm)
			{
				if (!isInHistory && isGameReal)
				{
					if (ui.isInitial())
						model.put("hint", "initial");
					else
						model.put("hint", "intermediate");
				}
				else if (ui.isInitial() && game.getSize() == realGame.getSize() + 1)
				{
					model.put("hint", "distinct");
				}
			}
		}

		model.put("actual_position_notation", rules.formatPosition(game.getActualPosition()));
		model.put("board_notation", rules.formatBoard(ui.getGameBoard()));
		model.put("rules", hGame.getRules());

		int activePlayerIndex = game.getActualPosition().getActivePlayerIndex();
		if (isInHistory)
			model.put("move_label", "is_in_history");
		else if (isPlayer && activePlayerIndex == hGamePlayer.getPosition())
			model.put("move_label", "move_you");
		else
			model.put("move_label", "move_" + rules.getPieceSet().getColorTag(activePlayerIndex));

		boolean isEnteringMove = isGameStartReal && gameService.canCommitMove(request.getRemoteUser(), gameId);
		boolean canCommitMove = isEnteringMove && game.getSize() > realGame.getSize();
		model.put("can_set_move", !isInHistory && isPlayer);
		model.put("can_commit_move", canCommitMove);
		model.put("can_add_conditional_moves", isPlayer && !canCommitMove && game.getSize() > realGame.getSize() + 1);
		model.put("can_clear_move", !isInHistory && !ui.isInitial());
		model.put("can_undo_move", !isGameReal && ui.isInitial());
		model.put("can_undo_all", !isGameReal && !isGameIrreal);
		model.put("can_undo_irreal", isGameIrreal);
		model.put("can_remis", isEnteringMove);
		model.put("can_resign_game", isEnteringMove);
		model.put("can_view_notation", !isInHistory && isGameReal);
		model.put("can_back_from_history", isInHistory);
		model.put("can_remind_active_player", isGameReal && gameService.canRemindActivePlayer(request.getRemoteUser(), gameId));
		model.put("can_disqualify_active_player", isGameReal && gameService.canDisqualifyActivePlayer(request.getRemoteUser(), gameId));
		model.put("can_invite_for_second_leg", isGameReal && gameService.canCreateSecondLeg(request.getRemoteUser(), gameId));

		model.put("offer_remis", command.isOfferRemis());
		model.put("show_claim_remis_confirm", command.isClaimRemisConfirm());
		model.put("show_resign_confirm", command.isResignConfirm());
		if (gameService.canRemisBeClaimed(request.getRemoteUser(), gameId))
			model.put("remis_right", "claim");
		else if (gameService.canRemisBeAccepted(request.getRemoteUser(), gameId))
			model.put("remis_right", "accept");

		if (isPlayer && hGame.getState() == GameState.RUNNING)
		{
			Map<Integer, String> conditionalMoves = new HashMap<Integer, String>();
			for (GameConditionalMoves gameConditionalMoves : hGamePlayer.getConditionalMoves())
			{
				GamePosition actualPosition = realGame.getActualPosition();
				Game conditionalGame = GameRulesHelper.game(gameConditionalMoves);
				conditionalMoves.put(gameConditionalMoves.getId(), rules.formatGame(conditionalGame, actualPosition.getFullmoveNumber(),
						actualPosition.getActivePlayerIndex(), request.getLocale()));
			}
			model.put("conditional_moves", conditionalMoves);
		}

		return model;
	}

	private Map<FormatGameArrayElement, String> determineNotationArrayClass(FormatGameArrayElement[] notationArray,
			FormatGameArrayElement[] realNotationArray, boolean isActive, Integer playerIndex)
	{
		final String CSSCLASS_FUTURE = "future";
		final String CSSCLASS_REAL = "real";
		final String CSSCLASS_UNREAL = "unreal";
		final String CSSCLASS_YOURMOVE = "move";
		final String CSSCLASS_CONDITIONAL = "conditional";

		Map<FormatGameArrayElement, String> notationArrayClass = new HashMap<FormatGameArrayElement, String>();

		boolean unreal = false;
		boolean conditional = false;

		for (int i = 0; i < notationArray.length; i++)
		{
			String cssClass;

			if (unreal)
			{
				cssClass = CSSCLASS_UNREAL;
			}
			else if (i < realNotationArray.length)
			{
				if (notationArray[i].equals(realNotationArray[i]))
				{
					cssClass = CSSCLASS_REAL;
				}
				else
				{
					unreal = true;
					cssClass = CSSCLASS_UNREAL;
				}
			}
			else if (isActive && (i == realNotationArray.length || (i == realNotationArray.length + 1 && !notationArray[i].isLabel())))
			{
				cssClass = CSSCLASS_YOURMOVE;
			}
			else if (playerIndex != null)
			{
				conditional = true;
				cssClass = CSSCLASS_CONDITIONAL;
			}
			else
			{
				cssClass = CSSCLASS_FUTURE;
			}

			notationArrayClass.put(notationArray[i], cssClass);
		}

		if (conditional)
		{
			int i = notationArray.length - 1;
			while (i >= 0 && (notationArray[i].isLabel() || !notationArray[i].getPlayerIndex().equals(playerIndex)))
				notationArrayClass.put(notationArray[i--], CSSCLASS_FUTURE);
		}

		return notationArrayClass;
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Command command = (Command) commandObj;

		Principal user = request.getUserPrincipal();

		SingleGame hGame = (SingleGame) gameViewHelper.getGame(request);
		int gameId = hGame.getId();
		Game realGame = command.getRealGame();
		GameRules rules = GameRulesHelper.rules(hGame);

		RuleBasedBoardUIState ui = command.getUi();
		Game game = command.getGame();

		if (isClick(request))
		{
			// click
			ui.click(request.getParameter(CLICK_ACTION));

			GameMove move = ui.getDistinctMove();
			if (move != null)
			{
				// rewind game if breaking out of history
				if (command.getCursorIndex() != null)
				{
					rules.rewind(game, command.getCursorIndex() + 1);
					command.setCursorIndex(null);
				}

				// execute
				rules.executeMove(game, move);

				// auto-execute subsequent moves
				autoExecuteSubsequentMoves(game, rules);

				// synchronize ui
				ui.setMove(game.getLastMove());
				ui.setPosition(game.getActualPosition());
			}
		}
		else if (isClearMove(request))
		{
			// clear
			ui.clear();
		}
		else if (isUndoMove(request))
		{
			if (game.getSize() > 0)
			{
				// undo
				rules.undoLastMove(game);

				// synchronize ui
				ui.setMove(game.getLastMove());
				ui.setPosition(game.getActualPosition());
			}
		}
		else if (isCommitMove(request))
		{
			if (gameService.canCommitMove(request.getRemoteUser(), gameId) && game.getSize() > realGame.getSize())
			{
				// commit move
				gameService.commitMove(user.getName(), gameId, game.getMove(realGame.getSize()), command.isOfferRemis());
				command.setOfferRemis(false);

				// refresh real game
				hGame = (SingleGame) gameService.game(gameId);
				gameViewHelper.setGame(request, hGame);
				realGame = GameRulesHelper.game(hGame);
				command.setRealGame(realGame);
				command.setRealGameLastActiveAt(hGame.getLastActiveAt());

				// refresh game
				if (realGame.startsWith(game))
				{
					game = GameRulesHelper.game(hGame);
					autoExecuteSubsequentMoves(game, rules);
					command.setGame(game);
				}

				// synchronize ui
				ui.setMove(game.getLastMove());
				ui.setPosition(game.getActualPosition());
			}
		}
		else if (isSetMove(request))
		{
			if (command.getMove() != null)
			{
				// rewind game if breaking out of history
				if (command.getCursorIndex() != null)
				{
					rules.rewind(game, command.getCursorIndex() + 1);
					command.setCursorIndex(null);
				}

				// execute moves
				String remainingMoves = rules.executeMoves(game, command.getMove(), request.getLocale());

				// auto-execute subsequent moves
				autoExecuteSubsequentMoves(game, rules);

				// synchronize ui
				ui.setMove(game.getLastMove());
				ui.setPosition(game.getActualPosition());

				// update form field
				command.setMove(remainingMoves);

				if (remainingMoves != null)
					errors.rejectValue("move", "invalid");
			}
		}
		else if (isSetCursor(request))
		{
			int cursorIndex = ServletRequestUtils.getIntParameter(request, SET_CURSOR_ACTION);
			if (cursorIndex >= game.getSize() - 1)
			{
				command.setCursorIndex(null);

				// synchronize ui
				ui.setMove(game.getLastMove());
				ui.setPosition(game.getActualPosition());
			}
			else
			{
				command.setCursorIndex(cursorIndex);

				// synchronize ui
				ui.setMove(game.getMove(cursorIndex));
				ui.setPosition(game.getPosition(cursorIndex));
			}
		}
		else if (isBackFromHistory(request))
		{
			command.setCursorIndex(null);

			// synchronize ui
			ui.setMove(game.getLastMove());
			ui.setPosition(game.getActualPosition());
		}
		else if (isUndoIrreal(request))
		{
			// rewind
			while (!game.startsWith(realGame) && !realGame.startsWith(game))
			{
				rules.undoLastMove(game);
			}

			if (command.getCursorIndex() != null && command.getCursorIndex() >= game.getSize())
				command.setCursorIndex(null);

			// synchronize ui
			ui.setMove(game.getLastMove());
			ui.setPosition(game.getActualPosition());
		}
		else if (isUndoAll(request))
		{
			game = GameRulesHelper.game(hGame);
			autoExecuteSubsequentMoves(game, rules);
			command.setGame(game);

			// synchronize ui
			ui.setMove(game.getLastMove());
			ui.setPosition(game.getActualPosition());
		}
		else if (isRemis(request))
		{
			if (gameService.canRemisBeClaimed(user.getName(), gameId) || gameService.canRemisBeAccepted(user.getName(), gameId))
			{
				String confirm = request.getParameter("confirm");
				if (confirm == null)
				{
					command.setClaimRemisConfirm(!command.isClaimRemisConfirm());
				}
				else
				{
					if (confirm.equals("true"))
					{
						if (gameService.canRemisBeClaimed(user.getName(), gameId))
							gameService.claimRemis(user.getName(), gameId);
						else
							gameService.acceptRemis(user.getName(), gameId);
					}
					command.setClaimRemisConfirm(false);
				}
			}
			else
			{
				command.setOfferRemis(!command.isOfferRemis());
			}
		}
		else if (isResignGame(request))
		{
			if (ServletRequestUtils.getBooleanParameter(request, "confirm", false))
			{
				gameService.resignGame(user.getName(), gameId);
				command.setResignConfirm(false);
			}
			else
			{
				command.setResignConfirm(!command.isResignConfirm());
			}
		}
		else if (isFlipBoard(request))
		{
			command.setFlip(!command.isFlip());
		}
		else if (isAddConditionalMoves(request))
		{
			// set conditional moves
			gameService.addConditionalMoves(user.getName(), gameId, rules.formatGame(game, Locale.ENGLISH));
		}
		else if (isRemoveConditionalMoves(request))
		{
			// remove conditional moves
			gameService.removeConditionalMoves(user.getName(), gameId, Integer.parseInt(request.getParameter(REMOVE_CONDITIONAL_MOVES_ACTION)));
		}
		else if (isDisqualifyActivePlayer(request))
		{
			if (request.getParameter(DISQUALIFY_ACTIVE_PLAYER_ACTION).equalsIgnoreCase("confirm"))
				gameService.disqualifyActivePlayer(user.getName(), gameId);
		}

		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
		private Game realGame;
		private Date realGameLastActiveAt;
		private Game game;
		private RuleBasedBoardUIState ui;
		private String move;
		private boolean flip;
		private Integer cursorIndex;
		private boolean offerRemis;
		private boolean claimRemisConfirm;
		private boolean resignConfirm;

		public Command(Game game, RuleBasedBoardUIState ui)
		{
			setGame(game);
			setUi(ui);
		}

		public Game getRealGame()
		{
			return realGame;
		}

		public void setRealGame(Game realGame)
		{
			this.realGame = realGame;
		}

		public Date getRealGameLastActiveAt()
		{
			return realGameLastActiveAt;
		}

		public void setRealGameLastActiveAt(Date realGameLastActiveAt)
		{
			this.realGameLastActiveAt = realGameLastActiveAt;
		}

		public Game getGame()
		{
			return game;
		}

		public void setGame(Game game)
		{
			this.game = game;
		}

		public RuleBasedBoardUIState getUi()
		{
			return ui;
		}

		public void setUi(RuleBasedBoardUIState ui)
		{
			this.ui = ui;
		}

		public String getMove()
		{
			return move;
		}

		public void setMove(String move)
		{
			this.move = move;
		}

		public boolean isFlip()
		{
			return flip;
		}

		public void setFlip(boolean flip)
		{
			this.flip = flip;
		}

		public Integer getCursorIndex()
		{
			return cursorIndex;
		}

		public void setCursorIndex(Integer cursorIndex)
		{
			this.cursorIndex = cursorIndex;
		}

		public boolean isOfferRemis()
		{
			return offerRemis;
		}

		public void setOfferRemis(boolean offerRemis)
		{
			this.offerRemis = offerRemis;
		}

		public boolean isClaimRemisConfirm()
		{
			return claimRemisConfirm;
		}

		public void setClaimRemisConfirm(boolean claimRemisConfirm)
		{
			this.claimRemisConfirm = claimRemisConfirm;
		}

		public boolean isResignConfirm()
		{
			return resignConfirm;
		}

		public void setResignConfirm(boolean resignConfirm)
		{
			this.resignConfirm = resignConfirm;
		}

		@Override
		public String toString()
		{
			return getClass().getName() + "[ui=" + ui + ",move=" + move + ",flip=" + flip + ",cursorIndex=" + cursorIndex + "]";
		}
	}
}
