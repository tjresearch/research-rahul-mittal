package us.tlatoani.comboprover.better_parser

import us.tlatoani.comboprover.*
import java.lang.IllegalArgumentException

data class ParseContext(val from: Int, val to: Int)

data class ParsedStatement(val statement: Statement, val context: ParseContext): Statement()
data class ParsedQuantity(val quantity: Quantity, val context: ParseContext): Quantity()
data class ParsedMathObject(val mathObject: MathObject, val context: ParseContext): MathObject()
data class ParsedIntent(val intent: Intent, val context: ParseContext): Intent()

fun <S> parsed(s: S, context: ParseContext): S = when (s) {
    is Statement -> ParsedStatement(s, context) as S
    is Quantity -> ParsedQuantity(s, context) as S
    is MathObject -> ParsedMathObject(s, context) as S
    is Intent -> ParsedIntent(s, context) as S
    else -> throw IllegalArgumentException("s must be MathThing or Intent, but is instead $s")
}