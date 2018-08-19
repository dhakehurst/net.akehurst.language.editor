/**
 * Copyright (C) 2018 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.ogl.semanticStructure

import net.akehurst.language.api.grammar.SimpleItem
import net.akehurst.language.api.grammar.Multi
import net.akehurst.language.api.grammar.Rule
import net.akehurst.language.api.grammar.RuleItem
import net.akehurst.language.api.grammar.Terminal
import net.akehurst.language.api.grammar.NonTerminal
import net.akehurst.language.api.grammar.GrammarVisitor
import net.akehurst.language.api.grammar.GrammarRuleItemNotFoundException

class MultiDefault(val min: Long, val max: Long, val item: SimpleItem) : RuleItemAbstract(), Multi {

    override fun setOwningRule(rule: Rule, indices: List<Int>) {
		this.owningRule = rule
		this.index = indices
		val nextIndex: List<Int> = indices + 0
		this.item.setOwningRule(rule, nextIndex)
	}
		
	override fun subItem(index: Int): RuleItem {
		return if (0==index) this.item else throw GrammarRuleItemNotFoundException("subitem ${index} not found")
	}
	
	override val allTerminal: Set<Terminal> by lazy {
		this.item.allTerminal
	}

	override val allNonTerminal: Set<NonTerminal> by lazy {
		this.item.allNonTerminal
	}
	
    override fun <T> accept(visitor: GrammarVisitor<T>, vararg arg: Any): T {
        return visitor.visit(this, arg);
    }
	
}