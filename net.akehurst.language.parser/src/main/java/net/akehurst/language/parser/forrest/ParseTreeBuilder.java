package net.akehurst.language.parser.forrest;

import java.util.Arrays;
import java.util.List;

import net.akehurst.language.core.parser.IBranch;
import net.akehurst.language.core.parser.ILeaf;
import net.akehurst.language.core.parser.INode;
import net.akehurst.language.core.parser.INodeType;
import net.akehurst.language.core.parser.IParseTree;
import net.akehurst.language.ogl.semanticModel.Grammar;
import net.akehurst.language.ogl.semanticModel.Rule;
import net.akehurst.language.ogl.semanticModel.RuleNotFoundException;
import net.akehurst.language.ogl.semanticModel.Terminal;
import net.akehurst.language.ogl.semanticModel.TerminalLiteral;
import net.akehurst.language.parser.runtime.Branch;
import net.akehurst.language.parser.runtime.Factory;
import net.akehurst.language.parser.runtime.Leaf;
import net.akehurst.language.parser.runtime.RuntimeRule;

public class ParseTreeBuilder {

	public ParseTreeBuilder(ForrestFactory ffactory, Grammar grammar, String goal, CharSequence text) {
		this.factory = ffactory.runtimeFactory;
		this.input = new Input(ffactory, " "+text);
		this.grammar = grammar;
		this.textAccumulator = "";
		this.textLength = 0;
	}

	Factory factory;
	Grammar grammar;
	Input input;
	String textAccumulator;
	int textLength;

	public ILeaf leaf(String terminalPattern, String text) {
		int start = this.textLength+1;
		this.textLength+=text.length();
		int end = this.textLength +1;
		Terminal terminal = this.grammar.getAllTerminal().stream().filter(t -> t.getPattern().pattern().equals(terminalPattern)).findFirst().get();
		RuntimeRule terminalRule = this.factory.getRuntimeRuleSet().getForTerminal(terminal);
		ILeaf l = this.factory.createLeaf(this.input, start, end, terminalRule);
		return l;
	}

	public ILeaf emptyLeaf() {
		int start = this.textLength +1;
		Terminal terminal = new TerminalLiteral("");
		RuntimeRule terminalRule = this.factory.getRuntimeRuleSet().getForTerminal(terminal);
		return new Leaf(this.input, start, start, terminalRule);
	}

	public IBranch branch(String ruleName, INode... children) {
		try {
			Rule rule = this.grammar.findRule(ruleName);
			RuntimeRule rr = this.factory.getRuntimeRuleSet().getRuntimeRule(rule);
			IBranch b = this.factory.createBranch(rr, children);
			return b;
		} catch (RuleNotFoundException e) {
			throw new RuntimeException("Error", e);
		}
	}

//	public IParseTree tree(IBranch root) {
//		try {
//			Rule rule = this.grammar.findRule(root.getName());
//			RuntimeRule rr = this.factory.getRuntimeRuleSet().getRuntimeRule(rule);
//			IParseTree t = new ParseTreeBranch(this.factory, input, (Branch)root, null, rr, Integer.MAX_VALUE);
//			return t;
//		} catch (RuleNotFoundException e) {
//			throw new RuntimeException("Error", e);
//		}
//	}

}
