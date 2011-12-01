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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.game.Board;
import de.schildbach.game.Coordinate;
import de.schildbach.game.Piece;
import de.schildbach.game.PieceSet;
import de.schildbach.game.checkers.CheckersBoardGeometry;
import de.schildbach.game.chess.ChessBoardGeometry;
import de.schildbach.game.common.ChessBoardLikeGeometry;
import de.schildbach.game.common.OrthogonalBoardGeometry;
import de.schildbach.game.dragonchess.DragonchessBoardGeometry;
import de.schildbach.game.go.GoBoardGeometry;
import de.schildbach.game.go.piece.GoPieceSet;
import de.schildbach.game.presentation.board.AbstractBoardUIState.Cursor;
import de.schildbach.game.presentation.board.AbstractBoardUIState.Marker;

/**
 * This fragment controller is responsible for displaying just the base board (without any captured pieces, player names
 * or game clocks) to the user. All arguments are expected in request attributes (see REQUEST_ATTRIBUTE_* constants).
 * 
 * @author Andreas Schildbach
 */
@Controller
public class BaseBoardController extends ParameterizableViewController
{
	public static final String REQUEST_ATTRIBUTE_GAME_BOARD = "game_board";
	public static final String REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY = "game_board_geometry";
	public static final String REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET = "game_board_pieceset";
	public static final String REQUEST_ATTRIBUTE_GAME_BOARD_CLICKABLES = "clickables";
	public static final String REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS = "cursors";
	public static final String REQUEST_ATTRIBUTE_GAME_BOARD_MARKERS = "markers";
	public static final String REQUEST_ATTRIBUTE_CLICK_ACTION = "click_action";
	public static final String REQUEST_ATTRIBUTE_FLIP = "flip";

	private String dragonchessViewName;

	@Required
	public void setDragonchessViewName(String dragonchessViewName)
	{
		this.dragonchessViewName = dragonchessViewName;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Locale locale = request.getLocale();

		Map<String, Object> model = new HashMap<String, Object>();

		String size = ServletRequestUtils.getStringParameter(request, "size", "small");
		model.put("size", size);

		Board board = (Board) request.getAttribute(REQUEST_ATTRIBUTE_GAME_BOARD);
		OrthogonalBoardGeometry geometry = (OrthogonalBoardGeometry) request.getAttribute(REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY);
		PieceSet pieceSet = (PieceSet) request.getAttribute(REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET);
		Map<Coordinate, String> clickables = (Map<Coordinate, String>) request.getAttribute(REQUEST_ATTRIBUTE_GAME_BOARD_CLICKABLES);
		Map<Coordinate, Cursor> cursors = (Map<Coordinate, Cursor>) request.getAttribute(REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS);
		Map<Coordinate, Marker> markers = (Map<Coordinate, Marker>) request.getAttribute(REQUEST_ATTRIBUTE_GAME_BOARD_MARKERS);

		String clickAction = (String) request.getAttribute(REQUEST_ATTRIBUTE_CLICK_ACTION);
		Boolean flip = (Boolean) request.getAttribute(REQUEST_ATTRIBUTE_FLIP);
		if (flip == null)
			flip = Boolean.FALSE;

		model.put("show_labels", Boolean.valueOf(request.getParameter("show_labels") == null || request.getParameter("show_labels").equals("true")));

		String imgPath = request.getContextPath() + "/piece";
		if (geometry instanceof ChessBoardGeometry)
		{
			prepareChessboard(board, (ChessBoardGeometry) geometry, pieceSet, clickables, cursors, markers, imgPath, "chess", clickAction, flip,
					model, locale);
			return new ModelAndView(getViewName(), model);
		}
		else if (geometry instanceof DragonchessBoardGeometry)
		{
			prepareDragonchessboard(board, (DragonchessBoardGeometry) geometry, pieceSet, clickables, cursors, markers, clickAction, flip, model);
			return new ModelAndView(dragonchessViewName, model);
		}
		else if (geometry instanceof CheckersBoardGeometry)
		{
			prepareChessboard(board, (CheckersBoardGeometry) geometry, pieceSet, clickables, cursors, markers, imgPath, "checkers", clickAction,
					flip, model, locale);
			return new ModelAndView(getViewName(), model);
		}
		else if (geometry instanceof GoBoardGeometry)
		{
			prepareGoboard(board, geometry, pieceSet, clickables, cursors, imgPath, "go", clickAction, flip, model);
			return new ModelAndView(getViewName(), model);
		}

		throw new IllegalStateException("can't handle: " + geometry != null ? geometry.getClass().getName() : "null geometry");
	}

	private void prepareChessboard(Board board, ChessBoardLikeGeometry geometry, PieceSet pieceSet, Map<Coordinate, String> clickables,
			Map<Coordinate, Cursor> cursors, Map<Coordinate, Marker> markers, String imgPath, String set, String clickAction, boolean flip,
			Map<String, Object> model, Locale locale)
	{
		prepareLabels(geometry, flip, model);

		// initialize markers and selectables
		String[][] selectable = new String[geometry.getHeight()][];
		Cursor[][] cursorArray = new Cursor[geometry.getHeight()][];
		Marker[][] markerArray = new Marker[geometry.getHeight()][];
		for (int i = 0; i < geometry.getHeight(); i++)
		{
			selectable[i] = new String[geometry.getWidth()];
			cursorArray[i] = new Cursor[geometry.getWidth()];
			markerArray[i] = new Marker[geometry.getWidth()];
		}

		if (clickables != null)
		{
			for (Map.Entry<Coordinate, String> entry : clickables.entrySet())
				selectable[geometry.getX(entry.getKey())][geometry.getY(entry.getKey())] = entry.getValue();
		}

		if (cursors != null)
		{
			for (Map.Entry<Coordinate, Cursor> entry : cursors.entrySet())
				cursorArray[geometry.getX(entry.getKey())][geometry.getY(entry.getKey())] = entry.getValue();
		}

		if (markers != null)
		{
			for (Map.Entry<Coordinate, Marker> entry : markers.entrySet())
				markerArray[geometry.getX(entry.getKey())][geometry.getY(entry.getKey())] = entry.getValue();
		}

		// matrix
		String[][][] matrix = new String[geometry.getHeight()][][];
		for (int iy = 0; iy < geometry.getHeight(); iy++)
		{
			matrix[iy] = new String[geometry.getWidth()][];
			for (int ix = 0; ix < geometry.getWidth(); ix++)
			{
				matrix[iy][ix] = new String[5];
				int x = flip ? geometry.getWidth() - ix - 1 : ix;
				int y = flip ? iy : geometry.getHeight() - iy - 1;

				int[] xy = new int[] { x, y };
				Piece piece = getPiece(geometry, board, xy);

				// square class
				String squareClass = geometry.isDarkSquare(x, y) ? "dark" : "light";
				if (markerArray[x][y] != null)
					squareClass += "_highlighted";
				if (cursorArray[x][y] != null)
					squareClass += " cursor";
				matrix[iy][ix][0] = squareClass;

				// image filename
				String img = piece != null ? set + "/" + pieceSet.getStringRepresentation(piece) : "e";
				matrix[iy][ix][1] = imgPath + "/" + img + ".png";

				// alternate image text
				String alt = "";
				if (piece != null)
				{
					char c = pieceSet.getCharRepresentation(piece.getClass(), locale);
					if (piece.getColor() == 0)
						alt += Character.toUpperCase(c);
					else
						alt += Character.toLowerCase(c);
				}
				matrix[iy][ix][2] = alt;

				// image title
				String title = geometry.getFieldLabel(xy);
				matrix[iy][ix][3] = title;

				// select target
				if (clickAction != null && selectable[x][y] != null)
					matrix[iy][ix][4] = MessageFormat.format(clickAction, selectable[x][y]);
			}
		}
		model.put("matrix", matrix);
	}

	private void prepareDragonchessboard(Board board, DragonchessBoardGeometry geometry, PieceSet pieceSet, Map<Coordinate, String> clickables,
			Map<Coordinate, Cursor> cursors, Map<Coordinate, Marker> markers, String clickAction, boolean flip, Map<String, Object> model)
	{
		prepareLabels(geometry, flip, model);

		int layers = geometry.getSize(DragonchessBoardGeometry.AXIS_LAYER);
		int height = geometry.getHeight();
		int width = geometry.getWidth();

		// initialize markers and selectables
		String[][][] selectable = new String[layers][][];
		Cursor[][][] cursorArray = new Cursor[layers][][];
		Marker[][][] markerArray = new Marker[layers][][];
		for (int iz = 0; iz < layers; iz++)
		{
			selectable[iz] = new String[height][];
			cursorArray[iz] = new Cursor[height][];
			markerArray[iz] = new Marker[height][];
			for (int iy = 0; iy < height; iy++)
			{
				selectable[iz][iy] = new String[width];
				cursorArray[iz][iy] = new Cursor[width];
				markerArray[iz][iy] = new Marker[width];
			}
		}

		if (clickables != null)
		{
			for (Map.Entry<Coordinate, String> entry : clickables.entrySet())
				selectable[geometry.getZ(entry.getKey())][geometry.getY(entry.getKey())][geometry.getX(entry.getKey())] = entry.getValue();
		}

		if (cursors != null)
		{
			for (Map.Entry<Coordinate, Cursor> entry : cursors.entrySet())
				cursorArray[geometry.getZ(entry.getKey())][geometry.getY(entry.getKey())][geometry.getX(entry.getKey())] = entry.getValue();
		}

		if (markers != null)
		{
			for (Map.Entry<Coordinate, Marker> entry : markers.entrySet())
				markerArray[geometry.getZ(entry.getKey())][geometry.getY(entry.getKey())][geometry.getX(entry.getKey())] = entry.getValue();
		}

		// matrix
		String[][][][] matrix = new String[layers][][][];
		for (int z = 0; z < layers; z++)
		{
			matrix[z] = new String[height][][];
			for (int iy = 0; iy < height; iy++)
			{
				matrix[z][iy] = new String[width][];
				for (int ix = 0; ix < width; ix++)
				{
					matrix[z][iy][ix] = new String[5];
					int x = flip ? width - ix - 1 : ix;
					int y = flip ? iy : height - iy - 1;

					int[] xyz = new int[] { x, y, z };
					Piece piece = getPiece(geometry, board, xyz);

					// square class
					String squareClass = geometry.isDarkSquare(x, y) ? "dark" : "light";
					if (markerArray[z][y][x] != null)
						squareClass += "_highlighted";
					if (cursorArray[z][y][x] != null)
						squareClass += " cursor";
					matrix[z][iy][ix][0] = squareClass;

					// character
					if (piece != null)
					{
						char p = pieceSet.getCharRepresentation(piece.getClass());
						matrix[z][iy][ix][1] = "" + (piece.getColor() == 0 ? Character.toUpperCase(p) : Character.toLowerCase(p));
						matrix[z][iy][ix][2] = pieceSet.getColorTag(piece.getColor());
					}

					// title
					matrix[z][iy][ix][3] = geometry.getFieldLabel(xyz);

					// select target
					if (clickAction != null && selectable[z][y][x] != null)
						matrix[z][iy][ix][4] = MessageFormat.format(clickAction, selectable[z][y][x]);
				}
			}
		}
		model.put("matrix", matrix);
	}

	private void prepareGoboard(Board board, OrthogonalBoardGeometry geometry, PieceSet pieceSet, Map<Coordinate, String> clickables,
			Map<Coordinate, Cursor> cursors, String imgPath, String set, String clickAction, boolean flip, Map<String, Object> model)
	{
		prepareLabels(geometry, flip, model);

		// initialize markers and selectables
		String[][] selectable = new String[geometry.getHeight()][];
		Cursor[][] cursorArray = new Cursor[geometry.getHeight()][];
		for (int i = 0; i < geometry.getHeight(); i++)
		{
			selectable[i] = new String[geometry.getWidth()];
			cursorArray[i] = new Cursor[geometry.getWidth()];
		}

		if (clickables != null)
		{
			for (Map.Entry<Coordinate, String> entry : clickables.entrySet())
				selectable[geometry.getX(entry.getKey())][geometry.getY(entry.getKey())] = entry.getValue();
		}

		if (cursors != null)
		{
			for (Map.Entry<Coordinate, Cursor> entry : cursors.entrySet())
				cursorArray[geometry.getX(entry.getKey())][geometry.getY(entry.getKey())] = entry.getValue();
		}

		// matrix
		String[][][] matrix = new String[geometry.getHeight()][][];
		for (int iy = 0; iy < geometry.getHeight(); iy++)
		{
			matrix[iy] = new String[geometry.getWidth()][];
			for (int ix = 0; ix < geometry.getWidth(); ix++)
			{
				matrix[iy][ix] = new String[5];
				int x = flip ? geometry.getWidth() - ix - 1 : ix;
				int y = flip ? iy : geometry.getHeight() - iy - 1;

				int[] xy = new int[] { x, y };
				Piece piece = getPiece(geometry, board, xy);

				// square class
				String squareClass = "";
				if (cursorArray[x][y] != null)
					squareClass += "cursor";
				matrix[iy][ix][0] = squareClass;

				// image filename
				String img = "";
				if (piece != null)
				{
					img += pieceSet.getStringRepresentation(piece) + "s";
				}
				else
				{
					if (iy == 0)
						img += "n";
					else if (iy == geometry.getHeight() - 1)
						img += "s";

					if (ix == 0)
						img += "w";
					else if (ix == geometry.getWidth() - 1)
						img += "e";

					if (img.length() == 0)
						img += "x";
				}

				matrix[iy][ix][1] = imgPath + "/" + set + "/" + img + ".png";

				// alternate image text
				String alt = "";
				if (piece != null)
					alt += ((GoPieceSet) pieceSet).getColorChars()[piece.getColor()];
				matrix[iy][ix][2] = alt;

				// image title
				String title = geometry.getFieldLabel(xy);
				matrix[iy][ix][3] = title;

				// select target
				if (clickAction != null && selectable[x][y] != null)
					matrix[iy][ix][4] = MessageFormat.format(clickAction, selectable[x][y]);
			}
		}
		model.put("matrix", matrix);
	}

	private Piece getPiece(OrthogonalBoardGeometry geometry, Board board, int[] components)
	{
		Coordinate coordinate = geometry.getCoordinate(components);

		if (coordinate != null)
			return board.getPiece(coordinate);
		else
			return null;
	}

	private void prepareLabels(OrthogonalBoardGeometry geometry, boolean flip, Map<String, Object> model)
	{
		// labels
		String[] topLabels = new String[geometry.getWidth()];
		String[] bottomLabels = new String[geometry.getWidth()];
		for (int i = 0; i < geometry.getWidth(); i++)
		{
			topLabels[i] = flip ? geometry.getBottomLabel(geometry.getWidth() - i - 1) : geometry.getTopLabel(i);
			bottomLabels[i] = flip ? geometry.getTopLabel(geometry.getWidth() - i - 1) : geometry.getBottomLabel(i);
		}
		String[] leftLabels = new String[geometry.getHeight()];
		String[] rightLabels = new String[geometry.getHeight()];
		for (int i = 0; i < geometry.getHeight(); i++)
		{
			leftLabels[i] = flip ? geometry.getRightLabel(i) : geometry.getLeftLabel(geometry.getHeight() - i - 1);
			rightLabels[i] = flip ? geometry.getLeftLabel(i) : geometry.getRightLabel(geometry.getHeight() - i - 1);
		}
		model.put("top_labels", topLabels);
		model.put("bottom_labels", bottomLabels);
		model.put("left_labels", leftLabels);
		model.put("right_labels", rightLabels);
	}
}
