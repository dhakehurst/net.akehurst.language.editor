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
import net.akehurst.language.core.sppt.SharedPackedParseTree;
import net.akehurst.language.grammar.parser.forrest.ParseTreeBuilder;
import net.akehurst.language.ogl.semanticStructure.GrammarStructure;
import net.akehurst.language.ogl.semanticStructure.GrammarBuilder;
import net.akehurst.language.ogl.semanticStructure.Namespace;
import net.akehurst.language.ogl.semanticStructure.NonTerminal;
import net.akehurst.language.ogl.semanticStructure.TerminalLiteral;
import net.akehurst.language.ogl.semanticStructure.TerminalPattern;

public class Parser_PriorityChoice_Test extends AbstractParser_Test {

    GrammarStructure abc() {
        final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
        b.rule("abc").priorityChoice(new NonTerminal("a"), new NonTerminal("b"), new NonTerminal("c"));
        b.rule("a").concatenation(new TerminalLiteral("a"));
        b.rule("b").concatenation(new TerminalLiteral("b"));
        b.rule("c").concatenation(new TerminalLiteral("c"));
        return b.get();
    }

    GrammarStructure aempty() {
        final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
        b.rule("a").priorityChoice();
        return b.get();
    }

    @Test
    public void aempty_a_empty() throws ParseFailedException {
        // grammar, goal, input

        final GrammarStructure g = this.aempty();
        final String goal = "a";
        final String text = "";

        final SharedPackedParseTree actual = this.process(g, text, goal);

        final ParseTreeBuilder b = this.builder(g, text, goal);
        b.define("a {");
        b.define("  $empty");
        b.define("}");
        final SharedPackedParseTree expected = b.buildAndAdd();

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void abc_abc_a() throws ParseFailedException {
        // grammar, goal, input

        final GrammarStructure g = this.abc();
        final String goal = "abc";
        final String text = "a";

        final SharedPackedParseTree actual = this.process(g, text, goal);

        final ParseTreeBuilder b = this.builder(g, text, goal);
        b.define("abc {");
        b.define("  a {");
        b.define("    'a'");
        b.define("  }");
        b.define("}");
        final SharedPackedParseTree expected = b.buildAndAdd();

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void abc_abc_b() throws ParseFailedException {
        // grammar, goal, input

        final GrammarStructure g = this.abc();
        final String goal = "abc";
        final String text = "b";

        final SharedPackedParseTree actual = this.process(g, text, goal);

        final ParseTreeBuilder b = this.builder(g, text, goal);
        b.define("abc {");
        b.define("  b {");
        b.define("    'b'");
        b.define("  }");
        b.define("}");
        final SharedPackedParseTree expected = b.buildAndAdd();

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void abc_abc_c() throws ParseFailedException {
        // grammar, goal, input

        final GrammarStructure g = this.abc();
        final String goal = "abc";
        final String text = "c";

        final SharedPackedParseTree actual = this.process(g, text, goal);

        final ParseTreeBuilder b = this.builder(g, text, goal);
        b.define("abc {");
        b.define("  c {");
        b.define("    'c'");
        b.define("  }");
        b.define("}");
        final SharedPackedParseTree expected = b.buildAndAdd();

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
    }

    GrammarStructure kwOrId1() {
        final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
        b.rule("S").choice(new NonTerminal("type"));
        b.rule("type").priorityChoice(new NonTerminal("id"), new NonTerminal("kw"));
        b.rule("kw").concatenation(new TerminalLiteral("int"));
        b.rule("id").concatenation(new TerminalPattern("[a-z]+"));
        return b.get();
    }

    @Test
    public void kwOrId_S_id() throws ParseFailedException {
        // grammar, goal, input

        final GrammarStructure g = this.kwOrId1();
        final String goal = "S";
        final String text = "int";

        final SharedPackedParseTree actual = this.process(g, text, goal);

        final ParseTreeBuilder b = this.builder(g, text, goal);
        b.define("S {");
        b.define("  type {");
        b.define("    id { '[a-z]+' : 'int' }");
        b.define("  }");
        b.define("}");
        final SharedPackedParseTree expected = b.buildAndAdd();

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);

    }

    GrammarStructure kwOrId2() {
        final GrammarBuilder b = new GrammarBuilder(new Namespace("test"), "Test");
        b.rule("S").choice(new NonTerminal("type"));
        b.rule("type").priorityChoice(new NonTerminal("kw"), new NonTerminal("id"));
        b.rule("kw").concatenation(new TerminalLiteral("int"));
        b.rule("id").concatenation(new TerminalPattern("[a-z]+"));
        return b.get();
    }

    @Test
    public void kwOrId_S_int() throws ParseFailedException {
        // grammar, goal, input

        final GrammarStructure g = this.kwOrId2();
        final String goal = "S";
        final String text = "int";

        final SharedPackedParseTree actual = this.process(g, text, goal);

        final ParseTreeBuilder b = this.builder(g, text, goal);
        b.define("S {");
        b.define("  type {");
        b.define("    kw { 'int' }");
        b.define("  }");
        b.define("}");
        final SharedPackedParseTree expected = b.buildAndAdd();

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);

    }
    // more tests needed!!!!
}
