package net.akehurst.language.editor.demo.gui

import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.akehurst.kotlin.compose.components.tree.TreeViewNode
import net.akehurst.kotlinx.collections.mutableStackOf
import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.kotlinx.logging.api.logger
import net.akehurst.kotlinx.utils.UniqueIdentityGenerator
import net.akehurst.language.agl.semanticAnalyser.contextFromTypesDomain
import net.akehurst.language.api.processor.AsmTransformString
import net.akehurst.language.api.processor.CrossReferenceString
import net.akehurst.language.api.processor.FormatString
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.api.processor.TypesString
import net.akehurst.language.asm.api.Asm
import net.akehurst.language.asm.api.AsmCollection
import net.akehurst.language.asm.api.AsmNothing
import net.akehurst.language.asm.api.AsmPrimitive
import net.akehurst.language.asm.api.AsmStructure
import net.akehurst.language.asm.api.AsmValue
import net.akehurst.language.asmTransform.asm.AsmTransformDomainDefault
import net.akehurst.language.editor.api.EventStatus
import net.akehurst.language.editor.information.Example
import net.akehurst.language.editor.information.ExternalContextLanguage
import net.akehurst.language.grammar.api.GrammarDomain
import net.akehurst.language.grammar.processor.contextFromGrammar
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.sppt.api.PathFunction
import net.akehurst.language.sppt.api.SharedPackedParseTree
import net.akehurst.language.sppt.api.SpptDataNodeInfo
import net.akehurst.language.sppt.api.SpptWalker
import net.akehurst.language.sppt.api.TreeData
import net.akehurst.language.sppt.treedata.matchedTextNoSkip
import net.akehurst.language.types.api.TypesDomain

class GuiHandler(
    val gui: Gui
) {
    companion object {
        val LOGGER = logger("GuiHandler")
    }

    private var doUpdates = true

    fun connectEditors() {
        // Connect Editors
        gui.grammarEditor.onSemanticAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {
                        this.updatesFromGrammar(null)
                        LOGGER.logError { gui.grammarEditor.endPointIdentity.editorId + ": " + event.message }
                        gui.sentenceEditor.languageDefinition.update(grammarString = null)
                    }

                    EventStatus.SUCCESS -> {
                        val grammars = event.asm as GrammarDomain? ?: error("should always be a GrammarDomain if success")
                        this.updatesFromGrammar(grammars)
                        try {
                            LOGGER.logDebug { "Debug: Grammar SemanticAnalysis success, resetting sentence processor" }
                            gui.sentenceEditor.languageDefinition.update(grammarString = GrammarString(gui.grammarEditor.text))
                        } catch (t: Throwable) {
                            LOGGER.log(LogLevel.Error, t) { gui.grammarEditor.endPointIdentity.editorId + ": " + t.message }
                            gui.sentenceEditor.languageDefinition.update(grammarString = null)
                        }
                    }
                }
            }
        }
        gui.styleEditor.onSemanticAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {
                        LOGGER.logError { gui.styleEditor.endPointIdentity.editorId + ": " + event.message }
                        gui.sentenceEditor.languageDefinition.update(styleString = null)
                    }

                    EventStatus.SUCCESS -> {
                        try {
                            LOGGER.logDebug { "Debug: Style SemanticAnalysis success, resetting sentence style" }
                            gui.sentenceEditor.languageDefinition.update(styleString = StyleString(gui.styleEditor.text))
                        } catch (t: Throwable) {
                            LOGGER.log(LogLevel.Error, t) { gui.styleEditor.endPointIdentity.editorId + ": " + t.message }
                            gui.sentenceEditor.languageDefinition.update(styleString = null)
                        }
                    }
                }
            }
        }
        gui.typesEditor.onSemanticAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {
                        LOGGER.logError { gui.typesEditor.endPointIdentity.editorId + ": " + event.message }
                        gui.sentenceEditor.languageDefinition.update(typesString = null)
                    }

                    EventStatus.SUCCESS -> {
                        try {
                            LOGGER.logDebug { "Debug: Types SemanticAnalysis success, resetting sentence types" }
                            gui.sentenceEditor.languageDefinition.update(typesString = TypesString(gui.typesEditor.text))
                        } catch (t: Throwable) {
                            LOGGER.log(LogLevel.Error, t) { gui.typesEditor.endPointIdentity.editorId + ": " + t.message }
                            gui.sentenceEditor.languageDefinition.update(typesString = null)
                        }
                    }
                }
            }
        }
        gui.referencesEditor.onSemanticAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {
                        LOGGER.logError { gui.referencesEditor.endPointIdentity.editorId + ": " + event.message }
                        gui.sentenceEditor.languageDefinition.update(crossReferenceString = null)
                    }

                    EventStatus.SUCCESS -> {
                        try {
                            LOGGER.logDebug { "Debug: CrossReferences SemanticAnalysis success, resetting scopes and references" }
                            gui.sentenceEditor.languageDefinition.update(crossReferenceString = CrossReferenceString(gui.referencesEditor.text))
                        } catch (t: Throwable) {
                            LOGGER.log(LogLevel.Error, t) { gui.referencesEditor.endPointIdentity.editorId + ": " + t.message }
                            gui.sentenceEditor.languageDefinition.update(crossReferenceString = null)
                        }
                    }
                }
            }
        }
        gui.asmTransEditor.onSemanticAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {
                        LOGGER.logError { gui.asmTransEditor.endPointIdentity.editorId + ": " + event.message }
                        gui.sentenceEditor.languageDefinition.update(asmTransformString = null)
                    }

                    EventStatus.SUCCESS -> {
                        try {
                            LOGGER.logDebug { "Debug: AsmTransform SemanticAnalysis success, updating sentence language" }
                            gui.sentenceEditor.languageDefinition.update(asmTransformString = AsmTransformString(gui.asmTransEditor.text))
                        } catch (t: Throwable) {
                            LOGGER.log(LogLevel.Error, t) { gui.asmTransEditor.endPointIdentity.editorId + ": " + t.message }
                            gui.sentenceEditor.languageDefinition.update(asmTransformString = null)
                        }
                    }
                }
            }
        }

        gui.sentenceEditor.onParse { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {

                    }

                    EventStatus.SUCCESS -> {
                        this.updateParseTree(event.tree)
                    }
                }
            }
        }
        gui.sentenceEditor.onSyntaxAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {

                    }

                    EventStatus.SUCCESS -> {
                        this.updateAsmTree(event.asm)
                    }
                }
            }
        }
    }

    fun openLanguageDirectory() {

    }

    fun loadExample(eg: Example) {
        //doUpdates = false
        gui.grammarEditor.text = eg.grammar ?: ""
        gui.styleEditor.text = eg.style ?: ""
        gui.typesEditor.text = eg.types ?: ""
        gui.asmTransEditor.text = eg.asmTransform ?: ""
        gui.referencesEditor.text = eg.references ?: ""
        gui.formatEditor.text = eg.format ?: ""

        gui.sentenceEditor.processOptions().semanticAnalysis.sentenceContext = eg.context?.let { ExternalContextLanguage.processor.process(it).asm }
        gui.sentenceEditor.languageDefinition.update(
            GrammarString(eg.grammar ?: ""),
            TypesString(eg.types?: ""),
            AsmTransformString(eg.asmTransform ?: ""),
            CrossReferenceString(eg.references?: ""),
            StyleString(eg.style?: ""),
            FormatString(eg.format ?: ""),
        )
        //doUpdates = true
        gui.sentenceEditor.text = eg.sentence ?: ""
    }

    fun updateTypesTree(typesDomain: TypesDomain?) {
        when (typesDomain) {
            null -> {
                gui.stateHolder.languageMode.typesTreeStateHolder.updateItems(emptyList())
            }

            else -> {
                val items = typesDomain.namespace.map { ns ->
                    TreeViewNode(ns.qualifiedName.value).apply {
                        hasChildren = ns.ownedTypes.isNotEmpty()
                        fetchChildren = {
                            ns.ownedTypes.map { ot ->
                                val label = when {
                                    ot.supertypes.isEmpty() -> ot.name.value
                                    else -> ot.name.value + ot.supertypes.joinToString(prefix = ": ", separator = ", ") { it.typeName.value }
                                }
                                TreeViewNode(ot.name.value).apply {
                                    content = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    hasChildren = ot.property.isNotEmpty()
                                    fetchChildren = {
                                        ot.property.map { prp ->
                                            TreeViewNode(prp.name.value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                gui.stateHolder.languageMode.typesTreeStateHolder.updateItems(items)
            }
        }
    }

    fun updatesFromGrammar(grammars: GrammarDomain?) {
        when (grammars) {
            null -> {
                gui.styleEditor.processOptions().semanticAnalysis.sentenceContext = null
                gui.referencesEditor.processOptions().semanticAnalysis.sentenceContext = null
                gui.formatEditor.processOptions().semanticAnalysis.sentenceContext = null
                updateTypesTree(null)
            }

            else -> {
                val trm = AsmTransformDomainDefault.fromGrammarDomain(grammars).let { it.asm!! }
                trm.typesDomain?.let { td ->
                    updateTypesTree(td)
                    val ctx = contextFromTypesDomain(td)
                    gui.styleEditor.processOptions().semanticAnalysis.sentenceContext = contextFromGrammar(grammars)
                    gui.referencesEditor.processOptions().semanticAnalysis.sentenceContext = ctx
                    gui.formatEditor.processOptions().semanticAnalysis.sentenceContext = ctx
                }
            }
        }
    }

    fun updateParseTree(tree: Any?) {
        when {
            tree is SharedPackedParseTree -> CoroutineScope(Dispatchers.Default).async {
                fun treeNode(id: String, label: String): TreeViewNode = TreeViewNode(id).apply {
                    content = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }

                val root = tree.treeData.root?.let { treeNode(it.rule.tag, it.rule.tag) }
                root?.let {
                    val sentence = SentenceDefault(gui.grammarEditor.text, gui.sentenceEditor.processOptions().parse.sentenceIdentity.invoke())
                    val stack = mutableStackOf(root)
                    val walker = object : SpptWalker {
                        override fun skip(skipData: TreeData) {
                            val skipRoot = skipData.root
                            skipRoot?.let {
                                val startPosition = skipRoot.startPosition
                                val nextInputPosition = skipRoot.nextInputPosition
                                val parent = stack.peek()
                                val matchedText = sentence.text.substring(startPosition, nextInputPosition)
                                val nd = treeNode(skipRoot.rule.tag, skipRoot.rule.tag + " '$matchedText'")
                                parent.children.update { it + nd }
                            }
                        }

                        override fun beginTree() {}

                        override fun endTree() {}

                        override fun leaf(nodeInfo: SpptDataNodeInfo) {
                            val parent = stack.peek()
                            val matchedText = sentence.matchedTextNoSkip(nodeInfo.node)//  substring(nodeInfo.node.startPosition, nodeInfo.node.nextInputNoSkip)
                            val nd = treeNode(nodeInfo.node.rule.tag, nodeInfo.node.rule.tag + " '$matchedText'")
                            if (nodeInfo.alt.index == 0) {
                                parent.children.update { it + nd }
                            } else {
                                //TODO:
                            }
                        }

                        override fun beginBranch(nodeInfo: SpptDataNodeInfo) {
                            stack.push(treeNode(nodeInfo.node.rule.tag, nodeInfo.node.rule.tag))
                        }

                        override fun endBranch(nodeInfo: SpptDataNodeInfo) {
                            val node = stack.pop()
                            val parent = stack.peek()
                            if (nodeInfo.alt.index == 0) {
                                parent.children.update { it + node }
                            } else {
                                //TODO:
                            }
                        }

                        override fun beginEmbedded(nodeInfo: SpptDataNodeInfo) {
                            this.beginBranch(nodeInfo)
                        }

                        override fun endEmbedded(nodeInfo: SpptDataNodeInfo) {
                            this.endBranch(nodeInfo)
                        }

                        override fun treeError(msg: String, path: PathFunction) {
                            val parent = stack.peek()
                            val node = treeNode("ERROR", msg)
                            parent.children.update { it + node }
                        }
                    }

                    tree.traverseTreeDepthFirst(walker, true)

                    val items = tree.treeData.root?.let { listOf(root) } ?: emptyList()

                    gui.stateHolder.sentenceMode.parseTreeState.updateItems(items)
                } ?: gui.stateHolder.sentenceMode.parseTreeState.updateItems(emptyList())

            }

            else -> gui.stateHolder.sentenceMode.parseTreeState.updateItems(emptyList())
        }
    }

    fun updateAsmTree(asm: Any?) {
        when {
            asm is Asm -> {
                fun treeNode(label: String, asm: AsmValue): TreeViewNode = when (asm) {
                    is AsmNothing -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("nothing")).apply { content = { Text(text = $$"$$label = $nothing") } }
                    is AsmPrimitive -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("primitive")).apply { content = { Text(text = "$label  = ${asm.asString()}") } }
                    is AsmStructure -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("structure")).apply {
                        content = { Text(text = label) }
                        hasChildren = asm.property.isNotEmpty()
                        fetchChildren = {
                            asm.property.map { (k, v) ->
                                if (v.isReference) {
                                    TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("ref")).apply { content = { Text(text = "$label = ${v.value}") } }
                                } else {
                                    treeNode("${k.value}: ${v.value.typeName.value}", v.value)
                                }
                            }
                        }
                    }

                    is AsmCollection -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("col")).apply {
                        content = { Text(text = label) }
                        hasChildren = asm.elements.isNotEmpty()
                        fetchChildren = {
                            asm.elements.map { treeNode(" :${it.typeName.value}", it) }
                        }
                    }

                    else -> TreeViewNode("TODO :${asm.typeName.value} ${asm::class.simpleName}").apply {
                        content = { Text(text = label) }
                    }
                }

                val items = asm.root.map { treeNode(" :${it.typeName.value}", it) }
                gui.stateHolder.sentenceMode.asmTreeState.updateItems(items)
            }

            else -> gui.stateHolder.sentenceMode.asmTreeState.updateItems(emptyList())
        }
    }

}