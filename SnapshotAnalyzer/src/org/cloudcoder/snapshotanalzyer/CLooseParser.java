package org.cloudcoder.snapshotanalzyer;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

public class CLooseParser {
	private TokenSequence seq;
	
	public CLooseParser(List<Token> tokens) {
		seq = new TokenSequence(tokens);
	}
	
	public Node parse() {
		Node root = new Node(NodeType.UNIT);
		
		while (!seq.isFinished()) {
			Token first = seq.peek();
			
			if (first.getTokenType() == TokenType.INCLUDE) {
				// include directive: parse until end of line
				root.getChildren().add(parseToEndOfLine(NodeType.INCLUDE));
			} else if (first.getTokenType() == TokenType.DEFINE) {
				// define directive: parse until end of line
				root.getChildren().add(parseToEndOfLine(NodeType.DEFINE));
			} else if (first.getTokenType().isType()) {
				// this is probably a declaration
				root.getChildren().add(parseDeclaration());
				expect(TokenType.SEMI);
			} else {
				throw new ParserException(seq, "Unknown construct at " + seq.peek().getTokenType());
			}
		}
		
		return root;
	}

	private Node parseToEndOfLine(NodeType nodeType) {
		Node node = new Node(nodeType);
		int row = seq.peek().getPosition().getRow();
		while (!seq.isFinished()) {
			if (seq.peek().getPosition().getRow() != row) {
				break;
			}
			node.getTokens().add(seq.consume());
		}
		return node;
	}
	
	private Node parseDeclaration() {
		Node decl = new Node(NodeType.DECLARATION);
		
		decl.getChildren().add(parseType());
		decl.getChildren().add(parseDeclaratorList());
		
		return decl;
	}

	private Node parseType() {
		Node type = new Node(NodeType.TYPE);
		if (seq.peek().getTokenType().isType()) {
			type.getTokens().add(seq.consume());
			return type;
		} else if (seq.nextIs(TokenType.STRUCT)) {
			type.getTokens().add(seq.consume());
			if (!seq.nextIs(TokenType.IDENT)) {
				// TODO: recovery
				throw new ParserException(seq, "Bad struct type");
			}
			type.getTokens().add(seq.consume());
			return type;
		} else if (seq.nextIs(TokenType.IDENT)) {
			// Assume this is a typedef?
			type.getTokens().add(seq.consume());
			return type;
		} else {
			throw new ParserException(seq, "Bad type");
		}
	}

	private Node parseDeclaratorList() {
		Node declaratorList = new Node(NodeType.DECLARATOR_LIST);
		
		while (true) {
			declaratorList.getChildren().add(parseDeclarator());
			if (seq.nextIs(TokenType.COMMA)) {
				// there are more declarator
				seq.consume();
			} else {
				break;
			}
		}
		
		return declaratorList;
	}

	private Node parseDeclarator() {
		// For now, just simple variables and functions
		Node declarator = new Node(NodeType.DECLARATOR);
		if (seq.nextIs(TokenType.IDENT)) {
			declarator.getTokens().add(seq.consume());
			if (seq.nextIs(TokenType.LPAREN)) {
				// it's a function, parse the parameter list
				declarator.getChildren().add(parseParameterList());
				if (seq.nextIs(TokenType.SEMI)) {
					// this is a function prototype
					seq.consume();
				} else if (seq.nextIs(TokenType.LBRACE)) {
					// this is a function definition
					declarator.getChildren().add(parseBlockStatement());
				} else {
					// TODO: recover
					throw new ParserException(seq, "Unexpected token following parameter list");
				}
			}
		}
		return declarator;
	}

	private Node parseParameterList() {
		Node paramList = new Node(NodeType.PARAMETER_LIST);
		
		expect(TokenType.LPAREN);
		
		// I suppose we should distinguish between different kinds of
		// declarators, although just treating them all the same seems like
		// it should work
		while (true) {
			if (seq.nextIs(TokenType.RPAREN)) {
				break;
			}
			paramList.getChildren().add(parseDeclarator());
			if (seq.nextIs(TokenType.COMMA)) {
				seq.consume();
			}
		}
		
		expect(TokenType.RPAREN);
		
		return paramList;
	}

	private Node parseBlockStatement() {
		Node block = new Node(NodeType.BLOCK_STATEMENT);
		
		expect(TokenType.LBRACE);

		expect(TokenType.RBRACE);
		
		return block;
	}

	private void expect(TokenType type) {
		if (seq.isFinished()) {
			throw new ParserException(seq, "Unexpected end of input");
		}
		if (!seq.nextIs(type)) {
			throw new ParserException(seq, "Expected " + type + ", saw " + seq.peek().getTokenType());
		}
		seq.consume();
	}
	
	public static void main(String[] args) throws IOException, LexerException {
		//InputStreamReader r = new InputStreamReader(System.in, Charset.forName("UTF-8"));
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Input file: ");
		try (Reader r = new FileReader(keyboard.nextLine())) {
			CLexer lexer = new CLexer(r);
			List<Token> tokens = LexerUtil.readAll(lexer);
			System.out.println("Read " + tokens.size() + " tokens");
			CLooseParser parser = new CLooseParser(tokens);
			Node unit = parser.parse();
			TreePrinter tp = new TreePrinter();
			tp.print(unit);
		}
	}
}
