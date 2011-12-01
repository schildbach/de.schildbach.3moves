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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import de.schildbach.game.Piece;
import de.schildbach.game.PieceSet;

/**
 * This fragment controller is responsible for displaying captured pieces of one player.
 * 
 * @author Andreas Schildbach
 */
@Controller
public class CapturedPiecesRowController extends AbstractController
{
	private static final String PREFIX = CapturedPiecesRowController.class.getName() + ".";
	public static final String REQUEST_ATTRIBUTE_CAPTURED_PIECES_BY_PLAYER = PREFIX + "capturedPiecesByPlayer";
	public static final String REQUEST_ATTRIBUTE_PIECESET = PREFIX + "pieceSet";

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		List<Piece[]> capturedPiecesByPlayer = (List<Piece[]>) request.getAttribute(REQUEST_ATTRIBUTE_CAPTURED_PIECES_BY_PLAYER);
		PieceSet pieceSet = (PieceSet) request.getAttribute(REQUEST_ATTRIBUTE_PIECESET);
		if (capturedPiecesByPlayer == null)
			return null;

		int pos = ServletRequestUtils.getRequiredIntParameter(request, "pos");
		Piece[] capturedPieces = capturedPiecesByPlayer.get(pos);

		Map<String, Object> model = new HashMap<String, Object>();
		boolean show = capturedPieces.length > 0;
		model.put("show", show);

		String size = ServletRequestUtils.getStringParameter(request, "size", "small");
		model.put("size", size);

		if (pieceSet.size() > 2)
		{
			List<String> pieces = new LinkedList<String>();
			for (Piece piece : capturedPieces)
				pieces.add(pieceSet.getStringRepresentation(piece));
			model.put("pieces", pieces);
			return new ModelAndView("captured_pieces_row.jspx", model);
		}
		else if (pieceSet.size() == 2)
		{
			if (show)
			{
				model.put("number", capturedPieces.length);
				model.put("piece", pieceSet.getStringRepresentation(capturedPieces[0]));
			}
			return new ModelAndView("num_captured_pieces_row.jspx", model);
		}
		else
		{
			throw new IllegalStateException("can't handle: " + pieceSet != null ? pieceSet.getClass().getName() : "null pieceSet");
		}
	}
}
