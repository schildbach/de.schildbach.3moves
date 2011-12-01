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

package de.schildbach.presentation;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class HtmlUtilsTest
{
	HtmlUtils htmlUtils = new HtmlUtils();

	@Test
	public void testLinkGames()
	{
		String contextPath = "";
		String text = "Spiel 123";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "spiel 123";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "Game 123";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "game 123";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "spiel 1";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "Partie 1";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "Match 1";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "Hallo. Spiel 1. Hugo.";
		assertEquals("Hallo. <a class=\"gametooltip\" href=\"/game/view_game.html?id=1\" name=\"1\">Spiel 1</a>. Hugo.", htmlUtils.linkGames(text,
				contextPath));
		text = "Spiel 1, Spiel 2";
		assertEquals(
				"<a class=\"gametooltip\" href=\"/game/view_game.html?id=1\" name=\"1\">Spiel 1</a>, <a class=\"gametooltip\" href=\"/game/view_game.html?id=2\" name=\"2\">Spiel 2</a>",
				htmlUtils.linkGames(text, contextPath));
		text = "Spiel 1?";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "(Spiel 1)";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "[Spiel 1]";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "{Spiel 1}";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "<Spiel 1>";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "\"Spiel 1\"";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "'Spiel 1'";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));
		text = "\nspiel 30834:\n";
		assertThat(htmlUtils.linkGames(text, contextPath), not(text));

		text = "HalloSpiel 1";
		assertEquals(text, htmlUtils.linkGames(text, contextPath));
		text = "Spiel 1hallo";
		assertEquals(text, htmlUtils.linkGames(text, contextPath));
	}

	@Test
	public void testLinkEMail()
	{
		String text = "bla@laber.de";
		assertEquals("<a href=\"mailto:bla@laber.de\">bla@laber.de</a>", htmlUtils.linkEMail(text));
		text = "bla@laber.de.";
		assertEquals("<a href=\"mailto:bla@laber.de\">bla@laber.de</a>.", htmlUtils.linkEMail(text));
		text = "bla@laber.de?";
		assertEquals("<a href=\"mailto:bla@laber.de\">bla@laber.de</a>?", htmlUtils.linkEMail(text));
		text = "(bla@laber.de)";
		assertThat(htmlUtils.linkEMail(text), not(text));
		text = "[bla@laber.de]";
		assertThat(htmlUtils.linkEMail(text), not(text));
		text = "{bla@laber.de}";
		assertThat(htmlUtils.linkEMail(text), not(text));
		text = "<bla@laber.de>";
		assertThat(htmlUtils.linkEMail(text), not(text));
		text = "\"bla@laber.de\"";
		assertThat(htmlUtils.linkEMail(text), not(text));
		text = "'bla@laber.de'";
		assertThat(htmlUtils.linkEMail(text), not(text));

		text = ".bla@laber.de";
		assertEquals(text, htmlUtils.linkEMail(text));
	}

	@Test
	public void testLinkUrls()
	{
		String text = "http://3moves.net";
		assertEquals("<a href=\"http://3moves.net\">http://3moves.net</a>", htmlUtils.linkUrls(text));
		text = "https://3moves.net";
		assertEquals("<a href=\"https://3moves.net\">https://3moves.net</a>", htmlUtils.linkUrls(text));
		text = "fdgfdg http://3moves.net/game/view_game.html?id=1234";
		assertThat(htmlUtils.linkUrls(text), not(text));
		text = "http://www.3moves.net/game/view_game.html?id=1234";
		assertEquals("<a href=\"http://www.3moves.net/game/view_game.html?id=1234\">http://www.3moves.net/game/view_game.html?id=1234</a>", htmlUtils
				.linkUrls(text));
	}
}
