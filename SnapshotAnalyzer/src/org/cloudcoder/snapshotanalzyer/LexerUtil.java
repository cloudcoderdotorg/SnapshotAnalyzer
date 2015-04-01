package org.cloudcoder.snapshotanalzyer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexerUtil {
	public static List<Token> readAll(ILexer lexer) throws IOException, LexerException {
		ArrayList<Token> result = new ArrayList<>();
		
		while (true) {
			Token t = lexer.readNext();
			if (t == null) {
				break;
			}
			result.add(t);
		}
		
		return result;
	}
}
