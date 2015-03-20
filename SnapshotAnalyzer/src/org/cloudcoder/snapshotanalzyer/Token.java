package org.cloudcoder.snapshotanalzyer;

public class Token {
	private final TokenType tokenType;
	private final String lexeme;
	
	public Token(TokenType tokenType, String lexeme) {
		this.tokenType = tokenType;
		this.lexeme = lexeme;
	}
}
