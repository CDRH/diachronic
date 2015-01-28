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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Elements extends Transformation {

    @Override
    public void transform(final Document document) throws Exception {
        traverse(document, node -> ELEMENTS_TO_REMOVE.contains(node.getNodeName())
                ? precedingNode(node).orElse(node.getParentNode().removeChild(node))
                : node, IS_ELEMENT);

        traverse(document, node -> {
            final String newName = NAME_MAPPING.get(node.getLocalName());
            return (newName == null ? node : document.renameNode(node, Converter.TEI_P5_NS, newName));
        }, IS_ELEMENT);

        traverse(document, node -> {
            Element element = (Element) document.renameNode(node, Converter.TEI_P5_NS, "div");
            if (element.hasAttribute("type")) {
                element.setAttribute("subtype", element.getAttribute("type"));
            }
            element.setAttribute("type", "textual-note");
            return element;
        }, IS_ELEMENT.and(hasNodeName("tn")));

        traverse(document, node -> {
            node = document.renameNode(node, Converter.TEI_P5_NS, "seg");
            ((Element) node).setAttribute("type", "line");
            return node;
        }, IS_ELEMENT.and(hasNodeName("dline")));

        traverse(document, node -> {
            node = document.renameNode(node, Converter.TEI_P5_NS, "metamark");
            ((Element) node).setAttribute("function", "docChange");
            return node;
        }, IS_ELEMENT.and(hasNodeName("docChange")));
    }

    private static Map<String, String> NAME_MAPPING = new HashMap<>();

    static {
        NAME_MAPPING.put("TEI.2", "TEI");
        NAME_MAPPING.put("comm", "note");
        NAME_MAPPING.put("display", "p");
    }

    public static List<String> parse(String desc) {
        return Pattern.compile("[\\+,]").splitAsStream(desc.replaceAll("[^\\p{Print}]", " "))
                .filter(d -> !d.isEmpty())
                .map(String::trim)
                .map(changeDesc -> {
                    final boolean inMargin = changeDesc.contains("(margin)");
                    final boolean printingCopyUnchanged = changeDesc.contains("pcu");
                    if (inMargin) {
                        changeDesc = changeDesc.replace("(margin)", "");
                    }
                    if (printingCopyUnchanged) {
                        changeDesc = changeDesc.replace("pcu", "");
                    }
                    return changeDesc.trim() + (inMargin ? "-in-margin" : "") + (printingCopyUnchanged ? "-print-copy-unchanged" : "");
                })
                .collect(Collectors.toList());
    }

    private static final Set<String> ELEMENTS_TO_REMOVE = Collections.singleton("synLevelList");
}
