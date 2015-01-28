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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DocumentTemplate extends Transformation {
    @Override
    public void transform(final Document document) throws Exception {

        // add TEI-P5 namespace
        traverse(document, node -> {
            final Element withNamespace = (Element) document.renameNode(node, Converter.TEI_P5_NS, node.getLocalName());
            if (withNamespace.hasAttribute("id")) {
                final String idValue = withNamespace.getAttribute("id");
                withNamespace.removeAttribute("id");
                withNamespace.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", idValue);
            }
            if (withNamespace.hasAttribute("lang")) {
                final String langValue = withNamespace.getAttribute("lang");
                withNamespace.removeAttribute("lang");
                withNamespace.setAttributeNS(XMLConstants.XML_NS_URI, "xml:lang", langValue);
            }
            return withNamespace;
        }, IS_ELEMENT);

        // add schema reference
        document.insertBefore(
                document.createProcessingInstruction("oxygen", "RNGSchema=\"http://gregor.middell.net/ulysses/tei_all_enhanced_app.rng\" type=\"xml\""),
                document.getDocumentElement()
        );

        // add header
        try (InputStream header = getClass().getResourceAsStream("/teiHeader.xml")) {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setCoalescing(true);
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            final Document teiHeader = documentBuilder.parse(header);

            final Node teiElement = document.getDocumentElement();
            teiElement.insertBefore(document.importNode(teiHeader.getDocumentElement(), true), teiElement.getFirstChild());
        }
    }
}
