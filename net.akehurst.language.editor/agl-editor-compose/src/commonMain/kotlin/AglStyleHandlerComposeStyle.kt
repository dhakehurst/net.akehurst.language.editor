/**
 * Copyright (C) 2025 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.compose

import net.akehurst.language.editor.api.AglStyleHandler
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.sppt.api.LeafData
import net.akehurst.language.style.api.AglStyleModel
import net.akehurst.language.style.api.AglStyleSelector

class AglStyleHandlerComposeStyle(): AglStyleHandler<ComposeStyle> {

    override val styleModel: AglStyleModel
        get() = TODO("not implemented")

    override fun reset() {
        TODO("not implemented")
    }

    override fun updateStyleModel(styleModel: AglStyleModel) {
        TODO("not implemented")
    }

    override fun editorStyleFor(identity: EditorStyleIdentity): ComposeStyle? {
        TODO("not implemented")
    }

    override fun convert(selector: AglStyleSelector): ComposeStyle {
        TODO("not implemented")
    }

    override fun transformToTokens(leafs: List<LeafData>): List<AglToken> {
        TODO("not implemented")
    }
}
