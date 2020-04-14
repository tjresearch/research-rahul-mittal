package us.tlatoani.comboprover

import java.util.*

val HIGHER_CAPITALS = arrayOf("Section", "End")
val CAPITALS = arrayOf("Variable", "Definition", "Theorem", "Lemma", "Notation", "Fixpoint", "Coercion", "Corollary", "Inductive")
val REPLACEMENTS = mapOf(
    "Theorem" to "Axiom",
    "Lemma" to "Axiom",
    "Corollary" to "Axiom",
    "→" to "->",
    "⇒" to "=>",
    "↔" to "<->",
    "∃" to "exists", "∧" to "/\\",
    "≤" to "<=",
    "≥" to ">=",
    "funu" to "fun u",
    "∀" to "forall",
    "≈" to "~="
)
val BRACKETS = "({[]})"
val ENGLISH = arrayOf("we", "that", "one", "the")

fun main() {
    val scanner = Scanner(System.`in`)
    val out = StringBuilder()
    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        if (line.trim() == "-1") {
            break
        }
        val tokens = mutableListOf<String>()
        var j = 0
        while (j < line.length) {
            if (line[j].isLetter()) {
                var token = ""
                while (j < line.length && (line[j].isLetter() || line[j] == '.' || line[j] == '\'')) {
                    token += line[j]
                    j++
                }
                for (cap in CAPITALS) {
                    if (token.startsWith(cap)) {
                        tokens.add(cap)
                        token = token.substring(cap.length)
                    }
                }
                if (token.isNotEmpty()) {
                    tokens.add(token)
                }
            } else if (!line[j].isWhitespace() && line[j] !in BRACKETS && line[j].toString() !in REPLACEMENTS) {
                var token = ""
                while (j < line.length && !line[j].isLetter() && !line[j].isWhitespace() && line[j] !in BRACKETS && line[j].toString() !in REPLACEMENTS) {
                    token += line[j]
                    j++
                }
                tokens.add(token)
            } else if (line[j] in BRACKETS || line[j].toString() in REPLACEMENTS) {
                tokens.add(line[j].toString())
                j++
            } else {
                j++
            }
        }
        if (tokens.any { it.toLowerCase() in ENGLISH }) {
            continue
        }
        var axiomIx = -2
        var colonIx = -1
        for (j in tokens.indices) {
            val token = tokens[j]
            if (token.all { it.isDigit() } && token.toInt() > 10) {
                continue
            }
            if (token == "_") {
                continue
            }
            if (token in HIGHER_CAPITALS) {
                out.appendln()
            } else if (token in CAPITALS) {
                out.appendln()//.append("  ")
            }
            if (token in CAPITALS && token != "Variable") {
                axiomIx = j
            } else if (token == ":" || token == ":=" || token in BRACKETS) {
                if (axiomIx > colonIx) {
                    out.deleteCharAt(out.length - 1)
                    out.append(" ")
                }
                colonIx = j
            }
            out.append(REPLACEMENTS[token] ?: token)
            if (axiomIx > colonIx && j > axiomIx) {
                out.append("_")
            } else {
                out.append(" ")
            }
        }
    }
    var res = out.toString()
    for (j in 0..5) {
        if (j < 3) {
            res = res.replace(BRACKETS[j] + " ", BRACKETS[j] + "")
        } else {
            res = res.replace(" " + BRACKETS[j], "" + BRACKETS[j])
        }
    }
    res = res
        .replace(" \n", "\n")
        .replace(" .", ".")
        .replace(" ,", ",")
        .replace(" ;", ";")
        .replace("injective fn", "injective_fn")
        .replace("surjective fn", "surjective_fn")
        .replace("right size", "right_size")
        .replace("all bij", "all_bij")
        .replace("closed under bij", "closed_under_bij")
        .replace("fnf", "fn f")
        .replace("injU", "inj U")
        .replace("surjU", "surj U")
        .replace("injV", "inj V")
        .replace("surjV", "surj V")
        .replace("(U V : Type) :", ": forall U V : Type,")
        .replace("(U V W : Type) :", ": forall U V W : Type,")
        .replace("c 1", "c1")
        .replace("c 2", "c2")
    println(res)
}

/*
Lemma cardinality eq (c1 c2 : cardinality) : right size c1 = right size c2 → c1 = c2.
It is straightforward to write a function that builds a cardinality out of a set by partially applying the relation bij to the given set. Proofs of closure follow from transitivity of bij.
Definition cardinality of (A : Type) : cardinality. refine {| right size := bij A |}; intros.
∃ A. auto.
apply bij transitive with A; auto.
apply bij transitive with U ; auto. Defined.
Following mathematical notation, we use |A| to denote the cardinality of the set A. Notation "| A |" := (cardinality of A) (at level 30) : cardinal scope.
Because of the closure law, two sets have the same cardinality if and only if they have a bijection between them.
23
Lemmacardeq bijAB :|A|=|B|↔A≈B.
An example of a cardinality that we can write down now is that of the empty set.
Definition zero cardinal := |Empty set|.
The general definition of inequalities between cardinals depends on the inj and surj rela- tions defined earlier. We say that |A| ≤ |B| if and only if there is an injection from A to B. This yields the following definition on Coq cardinalities.
Definition cardinal le (c1 c2 : cardinality) := ∃(A:Type),c1 A∧∃(B :Type),c2 B ∧injAB.
This definition of ≤ obeys the standard reflexivity and transitivity laws. Lemma cardinal le reflexive : reflexive cardinal le.
Lemma cardinal le transitive : transitive cardinal le.
The other inequality relations can be defined in terms of ≤.
Definition cardinal ge (c1 c2 : cardinality) := c2 ≤ c1. Definition cardinal lt (c1 c2 : cardinality) := c1 ≤ c2 ∧ c1 ̸= c2. Definition cardinality gt (c1 c2 : cardinality) := c2 < c1.
For cardinals taken from specific sets A and B, we show that ≤ and ≥ are equivalent to the relations inj and surj.
Theoremcardinal le inj : forall A B : Type, |A|≤|B|↔injAB. Theorem cardinal ge surj : forall A B : Type, |A| ≥ |B| ↔ surj A B.
 */