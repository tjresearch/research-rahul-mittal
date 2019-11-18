package us.tlatoani.comboprover

import edu.stanford.nlp.trees.Tree

enum class Action { DEFINITION, PREMISE, CLAIM, DEDUCTION }

data class ActionNode(val node: Tree, val action: Action)

val DEFINE_VERBS = setOf("let", "define")
val CLAIM_VERBS = setOf("claim")
val THEREFORE_WORDS = setOf("therefore", "thus")

val MATH_VERBS = setOf("choose", "chosen", "select", "match", "matched", "count", "equals", "counts",
    "contain", "contains", "include", "includes", "make", "results", "gives", "give", "have", "has", "decide")
val MATH_NOUNS = setOf("amount", "number", "cardinality", "quantity", "ways", "minimum", "maximum", "side", "sides",
    "subset", "subsets", "element", "elements", "sum", "sums", "choice", "choices", "option", "options",
    "sequence", "sequences", "total", "size", "sizes")
val MATH_ADJECTIVES = setOf("equal", "isomorphic", "greater", "less", "empty", "greatest", "largest", "least", "smallest")
val TO_BE = setOf("be", "is", "are", "was", "were")
val DO = setOf("do", "does", "doesn't")
val CAN = setOf("can", "could")
val THIS_THAT = setOf("this", "that")
val SEE_VERBS = setOf("see")

fun identifyActions(ancestors: List<Tree>): List<ActionNode> {
    val node = ancestors[0]
    println("node = $node")
    val res = mutableListOf<ActionNode>()
    if (node.name() == "ROOT") {
        for (child in node.children()) {
            if (child.name() == "S" || child.name() == "FRAG") {
                res += identifyActions(listOf(child) + ancestors)
            }
        }
    } else if (node.name() == "S" || node.name() == "FRAG") {
        for (child in node.children()) {
            if (child.name() == "S" || child.name() == "FRAG" || child.name() == "VP") {
                res += identifyActions(listOf(child) + ancestors)
            }
        }
    } else if (node.name() == "VP") {
        val verb = node.getVerb()
        if (DEFINE_VERBS.contains(verb)) {
            res += ActionNode(node, Action.DEFINITION)
        } else if (CLAIM_VERBS.contains(verb)) {
            res += ActionNode(node, Action.CLAIM)
        } else {
            // subject v2 and v1, subject v1, v2, and v3
            // it's cc for CoorDinaAting CoNjuCnetion, and is not a suboordinating conjunction k'ooshima???????
            if (node.getChildWithName("CC")?.word() == "and") {
                for (child in node.children()) {
                    if (child.name() == "VP") {
                        res += identifyActions(child + ancestors.subList(1, ancestors.size))
                    }
                }
                return res
            }
            // we see that
            if (SEE_VERBS.contains(verb) &&
                node.getChildWithName("SBAR")?.let {
                        sbar -> sbar.getChildWithName("IN")?.word() == "that" && sbar.getChildWithName("S") != null
                } == true) {
                res += identifyActions(node.getChildWithName("SBAR")!!.getChildWithName("S")!! + ancestors.subList(1, ancestors.size))
                return res
            }
            // add we see that blah blah for next time
            var couldBeDeduction = false
            // so
            if (ancestors[1].name() == "S") {
                val parentIx = ancestors[2].children().indexOf(ancestors[1])
                println("parentIx = $parentIx")
                if (parentIx > 0) {
                    val prevSiblingOfParent = ancestors[2].getChild(parentIx - 1)
                    if (prevSiblingOfParent.name() == "IN" && prevSiblingOfParent.word() == "so") {
                        couldBeDeduction = true
                    }
                }
            }
            // therefore
            for (ancestor in ancestors) {
                if (THEREFORE_WORDS.contains(ancestor.getChildWithName("ADVP")?.getChildWithName("RB")?.word())) {
                    couldBeDeduction = true
                }
            }
            /*if (THEREFORE_WORDS.contains(ancestors[1].getChildWithName("ADVP")?.getChildWithName("RB")?.word())) {
                couldBeDeduction = true
            }*/
            // since x, y
            ancestors[1].getChildWithName("SBAR")?.let { sbar ->
                if (sbar.getChildWithName("IN")?.word() == "since") {
                    couldBeDeduction = true
                    res += identifyActions(listOf(sbar.getChildWithName("S")!!, sbar) + ancestors)
                }
            }
            // by
            if (ancestors[1].getChildWithName("PP")?.getChildWithName("IN")?.word() == "by") {
                couldBeDeduction = true
            }

            var isPremise = false
            // math verb acting on math noun
            if (node.isMathVerbActingOnMathObject()) {
                isPremise = true
            }
            if (TO_BE.contains(node.getVerb())) {
                // quantity is quantity, there is something
                val npSubject = ancestors[1].getChildWithName("NP")
                val npObject = node.getChildWithName("NP")
                println("npSubject = $npSubject, npObject = $npObject")
                if ((npSubject?.isMathNoun() == true || npSubject?.getChildWithName("EX") != null) && npObject?.isMathNoun() == true) {
                    isPremise = true
                }
                //quantity is adjective
                val adjp = node.getChildWithName("ADJP")
                println("npSubject = $npSubject, adjp = $adjp")
                if (npSubject?.isMathNoun() == true && adjp?.isMathAdjective() == true) {
                    isPremise = true
                }
                // letter is true (for formulaic statements)
                if (npSubject?.isPlaceholder() == true && adjp?.getAdjective() == "true") { //lol
                    isPremise = true
                }
                // quantity is participle
                if (npSubject?.isMathNoun() == true && node.isMathVerb()) {
                    isPremise = true
                }
            }

            // either verb or verb
            if (node.getChildWithName("CC")?.word() == "either") {
                val vp1 = node.getChildWithName("VP")
                val vp2 = node.childrenAsList.subList(node.children().indexOf(vp1) + 1, node.children().size)
                    .find { child -> child.name() == "VP" }
                if (vp1 != null && vp2 != null) {
                    val actions1 = identifyActions(listOf(vp1) + ancestors.subList(1, ancestors.size))
                    val actions2 = identifyActions(listOf(vp2) + ancestors.subList(1, ancestors.size))
                    if (actions1.any { action -> action.node == vp1 } && actions2.any { action -> action.node == vp2 }) {
                        res += actions1.filter { action -> action.node != vp1 }
                        res += actions2.filter { action -> action.node != vp2 }
                        isPremise = true
                    }
                }
            }

            println("couldBeDeduction = $couldBeDeduction, isPremise = $isPremise")

            if (isPremise) {
                if (couldBeDeduction) {
                    res += ActionNode(node, Action.DEDUCTION)
                } else {
                    res += ActionNode(node, Action.PREMISE)
                }
            }
        }
    }
    return res
}

fun Tree.getChildWithName(name: String) =
    this.children().find { child -> child.name() == name }

fun Tree.getVerb() = children().find { child -> child.name().startsWith("VB") }?.word()
fun Tree.getNoun(): String? {
    var noun = children().reversed().find { child -> child.name().startsWith("NN") }?.word()
    if (noun == null) {
        noun = getChildWithName("NP")?.getNoun()
    }
    return noun
}
fun Tree.getAdjective() = children().find { child -> child.name().startsWith("JJ") }?.word()

fun Tree.name() = this.label().value()!!
fun Tree.word(): String {
    //if this.name() is not a POS, throw IllegalArgumentException() for the future
    var word = this.children().first().name().toLowerCase()
    while (!word.last().isLetterOrDigit()) {
        word = word.substring(0, word.length - 1)
    }
    return word
    //return this.children().first().name()
}

fun Tree.isMathVerb(): Boolean {
    val verb = getVerb()
    if (MATH_VERBS.contains(verb)) {
        return true
    }
    if (TO_BE.contains(verb) && getChildWithName("VP")?.isMathVerb() == true) {
        return true
    }
    if (DO.contains(verb) &&
        (getChildWithName("VP")?.isMathVerb() == true
                || getChildWithName("NP")?.getChildWithName("RB")?.word() == "not")) {
        return true
    }
    // can verb, cannot
    if (verb == null && CAN.contains(getChildWithName("MD")?.word()) &&
        (getChildWithName("RB")?.word() == "NOT" || getChildWithName("VP")?.isMathVerb() == true)) {
        return true
    }
    return false
}

fun Tree.isMathVerbActingOnMathObject(): Boolean {
    println("calling isMathVerbActingOnMathObject on $this")
    val verb = getVerb()
    if (MATH_VERBS.contains(verb)) {
        // checking if the object being acted on is a math noun, pls don't mess this up again
        if (getChildWithName("NP")?.isMathNoun() == true
            || (getChildWithName("NP") == null && getChildWithName("PP")?.getChildWithName("NP")?.isMathNoun() == true)) {
            return true
        }
    }
    if (TO_BE.contains(verb) && getChildWithName("VP")?.isMathVerbActingOnMathObject() == true) {
        return true
    }
    // do verb, do not, do this
    if (DO.contains(verb) &&
        (getChildWithName("VP")?.isMathVerbActingOnMathObject() == true
                || getChildWithName("NP")?.getChildWithName("RB")?.word() == "not"
                || THIS_THAT.contains(getChildWithName("NP")?.getChildWithName("DT")?.word()))) {
        return true
    }
    // can verb, cannot
    if (verb == null) {
        if (CAN.contains(getChildWithName("MD")?.word()) &&
            (getChildWithName("RB")?.word() == "not" || getChildWithName("VP")?.isMathVerbActingOnMathObject() == true)) {
            return true
        }
        if (getChildWithName("MD")?.word() == "must" && getChildWithName("VP")?.isMathVerbActingOnMathObject() == true) {
            return true
        }
    }
    return false
}

fun Tree.isMathNoun(): Boolean {
    println("calling isMathNoun on $this")
    if (isPlaceholder()) {
        return true
    }
    //if (children().any { child -> child.name().startsWith("NN") && MATH})
    val noun = getNoun()
    if (MATH_NOUNS.contains(noun)) {
        return true
    }
    if (noun == null) {
        if (getChildWithName("NP")?.isMathNoun() == true) {
            return true
        }
        if (getChildWithName("DT") != null) {
            return true
        }
        if (getChildWithName("CD") != null) {
            return true
        }
    }
    return false
}

fun Tree.isPlaceholder() = getNoun()?.let { noun -> noun.length == 1 && noun != "i" && noun != "a" } == true

fun Tree.isMathAdjective(): Boolean {
    val adjective = getAdjective()
    if (MATH_ADJECTIVES.contains(adjective)) {
        return true
    }
    return false
}