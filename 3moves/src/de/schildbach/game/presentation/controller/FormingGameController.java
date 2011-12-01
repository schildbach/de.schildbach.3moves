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

package de.schildbach.game.presentation.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import de.schildbach.game.presentation.GameInvitationClientData;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.web.crypto.EncryptionHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class FormingGameController extends MultiActionController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(FormingGameController.class);

	private EncryptionHelper gameInvitationEncryptionHelper;
	private GameService gameService;

	@Required
	public void setGameInvitationEncryptionHelper(EncryptionHelper gameInvitationEncryptionHelper)
	{
		this.gameInvitationEncryptionHelper = gameInvitationEncryptionHelper;
	}

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	public ModelAndView view_confirm_game_invitation(HttpServletRequest request, HttpServletResponse response) throws ServletRequestBindingException
	{
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("is_logged_in", request.getUserPrincipal() != null);

		model.put("link_home", request.getContextPath() + "/");

		String key = ServletRequestUtils.getRequiredStringParameter(request, "key");
		model.put("key", key);

		try
		{
			GameInvitationClientData gameInvitationClientData = decryptGameInvitationClientData(key);

			model.put("game_id", gameInvitationClientData.getGameId());
			model.put("inviting_user_name", gameInvitationClientData.getInvitingUsername());

			Game game = gameService.game(gameInvitationClientData.getGameId());

			boolean isDeleted = game == null;
			model.put("is_deleted", isDeleted);
			if (!isDeleted)
			{
				model.put("game", game);
				model.put("is_not_forming", game.getState() != GameState.FORMING);
				model.put("is_full", game.getPlayers().size() >= GameRulesHelper.maxPlayers(game));
			}
		}
		catch (Exception x)
		{
			model.put("is_invalid_key", true);
			LOG.info("caught exception", x);
		}

		return new ModelAndView("confirm_game_invitation.jspx", model);
	}

	private GameInvitationClientData decryptGameInvitationClientData(String urlEncodedKey) throws DecoderException, GeneralSecurityException
	{
		// decrypt key
		byte[] key = Hex.decodeHex(urlEncodedKey.toCharArray());
		return gameInvitationEncryptionHelper.decryptFromClient(key, GameInvitationClientData.class);
	}

	public ModelAndView confirm_game_invitation(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		String key = ServletRequestUtils.getRequiredStringParameter(request, "key");
		GameInvitationClientData gameInvitationClientData = decryptGameInvitationClientData(key);
		Game game = gameService.inviteSubjectByKey(user.getName(), gameInvitationClientData.getGameId());

		return new ModelAndView("game", "id", game.getId());
	}

	public ModelAndView invite_for_second_leg_confirm(HttpServletRequest request, HttpServletResponse response)
	{
		return new ModelAndView("invite_for_second_leg_confirm.jspx");
	}

	public ModelAndView invite_for_second_leg(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		int gameId = Integer.parseInt(request.getParameter("game_id"));

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		int secondLegId = gameService.createSecondLeg(user.getName(), gameId);

		model.put("id", secondLegId);

		return new ModelAndView("game", model);
	}

	public ModelAndView check_exists_beginner_tournaments(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		long start = System.currentTimeMillis();
		int count = gameService.checkExistsBeginnerTournaments();
		long end = System.currentTimeMillis();

		String message = "check_exists_beginner_tournaments: created " + count + " games, took " + (end - start) + " ms";
		LOG.info(message);
		if (count > 0)
			response.getWriter().write(message);

		return null;
	}
}
