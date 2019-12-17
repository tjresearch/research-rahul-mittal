package us.tlatoani.comboprover.better_parser

import us.tlatoani.comboprover.*
import java.util.*

data class ParseResult<out S>(val unusedWords: Int, val s: S)
data class ParsePosition<S>(
    val syntax: List<SyntaxElement>,
    val constructor: (TemplateParameters) -> S,
    val inTokens: Int,
    val unusedWords: Int,
    val parameters: TemplateParameters
)

fun parseStatementOrIntent(tokens: List<String>, formulae: Map<Char, Quantity>):
        Pair<ParseResult<Statement>?, ParseResult<Intent>?> {
    println("tokens = " + tokens.map { s -> "\"$s\"" }.joinToString(", ", "[", "]"))
    println("formulae = $formulae")
    val n = tokens.size
    val dpStatements = Array(n) { Array<ParseResult<Statement>?>(n + 1) { null } }
    val dpQuantities = Array(n) { Array<ParseResult<Quantity>?>(n + 1) { null } }
    val dpMathObjects = Array(n) { Array<ParseResult<MathObject>?>(n + 1) { null } }
    val dpIntents = Array(n) { Array<ParseResult<Intent>?>(n + 1) { null } }

    var statementRes: ParseResult<Statement>? = null
    var intentRes: ParseResult<Intent>? = null

    for (x in n - 1 downTo 0) {
        for (y in x + 1..n) {
            // formula statement
            if (y - x == 3) {
                if (tokens[x].length == 1 && tokens[x] != "a" && tokens[x] != "i" && tokens[x + 1] == "is" && tokens[x + 2] == "true") {
                    dpStatements[x][y] = ParseResult(0, parsed(FormulaStatement(tokens[x]), ParseContext(x, y)))
                }
            }
            // formula
            if (y - x == 1) {
                if (tokens[x].length == 1 && tokens[x] != "a" && tokens[x] != "i") {
                    dpQuantities[x][y] = ParseResult(0, parsed(formulae.getValue(tokens[x][0]), ParseContext(x, y)))
                }
            }
            // variable math object
            if (y - x == 1) {
                if (tokens[x].length == 1 && tokens[x] != "a" && tokens[x] != "i") {
                    val f = formulae.getValue(tokens[x][0])
                    if (f is Variable) {
                        dpMathObjects[x][y] = ParseResult(0, parsed(ObjectVariable(f.name), ParseContext(x, y)))
                    }
                }
            }
            // arbitrary math object
            if (y - x == 1) {
                if (ARBITRARY_MATH_OBJECT_WORDS.contains(tokens[x]) || (tokens[x].endsWith("s") && ARBITRARY_MATH_OBJECT_WORDS.contains(tokens[x].substringBeforeLast("s")))) {
                    dpMathObjects[x][y] = ParseResult(0, parsed(ArbitraryMathObject(tokens[x]), ParseContext(x, y)))
                }
            }
            // dfs
            val stack = Stack<ParsePosition<*>>()
            for (template in STATEMENT_TEMPLATES) {
                stack.push(ParsePosition(template.syntax, template.constructor, x, 0, EMPTY_PARAMETERS))
            }
            for (template in QUANTITY_TEMPLATES) {
                stack.push(ParsePosition(template.syntax, template.constructor, x, 0, EMPTY_PARAMETERS))
            }
            for (template in MATH_OBJECT_TEMPLATES) {
                stack.push(ParsePosition(template.syntax, template.constructor, x, 0, EMPTY_PARAMETERS))
            }
            for (template in INTENT_TEMPLATES) {
                stack.push(ParsePosition(template.syntax, template.constructor, x, 0, EMPTY_PARAMETERS))
            }
            while (!stack.isEmpty()) {
                val pos = stack.pop()
                if (pos.inTokens == y || pos.syntax.isEmpty()) {
                    if (pos.inTokens == y && pos.syntax.isEmpty()) {
                        when (val s = parsed(pos.constructor(pos.parameters), ParseContext(x, y))) {
                            is Statement -> if (pos.unusedWords < dpStatements[x][y]?.unusedWords ?: Int.MAX_VALUE) dpStatements[x][y] = ParseResult(pos.unusedWords, s)
                            is Quantity -> if (pos.unusedWords < dpQuantities[x][y]?.unusedWords ?: Int.MAX_VALUE) dpQuantities[x][y] = ParseResult(pos.unusedWords, s)
                            is MathObject -> if (pos.unusedWords < dpMathObjects[x][y]?.unusedWords ?: Int.MAX_VALUE) dpMathObjects[x][y] = ParseResult(pos.unusedWords, s)
                            is Intent -> if (pos.unusedWords < dpIntents[x][y]?.unusedWords ?: Int.MAX_VALUE) dpIntents[x][y] = ParseResult(pos.unusedWords, s)
                        }
                    }
                } else {
                    stack.push(pos.copy(inTokens = pos.inTokens + 1, unusedWords = pos.unusedWords + 1))
                    pos.syntax.first().let { elem -> when (elem) {
                        is Token -> if (tokens[pos.inTokens] == elem.token) {
                            stack.push(pos.copy(inTokens = pos.inTokens + 1, syntax = pos.syntax.subList(1)))
                        }
                        is SyntaxParameter -> for (z in pos.inTokens + 1..y) {
                            when (elem.type) {
                                ParameterType.STATEMENT -> dpStatements
                                ParameterType.QUANTITY -> dpQuantities
                                ParameterType.MATH_OBJECT -> dpMathObjects
                            }[pos.inTokens][z]?.let { stack.push(pos.copy(
                                inTokens = z,
                                syntax = pos.syntax.subList(1),
                                unusedWords = pos.unusedWords + it.unusedWords,
                                parameters = pos.parameters.with(it.s, elem.id))) }
                        }
                        is Varying -> for (option in elem.options) {
                            stack.push(pos.copy(syntax = option + pos.syntax.subList(1)))
                        }
                        is NotIndicator -> {
                            stack.push(pos.copy(syntax = pos.syntax.subList(1)))
                            stack.push((pos as ParsePosition<Statement>).let { it.copy(
                                syntax = elem.syntax + it.syntax.subList(1),
                                constructor = { params -> Not(it.constructor(params)) }
                            ) })
                            Unit
                        }
                    } }
                }
            }
            if (dpStatements[x][y] != null) {
                println("dpStatements[$x][$y] = ${dpStatements[x][y]}")
            }
            if (dpQuantities[x][y] != null) {
                println("dpQuantities[$x][$y] = ${dpQuantities[x][y]}")
            }
            if (dpMathObjects[x][y] != null) {
                println("dpMathObjects[$x][$y] = ${dpMathObjects[x][y]}")
            }
            if (dpIntents[x][y] != null) {
                println("dpIntents[$x][$y] = ${dpIntents[x][y]}")
            }
            dpStatements[x][y]
                ?.let { res -> ParseResult(res.unusedWords + x + n - y, res.s)}
                ?.let { res -> if (res.unusedWords < statementRes?.unusedWords ?: Int.MAX_VALUE) {
                    statementRes = res
                } }
            dpIntents[x][y]
                ?.let { res -> ParseResult(res.unusedWords + x + n - y, res.s)}
                ?.let { res -> if (res.unusedWords < intentRes?.unusedWords ?: Int.MAX_VALUE) {
                    intentRes = res
                } }
        }
    }
    println()
    return Pair(statementRes, intentRes)
}