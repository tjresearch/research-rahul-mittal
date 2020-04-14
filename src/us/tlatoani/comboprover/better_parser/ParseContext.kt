package us.tlatoani.comboprover.better_parser

import us.tlatoani.comboprover.*
import java.lang.IllegalArgumentException

data class ParseContext(val from: Int, val to: Int, val text: List<String>)

data class ParsedStatement(val statement: Statement, val parallel: Statement, val context: ParseContext): Statement()
data class ParsedQuantity(val quantity: Quantity, val parallel: Quantity, val context: ParseContext): Quantity()
data class ParsedMathObject(val mathObject: MathObject, val parallel: MathObject, val context: ParseContext): MathObject()
data class ParsedIntent(val intent: Intent, val parallel: Intent, val context: ParseContext): Intent()

val PARSED_CLASSES = listOf(ParsedStatement::class, ParsedQuantity::class, ParsedMathObject::class, ParsedIntent::class)

fun <S> parsed(s: S, p: S, context: ParseContext): S = when (s) {
    is Statement -> ParsedStatement(s, p as Statement, context) as S
    is Quantity -> ParsedQuantity(s, p as Quantity, context) as S
    is MathObject -> ParsedMathObject(s, p as MathObject, context) as S
    is Intent -> ParsedIntent(s, p as Intent, context) as S
    else -> throw IllegalArgumentException("s must be MathThing or Intent, but is instead $s")
}

fun <S> parallel(s: S): S = when (s) {
    is ParsedStatement -> s.parallel as S
    is ParsedQuantity -> s.parallel as S
    is ParsedMathObject -> s.parallel as S
    is ParsedIntent -> s.parallel as S
    else -> s
}

fun <S> unwrap(s: S): S = when (s) {
    is ParsedStatement -> s.statement as S
    is ParsedQuantity -> s.quantity as S
    is ParsedMathObject -> s.mathObject as S
    is ParsedIntent -> s.parallel as S
    else -> s
}

fun context(s: Any): ParseContext = when (s) {
    is ParsedStatement -> s.context
    is ParsedQuantity -> s.context
    is ParsedMathObject -> s.context
    is ParsedIntent -> s.context
    else -> throw IllegalArgumentException("s must be Parsed..., but is instead $s")
}