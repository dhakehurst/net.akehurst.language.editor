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

package net.akehurst.language.api.grammar


/**
 *
 * The definition of a Grammar. A grammar defines a list of rules and may be defined to extend a number of other Grammars.
 *
 */
interface Grammar {

	/**
	 *
	 * the namespace of this grammar;
	 */
	val namespace: Namespace

	/**
	 *
	 * the name of this grammar
	 */
	val name: String

	/**
	 *
	 * the list of rules defined by this grammar
	 */
	val rule: List<Rule>

	val allTerminal: Set<Terminal>

	fun findAllNodeType(): Set<NodeType>

	fun findAllRule(name: String): Rule

	fun findAllTerminal(terminalPattern: String): Terminal
}
