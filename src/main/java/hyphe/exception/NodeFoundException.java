package hyphe.exception;

import org.neo4j.graphdb.Node;

public class NodeFoundException extends RuntimeException {

	private Node node;

	public NodeFoundException(Node node ) {
		this.node = node;
	}

	public Node getNode() {
		return node;
	}
}
