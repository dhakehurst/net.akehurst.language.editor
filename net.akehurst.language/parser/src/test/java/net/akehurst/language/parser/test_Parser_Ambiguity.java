/**
 * Copyright (C) 2015 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.language.parser;

import org.junit.Assert;
import org.junit.Test;

import net.akehurst.language.core.parser.ParseFailedException;
import net.akehurst.language.core.sppt.SPPTBranch;
import net.akehurst.language.core.sppt.SharedPackedParseTree;
import net.akehurst.language.grammar.parser.forrest.ParseTreeBuilder;
import net.akehurst.language.ogl.semanticStructure.GrammarStructure;
import net.akehurst.language.ogl.semanticStructure.GrammarBuilder;
import net.akehurst.language.ogl.semanticStructure.Namespace;
import net.akehurst.language.ogl.semanticStructure.NonTerminal;
import net.akehurst.language.ogl.semanticStructure.TerminalLiteral;
import net.akehurst.language.ogl.semanticStructure.TerminalPattern;
import net.akehurst.language.parser.sppf.SharedPackedParseTreeSimple;

public class test_Parser_Ambiguity extends AbstractParser_Test {

	GrammarStructure am() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").multi(0, -1, new TerminalLiteral("a"));
		return b.get();
	}

	GrammarStructure aq() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").multi(0, 1, new TerminalLiteral("a"));
		return b.get();
	}

	GrammarStructure aab() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").choice(new TerminalLiteral("a"), new NonTerminal("ab"));
		b.rule("ab").concatenation(new TerminalLiteral("a"), new TerminalLiteral("b"));
		return b.get();
	}

	GrammarStructure ae() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").choice(new TerminalLiteral("a"), new NonTerminal("nothing"));
		b.rule("nothing").choice();
		return b.get();
	}

	GrammarStructure amq() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").multi(0, 1, new NonTerminal("am"));
		b.rule("am").multi(0, -1, new TerminalLiteral("a"));
		return b.get();
	}

	GrammarStructure x() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").multi(0, 1, new NonTerminal("aaa"));
		b.rule("aaa").choice(new NonTerminal("a1"), new NonTerminal("a2"), new NonTerminal("a3"));
		b.rule("a1").multi(0, 1, new TerminalLiteral("a"));
		b.rule("a2").multi(0, 2, new TerminalLiteral("a"));
		b.rule("a3").multi(0, 3, new TerminalLiteral("a"));
		return b.get();
	}

	GrammarStructure tg() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.skip("WS").concatenation(new TerminalPattern("\\s+"));
		b.rule("fps").choice(new NonTerminal("fps.choice1"), new NonTerminal("fps.choice2"));
		b.rule("fps.choice1").concatenation(new NonTerminal("fp"), new NonTerminal("fps.choice1.group.multi"));
		b.rule("fps.choice1.group.multi").multi(0, -1, new NonTerminal("fps.choice1.group"));
		b.rule("fps.choice1.group").concatenation(new TerminalLiteral(","), new NonTerminal("fp"));
		b.rule("fps.choice2").concatenation(new NonTerminal("rp"), new NonTerminal("fps.choice1.group.multi"));
		b.rule("fp").concatenation(new NonTerminal("t"), new NonTerminal("name"));
		b.rule("rp").concatenation(new NonTerminal("name"), new NonTerminal("rp.multi"), new TerminalLiteral("this"));
		b.rule("rp.multi").multi(0, 1, new NonTerminal("rp.multi.group"));
		b.rule("rp.multi.group").concatenation(new NonTerminal("name"), new TerminalLiteral("."));
		b.rule("t").choice(new NonTerminal("bt"), new NonTerminal("gt"));
		b.rule("bt").concatenation(new NonTerminal("name"));
		b.rule("gt").concatenation(new NonTerminal("name"), new TerminalLiteral("("), new NonTerminal("name"), new TerminalLiteral(")"));
		b.rule("name").choice(new TerminalPattern("[a-zA-Z]+"));
		return b.get();
	}

	@Test
	public void am_S_empty() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.am();
		final String goal = "S";
		final String text = "";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SharedPackedParseTree expected = new SharedPackedParseTreeSimple(b.branch("S", b.emptyLeaf("S")));
		Assert.assertEquals(expected, tree);

	}

	@Test
	public void am_S_a() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.am();
		final String goal = "S";
		final String text = "a";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		// b.define("S { 'a' }");
		final SharedPackedParseTree expected = new SharedPackedParseTreeSimple(b.branch("S", b.leaf("a")));
		// final IBranch expected = b.branch("S", b.leaf("a"));
		Assert.assertEquals(expected, tree);

	}

	@Test
	public void am_S_aa() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.am();
		final String goal = "S";
		final String text = "aa";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("S", b.leaf("a"), b.leaf("a"));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void x_S_a() throws ParseFailedException {
		// grammar, goal, input
		final GrammarStructure g = this.x();
		final String goal = "S";
		final String text = "a";

		final SharedPackedParseTree actual = this.process(g, text, goal);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("S{ aaa { a1{'a'} } }");
		b.buildAndAdd();

		b.define("S{ aaa { a2{'a'} } }");
		b.buildAndAdd();

		b.define("S{ aaa { a3{'a'} } }");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void x_S_aa() throws ParseFailedException {
		// grammar, goal, input
		final GrammarStructure g = this.x();
		final String goal = "S";
		final String text = "aa";

		final SharedPackedParseTree actual = this.process(g, text, goal);
		Assert.assertNotNull(actual);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("S{ aaa { a2{'a' 'a'} } }");
		b.buildAndAdd();

		b.define("S{ aaa { a3{'a' 'a'} } }");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void x_S_aaa() throws ParseFailedException {
		// grammar, goal, input
		final GrammarStructure g = this.x();
		final String goal = "S";
		final String text = "aaa";

		final SharedPackedParseTree actual = this.process(g, text, goal);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("S{ aaa { a3{'a' 'a' 'a'} } }");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	@Test
	public void tg_fp_V() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.tg();
		final String goal = "fp";
		final String text = "V v";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("fp",
				b.branch("t", b.branch("bt", b.branch("name", b.leaf("[a-zA-Z]+", "V"), b.branch("WS", b.leaf("\\s+", " "))))),
				b.branch("name", b.leaf("[a-zA-Z]+", "v")));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void tg_fp_VE() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.tg();
		final String goal = "fp";
		final String text = "V(E) v";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("fp", b.branch("t", b.branch("gt", b.branch("name", b.leaf("[a-zA-Z]+", "V")), b.leaf("("),
				b.branch("name", b.leaf("[a-zA-Z]+", "E")), b.leaf(")"), b.branch("WS", b.leaf("\\s+", " ")))), b.branch("name", b.leaf("[a-zA-Z]+", "v")));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void tg_fps_choice1_VE() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.tg();
		final String goal = "fps.choice1";
		final String text = "V(E) v";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("fps.choice1",
				b.branch("fp",
						b.branch("t",
								b.branch("gt", b.branch("name", b.leaf("[a-zA-Z]+", "V")), b.leaf("("), b.branch("name", b.leaf("[a-zA-Z]+", "E")), b.leaf(")"),
										b.branch("WS", b.leaf("\\s+", " ")))),
						b.branch("name", b.leaf("[a-zA-Z]+", "v"))),
				b.branch("fps.choice1.group.multi", b.emptyLeaf("fps.choice1.group.multi")));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void tg_fps_VE() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.tg();
		final String goal = "fps";
		final String text = "V(E) v";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SharedPackedParseTree expected = new SharedPackedParseTreeSimple(b.branch("fps",
				b.branch("fps.choice1",
						b.branch("fp",
								b.branch("t",
										b.branch("gt", b.branch("name", b.leaf("[a-zA-Z]+", "V")), b.leaf("("), b.branch("name", b.leaf("[a-zA-Z]+", "E")),
												b.leaf(")"), b.branch("WS", b.leaf("\\s+", " ")))),
								b.branch("name", b.leaf("[a-zA-Z]+", "v"))),
						b.branch("fps.choice1.group.multi", b.emptyLeaf("fps.choice1.group.multi")))));
		Assert.assertEquals(expected, tree);

	}

	@Test
	public void tg_fps_V_this() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.tg();
		final String goal = "fps";
		final String text = "V A.this";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("fps",
				b.branch("fps.choice2",
						b.branch("rp", b.branch("name", b.leaf("[a-zA-Z]+", "V"), b.branch("WS", b.leaf("\\s+", " "))),
								b.branch("rp.multi", b.branch("rp.multi.group", b.branch("name", b.leaf("[a-zA-Z]+", "A")), b.leaf("."))), b.leaf("this")),
						b.branch("fps.choice1.group.multi", b.emptyLeaf("fps.choice1.group.multi"))));
		Assert.assertEquals(expected, tree.getRoot());

	}

	GrammarStructure caseBlock() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.skip("WS").concatenation(new TerminalPattern("\\s+"));
		b.rule("block").concatenation(new TerminalLiteral("{"), new NonTerminal("group1"), new NonTerminal("group2"), new TerminalLiteral("}"));
		b.rule("group1").multi(0, -1, new NonTerminal("labelBlock"));
		b.rule("group2").multi(0, -1, new NonTerminal("label"));
		b.rule("labelBlock").concatenation(new NonTerminal("labels"), new TerminalLiteral("{"), new TerminalLiteral("}"));
		b.rule("labels").multi(0, -1, new NonTerminal("label"));
		b.rule("label").concatenation(new TerminalLiteral("case"), new NonTerminal("int"), new TerminalLiteral(":"));
		b.rule("int").choice(new TerminalPattern("[0-9]+"));
		return b.get();
	}

	@Test
	public void ambiguity_int() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.caseBlock();
		final String goal = "int";
		final String text = "1";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("int", b.leaf("[0-9]+", "1"));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void ambiguity_label() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.caseBlock();
		final String goal = "label";
		final String text = "case 1 :";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SharedPackedParseTree expected = new SharedPackedParseTreeSimple(b.branch("label", b.leaf("case"), b.branch("WS", b.leaf("\\s+", " ")),
				b.branch("int", b.leaf("[0-9]+", "1"), b.branch("WS", b.leaf("\\s+", " "))), b.leaf(":")));
		Assert.assertEquals(expected, tree);

	}

	@Test
	public void ambiguity_labels() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.caseBlock();
		final String goal = "labels";
		final String text = "case 1 :";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SharedPackedParseTree expected = new SharedPackedParseTreeSimple(b.branch("labels", b.branch("label", b.leaf("case"),
				b.branch("WS", b.leaf("\\s+", " ")), b.branch("int", b.leaf("[0-9]+", "1"), b.branch("WS", b.leaf("\\s+", " "))), b.leaf(":"))));
		Assert.assertEquals(expected, tree);

	}

	@Test
	public void ambiguity_labelBlock() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.caseBlock();
		final String goal = "labelBlock";
		final String text = "case 1 : { }";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("labelBlock",
				b.branch("labels",
						b.branch("label", b.leaf("case"), b.branch("WS", b.leaf("\\s+", " ")),
								b.branch("int", b.leaf("[0-9]+", "1"), b.branch("WS", b.leaf("\\s+", " "))), b.leaf(":"), b.branch("WS", b.leaf("\\s+", " ")))),
				b.leaf("{"), b.branch("WS", b.leaf("\\s+", " ")), b.leaf("}"));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void ambiguity_block() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.caseBlock();
		final String goal = "block";
		final String text = "{ case 1 : { } }";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("block", b.leaf("{"), b.branch("WS", b.leaf("\\s+", " ")),
				b.branch("group1",
						b.branch("labelBlock", b.branch("labels", b.branch("label", b.leaf("case"), b.branch("WS", b.leaf("\\s+", " ")),
								b.branch("int", b.leaf("[0-9]+", "1"), b.branch("WS", b.leaf("\\s+", " "))), b.leaf(":"), b.branch("WS", b.leaf("\\s+", " ")))),
								b.leaf("{"), b.branch("WS", b.leaf("\\s+", " ")), b.leaf("}"), b.branch("WS", b.leaf("\\s+", " ")))),
				b.branch("group2", b.emptyLeaf("group2")), b.leaf("}"));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void ambiguity_block2() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.caseBlock();
		final String goal = "block";
		final String text = "{ case 1 : }";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SharedPackedParseTree expected = new SharedPackedParseTreeSimple(
				b.branch("block", b.leaf("{"), b.branch("WS", b.leaf("\\s+", " ")), b.branch("group1", b.emptyLeaf("group1")),
						b.branch("group2", b.branch("label", b.leaf("case"), b.branch("WS", b.leaf("\\s+", " ")),
								b.branch("int", b.leaf("[0-9]+", "1"), b.branch("WS", b.leaf("\\s+", " "))), b.leaf(":"), b.branch("WS", b.leaf("\\s+", " ")))),
						b.leaf("}")));
		Assert.assertEquals(expected, tree);

	}

	GrammarStructure varDeclBlock() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.skip("WS").concatenation(new TerminalPattern("\\s+"));
		b.rule("block").concatenation(new TerminalLiteral("{"), new NonTerminal("decls"), new TerminalLiteral("}"));
		b.rule("decls").multi(0, -1, new NonTerminal("decl"));
		b.rule("decl").concatenation(new NonTerminal("type"), new NonTerminal("name"), new TerminalLiteral(";"));
		b.rule("type").priorityChoice(new TerminalLiteral("int"), new NonTerminal("name"));
		b.rule("name").choice(new TerminalPattern("[a-zA-Z0-9]+"));
		return b.get();
	}

	@Test
	public void varDeclBlock_block_empty() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.varDeclBlock();
		final String goal = "block";
		final String text = "{}";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		final SPPTBranch expected = b.branch("block", b.leaf("{"), b.branch("decls", b.emptyLeaf("decls")), b.leaf("}"));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void varDeclBlock_block_1() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.varDeclBlock();
		final String goal = "block";
		final String text = "{ int i; }";

		final SharedPackedParseTree actual = this.process(g, text, goal);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("block {");
		b.define("  '{'");
		b.define("  WS { '\\s+' : ' ' }");
		b.define("  decls {");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+' : ' ' } }");
		b.define("      name { '[a-zA-Z0-9]+' : 'i' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("  }");
		b.define("  '}'");
		b.define("}");

		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	@Test
	public void varDeclBlock_block_2() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.varDeclBlock();
		final String goal = "block";
		final String text = "{int i1;int i2;}";

		final SharedPackedParseTree actual = this.process(g, text, goal);
		Assert.assertNotNull(actual);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("block {");
		b.define("  '{'");
		b.define("  decls {");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i1' }");
		b.define("      ';'");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("    }");
		b.define("  }");
		b.define("  '}'");
		b.define("}");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	@Test
	public void varDeclBlock_block_8() throws ParseFailedException {
		// grammar, goal, input

		final GrammarStructure g = this.varDeclBlock();
		final String goal = "block";
		final String text = "{ int i1; int i2; int i3; int i4; int i5; int i6; int i7; int i8; }";

		final SharedPackedParseTree actual = this.process(g, text, goal);
		Assert.assertNotNull(actual);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("block {");
		b.define("  '{'");
		b.define("  WS { '\\s+':' '}");
		b.define("  decls {");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i1' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i1' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("    decl {");
		b.define("      type { 'int' WS { '\\s+':' '} }");
		b.define("      name { '[a-zA-Z0-9]+':'i2' }");
		b.define("      ';'");
		b.define("      WS { '\\s+' : ' ' }");
		b.define("    }");
		b.define("  }");
		b.define("  '}'");
		b.define("}");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	// abstraction of weird postfix rules from Java8 grammar
	GrammarStructure notGreedy() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("S").concatenation(new NonTerminal("postfix"), new TerminalLiteral("++"));
		b.rule("postfix").concatenation(new NonTerminal("expr"), new NonTerminal("multiPPs"));
		b.rule("multiPPs").multi(0, -1, new TerminalLiteral("++"));
		b.rule("expr").choice(new TerminalLiteral("a"));
		return b.get();
	}

	@Test
	public void notGreedy_S_app() throws ParseFailedException {
		final GrammarStructure g = this.notGreedy();
		final String goal = "S";
		final String text = "a++";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	// S = pd? td? ;
	// pd = pm? 'p' ;
	// pm = an ;
	// td = cd ;
	// cd = cm? 'c' ;
	// cm = an ;
	// an = 'a' ;
	GrammarStructure xxx() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.skip("WS").choice(new TerminalPattern("\\s+"));
		b.rule("S").concatenation(new NonTerminal("packageDeclaration_m"), new NonTerminal("importDeclaration_m"), new NonTerminal("typeDeclaration_m"));
		b.rule("packageDeclaration_m").multi(0, 1, new NonTerminal("packageDeclaration"));
		b.rule("packageDeclaration").concatenation(new NonTerminal("packageModifier_m"), new TerminalLiteral("package"));
		b.rule("importDeclaration_m").multi(0, -1, new NonTerminal("importDeclaration"));
		b.rule("importDeclaration").concatenation(new TerminalLiteral("import"), new TerminalLiteral(";"));
		b.rule("packageModifier_m").multi(0, -1, new NonTerminal("packageModifier"));
		b.rule("packageModifier").choice(new NonTerminal("annotation"));
		b.rule("typeDeclaration_m").multi(0, -1, new NonTerminal("typeDeclaration"));
		b.rule("typeDeclaration").choice(new NonTerminal("classDeclaration"), new NonTerminal("interfaceDeclaration"));
		b.rule("classDeclaration").concatenation(new NonTerminal("classModifier_m"), new TerminalLiteral("class"));
		b.rule("classModifier_m").multi(0, -1, new NonTerminal("classModifier"));
		b.rule("classModifier").choice(new NonTerminal("annotation"));
		b.rule("interfaceDeclaration").concatenation(new NonTerminal("interfaceModifier_m"), new TerminalLiteral("interface"));
		b.rule("interfaceModifier_m").multi(0, -1, new NonTerminal("interfaceModifier"));
		b.rule("interfaceModifier").choice(new NonTerminal("annotation"));
		b.rule("annotation").choice(new NonTerminal("normalAnnotation"), new NonTerminal("markerAnnotation"), new NonTerminal("singleElementAnnotation"));
		b.rule("normalAnnotation").concatenation(new TerminalLiteral("@"), new NonTerminal("Identifier"), new TerminalLiteral("("),
				new NonTerminal("elementValuePairList_m"), new TerminalLiteral(")"));
		b.rule("markerAnnotation").concatenation(new TerminalLiteral("@"), new NonTerminal("Identifier"));
		b.rule("singleElementAnnotation").concatenation(new TerminalLiteral("@"), new NonTerminal("Identifier"), new TerminalLiteral("("),
				new TerminalLiteral("value"), new TerminalLiteral(")"));
		b.rule("elementValuePairList_m").multi(0, 1, new NonTerminal("elementValuePairList"));
		b.rule("elementValuePairList").choice(new NonTerminal("elementValuePair"));
		b.rule("elementValuePair").concatenation(new TerminalLiteral("element"), new TerminalLiteral("="), new TerminalLiteral("value"));
		b.rule("Identifier").choice(new TerminalPattern("[a-zA-Z][a-zA-z0-9]*"));
		return b.get();
	}

	@Test
	public void xxx_S_apac() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An package @An class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_apac2() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An() package @An class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_apac3() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An() package @An() class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_p() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "package";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_ap() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An package";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_ap2() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An() package";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_apc() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An package class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_apc2() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An() package class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_ac() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_ac2() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An() class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_c() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "class";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_ai() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An interface";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	@Test
	public void xxx_S_ai2() throws ParseFailedException {
		final GrammarStructure g = this.xxx();
		final String goal = "S";
		final String text = "@An() interface";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}

	// S = pd? td? ;
	// pd = pm? 'p' ;
	// pm = an ;
	// td = cd ;
	// cd = cm? 'c' ;
	// cm = an ;
	// an = 'a' ;
	GrammarStructure xxx2() {
		final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.skip("WS").choice(new TerminalPattern("\\s+"));
		b.rule("S").concatenation(new NonTerminal("packageDeclaration_m"), new NonTerminal("importDeclaration_m"), new NonTerminal("typeDeclaration_m"));
		b.rule("packageDeclaration_m").multi(0, 1, new NonTerminal("packageDeclaration"));
		b.rule("packageDeclaration").concatenation(new NonTerminal("packageModifier_m"), new TerminalLiteral("package"));
		b.rule("importDeclaration_m").multi(0, -1, new NonTerminal("importDeclaration"));
		b.rule("importDeclaration").concatenation(new TerminalLiteral("import"), new TerminalLiteral(";"));
		b.rule("packageModifier_m").multi(0, -1, new NonTerminal("packageModifier"));
		b.rule("packageModifier").choice(new NonTerminal("annotation"));
		b.rule("typeDeclaration_m").multi(0, -1, new NonTerminal("typeDeclaration"));
		b.rule("typeDeclaration").choice(new NonTerminal("classDeclaration"), new NonTerminal("interfaceDeclaration"));
		b.rule("classDeclaration").choice(new NonTerminal("normalClassDeclaration"), new NonTerminal("enumDeclaration"));
		b.rule("normalClassDeclaration").concatenation(new NonTerminal("classModifier_m"), new TerminalLiteral("class"), new NonTerminal("Identifier"),
				new NonTerminal("typeParameters_m"), new NonTerminal("superclass_m"), new NonTerminal("superinterfaces_m"), new NonTerminal("classBody"));
		b.rule("classModifier_m").multi(0, -1, new NonTerminal("classModifier"));
		b.rule("classModifier").choice(new NonTerminal("annotation"));
		b.rule("interfaceDeclaration").concatenation(new NonTerminal("interfaceModifier_m"), new TerminalLiteral("interface"), new NonTerminal("Identifier"));
		b.rule("interfaceModifier_m").multi(0, -1, new NonTerminal("interfaceModifier"));
		b.rule("interfaceModifier").choice(new NonTerminal("annotation"));
		b.rule("annotation").choice(new NonTerminal("normalAnnotation"), new NonTerminal("markerAnnotation"), new NonTerminal("singleElementAnnotation"));
		b.rule("normalAnnotation").concatenation(new TerminalLiteral("@"), new NonTerminal("Identifier"), new TerminalLiteral("("),
				new NonTerminal("elementValuePairList_m"), new TerminalLiteral(")"));
		b.rule("markerAnnotation").concatenation(new TerminalLiteral("@"), new NonTerminal("Identifier"));
		b.rule("singleElementAnnotation").concatenation(new TerminalLiteral("@"), new NonTerminal("Identifier"), new TerminalLiteral("("),
				new TerminalLiteral("value"), new TerminalLiteral(")"));
		b.rule("elementValuePairList_m").multi(0, 1, new NonTerminal("elementValuePairList"));
		b.rule("elementValuePairList").choice(new NonTerminal("elementValuePair"));
		b.rule("elementValuePair").concatenation(new TerminalLiteral("element"), new TerminalLiteral("="), new TerminalLiteral("value"));
		b.rule("Identifier").choice(new TerminalPattern("[a-zA-Z][a-zA-z0-9]*"));
		b.rule("typeParameters_m").multi(0, -1, new NonTerminal("typeParameters"));
		b.rule("typeParameters").concatenation(new TerminalLiteral("<"), new TerminalLiteral(">"));
		b.rule("superclass_m").multi(0, -1, new NonTerminal("superclass"));
		b.rule("superclass").concatenation(new TerminalLiteral("extends"), new NonTerminal("Identifier"));
		b.rule("superinterfaces_m").multi(0, -1, new NonTerminal("superinterfaces"));
		b.rule("superinterfaces").concatenation(new TerminalLiteral("implements"), new NonTerminal("interfaceTypeList"));
		b.rule("interfaceTypeList").separatedList(0, -1, new TerminalLiteral(","), new NonTerminal("Identifier"));
		b.rule("classBody").concatenation(new TerminalLiteral("{"), new NonTerminal("classBodyDeclaration_m"), new TerminalLiteral("}"));
		b.rule("classBodyDeclaration_m").multi(0, -1, new NonTerminal("classBodyDeclaration"));
		b.rule("classBodyDeclaration").concatenation(new TerminalLiteral("body"));
		b.rule("enumDeclaration").concatenation(new NonTerminal("classModifier_m"), new TerminalLiteral("enum"), new NonTerminal("Identifier"),
				new NonTerminal("superinterfaces_m"), new NonTerminal("enumBody"));
		b.rule("enumBody").concatenation(new TerminalLiteral("{"), new TerminalLiteral("}"));

		return b.get();
	}

	@Test
	public void xxx2_S_ac() throws ParseFailedException {
		final GrammarStructure g = this.xxx2();
		final String goal = "S";
		final String text = "@An() class An { }";
		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);
	}
}
