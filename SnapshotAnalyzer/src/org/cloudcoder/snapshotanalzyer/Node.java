package org.cloudcoder.snapshotanalzyer;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private NodeType nodeType;
	private List<Token> tokens;
	private List<Node> children;
	
	public Node(NodeType nodeType) {
		this.nodeType = nodeType;
		this.tokens = new ArrayList<>();
		this.children = new ArrayList<>();
	}
	
	public NodeType getNodeType() {
		return nodeType;
	}
	
	public List<Token> getTokens() {
		return tokens;
	}
	
	public List<Node> getChildren() {
		return children;
	}
}
