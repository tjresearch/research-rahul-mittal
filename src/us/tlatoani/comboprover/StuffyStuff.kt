package us.tlatoani.comboprover

import edu.stanford.nlp.trees.Tree

sealed class MathThing

sealed class Statement: MathThing()

data class Not(val negated: Statement): Statement()
data class FormulaStatement(val placeholder: String): Statement()
data class Each(val obj: MathObject, val attribute: Attribute): Statement()

sealed class Quantity: MathThing()

data class Equals(val q1: Quantity, val q2: Quantity): Statement()

data class Formula(val placeholder: String): Quantity()

data class LessThan(val lesser: Quantity, val greater: Quantity): Statement()

data class Amount(val obj: MathObject): Quantity()

sealed class MathObject: MathThing()

data class Matching(val n1: MathObject, val n2: MathObject): MathObject()
data class Choice(val noun: MathObject): MathObject()

data class UnknownNoun(val noun: Tree): Quantity()

data class ArbitraryMathObject(val noun: String): MathObject()
data class LabelledMathObject(val obj: MathObject): MathObject()
data class Placement(val of: MathObject, val into: MathObject): MathObject()
data class SubjectToCondition(val obj: MathObject, val condition: Attribute): MathObject()
data class ForPurpose(val obj: MathObject, val condition: Statement): MathObject()
data class Multiple(val amount: Quantity, val obj: MathObject): MathObject()

sealed class Attribute

data class Contains(val elem: MathObject): Attribute()

// Formula Stuff

data class Constant(val k: Long): Quantity()
data class Variable(val name: String): Quantity()
data class Sum(val qs: List<Quantity>): Quantity()
data class Product(val qs: List<Quantity>): Quantity()
data class Difference(val left: Quantity, val right: Quantity): Quantity()
data class Quotient(val top: Quantity, val bottom: Quantity): Quantity()
data class Factorial(val q: Quantity): Quantity()
data class Combination(val n: Quantity, val k: Quantity): Quantity()

// Hanging Stuff

data class HangingAmount(val unit: Unit): Quantity()

// Parsed Stuff

data class ParsedStatement(val statement: Statement, val context: ParseContext): Statement()
data class ParsedQuantity(val quantity: Quantity, val context: ParseContext): Quantity()
data class ParsedMathObject(val mathObject: MathObject, val context: ParseContext): MathObject()

// Bounded

data class Infinity(val sign: Int): Quantity()
data class Bounded(val left: Quantity, val right: Quantity): Quantity()