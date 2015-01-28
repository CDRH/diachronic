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

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Attributes extends Transformation {

    @Override
    public void transform(Document document) throws Exception {
        traverse(document, node -> {
            Element element = (Element) node;

            element.removeAttribute("TEIform");

            if (element.hasAttribute("type") && "normal".equals(element.getAttribute("type"))) {
                element.removeAttribute("type");
            }

            return node;
        }, IS_ELEMENT);

        traverse(document, node -> {
            final Element q = (Element) node;
            final String direct = q.getAttribute("direct");
            if (!direct.isEmpty()) {
                q.removeAttribute("direct");
                q.setAttribute("rend", ("direct-" + direct + " " + q.getAttribute("rend")).trim());
            }
            return q;
        }, IS_ELEMENT.and(hasNodeName("q")));

        traverse(document, node -> {
            final Element element = (Element) node;
            for (String booleanAttributeName : Arrays.asList("anchored", "default")) {
                String attribute = element.getAttribute(booleanAttributeName).toLowerCase();
                if ("yes".equals(attribute)) {
                    element.setAttribute(booleanAttributeName, "true");
                } else if ("no".equals(attribute)) {
                    element.setAttribute(booleanAttributeName, "false");
                }
            }
            return element;
        }, IS_ELEMENT);
    }
}
