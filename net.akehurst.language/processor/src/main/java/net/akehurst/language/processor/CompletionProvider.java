package net.akehurst.language.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.akehurst.language.core.grammar.IRuleItem;
import net.akehurst.language.core.grammar.ITerminal;
import net.akehurst.language.core.parser.CompletionItem;
import net.akehurst.language.ogl.semanticStructure.TerminalPattern;
import net.akehurst.language.ogl.semanticStructure.Visitable;

public class CompletionProvider {

	public List<CompletionItem> provideFor(final IRuleItem item, final int desiredDepth) {
		if (item instanceof ITerminal) {
			final ITerminal terminal = (ITerminal) item;
			if (terminal.isPattern()) {
				return Arrays.asList(new CompletionItemPattern(terminal.getOwningRule().getName(), ((TerminalPattern) terminal).getPattern()));
			} else {
				return Arrays.asList(new CompletionItemText(((ITerminal) item).getValue()));
			}
		} else {
			try {
				final SampleGeneratorVisitor v = new SampleGeneratorVisitor(desiredDepth);
				final Set<CompletionItem> options = ((Visitable) item).accept(v, 0);
				return new ArrayList<>(options);
			} catch (final Throwable t) {
				t.printStackTrace();
				return Collections.emptyList();
			}
		}
	}

}
