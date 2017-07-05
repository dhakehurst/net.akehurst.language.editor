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
package net.akehurst.language.ogl.semanticStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChoicePriority extends AbstractChoice {

	public ChoicePriority(final Concatenation... alternative) {
		this.alternative = Arrays.asList(alternative);
	}

	List<Integer> index;

	@Override
	public List<Integer> getIndex() {
		return this.index;
	}

	@Override
	public void setOwningRule(final Rule value, final List<Integer> index) {
		this.owningRule = value;
		this.index = index;
		int i = 0;
		for (final Concatenation c : this.getAlternative()) {
			final ArrayList<Integer> nextIndex = new ArrayList<>(index);
			nextIndex.add(i++);
			c.setOwningRule(value, nextIndex);
		}
	}

	// @Override
	// public INodeType getNodeType() {
	// return new RuleNodeType(this.getOwningRule());
	// }

	@Override
	public <T, E extends Throwable> T accept(final Visitor<T, E> visitor, final Object... arg) throws E {
		return visitor.visit(this, arg);
	}

	// public Set<TangibleItem> findFirstTangibleItem() {
	// Set<TangibleItem> result = new HashSet<>();
	// for(Concatination c : this.getAlternative()) {
	// Set<TangibleItem> ft = c.findFirstTangibleItem();
	// result.addAll(ft);
	// } return result;
	// }
	//
	@Override
	public Set<Terminal> findAllTerminal() {
		final Set<Terminal> result = new HashSet<>();
		for (final Concatenation c : this.getAlternative()) {
			final Set<Terminal> ft = c.findAllTerminal();
			result.addAll(ft);
		}
		return result;
	}

	@Override
	public Set<NonTerminal> findAllNonTerminal() {
		final Set<NonTerminal> result = new HashSet<>();
		for (final Concatenation c : this.getAlternative()) {
			final Set<NonTerminal> ft = c.findAllNonTerminal();
			result.addAll(ft);
		}
		return result;
	}

	// public boolean isMatchedBy(INode node) throws RuleNotFoundException {
	// for(Concatination c : this.getAlternative()) {
	// boolean isMatched = c.isMatchedBy(node);
	// if (isMatched) {
	// return true;
	// }
	// }
	// return false;
	// }

	// --- Object ---
	@Override
	public String toString() {
		String r = "";
		for (final Concatenation a : this.getAlternative()) {
			r += a.toString() + " < ";
		}
		return r;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof ChoicePriority) {
			final ChoicePriority other = (ChoicePriority) arg;
			return this.getOwningRule().equals(other.getOwningRule()) && this.index.equals(other.index);
		} else {
			return false;
		}
	}
}
