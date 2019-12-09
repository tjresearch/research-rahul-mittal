package us.tlatoani.comboprover.archive

/*import edu.stanford.nlp.trees.Tree

data class StatementNode(val node: Tree, val action: Action, val statement: Statement)

fun identifyStuff(ancestors: List<Tree>): List<StatementNode> {
    val node = ancestors[0]
    println("node = $node")
    val res = mutableListOf<StatementNode>()
    if (node.name() == "ROOT") {
        for (child in node.children()) {
            if (child.name() == "S" || child.name() == "FRAG") {
                res += identifyStuff(listOf(child) + ancestors)
            }
        }
    } else if (node.name() == "S" || node.name() == "FRAG") {
        for (child in node.children()) {
            if (child.name() == "S" || child.name() == "FRAG" || child.name() == "VP") {
                res += identifyStuff(listOf(child) + ancestors)
            }
        }
    } else if (node.name() == "VP") {
        val verb = node.getVerb()
        if (verb == "let") { //&& node.getChildWithName("NP")?.isPlaceholder() == true
            if (node.getChildWithName("VP")?.word() == "be") {
                val np1 = node.getChildWithName("NP")!!
                val np2 = node.getChildWithName("VP")!!.getChildWithName("NP")!!
                res += StatementNode(node, Action.DEFINITION, Equals(
                    getQuantity(np1 + ancestors),
                    getQuantity(np2 + node.getChildWithName("VP")!! + ancestors)))
            } else if (node.getChildWithName("NP")?.isPlaceholder() == true) {
                res += StatementNode(node, Action.DEFINITION, FormulaStatement(node.getChildWithName("NP")!!))
            }
        } else if (verb == "define") {
            node.getChildWithName("NP")?.let { np1 ->
                if (node.getChildWithName("PP")?.getChildWithName("IN")?.word() == "as") {
                    res += StatementNode(node, Action.DEFINITION, Equals(getQuantity(np1 + ancestors),
                        getQuantity(node.getChildWithName("PP")!!.getChildWithName("NP")!! + node.getChildWithName("PP")!! + ancestors)))
                }
                node.getChildWithName("S")?.getChildWithName("VP")?.let { subVP ->
                    if (subVP.getChildWithName("TO") != null && subVP.getChildWithName("VP")?.getVerb() == "be") {
                        res += StatementNode(node, Action.DEFINITION, Equals(getQuantity(np1 + ancestors),
                            getQuantity(subVP.getChildWithName("VP")!!.getChildWithName("NP")!!
                                    + subVP.getChildWithName("VP")!! + subVP + node.getChildWithName("S")!! + ancestors)))
                    }
                }
            }
        } /*else if (CLAIM_VERBS.contains(verb)) {
            res += ActionNode(node, Action.CLAIM)
        }*/ else {
            // subject v2 and v1, subject v1, v2, and v3
            // it's cc for CoorDinaAting CoNjuCnetion, and is not a suboordinating conjunction k'ooshima???????
            if (node.getChildWithName("CC")?.word() == "and") {
                for (child in node.children()) {
                    if (child.name() == "VP") {
                        res += identifyStuff(child + ancestors.subList(1, ancestors.size))
                    }
                }
                return res
            }
            // we see that
            if (SEE_VERBS.contains(verb) &&
                node.getChildWithName("SBAR")?.let {
                        sbar -> sbar.getChildWithName("IN")?.word() == "that" && sbar.getChildWithName("S") != null
                } == true) {
                res += identifyStuff(node.getChildWithName("SBAR")!!.getChildWithName("S")!! + ancestors.subList(1, ancestors.size))
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
                    res += identifyStuff(listOf(sbar.getChildWithName("S")!!, sbar) + ancestors)
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
                    val actions1 = identifyStuff(listOf(vp1) + ancestors.subList(1, ancestors.size))
                    val actions2 = identifyStuff(listOf(vp2) + ancestors.subList(1, ancestors.size))
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

fun getQuantity(ancestors: List<Tree>): Quantity {
    val node = ancestors[0]
    if (node.isPlaceholder()) {
        return Formula(node)
    }
    TODO()
}*/