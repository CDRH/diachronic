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

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Converter {

    public static final String TEI_P5_NS = "http://www.tei-c.org/ns/1.0";

    public static void main(String... args) {
        try {
            final ArrayDeque<String> argsDeque = Arrays.stream(args).collect(Collectors.toCollection(ArrayDeque::new));
            final File inputFile = new File(Optional.ofNullable(argsDeque.poll()).orElseThrow(() -> new IllegalArgumentException("No input file given")));
            final File outputFile = new File(Optional.ofNullable(argsDeque.poll()).orElseThrow(() -> new IllegalArgumentException("No output file given")));

            final Document document = XML.newDocumentBuilder().parse(inputFile);

            for (Class<? extends Transformation> transformationClass : TEI_TRANSFORMATIONS) {
                transformationClass.newInstance().transform(document);
            }

            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                XML.newIndentingTransformer().transform(new DOMSource(document), new StreamResult(out));
            }

            try (
                    final InputStream schemaStream = Converter.class.getResourceAsStream("/tei_all_enhanced_app.rng");
                    final FileInputStream resultStream = new FileInputStream(outputFile)
            ) {

                final CustomErrorHandler validationErrors = new CustomErrorHandler();
                final PropertyMapBuilder schemaProps = new PropertyMapBuilder();
                schemaProps.put(ValidateProperty.XML_READER_CREATOR, new Sax2XMLReaderCreator());
                final Schema schema = SAXSchemaReader.getInstance().createSchema(new InputSource(schemaStream), schemaProps.toPropertyMap());


                final PropertyMapBuilder validationProps = new PropertyMapBuilder();
                validationProps.put(ValidateProperty.ERROR_HANDLER, validationErrors);
                final Validator validator = schema.createValidator(validationProps.toPropertyMap());

                final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setContentHandler(validator.getContentHandler());
                xmlReader.parse(new InputSource(resultStream));

                validationErrors.list.forEach(System.err::println);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static final Class<? extends Transformation>[] TEI_TRANSFORMATIONS = new Class[]{
            Comments.class,
            Attributes.class,
            DocumentTemplate.class,
            Responsibilities.class,
            Abbreviations.class,
            EditorialInterventions.class,
            Apparatus.class,
            References.class,
            Elements.class,
            WhitespaceNormalization.class,
            Breaks.class,
            FixIdReferences.class
    };

    private static class CustomErrorHandler implements ErrorHandler {

        private List<String> list = new ArrayList<>();

        private void register(SAXParseException e) {
            list.add(String.format("[%d:%d] %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }

        public void error(SAXParseException exception) throws SAXException {
            register(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            register(exception);
        }

        public void warning(SAXParseException exception) throws SAXException {
            register(exception);
        }
    }

}
