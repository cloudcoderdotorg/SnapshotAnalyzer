package org.cloudcoder.snapshotanalzyer;

public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ParserException(TokenSequence seq, String msg) {
		super("At " + seq.getWhere() + ": " + msg);
	}

}
