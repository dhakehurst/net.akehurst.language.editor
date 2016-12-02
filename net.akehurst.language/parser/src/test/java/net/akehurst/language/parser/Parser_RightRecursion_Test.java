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

import net.akehurst.language.core.parser.IBranch;
import net.akehurst.language.core.parser.IParseTree;
import net.akehurst.language.core.parser.ParseFailedException;
import net.akehurst.language.grammar.parser.forrest.ParseTreeBuilder;
import net.akehurst.language.ogl.semanticStructure.Grammar;
import net.akehurst.language.ogl.semanticStructure.GrammarBuilder;
import net.akehurst.language.ogl.semanticStructure.Namespace;
import net.akehurst.language.ogl.semanticStructure.NonTerminal;
import net.akehurst.language.ogl.semanticStructure.TerminalLiteral;

public class Parser_RightRecursion_Test extends AbstractParser_Test {
	
	Grammar as() {
		GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
		b.rule("as").choice(new NonTerminal("as$group1"), new NonTerminal("a"));
		b.rule("as$group1").concatenation(new NonTerminal("a"), new NonTerminal("as"));
		b.rule("a").concatenation(new TerminalLiteral("a"));
		return b.get();
	}
	
	@Test
	public void as_as_a() {
		// grammar, goal, input
		try {
			Grammar g = as();
			String goal = "as";
			String text = "a";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);

			ParseTreeBuilder b = this.builder(g, text, goal);;
			IBranch expected = 
				b.branch("as",
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
	public void as_as_aa() {
		// grammar, goal, input
		try {
			Grammar g = as();
			String goal = "as";
			String text = "aa";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);

			ParseTreeBuilder b = this.builder(g, text, goal);;
			IBranch expected = 
				b.branch("as",
					b.branch("as$group1",
						b.branch("a",
							b.leaf("a", "a")
						),
						b.branch("as",
							b.branch("a",
								b.leaf("a", "a")
							)				
						)
					)
				);
			Assert.assertEquals(expected, tree.getRoot());

			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void as_as_aaa() {
		// grammar, goal, input
		try {
			Grammar g = as();
			String goal = "as";
			String text = "aaa";
			
			IParseTree tree = this.process(g, text, goal);
			Assert.assertNotNull(tree);

			ParseTreeBuilder b = this.builder(g, text, goal);;
			IBranch expected = 
				b.branch("as",
					b.branch("as$group1",
						b.branch("a",
							b.leaf("a", "a")
						),
						b.branch("as",
							b.branch("as$group1",
								b.branch("a",
									b.leaf("a", "a")
								),
								b.branch("as",
									b.branch("a",
										b.leaf("a", "a")
									)				
								)
							)
						)
					)
				);
			Assert.assertEquals(expected, tree.getRoot());
			
		} catch (ParseFailedException e) {
			Assert.fail(e.getMessage());
		}
	}
}
