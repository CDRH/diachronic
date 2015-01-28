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

import javax.xml.XMLConstants;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Responsibilities extends Transformation {
    @Override
    public void transform(Document document) throws Exception {
        final Element editionStmt = elementPath(document.getDocumentElement(), "teiHeader", "fileDesc", "editionStmt");
        for (Map.Entry<String, Participant> editor : EDITOR_MAPPING.entrySet()) {
            Element element = (Element) editionStmt.appendChild(document.createElementNS(Converter.TEI_P5_NS, "editor"));
            element.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", editor.getKey());
            element.setAttribute("role", editor.getValue().getRole());
            element.setTextContent(editor.getValue().getName());
        }

        final Element handNotes = elementPath(document.getDocumentElement(), "teiHeader", "profileDesc", "handNotes");
        for (Map.Entry<String, String> hand : HAND_MAPPING.entrySet()) {
            Element element = (Element) handNotes.appendChild(document.createElementNS(Converter.TEI_P5_NS, "handNote"));
            element.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", hand.getKey());
            element.setTextContent(hand.getValue());
        }

        traverse(document, node -> {
            final Element element = (Element) node;
            final String hand = element.getAttribute("hand").toLowerCase();
            if (HAND_MAPPING.containsKey(hand)) {
                element.setAttribute("hand", "#" + hand);
            } else if (EDITOR_MAPPING.containsKey(hand)) {
                element.removeAttribute("hand");
                element.setAttribute("resp", "#" + hand);
            }
            return element;
        }, IS_ELEMENT);
    }

    static final Map<String, String> HAND_MAPPING = new LinkedHashMap<>();

    static final Map<String, Participant> EDITOR_MAPPING = new LinkedHashMap<>();

    static {
        HAND_MAPPING.put("author", "James Joyce");
        HAND_MAPPING.put("editor", "Hans Walter Gabler");
        HAND_MAPPING.put("scribe", "Various typists and scribes");

        EDITOR_MAPPING.put("gabler", new Participant("Hans Walter Gabler", "principal"));
        EDITOR_MAPPING.put("rischer", new Participant("Tobias Rischer", "compiler"));
        EDITOR_MAPPING.put("middell", new Participant("Gregor Middell", "compiler"));
    }

    public static class Participant {

        private final String name;
        private final String role;

        public Participant(String name, String role) {
            this.name = name;
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }
    }
}
