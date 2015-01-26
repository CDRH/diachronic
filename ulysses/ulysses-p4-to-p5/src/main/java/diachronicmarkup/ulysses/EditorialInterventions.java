package diachronicmarkup.ulysses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Optional;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditorialInterventions extends Transformation {
    @Override
    public void transform(final Document document) throws Exception {
        traverse(document, node -> {
            final Element metaMark = (Element) document.renameNode(node, Converter.TEI_P5_NS, "metamark");
            metaMark.setAttribute("function", "emptyadd");
            return metaMark;
        }, IS_ELEMENT.and(hasNodeName("emptyadd")));

        traverse(document, node -> {
            Element overlay = (Element) node;
            if (!children(node).stream().anyMatch(hasNodeName("del"))) {
                final Optional<Node> precedingNode = precedingNode(overlay);
                final Node parent = overlay.getParentNode();
                for (Node child = overlay.getFirstChild(); child != null; child = overlay.getFirstChild()) {
                    if (IS_ELEMENT.and(hasNodeName("add")).test(child)) {
                        final Element add = (Element) child;
                        add.setAttribute("place", "overlay");
                        copyAttributes(overlay, add);
                    }
                    parent.insertBefore(overlay.removeChild(child), overlay);
                }
                return precedingNode.orElse(parent.removeChild(overlay));
            } else {
                final Element subst = (Element) document.renameNode(overlay, Converter.TEI_P5_NS, "subst");
                children(subst).stream().filter(IS_ELEMENT.and(hasNodeName("add"))).forEach(add -> ((Element) add).setAttribute("place", "overlay"));
                return subst;
            }
        }, IS_ELEMENT.and(hasNodeName("overlay")));

        traverse(document, node -> {
            final Element editorial = (Element) node;
            if ("certain".equals(editorial.getAttribute("status"))) {
                editorial.setAttribute("cert", "high");
                editorial.removeAttribute("status");
            }
            final String entrydoc = editorial.getAttribute("entrydoc");
            if ("lost".equals(entrydoc)) {
                editorial.setAttribute("evidence", "conjecture");
                editorial.removeAttribute("entrydoc");
            } else if ("extant".equals(entrydoc)) {
                editorial.setAttribute("evidence", "external");
                editorial.removeAttribute("entrydoc");
            }

            if (editorial.hasAttribute("type")) {
                final String type = editorial.getAttribute("type");
                if ("cc".equals(type)) {
                    editorial.removeAttribute("type");
                    editorial.setAttribute("instant", "true");
                } else if ("ch10".equals(type)){
                    // FIXME: @scope is wrong here
                    editorial.removeAttribute("type");
                    editorial.setAttribute("scope", "ch10");
                } else if ("pseudo".equals(type)) {
                    // FIXME: @scope is wrong here
                    editorial.removeAttribute("type");
                    editorial.setAttribute("scope", "pseudo");
                }
            }
            return editorial;
        }, IS_ELEMENT.and(hasNodeName("subst").or(hasNodeName("add")).or(hasNodeName("del"))));
    }
}
