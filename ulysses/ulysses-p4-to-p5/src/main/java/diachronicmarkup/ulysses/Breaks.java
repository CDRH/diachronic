package diachronicmarkup.ulysses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Breaks extends Transformation {

    private static final Predicate<Node> IS_PB = hasNodeName("pb");
    private static final Predicate<Node> IS_LB = hasNodeName("lb");
    private static final Predicate<Node> IS_MILESTONE_ELEMENT = hasNodeName("milestone");
    private static final Predicate<Node> IS_MILESTONE = Transformation.IS_ELEMENT.and(IS_PB.or(IS_LB).or(IS_MILESTONE_ELEMENT));

    private static final Predicate<Node> IS_TEXT = Transformation.isOfType(Node.TEXT_NODE);

    private static final Predicate<Node> IS_TEXT_OR_MILESTONE = IS_TEXT.or(IS_MILESTONE);

    @Override
    public void transform(final Document document) throws Exception {
        traverse(document, node -> {
            Element element = (Element) node;
            if ("#gab".equals(element.getAttribute("ed"))) {
                element.removeAttribute("ed");
            } else {
                element.setAttribute("unit", element.getNodeName());
                element = (Element) document.renameNode(element, Converter.TEI_P5_NS, "milestone");
            }
            return element;
        }, IS_PB.or(IS_LB));

        document.normalize();

        traverse(document, (node) -> {
            final Node following = followingNode(node).orElse(null);
            if (following == null || IS_TEXT_OR_MILESTONE.test(following)) {
                return node;
            }

            final Node nextText = following(node).stream().filter(IS_TEXT_OR_MILESTONE).findFirst().orElse(null);
            if (nextText == null) {
                return node;
            }
            final Optional<Node> preceding = preceding(node).stream().filter(IS_TEXT_OR_MILESTONE.negate()).findFirst();
            nextText.getParentNode().insertBefore(node, nextText);
            return preceding.orElse(following);
        }, IS_MILESTONE);

        traverse(document, node -> {
            Element ms = (Element) document.renameNode(node, Converter.TEI_P5_NS, "milestone");
            if (ms.hasAttribute("gi")) {
                ms.setAttribute("unit", ms.getAttribute("gi"));
                ms.removeAttribute("gi");
            }
            if (ms.hasAttribute("btype")) {
                ms.setAttribute("type", ms.getAttribute("btype"));
                ms.removeAttribute("btype");
            }
            return ms;
        }, IS_ELEMENT.and(hasNodeName("boundary")));

        traverse(document, node -> {
            final Element wbreak = (Element) node;
            final String level = wbreak.getAttribute("level").trim();
            if (!level.isEmpty() && "lb".equals(wbreak.getAttribute("where"))) {
                final Node parentNode = wbreak.getParentNode();

                final Element app = (Element) parentNode.insertBefore(document.createElementNS(Converter.TEI_P5_NS, "app"), wbreak);
                app.appendChild(document.createElementNS(Converter.TEI_P5_NS, "lem"));

                final Element rdg = (Element) app.appendChild(document.createElementNS(Converter.TEI_P5_NS, "rdg"));
                rdg.setAttribute("wit", level);
                if ("1".equals(wbreak.getAttribute("hyphen"))) {
                    rdg.appendChild(document.createTextNode("\u00ad"));
                }
                rdg.appendChild(document.createElementNS(Converter.TEI_P5_NS, "lb"));

                parentNode.removeChild(wbreak);
                return app;
            }
            return wbreak;
        }, IS_ELEMENT.and(hasNodeName("wbreak")));
    }
}
