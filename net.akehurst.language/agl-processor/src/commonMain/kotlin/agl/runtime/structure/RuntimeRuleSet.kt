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

import net.akehurst.language.api.parser.ParseException
import net.akehurst.language.collections.transitveClosure
import net.akehurst.language.parser.scannerless.InputFromCharSequence

class RuntimeRuleSet(rules: List<RuntimeRule>) {


    companion object {
        val GOAL_RULE_NUMBER = -1;
        val EOT_RULE_NUMBER = -2;
        val END_OF_TEXT = RuntimeRule(EOT_RULE_NUMBER, InputFromCharSequence.END_OF_TEXT, RuntimeRuleKind.TERMINAL, false, false)

        fun createGoal(userGoalRule: RuntimeRule): RuntimeRule {
            val gr = RuntimeRule(GOAL_RULE_NUMBER, "<GOAL>", RuntimeRuleKind.GOAL, false, false)
            gr.rhsOpt = RuntimeRuleItem(RuntimeRuleItemKind.CONCATENATION, -1, 0, arrayOf(userGoalRule, END_OF_TEXT))
            return gr
        }
    }

    private val nonTerminalRuleNumber: MutableMap<String, Int> = mutableMapOf()
    private val terminalRuleNumber: MutableMap<String, Int> = mutableMapOf()

    data class IndexCanGrowIntoAt(
        val childRuleNumber: Int,
        val ancesstorRuleNumber: Int,
        val at: Int
    ) {}

    private val canGrowIntoAt_cache: MutableMap<IndexCanGrowIntoAt, Boolean> = mutableMapOf()

    //TODO: are Arrays faster than Lists?
    val runtimeRules: Array<out RuntimeRule> by lazy {
        rules.sortedBy { it.number }.toTypedArray()
    }

    val allSkipRules: Array<RuntimeRule> by lazy {
        this.runtimeRules.filter { it.isSkip }.toTypedArray()
    }

    val allSkipTerminals: Array<RuntimeRule> by lazy {
        this.allSkipRules.flatMap {
            if (it.isTerminal)
                listOf(it)
            else
                it.rhs.items.filter { it.isTerminal }
        }.toTypedArray()
    }

    val isSkipTerminal: Array<Boolean> by lazy {
        this.runtimeRules.map {
            this.calcIsSkipTerminal(it)
        }.toTypedArray()
    }

    val terminalRules: Array<RuntimeRule> by lazy {
        this.runtimeRules.mapNotNull {
            if (it.isTerminal)
                it
            else
                null
        }.toTypedArray()
    }

    val firstTerminals: Array<Set<RuntimeRule>> by lazy {
        this.runtimeRules.map { this.calcFirstTerminals(it) }
            .toTypedArray()
    }

    val firstSkipRuleTerminalPositions: Set<RulePosition> by lazy {
        this.calcFirstTerminalSkipRulePositions()
    }

    val expectedTerminalRulePositions = lazyMap<RulePosition, Array<RulePosition>> {
        calcExpectedTerminalRulePositions(it).toTypedArray()
    }

    val firstTerminals2 = lazyMap<RulePosition, Set<RuntimeRule>> {
        val trps = expectedTerminalRulePositions[it] ?: arrayOf()
        trps.flatMap { it.items }.toSet()
    }

    /** Map of userGoalRule -> next Closure number **/
    private val nextClosure = mutableMapOf<RuntimeRule, ClosureNumber>()

    /** Map of goalRule -> (Map of ClosureNumber -> closure of RulePositionClosure)  **/
    private val closures = lazyMapNonNull<RuntimeRule, MutableMap<ClosureNumber, RulePositionClosure>> { userGoalRule ->
        mutableMapOf()
    }

    /** Map of userGoalRule -> next State number **/
    private val nextState = mutableMapOf<RuntimeRule, StateNumber>()

    data class StateKey(val rp: RulePosition)

    private val states = lazyMapNonNull<RuntimeRule, LazyMapNonNull<StateKey, MutableSet<RulePositionPath>>> { userGoalRule ->
        lazyMapNonNull<StateKey, MutableSet<RulePositionPath>> { mutableSetOf() }
    }

    private fun calcNextStateNumber(userGoalRule: RuntimeRule): StateNumber {
        val stateNumberValue: StateNumber = this.nextState[userGoalRule] ?: StateNumber(0)
        this.nextState[userGoalRule] = StateNumber(stateNumberValue.value + 1)
        return stateNumberValue
    }

    private fun createRulePositionPath(userGoalRule: RuntimeRule, rulePosition: RulePosition, ancestorRPs: List<RulePosition>): RulePositionPath {
        val stateNumber = this.calcNextStateNumber(userGoalRule)
        val rps = RulePositionPath(stateNumber, rulePosition, ancestorRPs)
        return rps
    }

    private fun createAllRulePositionPaths(userGoalRule: RuntimeRule, goalRP: RulePosition): Set<RulePositionPath> {
        val currentStates = this.states[userGoalRule]
        val start = this.createRulePositionPath(userGoalRule, goalRP, emptyList())
        currentStates[StateKey(goalRP)].add(start)
        val states = setOf(start).transitveClosure { parent ->
            val parentRP = parent.rulePosition
            val ancestors = parent.ancestorRPs + parentRP
            parentRP.items.flatMap { rr ->
                rr.rulePositions.mapNotNull { childRP ->
                    if (ancestors.contains(childRP)) {
                        val rpp=this.createRulePositionPath(userGoalRule, childRP, ancestors)
                        currentStates[StateKey(childRP)].add(rpp)
                        null
                    } else {
                        val rpp=this.createRulePositionPath(userGoalRule, childRP, ancestors)
                        currentStates[StateKey(childRP)].add(rpp)
                        rpp
                    }
                }
            }.toSet()
        }
        return states
    }


    init {
        for (rr in rules) {
//            if (null == rrule) {
//                throw ParserConstructionFailedException("RuntimeRuleSet must not contain a null rule!")
//            }
            if (rr.isNonTerminal) {
                this.nonTerminalRuleNumber[rr.name] = rr.number
            } else {
                this.terminalRuleNumber[rr.name] = rr.number
            }
        }
    }

    private fun calcNextClosureNumber(userGoalRule: RuntimeRule): ClosureNumber {
        val num: ClosureNumber = this.nextClosure[userGoalRule] ?: ClosureNumber(0)
        this.nextClosure[userGoalRule] = ClosureNumber(num.value + 1)
        return num
    }

    private fun createRulePositionState(userGoalRule: RuntimeRule, closureNumber: ClosureNumber, rulePosition: RulePosition, ancestorRPs: List<RulePosition>, heightLookahead: Set<RuntimeRule>, graftLookahead: Set<RuntimeRule>): RulePositionState {
        val stateNumber = this.calcNextStateNumber(userGoalRule)
        val rps = RulePositionState(stateNumber, rulePosition, ancestorRPs, graftLookahead, closureNumber)
        return rps
    }

    private fun fetchRulePositionPath(userGoalRule: RuntimeRule, rulePosition: RulePosition, ancestorRPs: List<RulePosition>): RulePositionPath {
        val currentStates = this.states[userGoalRule]
        val fkey = StateKey(rulePosition)
        return if (currentStates.containsKey(fkey)) {
            val r = currentStates[fkey].firstOrNull { it.ancestorRPs == ancestorRPs }
            if (null==r) {
                throw Exception("Should never be happen, states shaould already be created")
            } else {
                r
            }
        } else {
            throw Exception("Should never be happen, states shaould already be created")
        }
    }

    private fun fetchRulePositionPaths(userGoalRule: RuntimeRule, rulePosition: RulePosition): Set<RulePositionPath> {
        val currentStates = this.states[userGoalRule]
        val fkey = StateKey(rulePosition)
        return if (currentStates.containsKey(fkey)) {
            currentStates[fkey]
        } else {
            throw Exception("Should never be happen, states shaould already be created")
        }
    }

    private fun calcHeightLookahead(ancestorRPs: List<RulePosition>, childRP: RulePosition): Set<RuntimeRule> {
        return if (childRP.isAtEnd) {
            if (ancestorRPs.isEmpty()) {
                emptySet<RuntimeRule>()
            } else {
                val parent = ancestorRPs.last()
                val parentAncestors = ancestorRPs.dropLast(1)
                this.calcHeightLookahead(parentAncestors, parent)
            }
        } else {
            childRP.items.flatMap { fstChildItem ->
                val nextRPs = nextRulePosition(childRP, fstChildItem)
                nextRPs.flatMap { nextRP ->
                    if (nextRP.isAtEnd) {
                        if (ancestorRPs.isEmpty()) {
                            emptySet<RuntimeRule>()
                        } else {
                            val parent = ancestorRPs.last()
                            val parentAncestors = ancestorRPs.dropLast(1)
                            this.calcHeightLookahead(parentAncestors, parent)
                        }
                    } else {
                        val lh: Set<RuntimeRule> = this.firstTerminals2[nextRP]
                            ?: throw ParseException("should never happen")
                        return if (lh.isEmpty()) {
                            throw ParseException("should never happen")
                        } else {
                            lh
                        }
                    }
                }
            }.toSet()
        }
    }

    private fun calcHeightLookahead(ancestorRPs: List<RulePosition>, childRP: RulePosition, ifEmpty: Set<RuntimeRule>): Set<RuntimeRule> {
        return this.calcHeightLookahead(ancestorRPs, childRP)
        /*
        //TODO: return emptySet where heightlookahead is not useful, maybe! i.e. when parentRP is not at start, maybe!
        return if (childRP.isAtEnd) {
            val nextRPs = this.nextRulePosition(parentRP, childRP.runtimeRule)
            nextRPs.flatMap { nextRP ->
                if (nextRP.isAtEnd) {
                    ifEmpty
                } else {
                    val lh: Set<RuntimeRule> = this.firstTerminals2[nextRP]
                        ?: throw ParseException("should never happen")
                    return if (lh.isEmpty()) {
                        ifEmpty
                    } else {
                        lh
                    }
                }
            }.toSet()
        } else {
            childRP.items.flatMap { fstChildItem ->
                val nextRPs = nextRulePosition(childRP, fstChildItem)
                nextRPs.flatMap { nextRP ->
                    if (nextRP.isAtEnd) {
                        ifEmpty
                    } else {
                        val lh: Set<RuntimeRule> = this.firstTerminals2[nextRP]
                            ?: throw ParseException("should never happen")
                        return if (lh.isEmpty()) {
                            ifEmpty
                        } else {
                            lh
                        }
                    }
                }
            }.toSet()
        }
        */
    }

    private fun calcGraftLookahead(ancestorRPs: List<RulePosition>, childRP: RulePosition): Set<RuntimeRule> {
        return if (ancestorRPs.isEmpty()) {
            emptySet()
        } else {
            val parent = ancestorRPs.last()
            val parentAncestors = ancestorRPs.dropLast(1)
            val nextRPs = nextRulePosition(parent, childRP.runtimeRule)
            return if (nextRPs.isEmpty()) {
                calcGraftLookahead(parentAncestors, parent)
            } else {
                nextRPs.flatMap { nextRP ->
                    if (nextRP.isAtEnd) {
                        calcGraftLookahead(parentAncestors, parent)
                    } else {
                        val lh: Set<RuntimeRule> = this.firstTerminals2[nextRP]
                            ?: throw ParseException("should never happen")
                        return if (lh.isEmpty()) {
                            throw ParseException("should never happen")
                        } else {
                            lh
                        }
                    }
                }.toSet()
            }
        }
    }

    internal fun createClosure(userGoalRule: RuntimeRule, closureNumber: ClosureNumber, root: RulePositionPath): RulePositionClosure {
        // assume all RulePositionPaths have alreay been created
        val closureSet = setOf(root).transitveClosure { parentRPP ->
            val parentRP = parentRPP.rulePosition
            val ancestorRPs = parentRPP.ancestorRPs + parentRP
            parentRP.items.flatMap { rr ->
                val childrenRP = rr.calcExpectedRulePositions(0)
                childrenRP.map { childRP ->
                    this.fetchRulePositionPath(userGoalRule, childRP, ancestorRPs)
                }
            }.toSet()
        }
        return RulePositionClosure(closureNumber, root.rulePosition, closureSet)
    }

    internal fun fetchOrCreateClosure(userGoalRule: RuntimeRule, rps: RulePositionState): RulePositionClosure {
        TODO("not needed")
    }

    internal fun fetchOrCreateClosure(userGoalRule: RuntimeRule, rp: RulePosition, ancestorRPs: List<RulePosition>): RulePositionClosure {
        val currentClosure = this.closures[userGoalRule]

        val closure = currentClosure.values.firstOrNull { it.root == rp }
        return if (null==closure)  {
            val closureNumber = this.calcNextClosureNumber(userGoalRule)
            val newRps = this.fetchRulePositionPath(userGoalRule, rp, ancestorRPs)
            val newClosure = this.createClosure(userGoalRule, closureNumber, newRps)
            currentClosure[closureNumber] = newClosure
            newClosure
        } else {
            closure
        }
    }

    internal fun fetchClosuresContaining(userGoalRule: RuntimeRule, rp: RulePosition): Set<RulePositionClosure> {
        val thisClosures = this.closures[userGoalRule]
        val result2 = thisClosures.values.filter { cl: RulePositionClosure ->
            cl.content.any { x -> x.rulePosition == rp }
        }.toSet()
        return result2
    }

    private fun createAllRulePositionPathsAndClosures(userGoalRule: RuntimeRule, goalRP: RulePosition): Set<RulePositionClosure> {
        val currentClosure = this.closures[userGoalRule]

        //TODO: can we create the closures reachable by transition on-demand rather than up front?
        //Do all rulePositions from here, rather than just the nextRulePosition, otherwise we can't keep the ancestorRPs

        val startPath = this.createRulePositionPath(userGoalRule, goalRP, emptyList())
        val startClosures = goalRP.runtimeRule.rulePositions.map { rp ->
            val closureNumber = this.calcNextClosureNumber(userGoalRule)
            val closure = this.createClosure(userGoalRule, closureNumber, startPath)
            currentClosure[closureNumber] = closure
            closure
        }.toSet()

        val closures = startClosures.transitveClosure { closure ->
            closure.content.flatMap { rps ->
                //Do all rulePositions from here, rather than just the nextRulePosition, otherwise we can't keep the ancestorRPs
                val rulePositions = rps.runtimeRule.rulePositions
                val rpsToDo = rulePositions.filter { it.isAtStart.not() && it.runtimeRule.isNonTerminal }
                rpsToDo.mapNotNull { rp ->
                    this.fetchOrCreateClosure(userGoalRule, rp, rps.ancestorRPs)
                }
            }.toSet()
        }

        return closures
    }

    /**
     * itemRule is the rule we use to increment rp
     */
    fun nextRulePosition(rp: RulePosition, itemRule: RuntimeRule): Set<RulePosition> { //TODO: cache this
        return if (RulePosition.END_OF_RULE == rp.position) {
            emptySet() //TODO: use goal rule to find next position? maybe
        } else {
            when (rp.runtimeRule.rhs.kind) {
                RuntimeRuleItemKind.EMPTY -> throw ParseException("This should never happen!")
                RuntimeRuleItemKind.CHOICE_EQUAL -> when {
                    itemRule == rp.runtimeRule.rhs.items[rp.choice] -> setOf(RulePosition(rp.runtimeRule, rp.choice, RulePosition.END_OF_RULE))
                    else -> emptySet() //throw ParseException("This should never happen!")
                }
                RuntimeRuleItemKind.CHOICE_PRIORITY -> when {
                    itemRule == rp.runtimeRule.rhs.items[rp.choice] -> setOf(RulePosition(rp.runtimeRule, rp.choice, RulePosition.END_OF_RULE))
                    else -> emptySet() //throw ParseException("This should never happen!")
                }
                RuntimeRuleItemKind.CONCATENATION -> { //TODO: check itemRule?
                    val np = rp.position + 1
                    if (np < rp.runtimeRule.rhs.items.size) {
                        setOf(RulePosition(rp.runtimeRule, 0, np))
                    } else {
                        setOf(RulePosition(rp.runtimeRule, 0, RulePosition.END_OF_RULE))
                    }
                }
                RuntimeRuleItemKind.MULTI -> when (rp.choice) {
                    RuntimeRuleItem.MULTI__EMPTY_RULE -> when {
                        0 == rp.position && rp.runtimeRule.rhs.multiMin == 0 && itemRule == rp.runtimeRule.rhs.MULTI__emptyRule -> setOf(
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.MULTI__EMPTY_RULE, RulePosition.END_OF_RULE)
                        )
                        else -> emptySet() //throw ParseException("This should never happen!")
                    }
                    RuntimeRuleItem.MULTI__ITEM -> when {
                        itemRule == rp.runtimeRule.rhs.MULTI__repeatedItem -> setOf(
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.MULTI__ITEM, 1),
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.MULTI__ITEM, RulePosition.END_OF_RULE)
                        )
                        else -> emptySet() //throw ParseException("This should never happen!")
                    }
                    else -> throw ParseException("This should never happen!")
                }
                RuntimeRuleItemKind.SEPARATED_LIST -> when (rp.choice) {
                    RuntimeRuleItem.SLIST__EMPTY_RULE -> when {
                        0 == rp.position && rp.runtimeRule.rhs.multiMin == 0 && itemRule == rp.runtimeRule.rhs.SLIST__emptyRule -> setOf(
                            RulePosition(rp.runtimeRule, rp.choice, RulePosition.END_OF_RULE)
                        )
                        else -> emptySet() //throw ParseException("This should never happen!")
                    }
                    RuntimeRuleItem.SLIST__ITEM -> when {
                        0 == rp.position && (rp.runtimeRule.rhs.multiMax == 1) && itemRule == rp.runtimeRule.rhs.SLIST__repeatedItem -> setOf(
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.SLIST__ITEM, RulePosition.END_OF_RULE)
                        )
                        0 == rp.position && (rp.runtimeRule.rhs.multiMax > 1 || -1 == rp.runtimeRule.rhs.multiMax) && itemRule == rp.runtimeRule.rhs.SLIST__repeatedItem -> setOf(
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.SLIST__SEPARATOR, 1),
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.SLIST__ITEM, RulePosition.END_OF_RULE)
                        )
                        2 == rp.position && (rp.runtimeRule.rhs.multiMax > 1 || -1 == rp.runtimeRule.rhs.multiMax) && itemRule == rp.runtimeRule.rhs.SLIST__repeatedItem -> setOf(
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.SLIST__SEPARATOR, 1),
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.SLIST__ITEM, RulePosition.END_OF_RULE)
                        )
                        else -> emptySet() //throw ParseException("This should never happen!")
                    }
                    RuntimeRuleItem.SLIST__SEPARATOR -> when {
                        1 == rp.position && (rp.runtimeRule.rhs.multiMax > 1 || -1 == rp.runtimeRule.rhs.multiMax) && itemRule == rp.runtimeRule.rhs.SLIST__separator -> setOf(
                            RulePosition(rp.runtimeRule, RuntimeRuleItem.SLIST__ITEM, 2)
                        )
                        else -> emptySet() //throw ParseException("This should never happen!")
                    }
                    else -> throw ParseException("This should never happen!")
                }
                RuntimeRuleItemKind.LEFT_ASSOCIATIVE_LIST -> throw ParseException("Not yet supported")
                RuntimeRuleItemKind.RIGHT_ASSOCIATIVE_LIST -> throw ParseException("Not yet supported")
                RuntimeRuleItemKind.UNORDERED -> throw ParseException("Not yet supported")
            }
        }
    }

    private fun createWidthTransition(from: RulePosition, closureRPS: RulePositionPath): Set<Transition> {
        val action = Transition.ParseAction.WIDTH
        val item = closureRPS.runtimeRule
        val lookaheadGuard = this.calcGraftLookahead(closureRPS.ancestorRPs, closureRPS.rulePosition) //closureRPS.graftLookahead
        val to = closureRPS
        return setOf(Transition(from, to.rulePosition, action, item, lookaheadGuard))
    }

    private fun createHeightTransition(userGoalRule: RuntimeRule, closureRPS: RulePositionPath): Set<Transition> {
        val parentRP = closureRPS.directParent ?: throw ParseException("Should never be null")
        val action = Transition.ParseAction.HEIGHT
        val from = closureRPS.rulePosition
        val item = closureRPS.runtimeRule
        val nextRPs = this.nextRulePosition(parentRP, item)
        return nextRPs.flatMap { nextRP ->
            val toSet = this.fetchRulePositionPaths(userGoalRule, nextRP)
            val lookaheadGuard = this.calcHeightLookahead(closureRPS.ancestorRPs, closureRPS.rulePosition) //from.heightLookahead
            toSet.map { to -> Transition(from, to.rulePosition, action, item, lookaheadGuard) }
        }.toSet()
    }

    private fun createGraftTransition(userGoalRule: RuntimeRule, from: RulePosition, closureRPS: RulePositionPath): Set<Transition> {
        val parentRP = closureRPS.directParent ?: throw ParseException("Should never be null")
        val action = Transition.ParseAction.GRAFT
        val item = closureRPS.runtimeRule
        val nextRPs = this.nextRulePosition(parentRP, item)
        return nextRPs.flatMap { nextRP ->
            val toSet = this.fetchRulePositionPaths(userGoalRule, nextRP)
            val lookaheadGuard = this.calcGraftLookahead(closureRPS.ancestorRPs.dropLast(1), nextRP)//from.graftLookahead
            toSet.map { to -> Transition(from, to.rulePosition, action, item, lookaheadGuard) }
        }.toSet()
    }

    private fun calcTransitions2(userGoalRule: RuntimeRule, from: RulePosition, prevRP: RulePosition?): Set<Transition> {
        //assume all closures are created already
        val closures = this.fetchClosuresContaining(userGoalRule, from)
        val transitions = mutableSetOf<Transition>()
        for (closure in closures) {
            for (closureRPS in closure.content) {
                val parentRP = closureRPS.directParent
                val width = closureRPS.runtimeRule.isTerminal && from.isAtEnd.not()
                //val height = closureRPS.ancestorRPs.contains(from.directParent) && from.isAtEnd && closureRPS.isAtEnd && (parentRP?.isAtStart
                //    ?: false)
                //val graft = closureRPS.ancestorRPs.contains(from.directParent) && from.isAtEnd && closureRPS.isAtEnd && (parentRP?.isAtStart?.not()
                //    ?: false)
                val height = closureRPS.rulePosition == from && from.isAtEnd && closureRPS.isAtEnd && (parentRP?.isAtStart
                    ?: false)
                val graft = closureRPS.rulePosition == from && from.isAtEnd && closureRPS.isAtEnd && (parentRP?.isAtStart?.not()
                    ?: false)

                if (width) {
                    transitions += this.createWidthTransition(from, closureRPS)
                }
                if (height) {
                    transitions += this.createHeightTransition(userGoalRule, closureRPS)
                }
                if (graft) {
                    transitions += this.createGraftTransition(userGoalRule, from, closureRPS)
                }
            }
        }
        return transitions
    }

    private fun calcTransitions(userGoalRule: RuntimeRule, from: RulePositionState, prevState: RulePositionState?): Set<Transition> {
        val prevRP = prevState?.rulePosition
        val closure = this.fetchOrCreateClosure(userGoalRule, from)
/*
        val transitions: Set<Transition> = closure.content.flatMap { closureRPS ->
            val closureRP = closureRPS.rulePosition
            val parentRP = closureRPS.directParent
            //val parentClosure = if (null==parentRPS) emptySet<RulePositionState>() else setOf(closureRPS).transitveClosure { if (null==it.parent) emptySet() else setOf(it.parent) }
            val relevantW = closureRP.runtimeRule.isTerminal //closureRP==from.rulePosition || closureRPS.ancestorRPs.contains(from.rulePosition) //parentClosure.contains(from)
            val relevantHG = closureRPS.ancestorRPs.contains(from.directParent)
            when {
                (relevantW && from.isAtEnd.not()) -> {
                    val tr = this.createWidthTransition(from, closureRPS)
                    return tr
                }
                (null == parentRP) -> when {
                    (from.runtimeRule.isGoal && from.isAtEnd && closureRPS.isAtEnd) -> {
                        val action = Transition.ParseAction.GOAL
                        val to = from
                        setOf(Transition(from, to, action, RuntimeRuleSet.END_OF_TEXT, emptySet()))
                    }
                    else -> emptySet<Transition>()
                }
                //special case because we 'artificially' create first child of goal in ParseGraph.start
                // (because we want starting skip nodes (i.e. whitespace) to appear inside the userGoal node, rather than inside the top 'GOAL' node)
                (parentRP.runtimeRule.isGoal && relevantHG && from.isAtEnd && closureRPS.isAtEnd && parentRP.isAtStart) -> {
                    val action = Transition.ParseAction.GRAFT
                    val item = closureRP.runtimeRule
                    val nextRPs = this.nextRulePosition(parentRP, item)
                    nextRPs.flatMap { nextRP ->
                        val toSet = this.fetchRulePositionStates(userGoalRule, nextRP, closureRPS.graftLookahead)
                        val lookaheadGuard = from.graftLookahead
                        toSet.map { to -> Transition(from, to, action, item, lookaheadGuard) }
                    }
                }
                (relevantHG && from.isAtEnd && closureRPS.isAtEnd && parentRP.isAtStart) -> {
                    this.createHeightTransition(userGoalRule, from, closureRPS)
                }
                (relevantHG && from.isAtEnd && closureRPS.isAtEnd && parentRP.isAtStart.not()) -> {
                    this.createGraftTransition(userGoalRule, from, closureRPS)
                }
                else -> emptySet<Transition>()
            }
        }.toSet()

        return transitions
 */
        return emptySet()
    }

    fun buildCaches() {

    }

    fun startingRulePosition(userGoalRule: RuntimeRule): RulePosition {
        val goalRule = RuntimeRuleSet.createGoal(userGoalRule)
        val goalRp = RulePosition(goalRule, 0, 0)

        val states = this.createAllRulePositionPaths(userGoalRule, goalRp)
        this.createAllRulePositionPathsAndClosures(userGoalRule, goalRp)

        //val userGoalRP = userGoalRule.calcExpectedRulePositions(0).first()
        //val eotLookahead = setOf(RuntimeRuleSet.END_OF_TEXT)
        //val hlh = emptySet<RuntimeRule>()//this.calcHeightLookahead(goalRp, userGoalRP, eotLookahead)
        // val glh = emptySet<RuntimeRule>() //eotLookahead
        //val startRPS = this.fetchOrCreateRulePositionStateAndItsClosure(userGoalRule, goalRp, emptyList(), hlh, glh)

        return this.fetchRulePositionPaths(userGoalRule, goalRp).first().rulePosition
    }

    fun transitions(userGoalRule: RuntimeRule, from: RulePosition, prevRP: RulePosition?): Set<Transition> {
        return this.calcTransitions2(userGoalRule, from, prevRP)
    }

    // ---

    fun findRuntimeRule(ruleName: String): RuntimeRule {
        val number = this.nonTerminalRuleNumber[ruleName]
            ?: throw ParseException("NonTerminal RuntimeRule '${ruleName}' not found")
        return this.runtimeRules[number]
    }

    fun findTerminalRule(pattern: String): RuntimeRule {
        val number = this.terminalRuleNumber[pattern]
            ?: throw ParseException("Terminal RuntimeRule ${pattern} not found")
        return this.runtimeRules[number]
    }

    fun findNextExpectedItems(runtimeRule: RuntimeRule, nextItemIndex: Int): Set<RuntimeRule> {
        return runtimeRule.findNextExpectedItems(nextItemIndex)
    }

    private fun calcExpectedItemRulePositionTransitive(rp: RulePosition): Set<RulePosition> {
        var s = setOf(rp)//rp.runtimeRule.calcExpectedRulePositions(rp.position)

        return s.transitveClosure { rp ->
            if (RulePosition.END_OF_RULE == rp.position) {
                emptySet()
            } else {
                if (rp.runtimeRule.isTerminal) {
                    emptySet<RulePosition>()
                } else {
                    rp.runtimeRule.items(rp.choice, rp.position).flatMap {
                        if (it.isTerminal) {
                            setOf(rp)
                        } else {
                            it.calcExpectedRulePositions(0)
                        }
                    }
                }
            }.toSet()
        }
    }

    private fun calcExpectedTerminalRulePositions(rp: RulePosition): Set<RulePosition> {
        val nextItems = this.calcExpectedItemRulePositionTransitive(rp)
        return nextItems.filter {
            if (it.runtimeRule.isTerminal) { //should never happen!
                false
            } else {
                if (RulePosition.END_OF_RULE == it.position) {
                    false
                } else {
                    it.items.any { it.isTerminal }
                }
            }
        }.toSet() //TODO: cache ?
    }

    private fun calcExpectedSkipItemRulePositionTransitive(): Set<RulePosition> {
        val skipRuleStarts = allSkipRules.map {
            val x = firstTerminals[it.number]
            RulePosition(it, 0, 0)
        }
        return skipRuleStarts.flatMap {
            this.calcExpectedItemRulePositionTransitive(it)
        }.toSet()
    }

    private fun calcFirstTerminalSkipRulePositions(): Set<RulePosition> {
        val skipRPs = calcExpectedSkipItemRulePositionTransitive()
        return skipRPs.filter {
            it.runtimeRule.itemsAt[it.position].any { it.isTerminal }
        }.toSet() //TODO: cache ?
    }

    private fun calcFirstSubRules(runtimeRule: RuntimeRule): Set<RuntimeRule> {
        return runtimeRule.findSubRulesAt(0)
    }

    private fun calcFirstTerminals(runtimeRule: RuntimeRule): Set<RuntimeRule> {
        var rr = runtimeRule.findTerminalAt(0)
        for (r in this.calcFirstSubRules(runtimeRule)) {
            rr += r.findTerminalAt(0)
        }
        return rr
    }

    private fun calcIsSkipTerminal(rr: RuntimeRule): Boolean {
        val b = this.allSkipTerminals.contains(rr)
        return b
    }

    //TODO: should be private and only a cache is public
    fun calcCanGrowInto(childRule: RuntimeRule, ancesstorRule: RuntimeRule, ancesstorItemIndex: Int): Boolean {
        return if (-1 == ancesstorItemIndex) {
            false
        } else {
            //return canGrowIntoAt_cache[childRule.number][ancesstorRule.number][ancesstorItemIndex];
            val index = IndexCanGrowIntoAt(childRule.number, ancesstorRule.number, ancesstorItemIndex)
            var result = canGrowIntoAt_cache[index]
            if (null == result) {
                //TODO: try using RulePositions to do this calculation
                val nextExpectedForStacked = this.findNextExpectedItems(ancesstorRule, ancesstorItemIndex)
                if (nextExpectedForStacked.contains(childRule)) {
                    result = true
                } else {
                    result = false
                    for (rr in nextExpectedForStacked) {
                        if (rr.isNonTerminal) {
                            // todo..can we reduce the possibles!
                            val possibles = this.calcFirstSubRules(rr)
                            if (possibles.contains(childRule)) {
                                result = true
                                break
                            }
                        } else {
                            val possibles = this.firstTerminals[rr.number]
                            if (possibles.contains(childRule)) {
                                result = true
                                break
                            }
                        }
                    }
                }
                canGrowIntoAt_cache[index] = result ?: throw ParseException("Should never happen")
            }
            return result
        }
    }

    override fun toString(): String {
        val rulesStr = this.runtimeRules.map {
            "  " + it.toString()
        }.joinToString("\n")
        return """
RuntimeRuleSet {
${rulesStr}
}
        """.trimIndent()
    }
}