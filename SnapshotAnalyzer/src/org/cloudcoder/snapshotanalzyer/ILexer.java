package org.cloudcoder.snapshotanalzyer;

import java.io.IOException;

public interface ILexer {
	public Token readNext() throws IOException, LexerException;
}
