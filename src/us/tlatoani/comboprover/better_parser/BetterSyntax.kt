package us.tlatoani.comboprover.better_parser

import java.lang.IllegalArgumentException
import java.util.*

sealed class SyntaxElement
data class Token(val token: String): SyntaxElement()
data class SyntaxParameter(val type: ParameterType, val id: Int): SyntaxElement()
data class Varying(val options: List<List<SyntaxElement>>): SyntaxElement()
data class NotIndicator(val syntax: List<SyntaxElement>): SyntaxElement()

fun tokenize(syntax: String): List<String> {
    val tokens = mutableListOf<String>()
    var curr = ""
    fun resetCurr() {
        if (curr.isNotEmpty()) {
            tokens.add(curr)
        }
        curr = ""
    }
    for (chara in syntax) {
        if (chara in "(|)[]{}") {
            resetCurr()
            tokens.add(chara + "")
        } else if (chara.isWhitespace()) {
            resetCurr()
        } else {
            curr += chara
        }
    }
    resetCurr()
    return tokens
}

fun parseSyntax(syntax: String): List<SyntaxElement> {
    val tokens = tokenize(syntax)
    val stack = Stack<Any>()
    stack.push(mutableListOf<SyntaxElement>())
    for (token in tokens) {
        when (token) {
            "(", "{" -> {
                stack.push(mutableListOf<List<SyntaxElement>>())
                stack.push(mutableListOf<SyntaxElement>())
            }
            "[" -> {
                stack.push(mutableListOf(listOf<SyntaxElement>()))
                stack.push(mutableListOf<SyntaxElement>())
            }
            "|" -> {
                val l = stack.pop() as List<SyntaxElement>
                (stack.peek() as MutableList<List<SyntaxElement>>).add(l)
                stack.push(mutableListOf<SyntaxElement>())
            }
            ")", "]" -> {
                val l = stack.pop() as List<SyntaxElement>
                val ll = stack.pop() as MutableList<List<SyntaxElement>>
                ll.add(l)
                (stack.peek() as MutableList<SyntaxElement>).add(Varying(ll))
            }
            "}" -> {
                val l = stack.pop() as List<SyntaxElement>
                val ll = stack.pop() as MutableList<List<SyntaxElement>>
                ll.add(l)
                (stack.peek() as MutableList<SyntaxElement>).add(NotIndicator(listOf(Varying(ll))))
            }
            else -> (stack.peek() as MutableList<SyntaxElement>).add(
                if (token.startsWith("%"))
                    SyntaxParameter(abbreviationToType(token.substring(1, 3)), token.substring(3, token.length - 1).toInt())
                else if (token.endsWith("*"))
                    token.substring(token.length - 1).let { t -> Varying(listOf(listOf(Token(t)), listOf(Token(t + "s")))) }
                else
                    Token(token)
            )
        }
    }
    return stack.pop() as List<SyntaxElement>
}