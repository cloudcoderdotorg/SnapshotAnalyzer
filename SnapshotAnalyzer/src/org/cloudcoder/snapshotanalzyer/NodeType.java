package org.cloudcoder.snapshotanalzyer;

public enum NodeType {
	UNIT, // root node of parse tree 
	
	INCLUDE, // #include directive
	DEFINE,  // #define directive

	DECLARATION,
	TYPE,
	DECLARATOR_LIST,
	DECLARATOR,
	PARAMETER_LIST,
	BLOCK_STATEMENT,
	PARAMETER,
	STATEMENT,
}
