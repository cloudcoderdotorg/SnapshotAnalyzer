package org.cloudcoder.snapshotanalzyer;

import java.util.List;

public class CLooseParser {
	public CLooseParser() {
	}
	
	public Node parse(List<Token> tokens) {
		Node root = new Node(NodeType.ROOT);
		
		int pos = 0;
		
		while (pos < tokens.size()) {
			Token first = tokens.get(pos);
			
			if (first.getTokenType() == TokenType.INCLUDE) {
				// include directive: parse until end of line
				Node child = parseToEndOfLine(tokens, pos, NodeType.INCLUDE);
				root.getChildren().add(child);
				pos += child.getTokens().size();
			} else if (first.getTokenType() == TokenType.DEFINE) {
				// define directive: parse until end of line
				Node child = parseToEndOfLine(tokens, pos, NodeType.DEFINE);
				root.getChildren().add(child);
				pos += child.getTokens().size();
			} else if (first.getTokenType().isType()) {
				// this is probably a declaration
				
			}
		}
		
		return root;
	}

	private Node parseToEndOfLine(List<Token> tokens, int pos, NodeType nodeType) {
		int row = tokens.get(pos).getPosition().getRow();
		Node child = new Node(nodeType);
		child.getTokens().add(tokens.get(pos));
		int next = pos+1;
		while (next < tokens.size()) {
			if (tokens.get(next).getPosition().getRow() == row) {
				child.getTokens().add(tokens.get(next));
			} else {
				break;
			}
		}
		return child;
	}
}
