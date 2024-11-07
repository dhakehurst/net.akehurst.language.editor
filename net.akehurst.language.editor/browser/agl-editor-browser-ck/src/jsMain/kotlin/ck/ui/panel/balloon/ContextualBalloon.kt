@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.panel.balloon

import org.w3c.dom.HTMLElement

external interface ViewConfiguration {
    /**
     * The ID of the stack that the view is added to.
     *
     * @default 'main'
     */
    var stackId: String?

    /**
     * The content of the balloon.
     */
    var view: ck.ui.View<HTMLElement>

    /**
     * Positioning options.
     */
    var position: dynamic? //Partial<PositionOptions>?

    /**
     * An additional CSS class added to the {@link #view balloon} when visible.
     */
    var balloonClassName: String?

    /**
     * Whether the {@link #view balloon} should be rendered with an arrow.
     *
     * @default true
     */
    var withArrow: Boolean?

    /**
     * Whether the view should be the only visible view even if other stacks were added.
     *
     * @default false
     */
    var singleViewMode: Boolean?
}

external class ContextualBalloon {
    companion object {
        fun pluginName(): String = definedExternally
    }

    var visibleView: ck.ui.View<HTMLElement>

    fun add(data:ViewConfiguration)
}