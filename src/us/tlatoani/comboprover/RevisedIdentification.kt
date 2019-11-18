package us.tlatoani.comboprover

/*import edu.stanford.nlp.trees.Tree

data class StatementNode(val node: Tree, val statement: Statement)

fun getStatements(ancestors: List<Tree>): List<StatementNode> {
    val node = ancestors[0]
    val pos = findStatement(node)
    if (pos != null) {
        return listOf(StatementNode(node, pos))
    }
    val res = mutableListOf<StatementNode>()
    for (child in node.children()) {
        res += getStatements(child + ancestors)
    }
    return res
}

data class TemplateParameters(val statements: Map<Int, Statement>, val quantities: Map<Int, Quantity>, val objects: Map<Int, MathObject>) {
    fun st(id: Int) = statements[id]!!
    fun qq(id: Int) = quantities[id]!!
    fun xn(id: Int) = objects[id]!!

    operator fun plus(other: TemplateParameters) =
        TemplateParameters(statements + other.statements, quantities + other.quantities, objects + other.objects)
}

val EMPTY_PARAMETERS = TemplateParameters(emptyMap(), emptyMap(), emptyMap())

/*data class Template<S>(val tree: Tree, val constructor: (TemplateParameters) -> S)

fun <S> template(text: String, constructor: (TemplateParameters) -> S): Template<S> {
    val document = CoreDocument(text)
    pipeline.annotate(document)
    return Template(document.sentences()[0].constituencyParse(), constructor)
}*/

data class Template<S>(val tree: Node, val constructor: (TemplateParameters) -> S)

val STATEMENT_TEMPLATES = listOf<Template<Statement>>(
    Template(XYZ(
        NP(PRP("we")),
        VP(
            MD("can"),
            VP(
                VB("choose"),
                XN(1)
            ),
            PP(
                IN("in"),
                NP(
                    QQ(1),
                    NNS("ways")
                )
            )
        )
    )) { Equals(Amount(Choice(it.xn(1))), it.qq(1)) },
    Template(XYZ(
        NP(PRP("we")),
        VP(
            MD("can"),
            VP(
                VB("choose"),
                NP(
                    XN(1),
                    PP(
                        IN("in"),
                        NP(
                            QQ(1),
                            NNS("ways")
                        )
                    )
                )
            )
        )
    )) { Equals(Amount(Choice(it.xn(1))), it.qq(1)) },
    Template(XYZ(
        XN(1),
        VP(
            MD("can"),
            VP(
                VB("be"),
                VP(
                    VBN("matched"),
                    PRT(RP("up")),
                    PP(
                        IN("with"),
                        NP(
                            XN(2),
                            PP(
                                IN("in"),
                                NP(
                                    QQ(1),
                                    NNS("ways")
                                )
                            )
                        )
                    )
                )
            )
        )
    )) { Equals(Amount(Matching(it.xn(1), it.xn(2))), it.qq(1)) }
)
val QUANTITY_TEMPLATES = listOf<Template<Quantity>>()
val MATH_OBJECT_TEMPLATES = listOf<Template<MathObject>>(
    Template(NP(
        XN(1),
        SBAR(
            WHNP(WDT("which")),
            S(VP(
                MD("will"),
                VP(
                    VB("contain"),
                    XN(2)
                )
            ))
        )
    )) { SubjectToCondition(it.xn(1), Contains(it.xn(2))) }
)

fun findStatement(tree: Tree): Statement? {
    if (tree.getChildWithName("NP") != null && tree.getChildWithName("VP") != null) {
        val np = tree.getChildWithName("NP")!!
        val vp = tree.getChildWithName("VP")!!
        if (np.isPlaceholder() && vp.getVerb() == "is" && vp.getChildWithName("ADJP")?.getChildWithName("JJ")?.word() == "true") {
            return FormulaStatement(np)
        }
    }
    for (template in STATEMENT_TEMPLATES) {
        val pos = matchTrees(tree, template)
        if (pos != null) {
            return pos
        }
    }
    return null
}

fun findQuantity(tree: Tree): Quantity? {
    if (tree.isPlaceholder() || (tree.label().value().startsWith("NN") && tree.word().length == 1 && tree.word() != "i" && tree.word() != "a")) {
        return Formula(tree)
    }
    for (template in QUANTITY_TEMPLATES) {
        val pos = matchTrees(tree, template)
        if (pos != null) {
            return pos
        }
    }
    return null
}

val ARBITRARY_MATH_OBJECT_WORDS = listOf("ball", "balls", "urn", "urns")

fun findMathObject(tree: Tree): MathObject? {
    for (template in MATH_OBJECT_TEMPLATES) {
        val pos = matchTrees(tree, template)
        if (pos != null) {
            return pos
        }
    }
    if (tree.label().value() == "NP" && ARBITRARY_MATH_OBJECT_WORDS.contains(tree.getNoun())) {
        return ArbitraryMathObject(tree)
    }
    if (tree.label().value().startsWith("NN") && ARBITRARY_MATH_OBJECT_WORDS.contains(tree.word())) {
        return ArbitraryMathObject(tree)
    }
    return null
}

fun <S> matchTrees(tree: Tree, template: Template<S>): S? = matchIntoParameters(tree, template.tree)?.let {
        parameters -> template.constructor(parameters) }

fun matchIntoParameters(tree: Tree, matchInto: Node): TemplateParameters? {
    val res = when (matchInto) {
        is TemplateParameter -> matchInto.label.substring(2).toInt().let { id ->
            when (matchInto.label.substring(0, 2)) {
                "ST" -> findStatement(tree)?.let { statement -> EMPTY_PARAMETERS.copy(statements = mapOf(id to statement)) }
                "QQ" -> findQuantity(tree)?.let { quantity -> EMPTY_PARAMETERS.copy(quantities = mapOf(id to quantity)) }
                "XN" -> findMathObject(tree)?.let { mathObject -> EMPTY_PARAMETERS.copy(objects = mapOf(id to mathObject)) }
                else -> null
            }
        }
        is Leaf -> if (tree.label().value() == matchInto.label && tree.word() == matchInto.word) EMPTY_PARAMETERS else null
        is Nonleaf -> if (matchInto.label == "XYZ" || tree.label().value() == matchInto.label) {
            var res: TemplateParameters? = null
            for (mask in 0 until (1 shl tree.children().size)) {
                if (bitAmt(mask) == matchInto.children.size) {
                    res = EMPTY_PARAMETERS
                    var j = 0
                    for (i in 0 until tree.children().size) {
                        if ((mask and (1 shl i)) != 0) {
                            val toAdd = matchIntoParameters(tree.children()[i], matchInto.children[j])
                            if (toAdd == null) {
                                res = null
                                break
                            } else {
                                res = res!! + toAdd
                            }
                            j++
                        }
                    }
                    if (res != null) {
                        break
                    }
                }
            }
            res
        } else null
    } ?: if (tree.children().size == 1) matchIntoParameters(tree.children()[0], matchInto) else null
    println("tree = $tree, matchInto = $matchInto, res = $res")
    return res
}*/