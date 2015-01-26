package diachronicmarkup.ulysses;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FixIdReferences extends Transformation {

    @Override
    public void transform(Document document) throws Exception {
        traverse(document, node -> {
            final NamedNodeMap attributes = node.getAttributes();
            for (int ac = 0, al = attributes.getLength(); ac < al; ac++) {
                Attr attr = (Attr) attributes.item(ac);
                if ("wit".equals(attr.getName())) {
                    attr.setValue(XML.toValueList(XML.toValues(attr.getValue()).map(id -> "#" + XML.localAnchorToId(id))));
                }
            }
            return node;
        }, IS_ELEMENT);
    }
}
