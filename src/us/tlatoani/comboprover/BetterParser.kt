package us.tlatoani.comboprover

import java.lang.IllegalArgumentException
import java.util.*

data class ParseContext(val from: Int, val to: Int)
fun <S: MathThing> parsed(s: S, context: ParseContext): S = when (s) {
    is Statement -> ParsedStatement(s, context) as S
    is Quantity -> ParsedQuantity(s, context) as S
    is MathObject -> ParsedMathObject(s, context) as S
    else -> throw IllegalArgumentException("wtf")
}

data class TemplateParameters(val statements: Map<Int, Statement>, val quantities: Map<Int, Quantity>, val objects: Map<Int, MathObject>) {
    fun st(id: Int) = statements[id]!!
    fun qq(id: Int) = quantities[id]!!
    fun xn(id: Int) = objects[id]!!

    operator fun plus(other: TemplateParameters) =
        TemplateParameters(statements + other.statements, quantities + other.quantities, objects + other.objects)

    fun with(thing: MathThing, id: Int) = when (thing) {
        is Statement -> copy(statements = statements.plus(id to thing))
        is Quantity -> copy(quantities = quantities.plus(id to thing))
        is MathObject -> copy(objects = objects.plus(id to thing))
    }
}

val EMPTY_PARAMETERS = TemplateParameters(emptyMap(), emptyMap(), emptyMap())

enum class ParameterType { STATEMENT, QUANTITY, MATH_OBJECT }

fun abbreviationToType(abbreviation: String) = Debug("abbr = $abbreviation") out when (abbreviation.toUpperCase()) {
    "ST" -> ParameterType.STATEMENT
    "QQ" -> ParameterType.QUANTITY
    "XN" -> ParameterType.MATH_OBJECT
    else -> throw IllegalArgumentException("bad")
}

sealed class SyntaxElement
data class Token(val token: String): SyntaxElement()
data class SyntaxParameter(val type: ParameterType, val id: Int): SyntaxElement()

class SyntaxTemplate<S>(val syntax: List<SyntaxElement>, val constructor: (TemplateParameters) -> S)

fun <S> template(syntax: String, constructor: (TemplateParameters) -> S) = SyntaxTemplate(parseSyntax(syntax), constructor)

fun parseSyntax(syntax: String) = syntax.split(" ").map { w ->
    if (w.startsWith("%") && w.endsWith("%"))
        SyntaxParameter(abbreviationToType(w.substring(1, 3)), w.substring(3, w.length - 1).toInt())
    else
        Token(w)
}

val STATEMENT_TEMPLATES = listOf<SyntaxTemplate<Statement>>(
    template("we can choose %xn1% in %qq1% ways") { Equals(Amount(Choice(it.xn(1))), it.qq(1)) },
    template("%xn1% can be matched up with %xn2% in %qq1% ways") { Equals(Amount(Matching(it.xn(1), it.xn(2))), it.qq(1)) },
    template("%qq1% is %qq2%") { Equals(it.qq(1), it.qq(2)) },
    template("each %xn1% contains %xn2%") { Each(it.xn(1), Contains(it.xn(2))) }
)
val QUANTITY_TEMPLATES = listOf<SyntaxTemplate<Quantity>>(
    template("number of %xn1%") { Amount(it.xn(1)) },
    template("amount of %xn1%") { Amount(it.xn(1)) },
    template("amount") { HangingAmount(Unit) },
    template("at most %qq1%") { Bounded(Infinity(-1), it.qq(1)) },
    template("at least %qq1%") { Bounded(it.qq(1), Infinity(1)) }
)
val MATH_OBJECT_TEMPLATES = listOf<SyntaxTemplate<MathObject>>(
    template("%qq1% %xn1%") { Multiple(it.qq(1), it.xn(1)) },
    template("%xn1% which will contain %xn2%") { SubjectToCondition(it.xn(1), Contains(it.xn(2))) },
    template("labelled %xn1%") { LabelledMathObject(it.xn(1)) },
    template("ways to place %xn1% into %xn2%") { Placement(it.xn(1), it.xn(2)) }
)

val ARBITRARY_MATH_OBJECT_WORDS = listOf("ball", "balls", "urn", "urns")

data class ParseResult<out S>(val unusedWords: Int, val s: S)
data class ParsePosition<S>(
    val template: SyntaxTemplate<S>,
    val inSyntax: Int,
    val inTokens: Int,
    val unusedWords: Int,
    val parameters: TemplateParameters
)

fun parseStatement(tokens: List<String>): Statement? {
    val n = tokens.size
    val dpStatements = Array(n) { Array<ParseResult<Statement>?>(n + 1) { null } }
    val dpQuantities = Array(n) { Array<ParseResult<Quantity>?>(n + 1) { null } }
    val dpMathObjects = Array(n) { Array<ParseResult<MathObject>?>(n + 1) { null } }

    var answer: ParseResult<Statement>? = null

    for (x in n - 1 downTo 0) {
        for (y in x + 1..n) {
            // formula statement
            if (y - x == 3) {
                if (tokens[x].length == 1 && tokens[x] != "a" && tokens[x] != "i" && tokens[x + 1] == "is" && tokens[x + 2] == "true") {
                    dpStatements[x][y] = ParseResult(0, FormulaStatement(tokens[x]))
                }
            }
            // formula
            if (y - x == 1) {
                if (tokens[x].length == 1 && tokens[x] != "a" && tokens[x] != "i") {
                    dpQuantities[x][y] = ParseResult(0, Formula(tokens[x]))
                }
            }
            // arbitrary math object
            if (y - x == 1) {
                if (ARBITRARY_MATH_OBJECT_WORDS.contains(tokens[x])) {
                    dpMathObjects[x][y] = ParseResult(0, ArbitraryMathObject(tokens[x]))
                }
            }
            // dfs
            val stack = Stack<ParsePosition<*>>()
            for (template in STATEMENT_TEMPLATES) {
                stack.push(ParsePosition(template, 0, x, 0, EMPTY_PARAMETERS))
            }
            for (template in QUANTITY_TEMPLATES) {
                stack.push(ParsePosition(template, 0, x, 0, EMPTY_PARAMETERS))
            }
            for (template in MATH_OBJECT_TEMPLATES) {
                stack.push(ParsePosition(template, 0, x, 0, EMPTY_PARAMETERS))
            }
            while (!stack.isEmpty()) {
                val pos = stack.pop()
                if (pos.inTokens == y || pos.inSyntax == pos.template.syntax.size) {
                    if (pos.inTokens == y && pos.inSyntax == pos.template.syntax.size) {
                        val s = parsed(pos.template.constructor(pos.parameters) as MathThing, ParseContext(x, y))
                        when (s) {
                            is Statement -> if (pos.unusedWords < dpStatements[x][y]?.unusedWords ?: Int.MAX_VALUE) dpStatements[x][y] = ParseResult(pos.unusedWords, s)
                            is Quantity -> if (pos.unusedWords < dpQuantities[x][y]?.unusedWords ?: Int.MAX_VALUE) dpQuantities[x][y] = ParseResult(pos.unusedWords, s)
                            is MathObject -> if (pos.unusedWords < dpMathObjects[x][y]?.unusedWords ?: Int.MAX_VALUE) dpMathObjects[x][y] = ParseResult(pos.unusedWords, s)
                        }
                    }
                } else {
                    stack.push(pos.copy(inTokens = pos.inTokens + 1, unusedWords = pos.unusedWords + 1))
                    pos.template.syntax[pos.inSyntax].let { elem -> when (elem) {
                        is Token -> if (tokens[pos.inTokens] == elem.token) {
                            stack.push(pos.copy(inTokens = pos.inTokens + 1, inSyntax = pos.inSyntax + 1))
                        }
                        is SyntaxParameter -> for (z in pos.inTokens + 1..y) {
                            when (elem.type) {
                                ParameterType.STATEMENT -> dpStatements
                                ParameterType.QUANTITY -> dpQuantities
                                ParameterType.MATH_OBJECT -> dpMathObjects
                            }[pos.inTokens][z]?.let { stack.push(pos.copy(
                                inTokens = z,
                                inSyntax = pos.inSyntax + 1,
                                unusedWords = pos.unusedWords + it.unusedWords,
                                parameters = pos.parameters.with(it.s, elem.id))) }
                        }
                    } }
                }
            }
            dpStatements[x][y]
                ?.let { res -> ParseResult(res.unusedWords + x + n - y, res.s)}
                ?.let { res -> if (res.unusedWords < answer?.unusedWords ?: Int.MAX_VALUE) {
                    answer = res
                } }
        }
    }
    return answer?.s
}