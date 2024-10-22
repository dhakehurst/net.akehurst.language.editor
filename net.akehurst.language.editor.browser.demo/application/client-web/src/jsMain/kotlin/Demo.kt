package net.akehurst.language.editor.application.client.web

import kotlinx.browser.document
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.CrossReferenceString
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.agl.StyleString
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.agl.simple.ContextAsmSimple
import net.akehurst.language.asm.api.*
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.EventStatus
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.ConsoleLogger
import net.akehurst.language.editor.information.Example
import net.akehurst.language.editor.information.Examples
import net.akehurst.language.editor.information.ExternalContextLanguage
import net.akehurst.language.editor.information.examples.BasicTutorial
import net.akehurst.language.editor.technology.gui.widgets.TreeView
import net.akehurst.language.editor.technology.gui.widgets.TreeViewFunctions
import net.akehurst.language.grammar.api.GrammarModel
import net.akehurst.language.grammar.processor.ContextFromGrammar
import net.akehurst.language.grammarTypemodel.api.GrammarTypeNamespace
import net.akehurst.language.reference.api.CrossReferenceModel
import net.akehurst.language.style.api.AglStyleModel
import net.akehurst.language.transform.asm.TransformModelDefault
import net.akehurst.language.typemodel.api.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement

class Demo(
    val editors: Map<String, AglEditor<*, *>>,
    val logger: ConsoleLogger
) {
    var doUpdate = true
    val trees = TreeView.initialise(document)

    val exampleSelect = document.querySelector("select#example") as HTMLElement
    val sentenceEditor = editors[Constants.sentenceEditorId]!! as AglEditor<Asm, ContextAsmSimple>
    val grammarEditor = editors[Constants.grammarEditorId]!!
    val styleEditor = editors[Constants.styleEditorId]!! as AglEditor<AglStyleModel, ContextFromGrammar>
    val referencesEditor = editors[Constants.referencesEditorId]!! as AglEditor<CrossReferenceModel, ContextFromTypeModelReference>
    //val formatEditor = editors["language-format"]!!

    fun configure() {
        this.connectEditors()
        this.connectTrees()
        this.configExampleSelector()
    }

    private fun connectEditors() {
        //ids should already be set when dom and editors are created
        grammarEditor.languageIdentity = Constants.grammarLanguageId
        grammarEditor.processOptions.semanticAnalysis.context = null // ensure this is null, so that Worker uses default of ContextFromGrammarRegistry
        styleEditor.languageIdentity = Constants.styleLanguageId
        referencesEditor.languageIdentity = Constants.referencesLanguageId
        //Agl.registry.unregister(Constants.sentenceLanguageId)
        sentenceEditor.languageIdentity = Constants.sentenceLanguageId
        grammarEditor.editorSpecificStyleStr = Agl.registry.agl.grammar.styleStr
        styleEditor.editorSpecificStyleStr = Agl.registry.agl.style.styleStr
        referencesEditor.editorSpecificStyleStr = Agl.registry.agl.crossReference.styleStr

        grammarEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit

                EventStatus.FAILURE -> {
                    styleEditor.processOptions.semanticAnalysis.context?.clear()
                    //referencesEditor.sentenceContext?.clear()
                    logger.logError(grammarEditor.endPointIdentity.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.grammarStr = GrammarString("")
                }

                EventStatus.SUCCESS -> {
                    logger.logDebug("Send grammarStr Semantic Analysis success")
                    val grammars = event.asm as GrammarModel? ?: error("should always be a List<Grammar> if success")
                    styleEditor.processOptions.semanticAnalysis.context = ContextFromGrammar.createContextFrom(grammars)
                    referencesEditor.processOptions.semanticAnalysis.context = ContextFromTypeModelReference(sentenceEditor.languageIdentity)
                    try {
                        if (doUpdate) {
                            logger.logDebug("Send set sentenceEditor grammarStr")
                            sentenceEditor.languageDefinition.grammarStr = GrammarString(grammarEditor.text)
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, grammarEditor.endPointIdentity.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.grammarStr = GrammarString("")
                    }
                }
            }

        }
        styleEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.FAILURE -> {
                    logger.logError(styleEditor.endPointIdentity.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.styleStr = StyleString("")
                }

                EventStatus.SUCCESS -> {
                    try {
                        logger.logDebug("Style parse success")
                        if (doUpdate) {
                            logger.logDebug("resetting sentence style")
                            sentenceEditor.languageDefinition.styleStr = StyleString(styleEditor.text)
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, styleEditor.endPointIdentity.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.styleStr = StyleString("")
                    }
                }
            }
        }
        referencesEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.FAILURE -> {
                    logger.logError(referencesEditor.endPointIdentity.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.crossReferenceModelStr = CrossReferenceString("")
                }

                EventStatus.SUCCESS -> {
                    try {
                        //sentenceScopeModel = event.asm as ScopeModel?
                        logger.logDebug("CrossReferences SyntaxAnalysis success")
                        if (doUpdate) {
                            logger.logDebug("Setting cross-reference model for sentenceEditor")
                            sentenceEditor.languageDefinition.crossReferenceModelStr = CrossReferenceString( referencesEditor.text)
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, referencesEditor.endPointIdentity.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.crossReferenceModelStr = CrossReferenceString("")
                    }
                }
            }
        }
    }

    private fun loading(parse: Boolean?, ast: Boolean?) {
        if (null != parse) trees["parse"]!!.loading = parse
        if (null != ast) trees["ast"]!!.loading = ast
    }

    private fun connectTrees() {
        trees["typemodel"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = { root, it ->
                when (it) {
                    is String -> it
                    is List<*> -> "List"
                    is TypeModel -> "model ${it.name}"
                    is GrammarTypeNamespace -> "namespace ${it.qualifiedName}"
                    is TypeNamespace -> "namespace ${it.qualifiedName}"
                    is Pair<String, TypeInstance> -> {
                        val type = it.second.declaration
                        val ruleName = it.first
                        when (type) {
                            is DataType -> when {
                                type.supertypes.isEmpty() -> "$ruleName : ${type.signature(type.namespace)}"
                                else -> "$ruleName : ${type.signature(type.namespace)} -> ${type.supertypes.joinToString { it.signature(type.namespace, 0) }}"
                            }

                            else -> "$ruleName : ${type.signature(it.second.namespace)}"
                        }
                    }

                    is Map.Entry<String, TypeDeclaration> -> {
                        val type = it.value
                        val typeName = it.key
                        when (type) {
                            is DataType -> when {
                                type.supertypes.isEmpty() -> "$typeName : ${type.signature(type.namespace)}"
                                else -> "$typeName : ${type.signature(type.namespace)} -> ${type.supertypes.joinToString { it.signature(type.namespace, 0) }}"
                            }

                            else -> "$typeName : ${type.signature(type.namespace)}"
                        }
                    }

                    is PropertyDeclaration -> "${it.name} : ${it.typeInstance.signature(it.owner.namespace, 0)}"
                    else -> error("Internal Error: type ${it::class.simpleName} not handled")
                }
            },
            hasChildren = {
                when (it) {
                    is String -> false
                    is List<*> -> true
                    is TypeModel -> it.namespace.isNotEmpty()
                    is GrammarTypeNamespace -> it.allTypesByRuleName.isNotEmpty()
                    is TypeNamespace -> it.ownedTypesByName.isNotEmpty()
                    is Pair<String, TypeInstance> -> {
                        val type = it.second.declaration
                        when (type) {
                            is TupleType -> type.property.isNotEmpty()
                            is DataType -> type.property.isNotEmpty()
                            else -> false
                        }
                    }

                    is Map.Entry<String, TypeDeclaration> -> when (it.value) {
                        is StructuredType -> (it.value as StructuredType).property.isNotEmpty()
                        else -> false
                    }

                    is PropertyDeclaration -> false
                    else -> error("Internal Error: type ${it::class.simpleName} not handled")
                }
            },
            children = {
                when (it) {
                    is String -> emptyArray<Any>()
                    is List<*> -> it.toArray()
                    is TypeModel -> it.namespace.toTypedArray()
                    is GrammarTypeNamespace -> it.allTypesByRuleName.toTypedArray()
                    is TypeNamespace -> it.ownedTypesByName.entries.toTypedArray()
                    is Pair<String, TypeInstance> -> {
                        val type = it.second.declaration
                        when (type) {
                            is StructuredType -> type.property.toTypedArray()
                            else -> emptyArray<Any>()
                        }
                    }

                    is Map.Entry<String, TypeDeclaration> -> when (it.value) {
                        is StructuredType -> (it.value as StructuredType).property.toTypedArray()
                        else -> emptyArray<Any>()
                    }

                    is PropertyDeclaration -> emptyArray<Any>()
                    else -> error("Internal Error: type ${it::class.simpleName} not handled")
                }
            }
        )
        grammarEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> trees["typemodel"]!!.loading = true
                EventStatus.FAILURE -> trees["typemodel"]!!.loading = false
                EventStatus.SUCCESS -> {
                    val gm = event.asm as GrammarModel
                    val trm = TransformModelDefault.fromGrammarModel(gm).let { it.asm!! }
                    val tm = trm.typeModel!!
                    trees["typemodel"]!!.loading = false
                    trees["typemodel"]!!.setRoots(listOf(tm))
                }
            }
        }

        trees["parse"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = { root, it ->
                when (it.isBranch) {
                    false -> "${it.name} = ${it.nonSkipMatchedText}"
                    true -> it.name
                    else -> error("error")
                }
            },
            hasChildren = { it.isBranch },
            children = { it.children }
        )

        sentenceEditor.onParse { event ->
            when (event.status) {
                EventStatus.START -> loading(true, true)
                EventStatus.FAILURE -> loading(false, false)
                EventStatus.SUCCESS -> {
                    loading(false, null)
                    trees["parse"]!!.setRoots(event.tree?.let { listOf(it) } ?: emptyList())
                }
            }
        }

        trees["ast"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = { root, it ->
                when (it) {
                    is Array<*> -> ": Array"
                    is List<*> -> ": List"
                    is Set<*> -> ": Set"
                    is AsmNothing -> "Nothing"
                    is AsmPrimitive -> "${it.value}"
                    is AsmList -> ": List"
                    is AsmListSeparated -> ": ListSeparated"
                    is AsmStructure -> ": " + it.typeName
                    is AsmStructureProperty -> {
                        val v = it.value
                        when (v) {
                            is AsmNothing -> "${it.name} = Nothing"
                            is AsmPrimitive -> "${it.name} = '${v.value}'"
                            is AsmList -> "${it.name} : List"
                            is AsmListSeparated -> "${it.name} : ListSeparated"
                            is AsmStructure -> "${it.name} : ${v.typeName}"
                            is AsmReference -> when (v.value) {
                                null -> "${it.name} = &'${v.reference}' - <unresolved reference>"
                                else -> "${it.name} = &'${v.reference}' : ${v.value?.typeName} - ${v.value?.path?.value}"
                            }
                            //it.name == "'${v}'" -> "${it.name}"
                            else -> "${it.name} = ${v}"
                        }
                    }

                    else -> it.toString()
                }
            },
            hasChildren = {
                when (it) {
                    is Array<*> -> true
                    is Collection<*> -> true
                    is AsmList -> true
                    is AsmListSeparated -> true
                    is AsmStructure -> it.property.isNotEmpty()
                    is AsmStructureProperty -> {
                        when (it.value) {
                            is AsmList -> true
                            is AsmListSeparated -> true
                            is AsmStructure -> true
                            else -> false
                        }
                    }

                    else -> false
                }
            },
            children = {
                when (it) {
                    is AsmList -> it.elements.toTypedArray()
                    is AsmListSeparated -> it.elements.toTypedArray()
                    is AsmStructure -> it.property.values.toTypedArray()
                    is AsmStructureProperty -> {
                        when (val v = it.value) {
                            is Array<*> -> v
                            is Collection<*> -> v.toTypedArray()
                            is AsmList -> v.elements.toTypedArray()
                            is AsmListSeparated -> v.elements.toTypedArray()
                            is AsmStructure -> v.property.values.toTypedArray()
                            else -> emptyArray<dynamic>()
                        }
                    }

                    else -> emptyArray<dynamic>()
                }
            }
        )

        sentenceEditor.onSyntaxAnalysis { event ->
            when (event.status) {
                EventStatus.START -> {
                    //trees["ast"]!!.loading = true
                }

                EventStatus.FAILURE -> {//Failure
                    logger.logError(event.message)
                    loading(null, false)
                    when (event.asm) {
                        is Asm -> trees["ast"]!!.setRoots((event.asm as Asm).root)
                        else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    }
                }

                EventStatus.SUCCESS -> {
                    loading(null, false)
                    when (event.asm) {
                        is Asm -> trees["ast"]!!.setRoots((event.asm as Asm).root)
                        else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    }
                }
            }
        }

        sentenceEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> {
                    //trees["ast"]!!.loading = true
                }

                EventStatus.FAILURE -> {//Failure
                    logger.logError(event.message)
                    loading(null, false)
                    //when(event.asm) {
                    //    is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                    //    else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    //}
                }

                EventStatus.SUCCESS -> {
                    loading(null, false)
                    when (event.asm) {
                        is Asm -> trees["ast"]!!.setRoots((event.asm as Asm).root)
                        else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    }
                }
            }
        }
    }

    private fun configExampleSelector() {
        exampleSelect.addEventListener("change", { _ ->
            loading(true, true)
            // delay setting stuff so that 'loading' is processed first
            val egName = js("event.target.value") as String
            val eg = Examples[egName]
//            window.setTimeout({ setExample(eg) }, 100)
            setExample(eg)
        })

        // select initial example
        loading(true, true)
        val eg = BasicTutorial.example// Examples.map["BasicTutorial"]!!
        (exampleSelect as HTMLSelectElement).value = eg.id
        setExample(eg)
    }

    fun setExample(eg: Example) {
        this.doUpdate = false
        grammarEditor.text = eg.grammar
        styleEditor.text = eg.style
        referencesEditor.text = eg.references
        //formatEditor.text = eg.format
        sentenceEditor.doUpdate = false
        sentenceEditor.processOptions.semanticAnalysis.context = ExternalContextLanguage.processor.process(eg.context).asm
        logger.log(LogLevel.Trace, "Update sentenceEditor with grammar, refs, style", null)
        sentenceEditor.languageDefinition.update(GrammarString(grammarEditor.text), CrossReferenceString(referencesEditor.text), StyleString(styleEditor.text))
        sentenceEditor.text = eg.sentence
        sentenceEditor.doUpdate = true
        this.doUpdate = true
        logger.log(LogLevel.Information, "Finished setting example", null)
    }

    fun finalize() {
        editors.values.forEach { aglEd ->
            aglEd.destroyAglEditor()
            aglEd.destroyBaseEditor()
        }
    }
}