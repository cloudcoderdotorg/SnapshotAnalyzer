package org.cloudcoder.snapshotanalzyer;

import java.util.List;

public class CLooseParser {
	private TokenSequence seq;
	
	public CLooseParser(List<Token> tokens) {
		seq = new TokenSequence(tokens);
	}
	
	public Node parse() {
		Node root = new Node(NodeType.ROOT);
		
		int pos = 0;
		
		while (!seq.isFinished()) {
			Token first = seq.peek();
			
			if (first.getTokenType() == TokenType.INCLUDE) {
				// include directive: parse until end of line
				pos += addToTree(root, parseToEndOfLine(NodeType.INCLUDE));
			} else if (first.getTokenType() == TokenType.DEFINE) {
				// define directive: parse until end of line
				pos += addToTree(root, parseToEndOfLine(NodeType.DEFINE));
			} else if (first.getTokenType().isType()) {
				// this is probably a declaration
				pos += addToTree(root, parseDeclaration());
			} else {
				throw new IllegalStateException("Unknown construct");
			}
		}
		
		return root;
	}

	private int addToTree(Node parent, Node child) {
		parent.getChildren().add(child);
		int size = child.getTokens().size();
		return size;
	}

	private Node parseToEndOfLine(NodeType nodeType) {
		Node node = new Node(nodeType);
		int row = seq.peek().getPosition().getRow();
		while (!seq.isFinished()) {
			if (seq.peek().getPosition().getRow() != row) {
				break;
			}
			node.getTokens().add(seq.next());
		}
		return node;
	}
	
	private Node parseDeclaration() {
		Node decl = new Node(NodeType.DECLARATION);
		
		decl.getChildren().add(parseType());

		// For now, just handle single variables and functions?
		
		return decl;
	}

	private Node parseType() {
		Node type = new Node(NodeType.TYPE);
		if (seq.peek().getTokenType().isType()) {
			type.getTokens().add(seq.next());
			return type;
		} else if (seq.nextIs(TokenType.STRUCT)) {
			type.getTokens().add(seq.next());
			if (!seq.nextIs(TokenType.IDENT)) {
				// TODO: recovery
				throw new ParserException("Bad struct type");
			}
			type.getTokens().add(seq.next());
			return type;
		} else if (seq.nextIs(TokenType.IDENT)) {
			// Assume this is a typedef?
			type.getTokens().add(seq.next());
			return type;
		} else {
			throw new ParserException("Bad type");
		}
	}
}
