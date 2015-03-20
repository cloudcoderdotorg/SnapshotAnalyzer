package org.cloudcoder.snapshotanalzyer;

public enum TokenType {
	// preprocessor directives
	DEFINE,
	INCLUDE,
	UNKNOWN_PREPROC, // unknown preprocessor directive
	
	// keywords
	IF,
	ELSE,
	FOR,
	WHILE,
	DO,
	STRUCT,
	
	// types
	VOID(TokenFlags.TYPE),
	SHORT(TokenFlags.TYPE),
	LONG(TokenFlags.TYPE),
	SIGNED(TokenFlags.TYPE),
	UNSIGNED(TokenFlags.TYPE),
	INT(TokenFlags.TYPE),
	CHAR(TokenFlags.TYPE),
	FLOAT(TokenFlags.TYPE),
	DOUBLE(TokenFlags.TYPE),
	BOOL(TokenFlags.TYPE),
	
	// operators
	EQ,
	NE,
	LT,
	GT,
	LTE,
	GTE,
	BITAND,
	AND,
	BITOR,
	OR,
	PLUS,
	MINUS,
	TIMES,
	DIVIDE,
	MOD,
	ASSIGN,
	ASSIGN_BITAND,
	ASSIGN_BITOR,
	ASSIGN_PLUS,
	ASSIGN_MINUS,
	ASSIGN_TIMES,
	ASSIGN_DIVIDE,
	ASSIGN_MOD,
	INCREMENT,
	DECREMENT,
	NOT,
	
	// punctuation
	SEMI,
	COLON,
	COMMA,
	DOT,
	
	// grouping
	LPAREN,
	RPAREN,
	LBRACKET,
	RBRACKET,
	LBRACE,
	RBRACE,
	
	// literals
	LITERAL_INT,
	LITERAL_FLOAT,
	LITERAL_DOUBLE,
	LITERAL_CHAR,
	LITERAL_STRING,
	LITERAL_TRUE,
	LITERAL_FALSE,
	
	// identifiers
	IDENT,
	;
	
	private final int flags;
	
	private TokenType() {
		this(0);
	}
	
	private TokenType(int flags) {
		this.flags = flags;
	}
	
	public boolean isType() {
		return (flags & TokenFlags.TYPE) != 0;
	}
}
