/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.common

import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.sppt.api.LeafData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_AglStyleHandler {

    @Test
    fun updateStyleModel() {
        val langId = LanguageIdentity("test.lang")
        val sut = AglStyleHandler(langId)

        val styleModel = Agl.registry.agl.style.processor!!.process(
            sentence = """
                namespace test
                styles Test {
                    $$ "'([^']+)'" {
                    
                    }
                }
            """
        ).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }

        sut.updateStyleModel(styleModel)


    }

    @Test
    fun mapToCssClasses() {
        val langId = LanguageIdentity("test.lang")
        val sut = AglStyleHandler(langId)

        val styleModel = Agl.registry.agl.style.processor!!.process(
            sentence = """
                namespace test
                styles Test {
                    $$ "'([^']+)'" {
                    
                    }
                }
            """
        ).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }

        sut.updateStyleModel(styleModel)


        val leaf = LeafData("'kw'", false, 0,1, listOf("'kw'"))
        val actual = sut.mapToCssClasses(leaf)

        val expected = listOf("agl_test_lang-1")
        assertEquals(expected, actual)
    }
}