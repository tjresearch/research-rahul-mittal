package us.tlatoani.comboprover.archive

sealed class Node(val label: String)

class Nonleaf(label: String, val children: List<Node>): Node(label) {
    override fun toString() = "$label$children"
}
class Leaf(label: String, val word: String): Node(label) {
    override fun toString() = "$label($word)"
}
class TemplateParameter(label: String): Node(label) {
    override fun toString() = "${label.substring(0, 2)}(${label.substring(2)})"
}

fun QQ(id: Int) = TemplateParameter("QQ$id")
fun XN(id: Int) = TemplateParameter("XN$id")

fun XYZ(vararg children: Node) = Nonleaf("XYZ", children.toList())
fun S(vararg children: Node) = Nonleaf("S", children.toList())
fun NP(vararg children: Node) = Nonleaf("NP", children.toList())
fun VP(vararg children: Node) = Nonleaf("VP", children.toList())
fun PP(vararg children: Node) = Nonleaf("PP", children.toList())
fun DET(vararg children: Node) = Nonleaf("DET", children.toList())
fun SBAR(vararg children: Node) = Nonleaf("SBAR", children.toList())
fun FRAG(vararg children: Node) = Nonleaf("FRAG", children.toList())
fun WHNP(vararg children: Node) = Nonleaf("WHNP", children.toList())
fun PRN(vararg children: Node) = Nonleaf("PRN", children.toList())
fun ADVP(vararg children: Node) = Nonleaf("ADVP", children.toList())
fun PRT(vararg children: Node) = Nonleaf("PRT", children.toList())

fun CD(word: String) = Leaf("CD", word)
fun DT(word: String) = Leaf("DT", word)
fun EX(word: String) = Leaf("EX", word)
fun FW(word: String) = Leaf("FW", word)
fun IN(word: String) = Leaf("IN", word)
fun JJ(word: String) = Leaf("JJ", word)
fun JJR(word: String) = Leaf("JJR", word)
fun JJS(word: String) = Leaf("JJS", word)
fun LS(word: String) = Leaf("LS", word)
fun MD(word: String) = Leaf("MD", word)
fun NN(word: String) = Leaf("NN", word)
fun NNS(word: String) = Leaf("NNS", word)
fun NNP(word: String) = Leaf("NNP", word)
fun NNPS(word: String) = Leaf("NNPS", word)
fun PDT(word: String) = Leaf("PDT", word)
fun POS(word: String) = Leaf("POS", word)
fun PRP(word: String) = Leaf("PRP", word)
fun RB(word: String) = Leaf("RB", word)
fun RBR(word: String) = Leaf("RBR", word)
fun RBS(word: String) = Leaf("RBS", word)
fun RP(word: String) = Leaf("RP", word)
fun SYM(word: String) = Leaf("SYM", word)
fun TO(word: String) = Leaf("TO", word)
fun UH(word: String) = Leaf("UH", word)
fun VB(word: String) = Leaf("VB", word)
fun VBD(word: String) = Leaf("VBD", word)
fun VBG(word: String) = Leaf("VBG", word)
fun VBN(word: String) = Leaf("VBN", word)
fun VBP(word: String) = Leaf("VBP", word)
fun VBZ(word: String) = Leaf("VBZ", word)
fun WDT(word: String) = Leaf("WDT", word)
fun WP(word: String) = Leaf("WP", word)
fun WRB(word: String) = Leaf("WRB", word)


