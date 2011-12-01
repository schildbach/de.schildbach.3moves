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

package de.schildbach.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class TextWrapperTest
{
	@Test
	public void testWrap()
	{
		String text = "Die Quellen Microsofts, braune Bettwäsche und der Unterbau der Sphinx\n\n"
				+ "Der Umgang mit dem Internet wird unsicherer und sicherer -- " + "das scheint eine Frage der Perspektive, so wie die Neigung, "
				+ "ob man eine 20-GByte-Festplatte, auf der sich 10 GByte Daten tummeln, "
				+ "als halb voll oder halb leer ansieht. Gewiefte Nutzer des weltweiten Netzes "
				+ "neigen ohnehin zur Vorsicht, auch gegenüber den verbreiteten Inhalten, die "
				+ "angesichts der schieren Fälle angebracht scheint. Wer ist nicht schon einmal "
				+ "durch einen Hoax auf den Arm genommen worden oder zeigte sich zumindest "
				+ "im Ansatz anfällig für eine Verschwörungstheorie? Das Internet sollte für den "
				+ "heutigen Tag noch ungeeigneter erscheinen, da darin auch anderntags nach Lust "
				+ "und Laune fabuliert wird. Man ist versucht, selbst einem so \"honorablen\" "
				+ "Menschen wie Konrad von Finckenstein keinen Glauben zu schenken -- "
				+ "auch wenn sein kanadisches Urteil sehr real und ganz unspaßig gemeint ist.\n\n"
				+ "So wird sich mancher heute bei einem Besuch der Website der "
				+ "Open-Source-Software-Sammlung FreshMeat die Augen oder seinen Browser "
				+ "gerieben haben. Die Beiträge auf der Eingangsseite zu Software-Updates "
				+ "scheinen völlig verunglückt zu sein, heißen die ersten Worte doch beispielsweise.";
		String expected = "Die Quellen Microsofts, braune Bettwäsche und der Unterbau der Sphinx\n\n"
				+ "Der Umgang mit dem Internet wird unsicherer und sicherer -- das\n"
				+ "scheint eine Frage der Perspektive, so wie die Neigung, ob man eine\n"
				+ "20-GByte-Festplatte, auf der sich 10 GByte Daten tummeln, als halb\n"
				+ "voll oder halb leer ansieht. Gewiefte Nutzer des weltweiten Netzes\n"
				+ "neigen ohnehin zur Vorsicht, auch gegenüber den verbreiteten Inhalten,\n"
				+ "die angesichts der schieren Fälle angebracht scheint. Wer ist nicht\n"
				+ "schon einmal durch einen Hoax auf den Arm genommen worden oder zeigte\n"
				+ "sich zumindest im Ansatz anfällig für eine Verschwörungstheorie? Das\n"
				+ "Internet sollte für den heutigen Tag noch ungeeigneter erscheinen, da\n"
				+ "darin auch anderntags nach Lust und Laune fabuliert wird. Man ist\n"
				+ "versucht, selbst einem so \"honorablen\" Menschen wie Konrad von\n"
				+ "Finckenstein keinen Glauben zu schenken -- auch wenn sein kanadisches\n"
				+ "Urteil sehr real und ganz unspaßig gemeint ist.\n\n"
				+ "So wird sich mancher heute bei einem Besuch der Website der\n"
				+ "Open-Source-Software-Sammlung FreshMeat die Augen oder seinen Browser\n"
				+ "gerieben haben. Die Beiträge auf der Eingangsseite zu Software-Updates\n"
				+ "scheinen völlig verunglückt zu sein, heißen die ersten Worte doch\n"
				+ "beispielsweise.\n";
		String actual = TextWrapper.wrap(text, 70);
		assertEquals(expected, actual);
	}
}
