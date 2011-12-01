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

package de.schildbach.game.presentation.board;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.game.Board;
import de.schildbach.game.GameRules;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
public class FreestyleBoardController extends SimpleFormController
{
	private static final String CLICK_ACTION = "id";

	private int largeBoardThreshold;
	private Environment environment;
	private PresenceService presenceService;

	@Required
	public void setLargeBoardThreshold(int largeBoardThreshold)
	{
		this.largeBoardThreshold = largeBoardThreshold;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest r)
	{
		presenceService.setLastActivity(r.getRemoteUser(), Activity.GAME_ANALYZE);

		return super.isFormSubmission(r) || isClick(r);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		GameRules rules = GameRulesHelper.rules(Rules.valueOf(ServletRequestUtils.getStringParameter(request, "rules", "CHESS")));
		String boardParam = ServletRequestUtils.getStringParameter(request, "board");
		Board board = rules.initialPositionFromBoard(null).getBoard();
		if (boardParam != null)
			rules.parseBoard(board, boardParam);
		boolean flip = ServletRequestUtils.getBooleanParameter(request, "flip", false);
		Command command = new Command(new FreestyleBoardUIState(rules.getBoardGeometry(), board, rules.getPieceSet()));
		command.setFlip(flip);
		return command;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Command command = (Command) commandObj;
		FreestyleBoardUIState state = command.getBoardUIState();
		Map<String, Object> model = new HashMap<String, Object>();

		// large?
		int resolution = environment.getScreenResolution();
		String size = resolution < largeBoardThreshold ? "small" : "large";
		model.put("size", size);

		// attributes for included view
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, state.getGameBoard());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, state.getGeometry());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, state.getPieceSet());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CLICKABLES, state.getClickables());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_MARKERS, state.getMarkers());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS, state.getCursors());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_CLICK_ACTION, "?" + CLICK_ACTION + "={0}");
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_FLIP, command.isFlip());

		return model;
	}

	private boolean isClick(HttpServletRequest request)
	{
		return request.getParameter(CLICK_ACTION) != null;
	}

	@Override
	protected boolean isFormChangeRequest(HttpServletRequest request)
	{
		return isClick(request);
	}

	@Override
	protected void onBindAndValidate(HttpServletRequest request, Object commandObj, BindException errors) throws Exception
	{
		Command command = (Command) commandObj;
		AbstractBoardUIState state = command.getBoardUIState();

		if (isClick(request))
		{
			state.click(request.getParameter(CLICK_ACTION));
		}
	}

	public final class Command implements Serializable
	{
		private FreestyleBoardUIState boardUIState;
		private boolean flip;

		public Command(FreestyleBoardUIState boardUIState)
		{
			this.boardUIState = boardUIState;
		}

		public final FreestyleBoardUIState getBoardUIState()
		{
			return boardUIState;
		}

		public final void setFlip(boolean flip)
		{
			this.flip = flip;
		}

		public final boolean isFlip()
		{
			return flip;
		}
	}
}
