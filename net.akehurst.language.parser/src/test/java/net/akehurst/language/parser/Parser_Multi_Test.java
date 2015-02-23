package net.akehurst.language.parser;

import net.akehurst.language.core.parser.INodeType;
import net.akehurst.language.core.parser.IParseTree;
import net.akehurst.language.core.parser.IParser;
import net.akehurst.language.core.parser.ParseFailedException;
import net.akehurst.language.core.parser.ParseTreeException;
import net.akehurst.language.ogl.semanticModel.Grammar;
import net.akehurst.language.ogl.semanticModel.GrammarBuilder;
import net.akehurst.language.ogl.semanticModel.Namespace;
import net.akehurst.language.ogl.semanticModel.NonTerminal;
import net.akehurst.language.ogl.semanticModel.RuleNotFoundException;
import net.akehurst.language.ogl.semanticModel.TerminalLiteral;

import org.junit.Assert;
import org.junit.Test;

public class Parser_Multi_Test extends AbstractParser_Test {

	Grammar ab01() {
		GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("ab01").concatination(new NonTerminal("a"), new NonTerminal("b01"));
		b.rule("b01").multi(0, 1, new NonTerminal("b"));
		b.rule("a").concatination(new TerminalLiteral("a"));
		b.rule("b").concatination(new TerminalLiteral("b"));

		return b.get();
	}
	
	Grammar ab01_2() {
		GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("ab01$group1").concatination(new NonTerminal("a"), new NonTerminal("b"));
		b.rule("ab01").choice(new NonTerminal("ab01$group1"), new NonTerminal("a"));
		b.rule("a").concatination(new TerminalLiteral("a"));
		b.rule("b").concatination(new TerminalLiteral("b"));

		return b.get();
	}
	
	Grammar as13() {
		GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("as").multi(1, 3, new NonTerminal("a"));
		b.rule("a").concatination(new TerminalLiteral("a"));

		return b.get();
	}
	
	@Test
	public void as13_as_a() {
		// grammar, goal, input
		try {
			Grammar g = as13();
			String goal = "as";
			String text = "a";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*?as 0, 1}",st); //the tree is marked as if it can still grow because the top rule is multi(1-3)
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("as : [a : ['a' : \"a\"]]",nt);
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void as13_as_aa() {
		// grammar, goal, input
		try {
			Grammar g = as13();
			String goal = "as";
			String text = "aa";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*?as 0, 2}",st); //the tree is marked as if it can still grow because the top rule is multi(1-3)
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("as : [a : ['a' : \"a\"], a : ['a' : \"a\"]]",nt);
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void as13_as_aaa() {
		// grammar, goal, input
		try {
			Grammar g = as13();
			String goal = "as";
			String text = "aaa";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*as 0, 3}",st);
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("as : [a : ['a' : \"a\"], a : ['a' : \"a\"], a : ['a' : \"a\"]]",nt);
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void ab01_ab01_a() {
		// grammar, goal, input
		try {
			Grammar g = ab01();
			String goal = "ab01";
			String text = "a";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*ab01 0, 1}",st);
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("ab01 : [a : ['a' : \"a\"], b01 : [ : \"\"]]",nt);

			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void ab01_2_ab01_a() {
		// grammar, goal, input
		try {
			Grammar g = ab01_2();
			String goal = "ab01";
			String text = "a";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*ab01 0, 1}",st);
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("ab01 : [a : ['a' : \"a\"]]",nt);

			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void ab01_2_ab01_ab() {
		// grammar, goal, input
		try {
			Grammar g = ab01_2();
			String goal = "ab01";
			String text = "ab";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*ab01 0, 2}",st);
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("ab01 : [ab01$group1 : [a : ['a' : \"a\"], b : ['b' : \"b\"]]]",nt);

			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void ab01_ab01_ab() {
		// grammar, goal, input
		try {
			Grammar g = ab01();
			String goal = "ab01";
			String text = "ab";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);
			
			ToStringVisitor v = new ToStringVisitor("","");
			String st = tree.accept(v, "");
			Assert.assertEquals("Tree {*ab01 0, 2}",st);
			
			String nt = tree.getRoot().accept(v, "");
			Assert.assertEquals("ab01 : [a : ['a' : \"a\"], b01 : [b : ['b' : \"b\"]]]",nt);
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void ab01_ab01_aa() {
		// grammar, goal, input
		try {
			Grammar g = ab01();
			String goal = "ab01";
			String text = "aa";
			
			IParseTree tree = this.process(g, text, goal);
			
			Assert.fail("This parse should fail");
			
		} catch (ParseFailedException e) {
			// this should occur
		}
	}
}
