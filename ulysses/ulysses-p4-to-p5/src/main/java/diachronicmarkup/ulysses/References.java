package diachronicmarkup.ulysses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static java.lang.Integer.parseInt;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class References extends Transformation {
    @Override
    public void transform(final Document document) throws Exception {
        traverse(document, node -> {
            final Node wit = preceding(node).stream().filter(hasNodeName("wit")).findFirst().orElse(null);
            if (wit != null) {
                final Element plref = (Element) node;
                final Element ref = document.createElementNS(Converter.TEI_P5_NS, "ref");

                ref.setAttribute("target", normalizeEditionRef(plref.getAttribute("ed")) +
                        ";page=" + (parseInt(plref.getAttribute("fromp")) - 1) + "," + parseInt(plref.getAttribute("top")) +
                        ";line=" + (parseInt(plref.getAttribute("froml")) - 1) + "," + parseInt(plref.getAttribute("tol")));

                ref.setTextContent(plref.getTextContent());
                plref.setTextContent(null);

                wit.appendChild(document.createTextNode(" "));
                wit.appendChild(ref);

                plref.getParentNode().removeChild(plref);

                return ref;
            }

            return node;
        }, IS_ELEMENT.and(hasNodeName("plref")));

        traverse(document, node -> {
            final Node wit = preceding(node).stream().filter(hasNodeName("wit")).findFirst().orElse(null);
            if (wit != null) {
                final Node preceding = precedingNode(node).get();

                wit.appendChild(document.createTextNode(" "));
                ((Element) wit.appendChild(document.renameNode(node, Converter.TEI_P5_NS, "ref"))).setAttribute("type", "depart");

                return preceding;
            }

            return node;
        }, IS_ELEMENT.and(hasNodeName("depart")));

        traverse(document, node -> {
            final Element element = (Element) node;

            element.removeAttribute("targOrder");

            final String edition = element.getAttribute("ed");
            if (!edition.isEmpty()) {
                element.setAttribute("ed", normalizeEditionRef(edition));
            }
            return element;
        }, IS_ELEMENT);
    }

    public static String normalizeEditionRef(String input) {
        input = XML.localAnchorToId(input).toLowerCase();
        if (!input.matches("(e[0-9][0-9])|(gab)|(syn)")) {
            throw new IllegalArgumentException(input);
        }
        return ("#" + input);
    }
}
