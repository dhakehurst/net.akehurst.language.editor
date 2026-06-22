// from [https://discuss.kotlinlang.org/t/how-to-use-javascript-iterator-protocol-from-kotlin/13107]
package js

/**
 * JavaScript Iterator Protocol (TM :))
 */
external interface JsIterator<T> {
    fun next() : JsIteratorResult<T>
}
external class JsIteratorResult<T> {
    val done : Boolean
    val value : T?
}

fun <T> JsIterator<T>.iterable() : Iterable<T> {
    return object : Iterable<T> {
        override fun iterator(): Iterator<T> =
            object : Iterator<T> {
                private var elem = this@iterable.next()
                override fun hasNext() = !elem.done
                override fun next(): T {
                    val ret = elem.value ?: error("No more values")
                    elem = this@iterable.next()
                    return ret
                }
            }
    }
}