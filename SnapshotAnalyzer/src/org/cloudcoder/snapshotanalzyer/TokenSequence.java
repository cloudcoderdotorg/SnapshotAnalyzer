package org.cloudcoder.snapshotanalzyer;

import java.util.List;

public class TokenSequence {
	private List<Token> tokens;
	private int pos;
	
	public TokenSequence(List<Token> tokens) {
		this.tokens = tokens;
		this.pos = 0;
	}
	
	public int getPos() {
		return pos;
	}
	
	public void setPos(int pos) {
		this.pos = pos;
	}

	public boolean isFinished() {
		return pos >= tokens.size();
	}

	public Token peek() {
		if (isFinished()) {
			throw new IllegalArgumentException("No more tokens");
		}
		return tokens.get(pos);
	}
	
	public Token consume() {
		if (isFinished()) {
			throw new IllegalArgumentException("No more tokens");
		}
		Token result = tokens.get(pos);
		pos++;
		return result;
	}

	public boolean nextIs(TokenType type) {
		return !isFinished() && tokens.get(pos).getTokenType() == type;
	}

	public String getWhere() {
		Token where = isFinished() ? tokens.get(tokens.size() - 1) : tokens.get(pos);
		return (where.getPosition().getRow() + 1) + ":" + where.getTokenType() + ":";
	}

	public int findNext(TokenType type) {
		for (int i = pos; i < tokens.size(); i++) {
			if (tokens.get(i).getTokenType() == type) {
				return i;
			}
		}
		return -1;
	}

	public int findMatching(TokenType ldelim, TokenType rdelim) {
		if (isFinished() || peek().getTokenType() != ldelim) {
			throw new IllegalStateException("token sequence not positioned at " + ldelim);
		}
		int count = 1;
		int i = pos+1;
		while (true) {
			if (i >= tokens.size()) {
				// No matching right delimiter
				return -1;
			}
			if (tokens.get(i).getTokenType() == ldelim) {
				count++;
			} else if (tokens.get(i).getTokenType() == rdelim) {
				count--;
				if (count == 0) {
					return i;
				}
			}
		}
	}
}
