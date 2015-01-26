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
            final List<Node> textNodes = children(node).stream().filter(isOfType(Node.TEXT_NODE)).collect(Collectors.toList());

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
