import org.w3c.dom.Element

external class ResizeObserver(callback:(Array<dynamic>) -> Unit) {
    fun observe(target:Element)
}