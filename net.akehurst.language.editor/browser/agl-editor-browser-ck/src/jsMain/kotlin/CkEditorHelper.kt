package net.akehurst.language.editor.browser.ck

import js.iterable
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.LogLevel

object CkEditorHelper {

    const val ERROR_MARKER_ATTRIBUTE_NAME = "agl_error"
    const val WARN_MARKER_ATTRIBUTE_NAME = "agl_warn"
    const val INFO_MARKER_ATTRIBUTE_NAME = "agl_info"


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