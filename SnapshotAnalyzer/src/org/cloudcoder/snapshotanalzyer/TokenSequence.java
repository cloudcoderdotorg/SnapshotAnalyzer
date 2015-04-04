package org.cloudcoder.snapshotanalzyer;

import java.util.List;
import java.util.function.Predicate;

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

	public boolean nextAre(TokenType... types) {
		if (pos + types.length > tokens.size()) {
			return false;
		}
		for (int i = 0; i < types.length; i++) {
			if (tokens.get(pos+i).getTokenType() != types[i]) {
				return false;
			}
		}
		return true;
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
			i++;
		}
	}

	public Token get(int index) {
		return tokens.get(index);
	}
	
	public int findAtSameNestingLevel(Predicate<Token> pred) {
		int nparen=0, nbracket=0, nbrace=0;
		for (int i = pos; i < tokens.size(); i++) {
			Token t = tokens.get(i);
			if (nparen <= 0 && nbracket <= 0 && nbrace <= 0 && pred.test(t)) {
				// Found!
				return i;
			} else {
				switch (t.getTokenType()) {
				case LPAREN: nparen++; break;
				case RPAREN: nparen--; break;
				case LBRACKET: nbracket++; break;
				case RBRACKET: nbracket--; break;
				case LBRACE: nbrace++; break;
				case RBRACE: nbrace--; break;
				default: break;
				}
			}
		}
		return -1;
	}
}
