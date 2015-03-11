package net.akehurst.language.parser.forrest;

import net.akehurst.language.core.parser.IBranch;
import net.akehurst.language.core.parser.INode;
import net.akehurst.language.core.parser.ParseTreeException;
import net.akehurst.language.ogl.semanticModel.Choice;
import net.akehurst.language.ogl.semanticModel.Concatenation;
import net.akehurst.language.ogl.semanticModel.Multi;
import net.akehurst.language.ogl.semanticModel.Rule;
import net.akehurst.language.ogl.semanticModel.RuleItem;
import net.akehurst.language.ogl.semanticModel.SeparatedList;
import net.akehurst.language.ogl.semanticModel.SkipNodeType;
import net.akehurst.language.ogl.semanticModel.TangibleItem;
import net.akehurst.language.parser.ToStringVisitor;

public class ParseTreeBranch extends AbstractParseTree {
	
	public ParseTreeBranch(Factory factory, Input input, IBranch root, AbstractParseTree stack, Rule rule, int nextItemIndex) {
		super(factory, input, root, stack);
		this.rule = rule;
		this.nextItemIndex = nextItemIndex;
		this.canGrow = this.calculateCanGrow();
		this.complete = this.calculateIsComplete();
		this.canGrowWidth = this.calculateCanGrowWidth();
		this.hashCode_cache = this.getRoot().hashCode();
	}
	
	Rule rule;
	int nextItemIndex;
	boolean canGrow;
	boolean complete;
	boolean canGrowWidth;
	
	@Override
	public boolean getCanGrow() {
		return this.canGrow;
	}
	
	@Override
	public boolean getIsComplete() {
		return this.complete;
	}
	
	@Override
	public boolean getCanGraftBack() {
		return this.getIsComplete() ;
	}
	
	@Override
	public boolean getCanGrowWidth() {
		return this.canGrowWidth;
	}
	
	@Override
	public IBranch getRoot() {
		return (IBranch)super.getRoot();
	}
	
	public ParseTreeBranch extendWith(INode extension) throws ParseTreeException {
		IBranch nb = this.getRoot().addChild(extension);
//		Stack<AbstractParseTree> stack = new Stack<>();
//		stack.addAll(this.stackedRoots);
		if (extension.getNodeType() instanceof SkipNodeType) {
			ParseTreeBranch newBranch = new ParseTreeBranch(this.factory, this.input, nb, this.stackedTree, this.rule, this.nextItemIndex);
			return newBranch;			
		} else {
			ParseTreeBranch newBranch = new ParseTreeBranch(this.factory, this.input, nb, this.stackedTree, this.rule, this.nextItemIndex+1);
			return newBranch;
		}
	}
	
	@Override
	public TangibleItem getNextExpectedItem() {
		RuleItem item = this.rule.getRhs();
		if (item instanceof Concatenation) {
			Concatenation c = (Concatenation)item;
			if (this.nextItemIndex >= c.getItem().size()) {
				throw new RuntimeException("Should never happen, no NextExpectedItem");
			} else {
				return c.getItem().get(this.nextItemIndex);
			}
		} else if (item instanceof Multi) {
			Multi m = (Multi)item;
			return m.getItem();
		} else if (item instanceof Choice) {
			Choice m = (Choice)item;
			throw new RuntimeException("Should never happen, item is choice");
		} else if (item instanceof SeparatedList) {
			SeparatedList sl = (SeparatedList)item;
			if ( (this.nextItemIndex % 2) == 1 ) {
				return sl.getSeparator();
			} else {
				return sl.getConcatination();
			}
		} else {
			throw new RuntimeException("Should never happen");
		}
	}

	boolean calculateIsComplete() {
		RuleItem item = this.rule.getRhs();
		if (item instanceof Concatenation) {
			Concatenation c = (Concatenation)item;
			return c.getItem().size() <= this.nextItemIndex;
		} else if (item instanceof Choice) {
			return true;
		} else if (item instanceof Multi) {
			Multi m = (Multi)item;
			int size = this.nextItemIndex;
			return size >= m.getMin();
			//return m.getMin() <= size && (size <= m.getMax() || -1 == m.getMax());
		} else if (item instanceof SeparatedList) {
			SeparatedList sl = (SeparatedList)item;
			int size = this.nextItemIndex;
			return (size % 2) == 1;
		} else {
			throw new RuntimeException("Should never happen");
		}
	}
	boolean calculateCanGrow() {
		if (this.stackedTree!=null) return true;
		return this.calculateCanGrowWidth();
	}
	
	boolean calculateCanGrowWidth() {
		RuleItem item = this.rule.getRhs();
		boolean reachedEnd = this.getRoot().getMatchedTextLength() >= this.input.getLength();
		if (reachedEnd)
			return false;
		if (item instanceof Concatenation) {
			Concatenation c = (Concatenation)item;
			if ( this.nextItemIndex < c.getItem().size() ) {
				return true;
			} else {
				return false; //!reachedEnd;
			}
		} else if (item instanceof Choice) {
			return false;
		} else if (item instanceof Multi) {
			Multi m = (Multi)item;
			int size = this.nextItemIndex;
			return -1==m.getMax() || size < m.getMax();
		} else if (item instanceof SeparatedList) {
			SeparatedList sl = (SeparatedList)item;
			int size = this.nextItemIndex;
			return !reachedEnd;
		} else {
			throw new RuntimeException("Should never happen");
		}
	}
	
//	public ParseTreeBranch deepClone() {
//		Stack<AbstractParseTree> stack = new Stack<>();
//		stack.addAll(this.stackedRoots);
//		ParseTreeBranch clone = new ParseTreeBranch(this.input, this.getRoot(), stack, this.rule, this.nextItemIndex);
//		return clone;
//	}
	
	//--- Object ---
	static ToStringVisitor v = new ToStringVisitor();
	String toString_cache;
	@Override
	public String toString() {
		if (null==this.toString_cache) {
			this.toString_cache = this.accept(v, "");
		}
		return this.toString_cache;
	}
	
	int hashCode_cache;
	@Override
	public int hashCode() {
		return hashCode_cache;
	}
	
	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ParseTreeBranch)) {
			return false;
		}
		ParseTreeBranch other = (ParseTreeBranch)arg;
		if ( this.getRoot().getStart() != other.getRoot().getStart() ) {
			return false;
		}
		if ( this.getRoot().getEnd() != other.getRoot().getEnd() ) {
			return false;
		}
		if ( ((Branch)this.getRoot()).getNodeTypeNumber() != ((Branch)other.getRoot()).getNodeTypeNumber() ) {
			return false;
		}
		if (this.complete != other.complete) {
			return false;
		}
		if (null==this.stackedTree && null==other.stackedTree) {
			return true;
		}
		if (!this.stackedTree.equals(other.stackedTree)) {
			return false;
		}
		return true;

	}
}
