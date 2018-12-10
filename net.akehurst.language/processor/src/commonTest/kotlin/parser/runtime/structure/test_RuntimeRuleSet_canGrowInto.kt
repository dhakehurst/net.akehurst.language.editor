package net.akehurst.language.ogl.runtime.structure

import net.akehurst.language.ogl.runtime.structure.RuntimeRuleSet
import kotlin.test.Test
import kotlin.test.assertEquals

class test_RuntimeRuleSet_canGrowInto {


    /**
     *   S =  ;
     */
    @Test
    fun canGrowInto_empty() {
        val rb = RuntimeRuleSetBuilder()

        val r_e = rb.rule("e").empty()
        val r_S = rb.rule("S").concatenation(r_e)

        val sut = rb.ruleSet()

        val actual = sut.calcCanGrowInto(r_e, r_S, 0)
        assertEquals(true, actual)
    }

    /**
     *   S = 'a' ;
     */
    @Test
    fun canGrowInto_concatenation_a() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").concatenation(r_a)

        val sut = rb.ruleSet()

        val actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)
    }

    /**
     *   S = 'a' 'b';
     */
    @Test
    fun canGrowInto_concatenation_ab() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_b = rb.literal("b")
        val r_S = rb.rule("S").concatenation(r_a, r_b)

        val sut = rb.ruleSet()

        val actual1 = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual1)

        val actual2 = sut.calcCanGrowInto(r_b, r_S, 0)
        assertEquals(false, actual2)

        val actual3 = sut.calcCanGrowInto(r_b, r_S, 1)
        assertEquals(true, actual3)
    }

    /**
     *   S = 'a' 'b' 'c';
     */
    @Test
    fun canGrowInto_concatenation_abc() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_b = rb.literal("b")
        val r_c = rb.literal("c")
        val r_S = rb.rule("S").concatenation(r_a, r_b, r_c)

        val sut = rb.ruleSet()

        val actual1 = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual1)

        val actual2 = sut.calcCanGrowInto(r_b, r_S, 0)
        assertEquals(false, actual2)

        val actual3 = sut.calcCanGrowInto(r_b, r_S, 1)
        assertEquals(true, actual3)

        val actual4 = sut.calcCanGrowInto(r_c, r_S, 0)
        assertEquals(false, actual4)

        val actual5 = sut.calcCanGrowInto(r_c, r_S, 1)
        assertEquals(false, actual5)

        val actual6 = sut.calcCanGrowInto(r_c, r_S, 2)
        assertEquals(true, actual6)
    }

    /**
     *   S = 'a' ;
     */
    @Test
    fun canGrowInto_choiceEqual_a() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").choiceEqual(r_a)

        val sut = rb.ruleSet()

        val actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)

        val actual2 = sut.calcCanGrowInto(r_a, r_S, 1)
        assertEquals(false, actual2)
    }

    /**
     *   S = 'a' | 'b' ;
     */
    @Test
    fun canGrowInto_choiceEqual_ab() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_b = rb.literal("b")
        val r_S = rb.rule("S").choiceEqual(r_a, r_b)

        val sut = rb.ruleSet()

        val actual1 = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual1)

        val actual2 = sut.calcCanGrowInto(r_b, r_S, 0)
        assertEquals(true, actual2)
    }

    /**
     *   S = 'a' | 'b' | 'c';
     */
    @Test
    fun canGrowInto_choiceEqual_abc() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_b = rb.literal("b")
        val r_c = rb.literal("c")
        val r_S = rb.rule("S").choiceEqual(r_a, r_b, r_c)

        val sut = rb.ruleSet()

        var actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_b, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_b, r_S, 1)
        assertEquals(false, actual)

        actual = sut.calcCanGrowInto(r_c, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_c, r_S, 1)
        assertEquals(false, actual)

    }

    /**
     *   S = 'a' ;
     */
    @Test
    fun canGrowInto_choicePriority_a() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").choicePriority(r_a)

        val sut = rb.ruleSet()

        val actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)

        val actual2 = sut.calcCanGrowInto(r_a, r_S, 1)
        assertEquals(false, actual2)
    }

    /**
     *   S = 'a' < 'b' ;
     */
    @Test
    fun canGrowInto_choicePriority_ab() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_b = rb.literal("b")
        val r_S = rb.rule("S").choicePriority(r_a, r_b)

        val sut = rb.ruleSet()

        var actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_a, r_S, 1)
        assertEquals(false, actual)

        actual = sut.calcCanGrowInto(r_b, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_b, r_S, 1)
        assertEquals(false, actual)
    }

    /**
     *   S = 'a' < 'b' < 'c' ;
     */
    @Test
    fun canGrowInto_choicePriority_abc() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_b = rb.literal("b")
        val r_c = rb.literal("c")
        val r_S = rb.rule("S").choicePriority(r_a, r_b, r_c)

        val sut = rb.ruleSet()

        var actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_b, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_b, r_S, 1)
        assertEquals(false, actual)

        actual = sut.calcCanGrowInto(r_c, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_c, r_S, 1)
        assertEquals(false, actual)

    }

    /**
     *   S = 'a'?;
     */
    @Test
    fun canGrowInto_multi01_a() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").multi(0,1,r_a)
        val r_e = rb.rules[2]

        val sut = rb.ruleSet()

        var actual = sut.calcCanGrowInto(r_a, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_a, r_S, 1)
        assertEquals(false, actual)

        actual = sut.calcCanGrowInto(r_a, r_S, 2)
        assertEquals(false, actual)

        actual = sut.calcCanGrowInto(r_e, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_e, r_S, 1)
        assertEquals(false, actual)

    }

    /**
     *   S = 'a'+;
     */
    @Test
    fun canGrowInto_multi1n_a() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").multi(1,-1,r_a)

        val sut = rb.ruleSet()

        assertEquals(2, rb.rules.size) // there is no empty to grow

        for (i in 0 until 15) {
            val actual = sut.calcCanGrowInto(r_a, r_S, i)
            assertEquals(true, actual,"index = $i")
        }
    }

    /**
     *   S = 'a'*;
     */
    @Test
    fun canGrowInto_multi0n_1() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").multi(0,-1,r_a)
        val r_e = rb.rules[2]

        val sut = rb.ruleSet()

        for (i in 0 until 15) {
            val actual = sut.calcCanGrowInto(r_a, r_S, i)
            assertEquals(true, actual,"index = $i")
        }

        var actual = sut.calcCanGrowInto(r_e, r_S, 0)
        assertEquals(true, actual)

        actual = sut.calcCanGrowInto(r_e, r_S, 1)
        assertEquals(false, actual)

        actual = sut.calcCanGrowInto(r_e, r_S, 2)
        assertEquals(false, actual)
    }

    /**
     *   S = 'a'[2,5];
     */
    @Test
    fun canGrowInto_multi25_1() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_S = rb.rule("S").multi(2,5,r_a)

        val sut = rb.ruleSet()

        for (i in 0 until 5) {
            var actual = sut.calcCanGrowInto(r_a, r_S, i)
            assertEquals(true, actual,"index = $i")
        }

        for (i in 5 until 15) {
            var actual = sut.calcCanGrowInto(r_a, r_S, i)
            assertEquals(false, actual,"index = $i")
        }
    }

    /**
     *   S = [ 'a' / ',']+;
     */
    @Test
    fun canGrowInto_sList1n_1() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_s = rb.literal(",")
        val r_S = rb.rule("S").separatedList(1,-1, r_s, r_a)

        val sut = rb.ruleSet()

        for (i in 0 until 12) {
            var actual_a = sut.calcCanGrowInto(r_a, r_S, i)
            var actual_s = sut.calcCanGrowInto(r_s, r_S, i)

            if (i % 2 == 0) {
                assertEquals(true, actual_a, "index = $i")
                assertEquals(false, actual_s, "index = $i")
            } else {
                assertEquals(false, actual_a, "index = $i")
                assertEquals(true, actual_s, "index = $i")
            }
        }
    }

    /**
     *   S = [ 'a' / ',']*;
     */
    @Test
    fun canGrowInto_sList0n_1() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_s = rb.literal(",")
        val r_S = rb.rule("S").separatedList(0,-1, r_s, r_a)
        val r_e = rb.rules[3]
        val sut = rb.ruleSet()

        for (i in 0 until 12) {
            var actual_a = sut.calcCanGrowInto(r_a, r_S, i)
            var actual_s = sut.calcCanGrowInto(r_s, r_S, i)

            if (i % 2 == 0) {
                assertEquals(true, actual_a, "item index = $i")
                assertEquals(false, actual_s, "separator index = $i")
            } else {
                assertEquals(false, actual_a, "item index = $i")
                assertEquals(true, actual_s, "separator index = $i")
            }
        }
    }

    /**
     *   S = [ 'a' / ','][2,5];
     */
    @Test
    fun canGrowInto_sList25_1() {
        val rb = RuntimeRuleSetBuilder()
        val r_a = rb.literal("a")
        val r_s = rb.literal(",")
        val r_S = rb.rule("S").separatedList(2,5, r_s, r_a)

        val sut = rb.ruleSet()

        for (i in 0 until 9) {
            var actual_a = sut.calcCanGrowInto(r_a, r_S, i)
            var actual_s = sut.calcCanGrowInto(r_s, r_S, i)

            if (i % 2 == 0) {
                assertEquals(true, actual_a, "item index = $i")
                assertEquals(false, actual_s, "index = $i")
            } else {
                assertEquals(false, actual_a, "item index = $i")
                assertEquals(true, actual_s, "separator index = $i")
            }
        }

        for (i in 9 until 20) {
            var actual_a = sut.calcCanGrowInto(r_a, r_S, i)
            var actual_s = sut.calcCanGrowInto(r_s, r_S, i)

            if (i % 2 == 0) {
                assertEquals(false, actual_a, "item index = $i")
                assertEquals(false, actual_s, "separator index = $i")
            } else {
                assertEquals(false, actual_a, "item index = $i")
                assertEquals(false, actual_s, "separator index = $i")
            }
        }
    }
}