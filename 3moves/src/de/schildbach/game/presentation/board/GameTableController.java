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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.game.Piece;
import de.schildbach.user.presentation.Environment;

/**
 * This fragment controller is responsible for displaying the whole board, including the player bars and captured pieces
 * (if any).
 * 
 * @author Andreas Schildbach
 */
@Controller
public class GameTableController extends ParameterizableViewController
{
	private int largeBoardThreshold;
	private Environment environment;

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

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();

		// large?
		int resolution = environment.getScreenResolution();
		String size = resolution < largeBoardThreshold ? "small" : "large";
		model.put("size", size);

		// flip?
		Boolean flip = (Boolean) request.getAttribute(BaseBoardController.REQUEST_ATTRIBUTE_FLIP);
		if (flip == null)
			flip = Boolean.FALSE;

		// player names
		List<String> names = new LinkedList<String>();
		names.add(request.getParameter("name_1"));
		names.add(request.getParameter("name_0"));
		if (flip)
			Collections.reverse(names);
		model.put("name", names);

		// player colors
		List<String> colors = new LinkedList<String>();
		colors.add(request.getParameter("color_1"));
		colors.add(request.getParameter("color_0"));
		if (flip)
			Collections.reverse(colors);
		model.put("color", colors);

		// player clocks
		if (request.getParameter("clock_0") != null && request.getParameter("clock_0").length() > 0)
		{
			List<Long> clocks = new LinkedList<Long>();
			clocks.add(ServletRequestUtils.getLongParameter(request, "clock_1"));
			clocks.add(ServletRequestUtils.getLongParameter(request, "clock_0"));
			if (flip)
				Collections.reverse(clocks);
			model.put("clock", clocks);
		}

		// captured pieces
		Piece[][] capturedPiecesArray = (Piece[][]) request.getAttribute(CapturedPiecesRowController.REQUEST_ATTRIBUTE_CAPTURED_PIECES_BY_PLAYER);
		List<Piece[]> capturedPieces = capturedPiecesArray != null ? Arrays.asList(capturedPiecesArray) : null;
		if (flip && capturedPieces != null)
			Collections.reverse(capturedPieces);
		model.put(CapturedPiecesRowController.REQUEST_ATTRIBUTE_CAPTURED_PIECES_BY_PLAYER, capturedPieces);

		return new ModelAndView(getViewName(), model);
	}
}
