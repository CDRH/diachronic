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
