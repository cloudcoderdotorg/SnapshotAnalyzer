package org.cloudcoder.snapshotanalzyer;

public enum TokenType {
	// preprocessor directives
	DEFINE,
	INCLUDE,
	
	// keywords
	IF,
	ELSE,
	FOR,
	WHILE,
	DO,
	
	// types
	VOID,
	INT,
	SHORT,
	CHAR,
	FLOAT,
	DOUBLE,
	SIGNED,
	UNSIGNED,
	
	// operators
	EQ,
	NE,
	LT,
	GT,
	LTE,
	GTE,
	PLUS,
	MINUS,
	TIMES,
	DIVIDE,
	MOD,
	ASSIGN,
	ASSIGN_PLUS,
	ASSIGN_MINUS,
	ASSIGN_TIMES,
	ASSIGN_DIVIDE,
	ASSIGN_MOD,
	INCREMENT,
	DECREMENT,
	
	// punctuation
	SEMI,
	COLON,
	COMMA,
	
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
	
	// identifiers
	IDENT,
}
