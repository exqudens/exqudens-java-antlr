package exqudens.antlr.processor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public interface NodeProcessor {

    static final String DOCUMENT_NODE_NAME = "#document";
    static final String TEXT_NODE_NAME = "#text";

    static NodeProcessor newInstance() {
        return new NodeProcessor() {};
    }

    default Node createNode(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Node node = builder.parse(new InputSource(new StringReader(content)));
            return node;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    default void removeEmptyTextNodes(Node rootNode, String... ignoreParentNames) {
        List<Node> forRemoveTextNodes = new ArrayList<>();
        for (Node n : getDescendants(rootNode)) {
            if (Node.TEXT_NODE == n.getNodeType() && TEXT_NODE_NAME.equals(n.getNodeName())) {
                if (ignoreParentNames != null && ignoreParentNames.length > 0) {
                    Set<String> ignoreParentNamesSet = Arrays.stream(ignoreParentNames).filter(Objects::nonNull).collect(Collectors.toSet());
                    if (n.getParentNode() != null && !ignoreParentNamesSet.contains(n.getParentNode().getNodeName())) {
                        if (n.getNodeValue().trim().isEmpty()) {
                            forRemoveTextNodes.add(n);
                        }
                    }
                } else {
                    if (n.getNodeValue().trim().isEmpty()) {
                        forRemoveTextNodes.add(n);
                    }
                }
            }
        }

        for (Node oldChild : forRemoveTextNodes) {
            if (oldChild.getParentNode() != null) {
                oldChild.getParentNode().removeChild(oldChild);
            }
        }
    }

    default List<Node> getDescendants(Node node) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(node);
        for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
            nodes.addAll(getDescendants(node.getChildNodes().item(i)));
        }
        return nodes;
    }

    default List<Integer> getIntegerPath(Node node) {
        List<Integer> path = new ArrayList<>();

        Node sibling = node;
        Integer index = 0;
        while ((sibling = sibling.getPreviousSibling()) != null) {
            index++;
        }
        path.add(index);

        Node parent = node;
        while ((parent = parent.getParentNode()) != null) {
            index = 0;
            sibling = parent;
            while ((sibling = sibling.getPreviousSibling()) != null) {
                index++;
            }
            if (parent.getParentNode() != null) {
                path.add(0, index);
            }
        }

        return path;
    }

    /*default List<Node> getNodePath(Node node) {
        List<Node> path = new ArrayList<>();
        path.add(node);
        Node parent = node;
        while ((parent = parent.getParentNode()) != null) {
            path.add(0, parent);
        }
        return path;
    }*/

    /*default List<Node> getTerminalNodes(Node node) {
        List<Node> nodes = new ArrayList<>();
        if (node.getChildNodes().getLength() > 0) {
            for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
                nodes.addAll(getTerminalNodes(node.getChildNodes().item(i)));
            }
        } else {
            nodes.add(node);
        }
        return nodes;
    }*/

    /*default Node getNode(List<Integer> integerPath, Node rootNode) {
        Node node = rootNode;
        Node last = rootNode;
        for (int i = 1; i < integerPath.size(); i++) {
            Integer pathIndex = integerPath.get(i);
            node = last.getChildNodes().item(pathIndex);
            last = node;
        }
        return node;
    }*/

    /*default List<Node> getAncestors(Node node) {
        if (node.getParentNode() == null) {
            return Collections.emptyList();
        }
        List<Node> ancestors = new ArrayList<>();
        node = node.getParentNode();
        while (node != null) {
            ancestors.add(0, node);
            node = node.getParentNode();
        }
        return ancestors;
    }*/

}
