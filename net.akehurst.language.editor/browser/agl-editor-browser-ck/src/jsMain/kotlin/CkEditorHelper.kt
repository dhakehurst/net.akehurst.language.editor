package net.akehurst.language.editor.browser.ck

import js.iterable
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.common.objectJSTyped

object CkEditorHelper {

    //FIXME: currently using CK.plugin, need to make own colors so we can clear them without affecting user colors
    const val ATTRIBUTE_NAME_STYLE_FONT_FORE_COLOUR = "fontColor"
    const val ATTRIBUTE_NAME_STYLE_FONT_BACK_COLOUR = "fontBackgroundColor"

//    const val ATTRIBUTE_NAME_STYLE_FONT_FORE_COLOUR = "agl_fore_colour"
//    const val ATTRIBUTE_NAME_STYLE_FONT_BACK_COLOUR = "agl_back_color"
    const val ATTRIBUTE_NAME_STYLE_BOLD = "agl_bold"
    const val ATTRIBUTE_NAME_STYLE_ITALIC = "agl_italic"
    const val ATTRIBUTE_NAME_STYLE_UNDERLINE = "agl_underline"
    val ATTRIBUTE_SET_SYNTAX_STYLE = setOf(ATTRIBUTE_NAME_STYLE_FONT_FORE_COLOUR, ATTRIBUTE_NAME_STYLE_FONT_BACK_COLOUR,
        ATTRIBUTE_NAME_STYLE_BOLD, ATTRIBUTE_NAME_STYLE_ITALIC, ATTRIBUTE_NAME_STYLE_UNDERLINE)

    const val ATTRIBUTE_NAME_ERROR_MARKER = "agl_error"
    const val ATTRIBUTE_NAME_WARN_MARKER = "agl_warn"
    const val ATTRIBUTE_NAME_INFO_MARKER = "agl_info"
    val ATTRIBUTE_SET_ISSUE_MARKERS = setOf(ATTRIBUTE_NAME_ERROR_MARKER, ATTRIBUTE_NAME_WARN_MARKER, ATTRIBUTE_NAME_INFO_MARKER)

    /**
     * create own styles so we can remove them, leaving user styles
     */
    fun createAglAttributes(ckEditor: ck.Editor) {
        //create style for foreground colour //TODO
        //create style for background colour //TODO


        //create style for bold
        // TODO: maybe support font-weight by number as ck.Bold does - see CK code
        val boldViewStyle = objectJS {}
        boldViewStyle["font-style"] = "bold"
        attributeToElement(ckEditor, ATTRIBUTE_NAME_STYLE_BOLD,objectJS {
            model = ATTRIBUTE_NAME_STYLE_BOLD
            view = "strong"
            classes = ATTRIBUTE_NAME_STYLE_BOLD
            styles = boldViewStyle
        })

        //create style for italic
        val italicViewStyle = objectJS {}
        italicViewStyle["font-style"] = "italic"
        attributeToElement(ckEditor, ATTRIBUTE_NAME_STYLE_ITALIC,objectJS {
            model = ATTRIBUTE_NAME_STYLE_ITALIC
            view = "i"
            classes = ATTRIBUTE_NAME_STYLE_ITALIC
            styles = underlineViewStyle
        })

        //create style for underline
        val underlineViewStyle = objectJS {}
        underlineViewStyle["text-decoration-line"] = "underline"
        attributeToElement(ckEditor, ATTRIBUTE_NAME_STYLE_UNDERLINE,objectJS {
            model = ATTRIBUTE_NAME_STYLE_UNDERLINE
            view = "u"
            classes = ATTRIBUTE_NAME_STYLE_UNDERLINE
            styles = underlineViewStyle
        })

        // create style for underlining errors
        val errorMarkerViewStyle = objectJS { }
        errorMarkerViewStyle["text-decoration-line"] = "underline"
        errorMarkerViewStyle["text-decoration-style"] = "wavy"
        errorMarkerViewStyle["text-decoration-color"] = "red"
        attributeToElement(ckEditor, ATTRIBUTE_NAME_ERROR_MARKER, objectJS {
            model = ATTRIBUTE_NAME_ERROR_MARKER
            view = objectJS {
                name = "span"
                classes = ATTRIBUTE_NAME_ERROR_MARKER
                styles = errorMarkerViewStyle
            }
        })

        // create style for underlining warn
        val warnMarkerViewStyle = objectJS { }
        warnMarkerViewStyle["text-decoration-line"] = "underline"
        warnMarkerViewStyle["text-decoration-style"] = "wavy"
        warnMarkerViewStyle["text-decoration-color"] = "yellow"
        attributeToElement(ckEditor, ATTRIBUTE_NAME_WARN_MARKER, objectJS {
            model = ATTRIBUTE_NAME_WARN_MARKER
            view = objectJS {
                name = "span"
                classes = ATTRIBUTE_NAME_WARN_MARKER
                styles = warnMarkerViewStyle
            }
        })
    }

    fun attributeToElement(ckEditor: ck.Editor, attributeName:String, a2e:dynamic) {
        ckEditor.model.schema.extend("\$text", objectJSTyped { allowAttributes = attributeName })
        ckEditor.model.schema.setAttributeProperties(attributeName, objectJS {
            isFormatting = true
            isAgl = true
        })
        ckEditor.conversion.attributeToElement(a2e)
    }

    fun getFormattingAttributeNames(item: ck.Item, schema: ck.Schema): List<String> {
        val result = mutableListOf<String>()
        for (att in item.getAttributes().iterable()) {
            val attName = att[0] as String
            val props = schema.getAttributeProperties(attName)
            if (props.isFormatting) {
                result.add(attName)
            }
        }
        return result
    }

    fun addAttributes(logger: AglEditorLogger, model:ck.Model, newAttributes: List<CkAttributeData>, allAttributeNames:Set<String>) {
        model.enqueueChange { writer ->
            try {
                removeAttributes(writer, allAttributeNames)
                for (tok in newAttributes) {
                    val rng = writer.createRange(tok.firstPosition, tok.lastPosition)
                    for (att in tok.attributes) {
                        logger.log(LogLevel.Trace, "Set '${att.key}' = '${att.value}' for [${rng.start.path} - ${rng.end.path}]", null)
                        writer.setAttribute(att.key, att.value, rng)
                    }
                }
            } catch (t: Throwable) {
                logger.logError("exception during addAttributes: ",t)
            }
        }
    }

    fun removeAttributes(writer: ck.Writer, attributeNames:Set<String>) {
        val rootRange = writer.model.createRangeIn(writer.model.document.getRoot())
        val items = rootRange.getItems().iterable()
        for (item in items) {
            //TODO: maybe use getAttributesWithProperty( node, propertyName, propertyValue )
            for (attributeName in CkEditorHelper.getFormattingAttributeNames(item, writer.model.schema)) {
                if (attributeNames.contains(attributeName)) {
                    writer.removeAttribute(attributeName, item)
                }
            }
        }
    }

}