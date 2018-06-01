package net.akehurst.language.parser.sppf;

import net.akehurst.language.api.sppt.SPPTNode;
import net.akehurst.language.grammar.parser.runtime.RuntimeRule;

public class Factory {

	public Branch createBranch(final RuntimeRule r, final SPPTNode[] children) {
		final Branch b = new Branch(r, children);
		return b;
	}

	public Leaf createLeaf(final String text, final int start, final int nextInputPosition, final RuntimeRule terminalRule) {
		final Leaf l = new Leaf(text, start, nextInputPosition, terminalRule);
		return l;
	}

	public Leaf createEmptyLeaf(final int pos, final RuntimeRule terminalRule) {
		return new EmptyLeaf(pos, terminalRule);
	}

}
