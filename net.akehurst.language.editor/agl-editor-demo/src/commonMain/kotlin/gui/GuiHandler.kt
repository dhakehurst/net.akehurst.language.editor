package net.akehurst.language.editor.demo.gui

import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import net.akehurst.kotlin.compose.components.tree.TreeViewNode
import net.akehurst.kotlinx.collections.mutableStackOf
import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.kotlinx.logging.api.logger
import net.akehurst.kotlinx.utils.UniqueIdentityGenerator
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.semanticAnalyser.contextFromTypesDomain
import net.akehurst.language.agl.simple.contextAsmSimple
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
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.EventStatus
import net.akehurst.language.editor.demo.information.Example
import net.akehurst.language.editor.demo.information.ExternalContextLanguage
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

    // the current parts of the user's language definition, null means not given (so use the default)
    private var langGrammar: String? = null
    private var langTypes: String? = null
    private var langAsmTransform: String? = null
    private var langReferences: String? = null
    private var langStyle: String? = null
    private var langFormat: String? = null

    private fun String?.blankAsNull(): String? = this?.takeIf { it.isNotBlank() }

    /**
     * Update the sentence language from the current parts of the language definition.
     * A blank part is passed as null.
     * [LanguageDefinition.update] keeps the old value for a null argument, so when a part has become null,
     * the configuration is reset first.
     */
    private fun updateSentenceLanguage() {
        val langDef = gui.sentenceEditor.languageDefinition
        val partRemoved = (null == langGrammar && null != langDef.grammarString)
                || (null == langTypes && null != langDef.typesString)
                || (null == langAsmTransform && null != langDef.asmTransformString)
                || (null == langReferences && null != langDef.crossReferenceString)
                || (null == langStyle && null != langDef.styleString)
                || (null == langFormat && null != langDef.formatString)
        if (partRemoved) {
            langDef.configuration = Agl.configurationSimple()
        }
        langDef.update(
            grammarString = langGrammar?.let { GrammarString(it) },
            typesString = langTypes?.let { TypesString(it) },
            asmTransformString = langAsmTransform?.let { AsmTransformString(it) },
            crossReferenceString = langReferences?.let { CrossReferenceString(it) },
            styleString = langStyle?.let { StyleString(it) },
            formatString = langFormat?.let { FormatString(it) },
        )
    }

    /**
     * When the definition in [editor] is analysed, set the part of the sentence language to the editor text (or null on failure)
     * and update the sentence language.
     */
    private fun connectLanguageEditor(editor: AglEditor<*, *>, partName: String, onResult: (asm: Any?) -> Unit = {}, setPart: (String?) -> Unit) {
        editor.onSemanticAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> {
                        LOGGER.logError { editor.endPointIdentity.editorId + ": " + event.message }
                        onResult(null)
                        setPart(null)
                        updateSentenceLanguage()
                    }

                    EventStatus.SUCCESS -> {
                        try {
                            LOGGER.logDebug { "Debug: $partName SemanticAnalysis success, updating sentence language" }
                            onResult(event.asm)
                            setPart(editor.text.blankAsNull())
                            updateSentenceLanguage()
                        } catch (t: Throwable) {
                            LOGGER.log(LogLevel.Error, t) { editor.endPointIdentity.editorId + ": " + t.message }
                            setPart(null)
                            updateSentenceLanguage()
                        }
                    }
                }
            }
        }
    }

    fun connectEditors() {
        connectLanguageEditor(gui.grammarEditor, "Grammar", onResult = { asm -> this.updatesFromGrammar(asm as GrammarDomain?) }) { langGrammar = it }
        connectLanguageEditor(gui.typesEditor, "Types") { langTypes = it }
        connectLanguageEditor(gui.asmTransEditor, "AsmTransform") { langAsmTransform = it }
        connectLanguageEditor(gui.referencesEditor, "CrossReferences") { langReferences = it }
        connectLanguageEditor(gui.styleEditor, "Style") { langStyle = it }
        connectLanguageEditor(gui.formatEditor, "Format") { langFormat = it }

        gui.sentenceEditor.onParse { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> this.updateParseTree(null)
                    EventStatus.SUCCESS -> this.updateParseTree(event.tree)
                }
            }
        }
        gui.sentenceEditor.onSyntaxAnalysis { event ->
            if (doUpdates) {
                when (event.status) {
                    EventStatus.START -> Unit
                    EventStatus.IGNORED -> Unit
                    EventStatus.FAILURE -> this.updateAsmTree(null)
                    EventStatus.SUCCESS -> this.updateAsmTree(event.asm)
                }
            }
        }
    }

    fun openLanguageDirectory() {

    }

    fun loadExample(eg: Example) {
        langGrammar = eg.grammar.blankAsNull()
        langTypes = eg.types.blankAsNull()
        langAsmTransform = eg.asmTransform.blankAsNull()
        langReferences = eg.references.blankAsNull()
        langStyle = eg.style.blankAsNull()
        langFormat = eg.format.blankAsNull()

        gui.sentenceEditor.processOptions().semanticAnalysis.sentenceContext =
            eg.context.blankAsNull()?.let { ExternalContextLanguage.processor.process(it).asm } ?: contextAsmSimple()
        gui.sentenceEditor.languageDefinition.configuration = Agl.configurationSimple()
        updateSentenceLanguage()

        gui.grammarEditor.text = eg.grammar ?: ""
        gui.styleEditor.text = eg.style ?: ""
        gui.typesEditor.text = eg.types ?: ""
        gui.asmTransEditor.text = eg.asmTransform ?: ""
        gui.referencesEditor.text = eg.references ?: ""
        gui.formatEditor.text = eg.format ?: ""
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
        when (tree) {
            is SharedPackedParseTree -> {
                // sibling nodes in a TreeView need distinct ids
                var nextId = 0
                fun treeNode(tag: String, label: String): TreeViewNode = TreeViewNode("$tag#${nextId++}").apply {
                    content = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }

                val sentence = SentenceDefault(gui.sentenceEditor.text, gui.sentenceEditor.processOptions().parse.sentenceIdentity.invoke())
                val top = treeNode("<top>", "<top>")
                val stack = mutableStackOf(top)
                val walker = object : SpptWalker {
                    override fun skip(skipData: TreeData) {
                        val skipRoot = skipData.root
                        skipRoot?.let {
                            val parent = stack.peek()
                            val matchedText = sentence.text.substring(skipRoot.startPosition, skipRoot.nextInputPosition)
                            val nd = treeNode(skipRoot.rule.tag, skipRoot.rule.tag + " '$matchedText'")
                            parent.children.update { it + nd }
                        }
                    }

                    override fun beginTree() {}

                    override fun endTree() {}

                    override fun leaf(nodeInfo: SpptDataNodeInfo) {
                        val parent = stack.peek()
                        val matchedText = sentence.matchedTextNoSkip(nodeInfo.node)
                        val nd = treeNode(nodeInfo.node.rule.tag, nodeInfo.node.rule.tag + " '$matchedText'")
                        if (nodeInfo.alt.index == 0) {
                            parent.children.update { it + nd }
                        } else {
                            //TODO: show ambiguous alternatives
                        }
                    }

                    override fun beginBranch(nodeInfo: SpptDataNodeInfo) {
                        stack.push(treeNode(nodeInfo.node.rule.tag, nodeInfo.node.rule.tag))
                    }

                    override fun endBranch(nodeInfo: SpptDataNodeInfo) {
                        val node = stack.pop()
                        node.hasChildren = node.children.value.isNotEmpty()
                        val parent = stack.peek()
                        if (nodeInfo.alt.index == 0) {
                            parent.children.update { it + node }
                        } else {
                            //TODO: show ambiguous alternatives
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
                gui.stateHolder.sentenceMode.parseTreeState.updateItems(top.children.value)
            }

            else -> gui.stateHolder.sentenceMode.parseTreeState.updateItems(emptyList())
        }
    }

    fun updateAsmTree(asm: Any?) {
        when {
            asm is Asm -> {
                val items = asm.root.map {
                    when (it) {
                        is AsmStructure -> asmTreeNode(" :${it.typeName.value}", it)
                        else -> asmTreeNode(": ${it::class.simpleName}", it)
                    }
                }
                gui.stateHolder.sentenceMode.asmTreeState.updateItems(items)
            }

            else -> gui.stateHolder.sentenceMode.asmTreeState.updateItems(emptyList())
        }
    }

    private fun asmTreeNode(label: String, asm: Any): TreeViewNode = when (asm) {
        is AsmNothing -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("nothing")).apply { content = { Text(text = $$"$$label = $nothing") } }
        is AsmPrimitive -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("primitive")).apply { content = { Text(text = "$label  = ${asm.asString()}") } }
        is AsmStructure -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("structure")).apply {
            content = { Text(text = label) }
            hasChildren = asm.property.isNotEmpty()
            fetchChildren = {
                asm.property.map { (k, v) ->
                    if (v.isReference) {
                        TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("ref")).apply { content = { Text(text = "${k.value} = ${v.value}") } }
                    } else {
                        val value = v.value
                        when (value) {
                            is AsmStructure -> asmTreeNode("${k.value}: ${value.typeName.value}", v.value)
                            else -> asmTreeNode("${k.value}: ${value::class.simpleName}", v.value)
                        }
                    }
                }
            }
        }

        is AsmCollection -> asmCollectionNode(label, asm.elements)

        // AsmSimple values may also be plain Kotlin collections and primitive values
        is Collection<*> -> asmCollectionNode(label, asm.filterNotNull())

        else -> TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("value")).apply {
            content = { Text(text = "$label = $asm") }
        }
    }

    private fun asmCollectionNode(label: String, elements: Collection<Any>): TreeViewNode = TreeViewNode(UniqueIdentityGenerator.GLOBAL.generate("col")).apply {
        content = { Text(text = "$label [${elements.size}]") }
        hasChildren = elements.isNotEmpty()
        fetchChildren = {
            elements.map {
                when (it) {
                    is AsmStructure -> asmTreeNode(" :${it.typeName.value}", it)
                    else -> asmTreeNode(": ${it::class.simpleName}", it)
                }
            }
        }
    }
}
