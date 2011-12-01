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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.bo.Activity;
import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ViewGameController extends SimpleFormController
{
	public static final String REQUEST_PARAMETER_ID = "id";

	private int bigScreenThreshold;
	private Environment environment;
	private GameViewHelper gameViewHelper;
	private GameService gameService;
	private PresenceService presenceService;
	private String viewGameNotFound;

	@Required
	public void setBigScreenThreshold(int bigScreenThreshold)
	{
		this.bigScreenThreshold = bigScreenThreshold;
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

	@Required
	public void setViewGameNotFound(String viewGameNotFound)
	{
		this.viewGameNotFound = viewGameNotFound;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		return request.getParameter(REQUEST_PARAMETER_ID) == null;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		int gameId = ServletRequestUtils.getRequiredIntParameter(request, REQUEST_PARAMETER_ID);

		Game game = gameService.viewGame(gameId);

		Command command = new Command(gameId);

		if (game != null)
		{
			gameViewHelper.setGame(request, gameService.game(gameId));
			GamePlayer player = gameService.player(request.getRemoteUser(), gameId);
			if (player != null)
				command.setPrivatePlayerNotes(player.getComment());
		}
		return command;
	}

	@Override
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors, Map controlModel)
			throws Exception
	{
		Command command = (Command) errors.getTarget();

		Game game = initGameToRequest(request, command.getGameId());
		if (game == null)
			return new ModelAndView(viewGameNotFound);
		else
			return super.showForm(request, response, errors, controlModel);
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		final Map<String, Object> model = new HashMap<String, Object>();

		Command command = (Command) commandObj;

		Game game = initGameToRequest(request, command.getGameId());
		if (game != null)
		{
			String userName = request.getRemoteUser();
			int gameId = game.getId();

			boolean bigScreen = environment.getScreenResolution() >= bigScreenThreshold;

			model.put("show_personal_notes", bigScreen && gameService.canAccessPrivatePlayerNotes(userName, gameId));
			boolean showComments = bigScreen && gameService.canReadGameComments(userName, gameId);
			model.put("show_comments", showComments);
			if (showComments)
				model.put("comments", game.getComments() != null ? game.getComments().split("\\n") : new String[] {});

			model.put("can_add_comment", gameService.canAddGameComment(userName, gameId));

			model.put("show_actions", bigScreen);
			model.put("can_watch", gameService.canWatchGame(userName, gameId));
			model.put("can_unwatch", gameService.canUnwatchGame(userName, gameId));
			model.put("can_delete", gameService.canDeleteGame(userName, gameId));
			model.put("can_reactivate", gameService.canReactivateGame(userName, gameId));

			if (game.getState() == GameState.FORMING || game.getState() == GameState.UNACCOMPLISHED)
			{
				model.put("import", "forming/form_game.dof");
			}
			else
			{
				game.accept(new GameVisitor()
				{
					public void visit(SingleGame singleGame)
					{
						model.put("import", "single_game.dof");
					}

					public void visit(GameGroup gameGroup)
					{
						model.put("import", "gamegroup.dof");
					}
				});
			}

			// update last user activity
			Principal user = request.getUserPrincipal();
			if (user != null)
			{
				GamePlayer gamePlayer = gameService.player(userName, gameId);
				if (gamePlayer != null)
					presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_PLAY);
				else
					presenceService.setLastActivity(request.getRemoteUser(), Activity.GAME_VIEW);
			}
		}

		return model;
	}

	private boolean isWatchGame(HttpServletRequest request)
	{
		return request.getParameter("watch_game") != null;
	}

	private boolean isUnwatchGame(HttpServletRequest request)
	{
		return request.getParameter("unwatch_game") != null;
	}

	private boolean isDeleteGame(HttpServletRequest request)
	{
		return request.getParameter("delete_game") != null;
	}

	private boolean isReactivateGame(HttpServletRequest request)
	{
		return request.getParameter("reactivate_game") != null;
	}

	private boolean isSetPrivatePlayerNotes(HttpServletRequest request)
	{
		return request.getParameter("set_private_player_notes") != null;
	}

	private boolean isAddPlayerComment(HttpServletRequest request)
	{
		return request.getParameter("add_player_comment") != null;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Command command = (Command) commandObj;
		int gameId = command.getGameId();
		String userName = request.getRemoteUser();

		if (isWatchGame(request))
		{
			gameService.watchGame(userName, gameId);
		}
		else if (isUnwatchGame(request))
		{
			gameService.unwatchGame(userName, gameId);
		}
		else if (isDeleteGame(request))
		{
			gameService.deleteGame(userName, gameId);
		}
		else if (isReactivateGame(request))
		{
			gameService.reactivateGame(userName, gameId);
		}
		else if (isSetPrivatePlayerNotes(request))
		{
			gameService.setPrivatePlayerNotes(userName, gameId, command.getPrivatePlayerNotes());
		}
		else if (isAddPlayerComment(request))
		{
			if (command.getPlayerComment() != null)
				gameService.addGameComment(userName, gameId, command.getPlayerComment());
			command.setPlayerComment(null);
		}

		return showForm(request, response, errors);
	}

	private Game initGameToRequest(HttpServletRequest request, int gameId)
	{
		Game game = gameViewHelper.getGame(request);

		if (game == null)
		{
			game = gameService.game(gameId);
			gameViewHelper.setGame(request, game);
		}

		return game;
	}

	public static class Command implements Serializable
	{
		private int gameId;
		private String privatePlayerNotes;
		private String playerComment;

		public Command(int gameId)
		{
			this.gameId = gameId;
		}

		public int getGameId()
		{
			return gameId;
		}

		public void setGameId(int gameId)
		{
			this.gameId = gameId;
		}

		public String getPrivatePlayerNotes()
		{
			return privatePlayerNotes;
		}

		public void setPrivatePlayerNotes(String notes)
		{
			this.privatePlayerNotes = notes;
		}

		public String getPlayerComment()
		{
			return playerComment;
		}

		public void setPlayerComment(String comment)
		{
			this.playerComment = comment;
		}
	}
}
