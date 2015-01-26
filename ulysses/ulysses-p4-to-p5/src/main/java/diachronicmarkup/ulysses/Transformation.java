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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class Transformation {

    public abstract void transform(Document document) throws Exception;

    public static final Predicate<Node> IS_ELEMENT = isOfType(Node.ELEMENT_NODE);

    public static void traverse(Node node, Function<Node, Node> transformFunc) {
        traverse(node, transformFunc, n -> true);
    }

    public static void traverse(Node node, Function<Node, Node> transformFunc, Predicate<Node> filter) {
        traverse(node, transformFunc, filter, Transformation::followingNode);
    }

    public static void traverse(Node node, Function<Node, Node> transformFunc, Predicate<Node> filter, Function<Node, Optional<Node>> successorFunc) {
        Optional<Node> next = Optional.ofNullable(node);
        while (next.isPresent()) {
            next = axis(next.get(), successorFunc, false).stream().filter(filter).map(transformFunc).findFirst();
        }
    }

    public static Element copyAttributes(Node source, Element target) {
        final NamedNodeMap attributes = source.getAttributes();
        for (int ac = 0, al = attributes.getLength(); ac < al; ac++) {
            final Node attr = attributes.item(ac);
            final Attr previousAttr = target.setAttributeNode((Attr) attr.cloneNode(false));
            if (previousAttr != null) {
                throw new IllegalStateException(attr.getNodeName());
            }
        }
        return target;
    }

    public static Optional<Node> followingNode(Node node) {
        Node following = node.getFirstChild();
        if (following != null) {
            return Optional.of(following);
        }
        following = node.getNextSibling();
        if (following != null) {
            return Optional.of(following);
        }
        for (Node ancestor : ancestors(node)) {
            following = ancestor.getNextSibling();
            if (following != null) {
                return Optional.of(following);
            }
        }
        return Optional.empty();
    }

    public static Optional<Node> precedingNode(final Node node) {
        Node preceding = node.getPreviousSibling();
        if (preceding == null) {
            return Optional.ofNullable(node.getParentNode());
        } else {
            while (preceding.hasChildNodes()) {
                preceding = preceding.getLastChild();
            }
            return Optional.of(preceding);
        }
    }

    protected static Collection<Node> axis(Node node, Function<Node, Optional<Node>> successorFunc, boolean includeSelf) {
        return new AbstractCollection<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    private Optional<Node> current = (includeSelf ? Optional.ofNullable(node) : successorFunc.apply(node));

                    @Override
                    public boolean hasNext() {
                        return current.isPresent();
                    }

                    @Override
                    public Node next() {
                        final Node next = current.get();
                        current = successorFunc.apply(next);
                        return next;
                    }
                };
            }

            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }
        };
    }
    public static Collection<Node> following(final Node node) {
        return axis(node, Transformation::followingNode, false);
    }

    public static Collection<Node> preceding(final Node node) {
        return axis(node, Transformation::precedingNode, false);
    }

    public static Collection<Node> ancestors(final Node node) {
        return axis(node, n -> Optional.ofNullable(n.getParentNode()), false);
    }

    public static Collection<Node> children(final Node parent) {
        return new AbstractCollection<Node>() {

            final NodeList children = parent.getChildNodes();
            final int length = children.getLength();

            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {

                    int nc = 0;

                    @Override
                    public boolean hasNext() {
                        return (nc < length);
                    }

                    @Override
                    public Node next() {
                        return children.item(nc++);
                    }
                };
            }

            @Override
            public int size() {
                return length;
            }
        };
    }

    public static Predicate<Node> isOfType(final short nodeType) {
        return (node -> node.getNodeType() == nodeType);
    }

    public static Predicate<Node> hasNodeName(final String nodeName) {
        return (node -> nodeName.equals(node.getNodeName()));
    }

    public static Element elementPath(Node current, String... elementNames) {
        for (String elementName : elementNames) {
            current = children(current).stream().filter(IS_ELEMENT.and(hasNodeName(elementName))).findFirst().orElse(null);
            if (current == null) {
                current = current.appendChild(doc(current).createElementNS(Converter.TEI_P5_NS, elementName));
            }
        }
        return (Element) current;
    }

    public static Document doc(Node node) {
        return (node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node : node.getOwnerDocument());
    }
}
