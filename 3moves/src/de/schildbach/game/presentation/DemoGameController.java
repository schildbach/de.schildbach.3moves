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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;

import de.schildbach.game.Board;
import de.schildbach.game.Game;
import de.schildbach.game.GameMove;
import de.schildbach.game.GameRules;
import de.schildbach.game.PieceCapturing;
import de.schildbach.game.exception.ParseException;
import de.schildbach.game.presentation.board.BaseBoardController;
import de.schildbach.game.presentation.board.CapturedPiecesRowController;
import de.schildbach.game.presentation.board.RuleBasedBoardUIState;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
public class DemoGameController extends AbstractFormController
{
	private static final String CLICK_ACTION = "id";

	private int showHintThreshold;
	private Environment environment;
	private List<Rules> rulesOptions;
	private String formView;

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
	public void setRulesOptions(List<Rules> rulesOptions)
	{
		this.rulesOptions = rulesOptions;
	}

	@Required
	public void setFormView(String formView)
	{
		this.formView = formView;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		Rules rules = Rules.valueOf(ServletRequestUtils.getStringParameter(request, "rules", "CHESS"));
		GameRules gameRules = GameRulesHelper.rules(rules);
		Game game = GameRulesHelper.newGame(rules);

		RuleBasedBoardUIState state = RuleBasedBoardUIState.getInstance(gameRules, game.getActualPosition(), game.getInitialPosition().getBoard());
		return new Command(rules, game, state);
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Command command = (Command) commandObj;

		Map<String, Object> model = new HashMap<String, Object>();

		RuleBasedBoardUIState ui = command.getUi();
		Game game = command.getGame();
		GameRules rules = GameRulesHelper.rules(command.getRules());
		Board board = ui.getGameBoard();

		// attributes for included view
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, board);
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, rules.getBoardGeometry());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CLICKABLES, ui.getClickables());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS, ui.getCursors());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_MARKERS, ui.getMarkers());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_CLICK_ACTION, "?" + CLICK_ACTION + "={0}");
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_FLIP, command.isFlip());

		if (game.getActualPosition() instanceof PieceCapturing)
		{
			model.put(CapturedPiecesRowController.REQUEST_ATTRIBUTE_CAPTURED_PIECES_BY_PLAYER, ((PieceCapturing) game.getActualPosition())
					.getCapturedPiecesByPlayer());
			model.put(CapturedPiecesRowController.REQUEST_ATTRIBUTE_PIECESET, rules.getPieceSet());
		}

		// attributes for this view
		model.put("rules", command.getRules());
		model.put("special_attributes", GameRulesHelper.specialAttributes(game, rules));
		model.put("game_notation", rules.formatGame(game, request.getLocale()));
		model.put("move_notation", ui.getMoveNotation());
		model.put("can_set_move", ui.isInitial() && ui.getNumberOfAllowedMoves() > 1);
		model.put("can_clear_move", !ui.isInitial());
		model.put("can_execute_move", ui.getDistinctMove() != null);
		model.put("can_undo_move", ui.isInitial() && game.getSize() > 0);

		model.put("move_label", "move_" + rules.getPieceSet().getColorTag(game.getActualPosition().getActivePlayerIndex()));

		if (environment.getScreenResolution() == null || environment.getScreenResolution() >= showHintThreshold)
		{
			if (ui.isInitial())
				model.put("hint", "initial");
			else if (ui.getDistinctMove() != null)
				model.put("hint", "distinct");
			else
				model.put("hint", "intermediate");
		}

		if (request.isUserInRole(Role.ADMIN.name()))
		{
			model.put("show_admin_controls", true);
			model.put("position_notation", rules.formatPosition(game.getActualPosition()));
		}

		// attributes for sidebar
		model.put("rules_options", rulesOptions);

		return model;
	}

	@Override
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception
	{
		return showForm(request, errors, formView);
	}

	private boolean isClick(HttpServletRequest request)
	{
		return request.getParameter(CLICK_ACTION) != null;
	}

	private boolean isClear(HttpServletRequest request)
	{
		return request.getParameter("clear") != null;
	}

	private boolean isExecute(HttpServletRequest request)
	{
		return request.getParameter("execute") != null;
	}

	private boolean isUndo(HttpServletRequest request)
	{
		return request.getParameter("undo") != null;
	}

	private boolean isSetMove(HttpServletRequest request)
	{
		return request.getParameter("set_move") != null;
	}

	private boolean isFlip(HttpServletRequest request)
	{
		return request.getParameter("flip") != null;
	}

	private boolean isSetGame(HttpServletRequest request)
	{
		return request.getParameter("set_game") != null;
	}

	private boolean isUnmarshalGame(HttpServletRequest request)
	{
		return request.getParameter("unmarshal_game") != null;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest r)
	{
		return super.isFormSubmission(r) || isClick(r) || isClear(r) || isExecute(r) || isUndo(r) || isSetMove(r) || isFlip(r) || isSetGame(r)
				|| isUnmarshalGame(r);
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Command command = (Command) commandObj;

		RuleBasedBoardUIState ui = command.getUi();
		Game game = command.getGame();
		GameRules rules = GameRulesHelper.rules(command.getRules());

		if (isClick(request))
		{
			// click
			ui.click(request.getParameter(CLICK_ACTION));

			GameMove move = ui.getDistinctMove();
			if (move != null)
			{
				// auto-execute
				rules.executeMove(game, move);

				// synchronize ui
				ui.setMove(move);
				ui.setPosition(game.getActualPosition());
				command.setGameNotation(rules.formatGame(game, Locale.ENGLISH));
				command.setMarshalledGame(rules.marshal(game));
			}
		}
		else if (isClear(request))
		{
			// clear
			ui.clear();
		}
		else if (isExecute(request))
		{
			GameMove move = ui.getDistinctMove();

			// execute
			rules.executeMove(game, move);

			// synchronize ui
			ui.setMove(move);
			ui.setPosition(game.getActualPosition());
			command.setGameNotation(rules.formatGame(game, Locale.ENGLISH));
			command.setMarshalledGame(rules.marshal(game));
		}
		else if (isUndo(request))
		{
			if (game.getSize() > 0) // double click protection
			{
				// undo
				rules.undoLastMove(game);

				// synchronize ui
				ui.setMove(game.getLastMove());
				ui.setPosition(game.getActualPosition());
				command.setGameNotation(rules.formatGame(game, Locale.ENGLISH));
				command.setMarshalledGame(rules.marshal(game));
			}
		}
		else if (isSetMove(request))
		{
			if (command.getMove() != null)
			{
				try
				{
					// parse move
					GameMove move = rules.parseMove(command.getMove(), request.getLocale(), game);

					// execute move
					rules.executeMove(game, move);

					// synchronize ui
					ui.setMove(move);
					ui.setPosition(game.getActualPosition());
					command.setGameNotation(rules.formatGame(game, Locale.ENGLISH));
					command.setMarshalledGame(rules.marshal(game));

					// clear form field
					command.setMove(null);
				}
				catch (ParseException x)
				{
					errors.rejectValue("move", "invalid");
				}
			}
		}
		else if (isFlip(request))
		{
			command.setFlip(!command.isFlip());
		}
		else if (isSetGame(request))
		{
			try
			{
				String initialBoard = rules.formatBoard(game.getInitialPosition().getBoard());
				game = rules.newGame(initialBoard, command.getGameNotation(), Locale.ENGLISH);
				command.setGame(game);
				command.setUi(RuleBasedBoardUIState.getInstance(rules, game.getActualPosition(), game.getInitialPosition().getBoard()));
				command.setGameNotation(rules.formatGame(game, Locale.ENGLISH));
				command.setMarshalledGame(rules.marshal(game));
			}
			catch (ParseException x)
			{
				errors.rejectValue("gameNotation", "invalid", x.getMessage());
			}
		}
		else if (isUnmarshalGame(request))
		{
			try
			{
				String initialBoard = rules.formatBoard(game.getInitialPosition().getBoard());
				game = rules.unmarshal(initialBoard, command.getMarshalledGame());
				command.setGame(game);
				command.setUi(RuleBasedBoardUIState.getInstance(rules, game.getActualPosition(), game.getInitialPosition().getBoard()));
				command.setGameNotation(rules.formatGame(game, Locale.ENGLISH));
				command.setMarshalledGame(rules.marshal(game));
			}
			catch (Exception x)
			{
				errors.rejectValue("marshalledGame", "invalid", x.getMessage());
			}
		}

		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
		private Rules rules;
		private Game game;
		private RuleBasedBoardUIState ui;
		private String move;
		private boolean flip;
		private String gameNotation;
		private String marshalledGame;

		public Command(Rules rules, Game game, RuleBasedBoardUIState ui)
		{
			setRules(rules);
			setGame(game);
			setUi(ui);
		}

		public Rules getRules()
		{
			return rules;
		}

		public void setRules(Rules rules)
		{
			this.rules = rules;
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

		public String getGameNotation()
		{
			return gameNotation;
		}

		public void setGameNotation(String gameNotation)
		{
			this.gameNotation = gameNotation;
		}

		public String getMarshalledGame()
		{
			return marshalledGame;
		}

		public void setMarshalledGame(String marshalledGame)
		{
			this.marshalledGame = marshalledGame;
		}

		@Override
		public String toString()
		{
			return getClass().getName() + "[ui=" + ui + ",move=" + move + ",flip=" + flip + "]";
		}
	}
}
