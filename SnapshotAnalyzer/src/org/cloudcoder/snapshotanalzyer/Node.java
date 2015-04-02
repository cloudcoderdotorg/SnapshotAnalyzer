package org.cloudcoder.snapshotanalzyer;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private NodeType nodeType;
	private List<Node> children;
	private int startPos, endPos;
	
	public Node(NodeType nodeType) {
		this.nodeType = nodeType;
		this.children = new ArrayList<>();
		this.startPos = this.endPos = -1;
	}
	
	public NodeType getNodeType() {
		return nodeType;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	@Override
	public String toString() {
		return nodeType.toString();
	}
	
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	
	public int getStartPos() {
		return startPos;
	}
	
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}
	
	public int getEndPos() {
		return endPos;
	}
}
