package org.cloudcoder.snapshotanalzyer;

public enum NodeType {
	UNIT, // root node of parse tree 
	
	INCLUDE, // #include directive
	DEFINE,  // #define directive

	DECLARATION,
	TYPE,
	DECLARATOR_LIST,
	DECLARATOR,
	INITIALIZER,
	PARAMETER_LIST,
	BLOCK_STATEMENT,
	PARAMETER,
	STATEMENT,
	IF_STATEMENT,
	WHILE_STATEMENT,
	FOR_STATEMENT,
	DO_WHILE_STATEMENT,
	
	EXPRESSION,
}
