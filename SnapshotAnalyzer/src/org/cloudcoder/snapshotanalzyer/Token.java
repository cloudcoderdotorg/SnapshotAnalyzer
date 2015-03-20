package org.cloudcoder.snapshotanalzyer;

public class Token {
	private final TokenType tokenType;
	private final String lexeme;
	private final Position position;
	
	public Token(TokenType tokenType, String lexeme, Position position) {
		this.tokenType = tokenType;
		this.lexeme = lexeme;
		this.position = position;
	}
	
	public TokenType getTokenType() {
		return tokenType;
	}
	
	public String getLexeme() {
		return lexeme;
	}
	
	public Position getPosition() {
		return position;
	}
}
