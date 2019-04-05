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

package net.akehurst.language.agl.runtime.structure

data class RulePosition(
    val runtimeRule: RuntimeRule,
    val choice: Int,
    val position: Int
) {

    companion object {
        val END_OF_RULE = -1
    }

    val isAtStart = position == 0
    val isAtEnd = position == END_OF_RULE

    val items:Set<RuntimeRule> get() {
         return if (END_OF_RULE==position) {
             emptySet()
         } else {
             runtimeRule.items(choice, position)
         }
    }

    override fun toString(): String {
        val r = when {
            runtimeRule == RuntimeRuleSet.END_OF_TEXT -> "EOT"
            runtimeRule.isTerminal -> if (runtimeRule.isPattern) "\"${runtimeRule.name}\"" else "'${runtimeRule.name}'"
            else -> runtimeRule.name
        }
        return "RP(${r},$choice,$position)"
    }

}