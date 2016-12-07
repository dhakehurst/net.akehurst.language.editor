package net.akehurst.language.parse.graph;

import java.util.Collections;
import java.util.List;

import net.akehurst.language.core.parser.ILeaf;
import net.akehurst.language.core.parser.INode;
import net.akehurst.language.core.parser.IParseTreeVisitor;
import net.akehurst.language.grammar.parse.tree.Leaf;
import net.akehurst.language.grammar.parser.runtime.RuntimeRule;

public class GraphNodeLeaf extends AbstractGraphNode implements IGraphNode, ILeaf {

	// public GraphNodeLeaf(ParseGraph graph, RuntimeRule runtimeRule, int startPosition, int machedTextLength) {
	public GraphNodeLeaf(ParseGraph graph, Leaf leaf) {
		super(graph, leaf.getRuntimeRule(), leaf.getStartPosition(), leaf.getMatchedTextLength());
		this.leaf = leaf;
	}

	Leaf leaf;

	@Override
	public IGraphNode duplicateWithNextChild(IGraphNode nextChild) {
		throw new RuntimeException("Internal Error: Should never happen");
	}

	@Override
	public IGraphNode duplicateWithNextSkipChild(IGraphNode nextChild) {
		throw new RuntimeException("Internal Error: Should never happen");
	}

	@Override
	public IGraphNode duplicateWithOtherStack(int priority, List<PreviousInfo> previous) {
		throw new RuntimeException("Internal Error: Should never happen");
	}
	
	@Override
	public boolean getIsLeaf() {
		return true;
	}

	@Override
	public int getNextItemIndex() {
		return -1;
	}


	@Override
	public int getPriority() {
		return this.getRuntimeRule().getIsEmptyRule()? Integer.MAX_VALUE: 0;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public boolean getCanGrow() {
		return this.getIsStacked();
	}

	@Override
	public boolean getIsSkip() {
		return this.getRuntimeRule().getIsSkipRule();
	}

	@Override
	public boolean getIsComplete() {
		return true;
	}

	@Override
	public boolean getCanGraftBack() {
		return this.getIsComplete() && this.getIsStacked();
	}

	@Override
	public boolean getCanGrowWidth() {
		return false;
	}

	@Override
	public boolean getCanGrowWidthWithSkip() {
		return false;
	}
	
	@Override
	public boolean getIsStacked() {
		return !this.getPrevious().isEmpty();
	}

	@Override
	public boolean hasNextExpectedItem() {
		return false;
	}

	@Override
	public List<RuntimeRule> getNextExpectedTerminals() {
		return Collections.emptyList();
	}

	@Override
	public List<RuntimeRule> getNextExpectedItem() {
		throw new RuntimeException("Internal Error: Should never happen");
	}

	@Override
	public RuntimeRule getExpectedItemAt(int atPosition) {
		throw new RuntimeException("Internal Error: Should never happen");
	}

	@Override
	public boolean getExpectsItemAt(RuntimeRule item, int atPosition) {
		return false;
	}
	
	@Override
	public List<INode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String getMatchedText() {
		return this.leaf.getMatchedText();//input.get(this.start, this.end).toString();
	}
	
	@Override
	public <T, A, E extends Throwable> T accept(IParseTreeVisitor<T, A, E> visitor, A arg) throws E {
		return visitor.visit(this, arg);
	}
	
	@Override
	public String toString() {
		return "'" + this.getRuntimeRule().getTerminalPatternText() + "'" + "(" + this.getRuntimeRule().getRuleNumber() + "," + this.getStartPosition() + ","
				+ this.getMatchedTextLength() + ")" + (this.getPrevious().isEmpty() ? "" : " -> " + this.getPrevious().get(0));
	}

}
