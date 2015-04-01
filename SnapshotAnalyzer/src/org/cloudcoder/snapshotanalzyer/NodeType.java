package org.cloudcoder.snapshotanalzyer;

public enum NodeType {
	ROOT, // root node of parse tree 
	
	INCLUDE, // #include directive
	DEFINE,  // #define directive

	DECLARATION,
	TYPE,
}
