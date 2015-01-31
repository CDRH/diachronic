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
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class WhitespaceNormalization extends Transformation {

    @Override
    public void transform(Document document) throws Exception {
        document.normalizeDocument();
        traverse(document, node -> {
            final boolean textElement = TEXT_ELEMENTS.contains(node.getNodeName());
            final List<Node> textNodes = children(node).filter(isOfType(Node.TEXT_NODE)).collect(Collectors.toList());

            final StringBuilder textContent = new StringBuilder();
            for (Node textNode : textNodes) {
                final String text = textNode.getNodeValue().replaceAll("\\s+", " ");
                textNode.setNodeValue(text);
                if (!textElement) {
                    textContent.append(text);
                }
            }
            if (!textElement && textContent.toString().trim().length() == 0) {
                textNodes.forEach(node::removeChild);
            }
            return node;
        }, IS_ELEMENT);
    }

    private static final Set<String> TEXT_ELEMENTS = new HashSet<>(Arrays.asList("p", "add", "del", "rdg", "lem"));
}
