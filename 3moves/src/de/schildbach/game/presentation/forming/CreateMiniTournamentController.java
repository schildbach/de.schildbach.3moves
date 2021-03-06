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
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import de.schildbach.game.Game;
import de.schildbach.game.GameMove;
import de.schildbach.game.GameRules;
import de.schildbach.game.exception.ParseException;
import de.schildbach.game.presentation.board.BaseBoardController;
import de.schildbach.game.presentation.board.RuleBasedBoardUIState;
import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.ClockConstraint;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.game.bo.CreateMiniTournamentCommand;
import de.schildbach.portal.service.game.bo.DeadlineOption;
import de.schildbach.portal.service.game.bo.InvitationType;
import de.schildbach.portal.service.game.exception.IllegalRequiredRatingException;
import de.schildbach.portal.service.game.exception.IllegalStartAtDateException;

/**
 * @author Andreas Schildbach
 */
@Controller
public class CreateMiniTournamentController extends AbstractWizardFormController
{
	private enum Page
	{
		BASIC, ADVANCED, THEME
	}

	private static final String CLICK_ACTION = "id";

	private List<Rules> rulesOptions;
	private GameService gameService;
	private String successView;

	@Required
	public void setRulesOptions(List<Rules> rulesOptions)
	{
		this.rulesOptions = rulesOptions;
	}

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setSuccessView(String successView)
	{
		this.successView = successView;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		CreateMiniTournamentCommand createMiniTournamentCommand = gameService.createMiniTournamentDefaults();
		Command command = new Command(createMiniTournamentCommand);
		String rules = ServletRequestUtils.getStringParameter(request, "rules");
		if (rules != null)
			createMiniTournamentCommand.setRules(Rules.valueOf(rules));
		return command;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		binder.registerCustomEditor(int.class, "remote.numberOfPlayers", new CustomNumberEditor(Integer.class, false));
		// TODO take into account TimeZone
		DateFormat deadlineDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
		binder.registerCustomEditor(Date.class, "remote.startAt.date", new CustomDateEditor(deadlineDateFormat, true));
		binder.registerCustomEditor(int.class, "remote.startAt.hour", new CustomNumberEditor(Integer.class, false));
		binder.registerCustomEditor(ClockConstraint.class, new ClockConstraintEditor());
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors, int page) throws Exception
	{
		Command command = (Command) commandObj;
		CreateMiniTournamentCommand createMiniTournamentCommand = command.getRemote();

		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		if (gameService.canCreateMiniTournament(user.getName()))
		{
			if (page == Page.BASIC.ordinal())
			{
				model.put("rules_options", rulesOptions);
				model.put("aids", Arrays.asList(Aid.values()));
				model.put("invitation_types", Arrays.asList(InvitationType.values()));
			}
			else if (page == Page.ADVANCED.ordinal())
			{
				List<Integer> numberOfPlayers = new LinkedList<Integer>();
				numberOfPlayers.add(3);
				numberOfPlayers.add(4);
				numberOfPlayers.add(5);
				model.put("number_of_players", numberOfPlayers);

				List<ClockConstraint> clockConstraints = ClockConstraint.getAvailableClockConstraints();
				clockConstraints.add(0, null);
				model.put("clock_constraints", clockConstraints);

				model.put("deadline_options", Arrays.asList(DeadlineOption.values()));

				// TODO take into account TimeZone
				SortedMap<Integer, Date> deadlineHours = new TreeMap<Integer, Date>();
				Calendar calendar = new GregorianCalendar();
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				for (int hour = 0; hour < 24; hour++)
				{
					calendar.set(Calendar.HOUR_OF_DAY, hour);
					deadlineHours.put(new Integer(hour), calendar.getTime());
				}
				model.put("start_at_hours", deadlineHours);

				model.put("can_theme", !GameRulesHelper.rulesForceInitialBoard(createMiniTournamentCommand.getRules()));
			}
			else if (page == Page.THEME.ordinal())
			{
				Game game = createMiniTournamentCommand.getOpening();
				GameRules gameRules = GameRulesHelper.rules(createMiniTournamentCommand.getRules());
				RuleBasedBoardUIState state = command.getOpeningState();

				// attributes for included view
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, state.getGameBoard());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, gameRules.getBoardGeometry());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, gameRules.getPieceSet());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CLICKABLES, state.getClickables());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS, state.getCursors());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_MARKERS, state.getMarkers());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_CLICK_ACTION, "?" + CLICK_ACTION + "={0}");

				// attributes for this view
				model.put("special_attributes", GameRulesHelper.specialAttributes(game, gameRules));
				model.put("game_notation", gameRules.formatGame(game, request.getLocale()));
				model.put("move_notation", state.getMoveNotation());
				model.put("can_set_move", state.isInitial() && state.getNumberOfAllowedMoves() > 1);
				model.put("can_clear_move", !state.isInitial());
				model.put("can_execute_move", state.getDistinctMove() != null);
				model.put("can_undo_move", state.isInitial() && game.getSize() > 0);

				model.put("move_label", "move_" + gameRules.getPieceSet().getColorTag(game.getActualPosition().getActivePlayerIndex()));
			}
		}
		else
		{
			model.put("show_not_allowed", true);
		}

		return model;
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

	private boolean isSet(HttpServletRequest request)
	{
		return request.getParameter("set_move") != null;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest r)
	{
		return super.isFormSubmission(r) || isClick(r) || isClear(r) || isExecute(r) || isUndo(r) || isSet(r);
	}

	@Override
	protected void validatePage(Object commandObj, Errors errors, int page)
	{
		Command command = (Command) commandObj;
		CreateMiniTournamentCommand createMiniTournamentCommand = command.getRemote();
		if (page == Page.ADVANCED.ordinal())
		{
			if (createMiniTournamentCommand.getStartAt().getOption() == DeadlineOption.TIME
					&& createMiniTournamentCommand.getStartAt().getDate() == null)
				errors.rejectValue("remote.startAt.date", "missing");
		}
	}

	@Override
	protected void onBindAndValidate(HttpServletRequest request, Object commandObj, BindException errors, int page) throws Exception
	{
		Command command = (Command) commandObj;
		CreateMiniTournamentCommand createMiniTournamentCommand = command.getRemote();
		Rules rules = createMiniTournamentCommand.getRules();
		GameRules gameRules = GameRulesHelper.rules(rules);

		if (page == Page.BASIC.ordinal())
		{
			Game game = GameRulesHelper.newGame(rules);
			createMiniTournamentCommand.setOpening(game);
			command.setOpeningState(RuleBasedBoardUIState.getInstance(gameRules, game.getActualPosition(), game.getInitialPosition().getBoard()));
		}
		else if (page == Page.THEME.ordinal())
		{
			RuleBasedBoardUIState ui = command.getOpeningState();
			Game game = createMiniTournamentCommand.getOpening();

			if (isClick(request))
			{
				// click
				ui.click(request.getParameter(CLICK_ACTION));

				GameMove move = ui.getDistinctMove();
				if (move != null)
				{
					// auto-execute
					gameRules.executeMove(game, move);

					// synchronize ui
					ui.setMove(move);
					ui.setPosition(game.getActualPosition());
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
				gameRules.executeMove(game, move);

				// synchronize ui
				ui.setMove(move);
				ui.setPosition(game.getActualPosition());
			}
			else if (isUndo(request))
			{
				if (game.getSize() > 0) // double click protection
				{
					// undo
					gameRules.undoLastMove(game);

					// synchronize ui
					ui.setMove(game.getLastMove());
					ui.setPosition(game.getActualPosition());
				}
			}
			else if (isSet(request))
			{
				if (command.getMove() != null)
				{
					try
					{
						// parse move
						GameMove move = gameRules.parseMove(command.getMove(), request.getLocale(), game);

						// execute move
						gameRules.executeMove(game, move);

						// synchronize ui
						ui.setMove(move);
						ui.setPosition(game.getActualPosition());

						// clear form field
						command.setMove(null);
					}
					catch (ParseException x)
					{
						errors.rejectValue("move", "invalid");
					}
				}
			}
		}
	}

	@Override
	protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;
		CreateMiniTournamentCommand createGameCommand = command.getRemote();

		try
		{
			int id = gameService.createMiniTournament(user.getName(), createGameCommand);
			return new ModelAndView(successView, "id", id);
		}
		catch (IllegalStartAtDateException x)
		{
			errors.rejectValue("remote.startAt.date", x.getMessage());
			return showPage(request, errors, Page.ADVANCED.ordinal());
		}
		catch (IllegalRequiredRatingException x)
		{
			errors.rejectValue("remote.requiredRatings", x.getMessage());
			return showPage(request, errors, Page.ADVANCED.ordinal());
		}
	}

	public static class Command implements Serializable
	{
		private CreateMiniTournamentCommand remote;
		private RuleBasedBoardUIState openingState;
		private String move;

		public Command(CreateMiniTournamentCommand createMiniTournamentCommand)
		{
			setRemote(createMiniTournamentCommand);
		}

		public CreateMiniTournamentCommand getRemote()
		{
			return remote;
		}

		private void setRemote(CreateMiniTournamentCommand remote)
		{
			this.remote = remote;
		}

		public RuleBasedBoardUIState getOpeningState()
		{
			return openingState;
		}

		public void setOpeningState(RuleBasedBoardUIState openingState)
		{
			this.openingState = openingState;
		}

		public String getMove()
		{
			return move;
		}

		public void setMove(String move)
		{
			this.move = move;
		}

		@Override
		public String toString()
		{
			return getClass().getName() + "[remote=" + remote + ",ui=" + openingState + ",move=" + move + "]";
		}
	}
}
