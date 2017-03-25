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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XML {

    public static final Pattern WHITESPACE_RUN_PATTERN = Pattern.compile("\\s+");

    private static DocumentBuilderFactory documentBuilderFactory;
    private static XMLOutputFactory xmlOutputFactory;
    private static XMLInputFactory xmlInputFactory;
    private static TransformerFactory transformerFactory;

    public static DocumentBuilderFactory documentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setCoalescing(true);
            documentBuilderFactory.setExpandEntityReferences(true);
            documentBuilderFactory.setValidating(false);
        }
        return documentBuilderFactory;
    }

    public static DocumentBuilder newDocumentBuilder() {
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory().newDocumentBuilder();
            documentBuilder.setErrorHandler(STRICT_ERROR_HANDLER);
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static TransformerFactory transformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = TransformerFactory.newInstance();
        }
        return transformerFactory;
    }

    public static Transformer newTransformer() {
        try {
            return transformerFactory().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Transformer newIndentingTransformer() {
        final Transformer transformer = newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        return transformer;
    }

    public static Stream<Node> nodes(final NodeList nodeList) {
        final int nl = nodeList.getLength();
        return StreamSupport.stream(((Iterable<Node>) () -> new Iterator<Node>() {

            private int nc = 0;

            @Override
            public boolean hasNext() {
                return nc < nl;
            }

            @Override
            public Node next() {
                return nodeList.item(nc++);
            }
        }).spliterator(), false);
    }

    public static Stream<Node> elements(final NodeList nodeList) {
        return nodes(nodeList).filter((n) -> n.getNodeType() == Node.ELEMENT_NODE);
    }

    public static XMLOutputFactory outputFactory() {
        if (xmlOutputFactory == null) {
            xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        }
        return xmlOutputFactory;
    }

    public static XMLInputFactory inputFactory() {
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        }
        return xmlInputFactory;
    }

    public static Stream<XMLEvent> read(Source from) {
        try {
            final XMLEventReader eventReader = inputFactory().createXMLEventReader(from);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<XMLEvent>() {
                @Override
                public boolean hasNext() {
                    return eventReader.hasNext();
                }

                @Override
                public XMLEvent next() {
                    try {
                        return eventReader.nextEvent();
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    }
                }


            }, Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED), false).onClose(() -> close(eventReader));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(Iterator<XMLEvent> events, XMLEventWriter eventWriter) {
        try {
            while (events.hasNext()) {
                eventWriter.add(events.next());
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(Iterator<XMLEvent> events, Result to) {
        XMLEventWriter writer = null;
        try  {
            write(events, writer = outputFactory().createXMLEventWriter(to));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            close(writer);
        }
    }

    public static Source source(InputSource inputSource) throws SAXException {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        return new SAXSource(xmlReader, inputSource);
    }

    public static void close(XMLEventReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close(XMLEventWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<String> toValues(String value) {
        return WHITESPACE_RUN_PATTERN.splitAsStream(value).filter(v -> !v.isEmpty());
    }

    public static String toValueList(Stream<String> values) {
        return values.filter(v -> (v != null && !v.isEmpty())).collect(Collectors.joining(" "));
    }

    public static String localAnchorToId(String anchor) {
        return (anchor == null || anchor.isEmpty()) ? anchor : (anchor.charAt(0) == '#' ? anchor.substring(1) : anchor);
    }

    public static final ErrorHandler STRICT_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };
}
