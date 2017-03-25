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

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Comments extends Transformation {

    @Override
    public void transform(Document document) throws Exception {
        traverse(
                document,
                node -> precedingNode(node).orElse(node.getParentNode().removeChild(node)),
                isOfType(Node.COMMENT_NODE)
        );
    }
}
