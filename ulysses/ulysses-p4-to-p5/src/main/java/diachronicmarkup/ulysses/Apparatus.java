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
import org.w3c.dom.Node;

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Apparatus extends Transformation {

    @Override
    public void transform(final Document document) throws Exception {
        traverse(document, node -> {
            Element element = (Element) node;
            final String localName = element.getLocalName();
            if ("emen".equals(localName)) {
                element = (Element) document.renameNode(element, Converter.TEI_P5_NS, "app");
                element.setAttribute("type", ("emendation-" + element.getAttribute("type")).trim().replaceAll("\\-$", ""));
            } else if ("hico".equals(localName)) {
                element = (Element) document.renameNode(element, Converter.TEI_P5_NS, "app");
                element.setAttribute("type", ("edition-" + element.getAttribute("type")).trim().replaceAll("\\-$", ""));
            } else if ("syn".equals(localName)) {
                element = (Element) document.renameNode(element, Converter.TEI_P5_NS, "app");
                element.setAttribute("type", ("synopsis-" + element.getAttribute("type")).trim().replaceAll("\\-$", ""));
            } else if ("new".equals(localName)) {
                final Node parent = element.getParentNode();
                Node prev = element;
                while ((prev = prev.getPreviousSibling()) != null) {
                    if (prev.getNodeType() == Node.ELEMENT_NODE && "rdg".equals(prev.getNodeName())) {
                        prev = parent.insertBefore(element, prev);
                    }
                }
                element = (Element) document.renameNode(element, Converter.TEI_P5_NS, "lem");
            } else if ("old".equals(localName)) {
                element = (Element) document.renameNode(element, Converter.TEI_P5_NS, "rdg");
            }
            final String levels = element.getAttribute("levels");
            if (!levels.isEmpty()) {
                element.removeAttribute("levels");
                element.setAttribute("wit", levels);
            }

            return element;
        }, IS_ELEMENT);

        traverse(document, node -> {
            final Element app = (Element) node;
            app.removeAttribute("key");

            final Element lem = (Element) children(app).stream().filter(IS_ELEMENT.and(hasNodeName("lem"))).findFirst().orElse(null);
            if (lem == null) {
                return app;
            }

            app.removeAttribute("level");

            final String entrydoc = app.getAttribute("entrydoc");
            if (!entrydoc.isEmpty()) {
                app.removeAttribute("entrydoc");
                lem.setAttribute("cause", entrydoc + "-doc");
            }

            final String status = app.getAttribute("status");
            if ("certain".equals(status)) {
                app.removeAttribute("status");
                lem.setAttribute("cert", "high");
            }else if ("content-supplied".equals(status)) {
                app.removeAttribute("status");
                lem.setAttribute("cert", "unknown");
            }

            for (String attachAttr : Arrays.asList("from", "to")) {
                final String anchor = XML.localAnchorToId(app.getAttribute(attachAttr));
                if (!anchor.isEmpty()) {
                    app.setAttribute(attachAttr, "#" + anchor);
                }
            }

            final String hand = XML.localAnchorToId(app.getAttribute("hand"));
            if (Responsibilities.HAND_MAPPING.containsKey(hand) || Responsibilities.EDITOR_MAPPING.containsKey(hand)) {
                app.removeAttribute("hand");
                lem.setAttribute("resp", "#" + hand);
            }
            return app;
        }, IS_ELEMENT.and(hasNodeName("app")));

        following(document).stream()
                .filter(hasNodeName("back"))
                .forEach(back -> traverse(back, node -> document.renameNode(node, Converter.TEI_P5_NS, "head"), hasNodeName("lem")));
    }
}
