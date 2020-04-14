package us.tlatoani.comboprover

import us.tlatoani.comboprover.better_parser.*
import java.util.*
import kotlin.reflect.KClass

data class Implication(val from: Int, val to: Int)

sealed class StatementTag

data class ThereforeTag(val id: UUID): StatementTag()
data class ByTag(val id: UUID, val concept: ByConcept): StatementTag()
data class AndTag(val id: UUID, val other: Int): StatementTag()
data class SentenceTag(val ix: Int): StatementTag()

data class SubstringIdentifier(val statementIx: Int, val from: Int)

const val IMPLICATION_SIMILARITY_THRESHOLD = 5

fun findSimilarSubstrings(statements: List<Statement>, tags: List<List<StatementTag>>, tokenizations: List<List<String>>): Trie<String, SubstringIdentifier> {
    val root = Trie<String, SubstringIdentifier>(0)
    for (j in statements.indices) {
        val statement = statements[j] as ParsedStatement
        val ix = (tags[j].find { it is SentenceTag } as SentenceTag).ix
        for (k in statement.context.from until statement.context.to) {
            root.add(tokenizations[ix].subList(k, statement.context.to), SubstringIdentifier(j, k))
        }
    }
    return root
}

val CONCRETE_QUANTITY_CLASSES = mutableListOf<KClass<out Quantity>>(
    Amount::class, Size::class
)

fun getStatementsAndImplications(sentenceStatements: List<Statement>, intents: List<Intent>, tokenizations: List<List<String>>): Pair<List<Statement>, Set<Implication>> {
    println("sentenceStatements = $sentenceStatements")
    println("tokenizations = $tokenizations")
    val statements = mutableListOf<Statement>()
    val statementTags = mutableListOf<MutableList<StatementTag>>()
    var k = 0
    val implications = mutableSetOf<Implication>()
    for (ix in sentenceStatements.indices) {
        fun addTag(ix: Int, tag: StatementTag) {
            if (statementTags.size <= ix) {
                statementTags.add(mutableListOf())
            }
            statementTags[ix].add(tag)
        }
        fun dfs(statementInitial: Statement) {
            val statement = if (statementInitial is ParsedStatement) statementInitial.statement else statementInitial
            println("dfs | k = $k, statement = $statement")
            if (statement is MetaStatement) {
                val curr = k
                val id = UUID.randomUUID()
                when (statement) {
                    is HangingTherefore -> {
                        dfs(statement.conclusion)
                        (curr until k).map { addTag(it, ThereforeTag(id)) }
                    }
                    is And -> {
                        dfs(statement.statement1)
                        val curr2 = k
                        dfs(statement.statement2)
                        for (j1 in curr until curr2) {
                            for (j2 in curr2 until k) {
                                addTag(j1, AndTag(id, j2))
                                addTag(j2, AndTag(id, j1))
                            }
                        }
                    }
                    is By -> {
                        dfs(statement.conclusion)
                        for (j in curr until k) {
                            addTag(j, ByTag(id, statement.concept))
                        }
                    }
                    is Since -> {
                        dfs(statement.reason)
                        val curr2 = k
                        dfs(statement.conclusion)
                        for (j1 in curr until curr2) {
                            for (j2 in curr2 until k) {
                                implications.add(Implication(j1, j2))
                            }
                        }
                    }
                }
            } else {
                statements.add(statementInitial)
                addTag(k, SentenceTag(ix))
                k++
            }
        }
        dfs(sentenceStatements[ix])
    }
    for (j in statements.indices) {
        println("j = $j")
        println("\tstatement = ${statements[j]}")
        println("\ttags = ${statementTags[j]}")
    }
    // adjacent implications
    for (j in 0..statements.size - 2) {
        statementTags[j + 1].find { it is ThereforeTag }?.let { tag ->
            if (!statementTags[j].contains(tag)) {
                for (k in j + 1 until statements.size) {
                    if (!statementTags[k].contains(tag)) {
                        break
                    }
                    implications.add(Implication(j, k))
                }
            }
        }
    }
    // multiplication principle
    for (j in statements.indices) {
        if (statementTags[j].any { it is ByTag && it.concept == ByConcept.MULTIPLICATION_PRINCIPLE }) {
            val prevs = mutableListOf<Int>()
            for (k in j - 1 downTo 0) {
                val statement = parallel(statements[k])
                if (statement is Equals && (statement.q1 is Amount || statement.q2 is Amount)) {
                    prevs.add(k)
                }
            }
            if (prevs.size >= 2) {
                implications.add(Implication(prevs[0], j))
                implications.add(Implication(prevs[1], j))
            }
        }
    }
    // desired quantity
    var desiredQuantity: Quantity? = null
    intentLoop@ for (parsedIntent in intents) {
        val intent = unwrap(parsedIntent)
        when (intent) {
            is Determine -> {
                if (unwrap(intent.quantity)::class in CONCRETE_QUANTITY_CLASSES) {
                    desiredQuantity = intent.quantity
                    break@intentLoop
                }
                Unit
            }
            is Show -> {
                val flattened = mutableListOf<Statement>()
                fun dfs(statement: Statement) {
                    val unwrapped = unwrap(statement)
                    if (unwrapped is MetaStatement) {
                        if (unwrapped is And) {
                            dfs(unwrapped.statement1)
                            dfs(unwrapped.statement2)
                        }
                    } else {
                        flattened.add(statement)
                    }
                }
                dfs(intent.statement)
                for (statement in flattened) {
                    val unwrapped = unwrap(statement)
                    when (unwrapped) {
                        is Equals -> {
                            if (unwrapped.q1::class in CONCRETE_QUANTITY_CLASSES) {
                                desiredQuantity = unwrapped.q1
                                break@intentLoop
                            }
                            if (unwrapped.q2::class in CONCRETE_QUANTITY_CLASSES) {
                                desiredQuantity = unwrapped.q1
                                break@intentLoop
                            }
                        }
                    }
                }
                Unit
            }
        }
    }
    val similarity = findSimilarSubstrings(statements, statementTags, tokenizations)
    for (j in statements.indices) {
        val ix = (statementTags[j].find { it is SentenceTag } as SentenceTag).ix
        val replaced = replaceHangings(statements[j], tokenizations[ix], desiredQuantity?.let { context(it).text })
        val possible = mutableSetOf<Int>()
        fromLoop@ for (from in replaced.indices) {
            var node = similarity
            for (to in 1..IMPLICATION_SIMILARITY_THRESHOLD) {
                if (!node.children.containsKey(replaced[to])) {
                    continue@fromLoop
                }
                node = node.children[replaced[to]]!!
            }
            for (id in node.terminal) {
                possible.add(id.statementIx)
            }
        }
        val stack = Stack<Int>()
        for (implication in implications) {
            val k = implication.from
            if (implication.to == j && k > 0) {
                val kx = (statementTags[k].find { it is SentenceTag} as SentenceTag).ix
                if (tokenizations[kx].subList(context(statements[k]).from, context(statements[k]).to).contains("this") && possible.contains(k - 1)) {
                    implications.add(Implication(k - 1, j))
                }
            }
        }
    }
    return Pair(statements, implications)
}

fun replaceHangings(statement: Statement, tokenization: List<String>, desiredQuantityText: List<String>?): List<String> {
    val res = mutableListOf<String>()
    fun dfs(parsed: Any) {
        if (parsed is HangingAmount && desiredQuantityText != null) {
            res.addAll(desiredQuantityText)
            return
        }
        val ps = mutableListOf<Any>()
        val stack = Stack<Any>()
        stack.push(parsed)
        while (stack.isNotEmpty()) {
            val s = stack.pop()
            for (member in s::class.members) {
                val t = member.call(s)!!
                if (t::class in PARSED_CLASSES) {
                    ps.add(t)
                } else {
                    stack.push(t)
                }
            }
        }
        var j = context(parsed).from
        while (j <= context(parsed).to) {
            val s = ps.find { context(it).from == j }
            if (s == null) {
                res.add(tokenization[j])
                j++
            } else {
                dfs(s)
                j = context(s).to
            }
        }
    }
    dfs(statement)
    return res
}