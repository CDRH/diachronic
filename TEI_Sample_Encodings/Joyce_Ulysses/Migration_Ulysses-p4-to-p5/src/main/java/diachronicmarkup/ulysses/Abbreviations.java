/*
 * Copyright (c) 2015 Gregor Middell.
 *
 * This file is part of the Ulysses TEI P4-to-P5 Converter.
 *
 * The Converter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Converter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Converter. If not, see <http://www.gnu.org/licenses/>.
 */

package diachronicmarkup.ulysses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Abbreviations extends Transformation {
    @Override
    public void transform(final Document document) throws Exception {
        traverse(document, node -> {
            Element reg = (Element) node;
            if (!reg.hasAttribute("orig")) {
                return node;
            }
            final String orig = reg.getAttribute("orig");
            reg.removeAttribute("orig");

            final Element choice = (Element) reg.getParentNode().insertBefore(document.createElementNS(Converter.TEI_P5_NS, "choice"), reg);
            choice.appendChild(document.createElementNS(Converter.TEI_P5_NS, "orig")).setTextContent(orig);
            choice.appendChild(reg);
            return choice;
        }, IS_ELEMENT.and(hasNodeName("reg")));

        traverse(document, node -> {
            Element orig = (Element) node;
            if (!orig.hasAttribute("reg")) {
                return node;
            }
            final String reg = orig.getAttribute("reg");
            orig.removeAttribute("reg");

            final Element choice = (Element) orig.getParentNode().insertBefore(document.createElementNS(Converter.TEI_P5_NS, "choice"), orig);
            choice.appendChild(orig);
            choice.appendChild(document.createElementNS(Converter.TEI_P5_NS, "reg")).setTextContent(reg);
            return choice;
        }, IS_ELEMENT.and(hasNodeName("orig")));

        traverse(document, node -> {
            final Element abbr = (Element) node;
            final String expan = abbr.getAttribute("expan");
            if (expan.isEmpty()) {
                return abbr;
            }

            abbr.removeAttribute("expan");

            final Element choice = (Element) abbr.getParentNode().insertBefore(document.createElementNS(Converter.TEI_P5_NS, "choice"), abbr);
            choice.appendChild(abbr);
            choice.appendChild(document.createElementNS(Converter.TEI_P5_NS, "expan")).setTextContent(expan);

            return abbr;
        }, IS_ELEMENT.and(hasNodeName("abbr")));
    }
}
