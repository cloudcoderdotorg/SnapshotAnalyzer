package org.cloudcoder.snapshotanalzyer;

public class LexerException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final Position pos;

	public LexerException(String msg, Position pos) {
		super(msg);
		this.pos = pos;
	}
	
	public LexerException(String msg, Position pos, Throwable cause) {
		super(msg, cause);
		this.pos = pos;
	}
	
	public Position getPosition() {
		return pos;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + " at " + pos;
	}
}
