@file:JsModule("@ckeditor/ckeditor5-utils")
@file:JsNonModule
package ck.utils.dom

import org.w3c.dom.HTMLElement

external class Rect(element:HTMLElement) {
    companion object {
        @JsStatic
        fun getDomRangeRects(range:org.w3c.dom.Range): Array<Rect>
    }

    /**
     * The "top" value of the rect.
     *
     * @readonly
     */
    var top: Number

    /**
     * The "right" value of the rect.
     *
     * @readonly
     */
    var right: Number

    /**
     * The "bottom" value of the rect.
     *
     * @readonly
     */
    var bottom: Number

    /**
     * The "left" value of the rect.
     *
     * @readonly
     */
    var left: Number

    /**
     * The "width" value of the rect.
     *
     * @readonly
     */
    var width: Number

    /**
     * The "height" value of the rect.
     *
     * @readonly
     */
    var height: Number

    fun contains(other:Rect):Boolean
}

external interface Options {

    /**
     * Element that is to be positioned.
     */
    var element: HTMLElement

    /**
     * Target with respect to which the `element` is to be positioned.
     */
    var target: dynamic //RectSource | ( () => RectSource );

    /**
     * An array of positioning functions.
     *
     * **Note**: Positioning functions are processed in the order of preference. The first function that works
     * in the current environment (e.g. offers the complete fit in the viewport geometry) will be picked by
     * `getOptimalPosition()`.
     *
     * **Note**: Any positioning function returning `null` is ignored.
     */
    var positions: Array<(elementRect : ck.utils.dom.Rect,targetRect : ck.utils.dom.Rect, viewportRect : ck.utils.dom.Rect,limiterRect:Array<ck.utils.dom.Rect>) -> PositioningFunctionResult?>

    /**
     * When set, the algorithm will chose position which fits the most in the
     * limiter's bounding rect.
     */
    var limiter: dynamic? //RectSource | ( () => ( RectSource | null ) ) | null;

    /**
     * When set, the algorithm will choose such a position which fits `element`
     * the most inside visible viewport.
     */
    var fitInViewport: Boolean?

    /**
     * Viewport offset config object. It restricts the visible viewport available to the `getOptimalPosition()` from each side.
     *
     * ```ts
     * {
     * 	top: 50,
     * 	right: 50,
     * 	bottom: 50,
     * 	left: 50
     * }
     * ```
     */
    var viewportOffsetConfig: dynamic? //TODO
}


external interface PositioningFunctionResult {
    /**
     * The `top` value of the element rect that would represent the position.
     */
    var top: Number

    /**
     * The `left` value of the element rect that would represent the position.
     */
    var left: Number

    /**
     * The name of the position. It helps the user of the {@link module:utils/dom/position~getOptimalPosition}
     * helper to recognize different positioning function results. It will pass through to the {@link module:utils/dom/position~DomPoint}
     * returned by the helper.
     */
    var name: String?

    /**
     * An optional configuration that will pass-through the {@link module:utils/dom/position~getOptimalPosition} helper
     * to the {@link module:utils/dom/position~DomPoint} returned by this helper.
     * This configuration may, for instance, let the user of {@link module:utils/dom/position~getOptimalPosition} know that this particular
     * position comes with a certain presentation.
     */
    var config: Any?
}

external interface DomEmitter {

}