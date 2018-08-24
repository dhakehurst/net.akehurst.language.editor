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

import net.akehurst.language.api.parser.ParseFailedException;
import net.akehurst.language.api.sppt.SPPTBranch;
import net.akehurst.language.api.sppt.SharedPackedParseTree;
import net.akehurst.language.parser.sppf.ParseTreeBuilder;
import net.akehurst.language.ogl.semanticStructure.GrammarDefault;
import net.akehurst.language.ogl.semanticStructure.GrammarBuilderDefault;
import net.akehurst.language.ogl.semanticStructure.NamespaceDefault;
import net.akehurst.language.ogl.semanticStructure.NonTerminalDefault;
import net.akehurst.language.ogl.semanticStructure.TerminalLiteralDefault;
import net.akehurst.language.ogl.semanticStructure.TerminalPatternDefault;

public class Parser_Patterns_Test extends AbstractParser_Test {

	GrammarDefault as() {
		final GrammarBuilderDefault b = new GrammarBuilderDefault(new NamespaceDefault("test"), "Test");
		b.rule("as").concatenation(new NonTerminalDefault("a"));
		b.rule("a").concatenation(new TerminalPatternDefault("[a]+"));
		return b.get();
	}

	GrammarDefault asxas() {
		final GrammarBuilderDefault b = new GrammarBuilderDefault(new NamespaceDefault("test"), "Test");
		b.rule("as").concatenation(new NonTerminalDefault("a"), new TerminalLiteralDefault(":"), new NonTerminalDefault("a"));
		b.rule("a").concatenation(new TerminalPatternDefault("[a]+"));
		return b.get();
	}

	@Test
	public void as_as_a() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.as();
		final String goal = "as";
		final String text = "a";

		final SharedPackedParseTree actual = this.process(g, text, goal);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("as {");
		b.define("  a {");
		b.define("    '[a]+' : 'a' ");
		b.define("  }");
		b.define("}");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	@Test
	public void as_as_aa() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.as();
		final String goal = "as";
		final String text = "aa";

		final SharedPackedParseTree actual = this.process(g, text, goal);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("as {");
		b.define("  a {");
		b.define("    '[a]+' : 'aa' ");
		b.define("  }");
		b.define("}");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	@Test
	public void as_as_aaa() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.as();
		final String goal = "as";
		final String text = "aaa";

		final SharedPackedParseTree actual = this.process(g, text, goal);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		b.define("as {");
		b.define("  a {");
		b.define("    '[a]+' : 'aaa' ");
		b.define("  }");
		b.define("}");
		final SharedPackedParseTree expected = b.buildAndAdd();

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);

	}

	@Test(expected = ParseFailedException.class)
	public void asxas_as_a() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.asxas();
		final String goal = "as";
		final String text = "a";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.fail("This parse should fail");

	}

	@Test(expected = ParseFailedException.class)
	public void asxas_as_aa() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.asxas();
		final String goal = "as";
		final String text = "aa";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.fail("This parse should fail");

	}

	@Test
	public void asxas_as_axa() {
		// grammar, goal, input
		try {
			final GrammarDefault g = this.asxas();
			final String goal = "as";
			final String text = "a:a";

			final SharedPackedParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);

			final ParseTreeBuilder b = this.builder(g, text, goal);
			;
			final SPPTBranch expected = b.branch("as", b.branch("a", b.leaf("[a]+", "a")), b.leaf(":", ":"), b.branch("a", b.leaf("[a]+", "a")));
			Assert.assertEquals(expected, tree.getRoot());

		} catch (final ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void asxas_as_aaxa() {
		// grammar, goal, input
		try {
			final GrammarDefault g = this.asxas();
			final String goal = "as";
			final String text = "aa:a";

			final SharedPackedParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);

			final ParseTreeBuilder b = this.builder(g, text, goal);
			;
			final SPPTBranch expected = b.branch("as", b.branch("a", b.leaf("[a]+", "aa")), b.leaf(":", ":"), b.branch("a", b.leaf("[a]+", "a")));
			Assert.assertEquals(expected, tree.getRoot());

		} catch (final ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void asxas_as_axaa() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.asxas();
		final String goal = "as";
		final String text = "a:aa";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		;
		final SPPTBranch expected = b.branch("as", b.branch("a", b.leaf("[a]+", "a")), b.leaf(":", ":"), b.branch("a", b.leaf("[a]+", "aa")));
		Assert.assertEquals(expected, tree.getRoot());

	}

	@Test
	public void asxas_as_aaaxaaa() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.asxas();
		final String goal = "as";
		final String text = "aaa:aaa";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		;
		final SPPTBranch expected = b.branch("as", b.branch("a", b.leaf("[a]+", "aaa")), b.leaf(":", ":"), b.branch("a", b.leaf("[a]+", "aaa")));
		Assert.assertEquals(expected, tree.getRoot());

	}

	GrammarDefault chars() {
		final GrammarBuilderDefault b = new GrammarBuilderDefault(new NamespaceDefault("test"), "Test");
		b.rule("S").concatenation(new TerminalLiteralDefault("'"), new TerminalPatternDefault("[^'\\\\]"), new TerminalLiteralDefault("'"));
		return b.get();
	}

	@Test
	public void chars_S_x() throws ParseFailedException {
		// grammar, goal, input

		final GrammarDefault g = this.chars();
		final String goal = "S";
		final String text = "'x'";

		final SharedPackedParseTree tree = this.process(g, text, goal);
		Assert.assertNotNull(tree);

		final ParseTreeBuilder b = this.builder(g, text, goal);
		// Assert.assertEquals(expected, tree.getRoot());

	}
}
