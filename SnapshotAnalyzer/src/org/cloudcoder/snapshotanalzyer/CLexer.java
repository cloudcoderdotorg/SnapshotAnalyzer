package org.cloudcoder.snapshotanalzyer;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Simple ad-hoc C lexer.
 * Does not attempt to recognize all valid C tokens.
 * Does not assume that preprocessing has taken place
 * (and does handle some basic preprocessor directives.)
 * 
 * @author David Hovemeyer
 */
public class CLexer implements ILexer {
	private PushbackReader reader;
	private boolean eof;
	private Position previous, current;
	
	public CLexer(Reader reader) {
		this.reader = new PushbackReader(reader, 2);
		this.eof = false;
		this.previous = null;
		this.current = new Position(0, 0);
	}
	
	@Override
	public Token readNext() throws IOException, LexerException {
		int first;
		boolean sawComment;
		do {
			sawComment = false;
			
			// Skip whitespace
			skipWs();
			if (atEOF()) {
				// No more tokens
				return null;
			}
			
			// Peek ahead to the next character
			first = read();
			if (first < 0) {
				throw new IllegalStateException("Unexpected EOF?");
			}
			
			if (first == '/') {
				// Check to see if this is the start of a comment
				int second = read();
				if (second == '/') {
					sawComment = true;
					skipToEOL();
				} else if (second == '*') {
					sawComment = true;
					skipToEndOfBlockComment();
				} else {
					unread(second);
				}
			}
		} while (sawComment);
		
		reader.unread(first);
		
		switch (first) {
		// Preprocessor directives
		case '#': return readPreproc();
		
		// One character tokens
		case ';': return readOne(TokenType.SEMI);
		case ':': return readOne(TokenType.COLON);
		case ',': return readOne(TokenType.COMMA);
		case '.': return readOne(TokenType.DOT);
		case '(': return readOne(TokenType.LPAREN);
		case ')': return readOne(TokenType.RPAREN);
		case '[': return readOne(TokenType.LBRACKET);
		case ']': return readOne(TokenType.RBRACKET);
		case '{': return readOne(TokenType.LBRACKET);
		case '}': return readOne(TokenType.RBRACKET);
		
		// Operators
		case '+': return readPlusOp();
		case '-': return readMinusOp();
		case '*': return readTimesOp();
		case '/': return readDivOp();
		case '%': return readModOp();
		case '&': return readAndOp();
		case '|': return readOrOp();
		case '=': return readEqOp();
		case '!': return readNotOp();
		case '<': return readLtOp();
		case '>': return readGtOp();
		
		// Literal numbers
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			return readLiteralNumber();
			
		// Character literal
		case '\'': return readLiteralChar();
		
		// String literal
		case '"': return readLiteralString();
		
		default:
			if (Character.isAlphabetic(first) || first == '_') {
				return readIdentifierOrKeyword();
			} else {
				throw new LexerException("Unrecognized character", current);
			}
		}
	}
	
	/**
	 * Read one character and update the current and previous positions.
	 * 
	 * @return the character, -1 if there are no more characters to read
	 * @throws IOException
	 */
	private int read() throws IOException {
		int c = reader.read();
		if (c >= 0) {
			previous = current;
			current = updatePos(current, c);
		} else {
			eof = true;
		}
		return c;
	}
	
	private int mustRead(String msg) throws IOException {
		int c = read();
		if (c < 0) {
			throw new IOException(msg + " at " + current);
		}
		return c;
	}
	
	/**
	 * Unread a character, resetting current position to previous position.
	 * 
	 * @param c the character to unread
	 * @throws IOException
	 */
	private void unread(int c) throws IOException {
		if (previous == null) {
			throw new IllegalStateException("No character to unread!");
		}
		reader.unread(c);
		eof = false;
		current = previous;
		previous = null;
	}

	private boolean atEOF() {
		return eof;
	}

	private void skipWs() throws IOException {
		boolean done = false;
		while (!done) {
			int c = read();
			if (c < 0) {
				// Reached EOF
				done = true;
			} else if (!Character.isWhitespace(c)) {
				// Saw a non-whitespace character, unread it
				unread(c);
				done = true;
			}
		}
	}
	
	private void skipToEOL() throws IOException {
		while (true) {
			int c = read();
			if (c < 0 || c == '\n') {
				break;
			}
		}
	}
	
	private enum BlockState {
		SCAN,
		STAR,
		DONE,
	}
	
	private void skipToEndOfBlockComment() throws IOException {
		BlockState state = BlockState.SCAN;
		while (state != BlockState.DONE) {
			int c = read();
			if (c < 0) {
				break;
			}
			switch (state) {
			case SCAN:
				if (c == '*') {
					state = BlockState.STAR;
				}
				break;
			case STAR:
				if (c == '/') {
					state = BlockState.DONE;
				} else if (c != '*') {
					state = BlockState.SCAN;
				}
				break;
			default:
				throw new IllegalStateException("Should not happen");
			}
		}
	}

	private static Position updatePos(Position current, int c) {
		int row = current.getRow(), column = current.getColumn();
		if (c == '\n') {
			row++;
			column = 0;
		} else {
			column++;
		}
		return new Position(row, column);
	}

	private Token readPreproc() throws IOException {
		Position pos = current;
		String lexeme = munch(c -> (c == '#' || Character.isAlphabetic(c)));
		TokenType type;
		if (lexeme.equals("#define")) {
			type = TokenType.DEFINE;
		} else if (lexeme.equals("#include")) {
			type = TokenType.INCLUDE;
		} else {
			type = TokenType.UNKNOWN_PREPROC;
		}
		return new Token(type, lexeme, pos);
	}

	private Token readOne(TokenType tokenType) throws IOException {
		Position pos = current;
		int c = read();
		return new Token(tokenType, concatCodePoints(c), pos);
	}
	
	private Token readPlusOp() throws IOException {
		return readOp('+', TokenType.PLUS, TokenType.ASSIGN_PLUS, TokenType.INCREMENT);
	}
	
	private Token readMinusOp() throws IOException {
		return readOp('-', TokenType.MINUS, TokenType.ASSIGN_MINUS, TokenType.DECREMENT);
	}
	
	private Token readTimesOp() throws IOException {
		return readOp('*', TokenType.TIMES, TokenType.ASSIGN_TIMES, null);
	}
	
	private Token readDivOp() throws IOException {
		return readOp('/', TokenType.DIVIDE, TokenType.ASSIGN_DIVIDE, null);
	}
	
	private Token readModOp() throws IOException {
		return readOp('%', TokenType.MOD, TokenType.ASSIGN_MOD, null);
	}
	
	private Token readAndOp() throws IOException {
		return readOp('&', TokenType.BITAND, TokenType.ASSIGN_BITAND, TokenType.AND);
	}
	
	private Token readOrOp() throws IOException {
		return readOp('|', TokenType.BITOR, TokenType.ASSIGN_BITOR, TokenType.OR);
	}
	
	private Token readOp(int op, TokenType type, TokenType compoundAssignType, TokenType incDecType) throws IOException {
		Position pos = current;
		read(); // read operator
		int next = read();
		if (next == '=') { // check whether it's compound assignment
			return new Token(compoundAssignType, concatCodePoints(op, '='), pos);
		} else if (incDecType != null && next == op) { // check whether it's ++ or --
			return new Token(incDecType, concatCodePoints(op, op), pos);
		} else {
			// It's just the "plain" operator
			if (next >= 0) {
				unread(next);
			}
			return new Token(type, concatCodePoints(op), pos);
		}
	}
	
	private Token readEqOp() throws IOException {
		return readEqOrCompareOp('=', TokenType.ASSIGN, TokenType.EQ);
	}
	
	private Token readNotOp() throws IOException {
		return readEqOrCompareOp('!', TokenType.NOT, TokenType.NE);
	}
	
	private Token readLtOp() throws IOException {
		return readEqOrCompareOp('<', TokenType.LT, TokenType.LTE);
	}
	
	private Token readGtOp() throws IOException {
		return readEqOrCompareOp('>', TokenType.GT, TokenType.GTE);
	}
	
	private Token readEqOrCompareOp(int op, TokenType type, TokenType followedByEqType) throws IOException {
		Position pos = current;
		read(); // read op
		int next = read();
		if (next == op) {
			return new Token(followedByEqType, concatCodePoints(op, '='), pos);
		} else {
			if (next >= 0) {
				unread(next);
			}
			return new Token(type, concatCodePoints(op), pos);
		}
	}
	
	private enum NumberState {
		DIG,
		HEX,
		POINT,
		DONE,
	}
	
	private Token readLiteralNumber() throws IOException {
		Position pos = current;
		StringBuffer buf = new StringBuffer();
		TokenType type = TokenType.LITERAL_INT;
		
		NumberState state = NumberState.DIG;
		
		while (state != NumberState.DONE) {
			int c = read();
			if (c < 0) {
				state = NumberState.DONE;
			} else {
				switch (state) {
				case DIG:
					if (Character.isDigit(c)) {
						buf.append((char)c);
					} else if (buf.length() == 1 && buf.charAt(0) == '0' && (c == 'x' || c == 'X')) {
						buf.append((char)c);
						state = NumberState.HEX;
					} else if (c == '.') {
						buf.append((char)c);
						type = TokenType.LITERAL_DOUBLE;
						state = NumberState.POINT;
					} else if (c == 'f' || c == 'F') {
						buf.append((char)c);
						type = TokenType.LITERAL_FLOAT;
						state = NumberState.DONE;
					} else {
						unread(c);
						state = NumberState.DONE;
					}
					break;
				case HEX:
					if (Character.isDigit(c) || "abcdef".indexOf(Character.toLowerCase(c)) >= 0) {
						buf.append((char)c);
					} else {
						unread(c);
						state = NumberState.DONE;
					}
					break;
				case POINT:
					if (Character.isDigit(c)) {
						buf.append((char)c);
					} else if (c == 'f' || c == 'F') {
						buf.append((char)c);
						type = TokenType.LITERAL_FLOAT;
						state = NumberState.DONE;
					} else {
						unread(c);
						state = NumberState.DONE;
					}
					break;
				default:
					throw new IllegalStateException("Not possible");
				}
			}
		}
		
		return new Token(type, buf.toString(), pos);
	}
	
	private Token readLiteralChar() throws IOException, LexerException {
		Position pos = current;
		StringBuilder buf = new StringBuilder();
		buf.append((char)read()); // consume '
		int c = mustRead("Unexpected EOF in character literal");
		buf.append((char)c);
		if (c == '\\') {
			c = mustRead("Unexpected EOF in character literal");
			buf.append((char)c);
		}
		c = read();
		if (c != '\'') {
			throw new LexerException("Unexpected character in character literal", current);
		}
		return new Token(TokenType.LITERAL_CHAR, buf.toString(), pos);
	}
	
	private enum StringState {
		NORMAL,
		ESCAPE,
		DONE,
	}
	
	private Token readLiteralString() throws IOException, LexerException {
		Position pos = current;
		StringBuilder buf = new StringBuilder();
		buf.append((char)read()); // consume "
		StringState state = StringState.NORMAL;
		while (state != StringState.DONE) {
			int c;
			switch (state) {
			case NORMAL:
				c = mustRead("Unexpected EOF in string literal");
				buf.append((char)c);
				if (c == '"') {
					state = StringState.DONE;
				} else if (c == '\\') {
					state = StringState.ESCAPE;
				}
				break;
			case ESCAPE:
				c = mustRead("Unexpected EOF in string literal");
				buf.append((char)c);
				state = StringState.NORMAL;
				break;
			default:
				throw new IllegalStateException("Should not happen");
			}
		}
		return new Token(TokenType.LITERAL_STRING, buf.toString(), pos);
	}
	
	private static Map<String, TokenType> KEYWORD_MAP = new HashMap<>();
	static {
		KEYWORD_MAP.put("if", TokenType.IF);
		KEYWORD_MAP.put("else", TokenType.ELSE);
		KEYWORD_MAP.put("for", TokenType.FOR);
		KEYWORD_MAP.put("while", TokenType.WHILE);
		KEYWORD_MAP.put("do", TokenType.DO);
		KEYWORD_MAP.put("struct", TokenType.STRUCT);
		KEYWORD_MAP.put("void", TokenType.VOID);
		KEYWORD_MAP.put("short", TokenType.SHORT);
		KEYWORD_MAP.put("long", TokenType.LONG);
		KEYWORD_MAP.put("signed", TokenType.SIGNED);
		KEYWORD_MAP.put("unsigned", TokenType.UNSIGNED);
		KEYWORD_MAP.put("int", TokenType.INT);
		KEYWORD_MAP.put("char", TokenType.CHAR);
		KEYWORD_MAP.put("float", TokenType.FLOAT);
		KEYWORD_MAP.put("double", TokenType.DOUBLE);
		KEYWORD_MAP.put("bool", TokenType.BOOL);
		KEYWORD_MAP = Collections.unmodifiableMap(KEYWORD_MAP);
	}
	
	private Token readIdentifierOrKeyword() throws IOException {
		Position pos = current;
		String lexeme = munch(c -> Character.isAlphabetic(c) || Character.isDigit(c) || c == '_');
		if (KEYWORD_MAP.containsKey(lexeme)) {
			return new Token(KEYWORD_MAP.get(lexeme), lexeme, pos);
		} else {
			return new Token(TokenType.IDENT, lexeme, pos);
		}
	}
	
	private static String concatCodePoints(int... codePoints) {
		StringBuilder buf = new StringBuilder();
		for (int c : codePoints) {
			buf.append((char)c);
		}
		return buf.toString();
	}
	
	private String munch(Predicate<Integer> pred) throws IOException {
		StringBuilder buf = new StringBuilder();
		boolean done = false;
		while (!done) {
			int c = read();
			if (c < 0) {
				done = true;
			} else if (!pred.test(c)) {
				unread(c);
				done = true;
			} else {
				buf.append((char)c);
			}
		}
		return buf.toString();
	}
	
	public static void main(String[] args) throws IOException, LexerException {
		// Just for testing
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Read which file: ");
		String fileName = keyboard.nextLine();
		try (FileReader r = new FileReader(fileName)) {
			ILexer lexer = new CLexer(r);
			while (true) {
				Token t = lexer.readNext();
				if (t == null) {
					break;
				}
				System.out.println(t.getTokenType() + ":" + t.getLexeme() + ":" + t.getPosition());
			}
		}
	}
}
