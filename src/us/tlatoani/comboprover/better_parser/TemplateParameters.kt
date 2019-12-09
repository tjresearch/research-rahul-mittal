package us.tlatoani.comboprover.better_parser

import us.tlatoani.comboprover.MathObject
import us.tlatoani.comboprover.MathThing
import us.tlatoani.comboprover.Quantity
import us.tlatoani.comboprover.Statement
import java.lang.IllegalArgumentException

data class TemplateParameters(val statements: Map<Int, Statement>, val quantities: Map<Int, Quantity>, val objects: Map<Int, MathObject>) {
    fun st(id: Int) = statements[id]!!
    fun qq(id: Int) = quantities[id]!!
    fun xn(id: Int) = objects[id]!!

    operator fun plus(other: TemplateParameters) =
        TemplateParameters(statements + other.statements, quantities + other.quantities, objects + other.objects)

    fun with(thing: MathThing, id: Int) = when (thing) {
        is Statement -> copy(statements = statements.plus(id to thing))
        is Quantity -> copy(quantities = quantities.plus(id to thing))
        is MathObject -> copy(objects = objects.plus(id to thing))
    }
}

val EMPTY_PARAMETERS = TemplateParameters(emptyMap(), emptyMap(), emptyMap())

enum class ParameterType { STATEMENT, QUANTITY, MATH_OBJECT }

fun abbreviationToType(abbreviation: String) = when (abbreviation.toUpperCase()) {
    "ST" -> ParameterType.STATEMENT
    "QQ" -> ParameterType.QUANTITY
    "XN" -> ParameterType.MATH_OBJECT
    else -> throw IllegalArgumentException("bad")
}