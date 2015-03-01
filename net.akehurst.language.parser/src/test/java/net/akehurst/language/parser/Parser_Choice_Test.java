package net.akehurst.language.parser;

import net.akehurst.language.core.parser.IBranch;
import net.akehurst.language.core.parser.IParseTree;
import net.akehurst.language.core.parser.ParseFailedException;
import net.akehurst.language.ogl.semanticModel.Grammar;
import net.akehurst.language.ogl.semanticModel.GrammarBuilder;
import net.akehurst.language.ogl.semanticModel.Namespace;
import net.akehurst.language.ogl.semanticModel.NonTerminal;
import net.akehurst.language.ogl.semanticModel.TerminalLiteral;
import net.akehurst.language.parser.forrest.ParseTreeBuilder;

import org.junit.Assert;
import org.junit.Test;

public class Parser_Choice_Test extends AbstractParser_Test {

	Grammar abc() {
		GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("abc").choice(new NonTerminal("a"), new NonTerminal("b"), new NonTerminal("c"));
		b.rule("a").concatination(new TerminalLiteral("a"));
		b.rule("b").concatination(new TerminalLiteral("b"));
		b.rule("c").concatination(new TerminalLiteral("c"));
		return b.get();
	}
	
	@Test
	public void abc_abc_a() {
		// grammar, goal, input
		try {
			Grammar g = abc();
			String goal = "abc";
			String text = "a";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*abc 1, 2}",st);
			
			ParseTreeBuilder b = new ParseTreeBuilder(this.abc(), goal, text);
			IBranch expected = 
				b.branch("abc",
					b.branch("a",
						b.leaf("a", "a")
					)
				);
			Assert.assertEquals(expected, tree.getRoot());

		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void abc_abc_b() {
		// grammar, goal, input
		try {
			Grammar g = abc();
			String goal = "abc";
			String text = "b";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*abc 1, 2}",st);
			
			ParseTreeBuilder b = new ParseTreeBuilder(this.abc(), goal, text);
			IBranch expected = 
				b.branch("abc",
					b.branch("b",
						b.leaf("b", "b")
					)
				);
			
			Assert.assertEquals(expected, tree.getRoot());
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void abc_abc_c() {
		// grammar, goal, input
		try {
			Grammar g = abc();
			String goal = "abc";
			String text = "c";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*abc 1, 2}",st);
			
			ParseTreeBuilder b = new ParseTreeBuilder(this.abc(), goal, text);
			IBranch expected = 
				b.branch("abc",
					b.branch("c",
						b.leaf("c", "c")
					)
				);
			
			Assert.assertEquals(expected, tree.getRoot());
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}
}
