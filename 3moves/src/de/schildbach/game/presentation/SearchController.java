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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.user.UserNameStatus;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.presentation.EnumEditor;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class SearchController
{
	private static final Integer[] VALID_WINDOWS = new Integer[] { null, 1, 3, 6, 12, 24, 36 };

	private List<Rules> rulesOptions;
	private UserService userService;
	private GameService gameService;
	private String view;

	@Required
	public void setRulesOptions(List<Rules> rulesOptions)
	{
		this.rulesOptions = rulesOptions;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@InitBinder
	public void initBinder(ServletRequestDataBinder binder)
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		binder.registerCustomEditor(Aid.class, new EnumEditor(Aid.class, true));
		binder.registerCustomEditor(Parent.class, new EnumEditor(Parent.class, true));
	}

	@ModelAttribute("window_options")
	public Integer[] windowOptions()
	{
		return VALID_WINDOWS;
	}

	@ModelAttribute("rules_options")
	public List<Rules> rulesOptions()
	{
		return rulesOptions;
	}

	@ModelAttribute("state_options")
	public GameState[] stateOptions()
	{
		return GameState.values();
	}

	@ModelAttribute("aid_options")
	public List<Aid> aidOptions()
	{
		List<Aid> aidOptions = new LinkedList<Aid>();
		aidOptions.add(null);
		aidOptions.addAll(Arrays.asList(Aid.values()));
		return aidOptions;
	}

	@ModelAttribute("parent_options")
	public List<Parent> parentOptions()
	{
		List<Parent> parentOptions = new LinkedList<Parent>();
		parentOptions.add(null);
		parentOptions.addAll(Arrays.asList(Parent.values()));
		return parentOptions;
	}

	@RequestMapping(method = RequestMethod.GET, params = "!rules")
	public String setupForm(User user, Model model)
	{
		Command command = new Command();
		command.setRules(Rules.CHESS);
		command.setStates(EnumSet.of(GameState.READY, GameState.RUNNING, GameState.FINISHED));

		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.GET, params = "rules")
	public String processSubmit(User user, @ModelAttribute Command command, BindingResult result, Model model)
	{
		if (command.getPlayer() != null)
		{
			UserNameStatus userNameStatus = userService.checkUserName(command.getPlayer());
			if (userNameStatus == UserNameStatus.AVAILABLE || userNameStatus == UserNameStatus.BANNED)
				result.rejectValue("player", "player_invalid");
			else if (userNameStatus == UserNameStatus.REGISTERED_BUT_MISSPELLED)
				command.setPlayer(userService.correctUserName(command.getPlayer()));
		}

		if (!result.hasErrors())
		{
			Boolean hasParent = null;
			if (command.getParent() == Parent.HAS_PARENT)
				hasParent = Boolean.TRUE;
			else if (command.getParent() == Parent.HAS_NO_PARENT)
				hasParent = Boolean.FALSE;

			List<SingleGame> games = gameService.search(command.getRules(), command.getAid(), command.getPlayer(), command.getStates(), hasParent,
					command.getWindow());
			model.addAttribute("games", games);
			model.addAttribute("has_results", true);
		}

		return view;
	}

	public static enum Parent
	{
		HAS_PARENT, HAS_NO_PARENT;
	}

	public static class Command implements Serializable
	{
		private Rules rules;
		private Aid aid;
		private String player;
		private Set<GameState> states;
		private Parent parent;
		private Integer window;

		public void setRules(Rules rules)
		{
			this.rules = rules;
		}

		public void setAid(Aid aid)
		{
			this.aid = aid;
		}

		public Aid getAid()
		{
			return aid;
		}

		public Rules getRules()
		{
			return rules;
		}

		public void setPlayer(String player)
		{
			this.player = player;
		}

		public String getPlayer()
		{
			return player;
		}

		public void setStates(Set<GameState> state)
		{
			this.states = state;
		}

		public Set<GameState> getStates()
		{
			return states;
		}

		public void setParent(Parent parent)
		{
			this.parent = parent;
		}

		public Parent getParent()
		{
			return parent;
		}

		public void setWindow(Integer window)
		{
			this.window = window;
		}

		public Integer getWindow()
		{
			return window;
		}
	}
}
