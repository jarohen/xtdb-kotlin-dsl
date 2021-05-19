package crux.api.query

import clojure.lang.Keyword
import clojure.lang.PersistentVector
import clojure.lang.Symbol
import crux.api.kw
import crux.api.pl
import crux.api.pv
import crux.api.sym

class WhereContext {
    private val clauses = mutableListOf<Any>()

    private var hangingClause: Any? = null

    data class SymbolAndKey(val symbol: Symbol, val key: Keyword)

    infix fun Symbol.has(key: Keyword) =
        SymbolAndKey(this, key).also {
            lockIn()
            hangingClause = listOf(this, key).pv
        }

    infix fun Symbol.has(key: String) = has(key.kw)
    infix fun String.has(key: Keyword) = sym.has(key)
    infix fun String.has(key: String) = has(key.kw)


    infix fun SymbolAndKey.eq(value: Any) {
        hangingClause = listOf(symbol, key, value).pv
    }

    private fun lockIn() {
        hangingClause?.run(clauses::add)
        hangingClause = null
    }

    fun build(): PersistentVector {
        lockIn()
        return clauses.pv
    }
}