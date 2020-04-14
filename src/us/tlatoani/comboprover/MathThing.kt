package us.tlatoani.comboprover

import edu.stanford.nlp.trees.Tree

sealed class MathThing

open class Statement: MathThing()

data class Not(val negated: Statement): Statement()
data class FormulaStatement(val placeholder: String): Statement()
data class Each(val obj: MathObject, val condition: (MathObject) -> Statement): Statement()

open class Quantity: MathThing()

data class Equals(val q1: Quantity, val q2: Quantity): Statement()

data class Formula(val placeholder: String): Quantity()

data class LessThan(val lesser: Quantity, val greater: Quantity): Statement()

data class Size(val of: MathObject): Quantity()

data class Amount(val obj: MathObject): Quantity()

open class MathObject: MathThing()

data class Matching(val n1: MathObject, val n2: MathObject): MathObject()
data class Choice(val noun: MathObject): MathObject()
data class ChoiceFrom(val of: MathObject, val from: MathObject): MathObject()

data class UnknownNoun(val noun: Tree): Quantity()

data class ArbitraryMathObject(val noun: String): MathObject()
data class LabelledMathObject(val obj: MathObject): MathObject()
data class Placement(val of: MathObject, val into: MathObject): MathObject()
data class SubjectToCondition(val obj: MathObject, val condition: (MathObject) -> Statement): MathObject()
data class ForPurpose(val obj: MathObject, val condition: (MathObject) -> Statement): MathObject()
data class Multiple(val amount: Quantity, val obj: MathObject): MathObject()
data class OfSize(val obj: MathObject, val size: Quantity): MathObject()
data class Element(val of: MathObject): MathObject()

data class ObjectEquals(val obj1: MathObject, val obj2: MathObject): Statement()

data class Largest(val obj: MathObject): MathObject()

// Qualified

data class Qualified(val obj: MathObject, val qualification: Statement): MathObject()

// Contains

data class Contains(val container: MathObject, val elem: MathObject): Statement()

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
data class HangingWays(val unit: Unit): MathObject()
data class RemainingElements(val of: MathObject): MathObject()
data class Part(val of: MathObject): MathObject()

// Bounded

data class Infinity(val sign: Int): Quantity()
data class Bounded(val left: Quantity, val right: Quantity): Quantity()

// Sides

enum class Side { LEFT, RIGHT }
data class SideOfEquation(val side: Side): Quantity()

// Intent

open class Intent

data class Show(val statement: Statement): Intent()
data class Determine(val quantity: Quantity): Intent()
data class ChooseFor(val choice: MathObject, val forObj: MathObject): Intent()

// Subset

data class Subset(val of: MathObject): MathObject()

// MetaStatement

sealed class MetaStatement: Statement()

data class Since(val reason: Statement, val conclusion: Statement): MetaStatement()
data class DefineQuantity(val defined: Quantity, val target: Quantity): MetaStatement()
data class DefineMathObject(val defined: MathObject, val target: MathObject): MetaStatement()
data class HangingTherefore(val conclusion: Statement): MetaStatement()
data class And(val statement1: Statement, val statement2: Statement): MetaStatement()

data class By(val concept: ByConcept, val conclusion: Statement): MetaStatement()

enum class ByConcept { DEFINITION, MULTIPLICATION_PRINCIPLE }

// Variable MathObject

data class ObjectVariable(val name: String): MathObject()