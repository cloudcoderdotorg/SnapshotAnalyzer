package org.cloudcoder.snapshotanalzyer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Scanner;

/**
 * Ad-hoc parser for a reasonable subset of C (the subset we would
 * expect to see in exercise submissions for an intro programming
 * course.
 * 
 * @author David Hovemeyer
 */
public class CParser {
	private TokenSequence seq;
	
	public CParser(List<Token> tokens) {
		seq = new TokenSequence(tokens);
	}
	
	public Node parse() {
		Node root = new Node(NodeType.UNIT);
		
		root.setStartPos(seq.getPos());
		
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
			} else {
				throw new ParserException(seq, "Unknown construct at " + seq.peek().getTokenType());
			}
		}
		
		root.setEndPos(seq.getPos());
		
		return root;
	}

	private Node parseToEndOfLine(NodeType nodeType) {
		Node node = new Node(nodeType);
		
		node.setStartPos(seq.getPos());
		
		int row = seq.peek().getPosition().getRow();
		while (!seq.isFinished()) {
			if (seq.peek().getPosition().getRow() != row) {
				break;
			}
			seq.consume();
		}
		
		node.setEndPos(seq.getPos());
		
		return node;
	}
	
	private Node parseDeclaration() {
		Node decl = new Node(NodeType.DECLARATION);
		
		decl.setStartPos(seq.getPos());
		
		decl.getChildren().add(parseType());
		decl.getChildren().add(parseDeclaratorList());
		
		decl.setEndPos(seq.getPos());
		
		return decl;
	}

	private Node parseType() {
		Node type = new Node(NodeType.TYPE);
		
		type.setStartPos(seq.getPos());
		
		if (seq.peek().getTokenType().isType()) {
			seq.consume();
		} else if (seq.nextIs(TokenType.STRUCT)) {
			seq.consume();
			if (!seq.nextIs(TokenType.IDENT)) {
				// TODO: recovery
				throw new ParserException(seq, "Bad struct type");
			}
			seq.consume();
		} else if (seq.nextIs(TokenType.IDENT)) {
			// Assume this is a typedef?
			seq.consume();
		} else {
			throw new ParserException(seq, "Bad type");
		}
		
		type.setEndPos(seq.getPos());
		
		return type;
	}

	private Node parseDeclaratorList() {
		Node declaratorList = new Node(NodeType.DECLARATOR_LIST);
		
		declaratorList.setStartPos(seq.getPos());
		
		while (true) {
			declaratorList.getChildren().add(parseDeclarator());
			if (seq.nextIs(TokenType.COMMA)) {
				// there are more declarator
				seq.consume();
			} else {
				break;
			}
		}
		
		declaratorList.setEndPos(seq.getPos());
		
		return declaratorList;
	}

	private Node parseDeclarator() {
		// For now, just simple variables and functions
		Node declarator = new Node(NodeType.DECLARATOR);
		
		declarator.setStartPos(seq.getPos());
		
		if (seq.nextIs(TokenType.IDENT)) {
			seq.consume();
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
		} else {
			throw new ParserException(seq, "Unknown declarator");
		}
		
		declarator.setEndPos(seq.getPos());
		
		return declarator;
	}

	private Node parseParameterList() {
		Node paramList = new Node(NodeType.PARAMETER_LIST);
		
		paramList.setStartPos(seq.getPos());
		
		expect(TokenType.LPAREN);
		
		// I suppose we should distinguish between different kinds of
		// declarators, although just treating them all the same seems like
		// it should work
		while (true) {
			if (seq.nextIs(TokenType.RPAREN)) {
				break;
			}
			//paramList.getChildren().add(parseDeclarator());
			paramList.getChildren().add(parseParameter());
			if (seq.nextIs(TokenType.COMMA)) {
				seq.consume();
			}
		}
		
		expect(TokenType.RPAREN);
		
		paramList.setEndPos(seq.getPos());
		
		return paramList;
	}

	private Node parseParameter() {
		// Parameters are kind of like declarations, except that they don't
		// necessarily have an identifier (parameter name)
		Node param = new Node(NodeType.PARAMETER);
		
		param.setStartPos(seq.getPos());
		
		param.getChildren().add(parseType());
		// For now, just support simple variable names (not arrays yet)
		if (seq.nextIs(TokenType.IDENT)) {
			seq.consume();
		}
		
		param.setEndPos(seq.getPos());
		
		return param;
	}

	private Node parseBlockStatement() {
		Node block = new Node(NodeType.BLOCK_STATEMENT);
		
		block.setStartPos(seq.getPos());
		
		expect(TokenType.LBRACE);
		
		while (!seq.isFinished() && !seq.nextIs(TokenType.RBRACE)) {
			block.getChildren().add(parseStatement());
		}

		expect(TokenType.RBRACE);
		
		block.setEndPos(seq.getPos());
		
		return block;
	}

	private Node parseStatement() {
		if (seq.isFinished()) {
			throw new ParserException(seq, "Unexpected EOF looking for statement");
		}
		if (seq.nextIs(TokenType.LBRACE)) {
			return parseBlockStatement();
		} else if (seq.nextIs(TokenType.IF)) {
			return parseIfStatement();
		} else if (seq.nextIs(TokenType.WHILE)) {
			return parseWhileStatement();
		} else if (seq.nextIs(TokenType.FOR)) {
			return parseForStatement();
		} else if (seq.nextIs(TokenType.DO)) {
			return parseDoWhileStatement();
		} else {
			// Are there other types of statements?
			// Do we need to be interested in the code within statements?
			return parseToNextSemi(NodeType.STATEMENT);
		}
	}

	private Node parseIfStatement() {
		Node ifStmt = new Node(NodeType.IF_STATEMENT);
		ifStmt.setStartPos(seq.getPos());
		
		parseGuardedStatement("if", ifStmt);
		
		// See if there is an else part
		if (seq.nextIs(TokenType.ELSE)) {
			seq.consume();
			ifStmt.getChildren().add(parseStatement());
		}
		
		ifStmt.setEndPos(seq.getPos());
		
		return ifStmt;
	}

	private Node parseWhileStatement() {
		Node whileStmt = new Node(NodeType.WHILE_STATEMENT);
		whileStmt.setStartPos(seq.getPos());
		parseGuardedStatement("while", whileStmt);
		whileStmt.setEndPos(seq.getPos());
		return whileStmt;
	}

	private Node parseForStatement() {
		Node forStmt = new Node(NodeType.FOR_STATEMENT);
		forStmt.setStartPos(seq.getPos());
		parseGuardedStatement("for", forStmt);
		forStmt.setEndPos(seq.getPos());
		return forStmt;
	}

	private void parseGuardedStatement(String kw, Node stmt) {
		// Find guard expression
		seq.consume();
		if (!seq.nextIs(TokenType.LPAREN)) {
			// TODO: recovery
			throw new ParserException(seq, kw + " keyword not followed by left paren");
		}
		
		// Find matching right paren
		int rparen = seq.findMatching(TokenType.LPAREN, TokenType.RPAREN);
		if (rparen < 0) {
			// TODO: recover
			throw new ParserException(seq, "Could not find matching right parenthesis");
		}
		
		// Add condition: note that we haven't actually parsed the expression
		Node condition = new Node(NodeType.EXPRESSION);
		condition.setStartPos(seq.getPos() + 1);
		condition.setEndPos(rparen);
		stmt.getChildren().add(condition);
		
		// Parse child statement
		seq.setPos(rparen+1);
		Node body = parseStatement();
		stmt.getChildren().add(body);
	}

	private Node parseDoWhileStatement() {
		Node doWhileStmt = new Node(NodeType.DO_WHILE_STATEMENT);
		
		seq.consume();
		doWhileStmt.getChildren().add(parseStatement());
		if (!seq.nextIs(TokenType.WHILE)) {
			// TODO: recover
			throw new ParserException(seq, "Missing while after body of do/while");
		}
		seq.consume();
		if (!seq.nextIs(TokenType.LPAREN)) {
			// TODO: recover
			throw new ParserException(seq, "Missing left parenthesis in condition of do/while");
		}
		int rparen = seq.findMatching(TokenType.LPAREN, TokenType.RPAREN);
		if (rparen < 0) {
			// TODO: recover
			throw new ParserException(seq, "Could not find matching right parenthesis");
		}
		Node condition = new Node(NodeType.EXPRESSION);
		condition.setStartPos(seq.getPos() + 1);
		condition.setEndPos(rparen);
		
		seq.setPos(rparen+1);
		
		expect(TokenType.SEMI);
		
		return doWhileStmt;
	}

	private Node parseToNextSemi(NodeType type) {
		int semi = seq.findNext(TokenType.SEMI);
		if (semi < 0) {
			throw new ParserException(seq, "Could not find semicolon terminating statement");
		}
		Node node = new Node(type);
		node.setStartPos(seq.getPos());
		node.setEndPos(semi+1);
		seq.setPos(semi+1);
		return node;
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
			CParser parser = new CParser(tokens);
			Node unit = parser.parse();
			TreePrinter tp = new TreePrinter();
			tp.print(unit);
		}
	}
}
