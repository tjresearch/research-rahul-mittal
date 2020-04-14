package us.tlatoani.comboprover.better_parser

import us.tlatoani.comboprover.*

class SyntaxTemplate<S>(val syntax: List<SyntaxElement>, val constructor: (TemplateParameters) -> S)

fun <S> template(syntax: String, constructor: (TemplateParameters) -> S) = SyntaxTemplate(parseSyntax(syntax), constructor)

val STATEMENT_TEMPLATES = listOf<SyntaxTemplate<Statement>>(
    template("we can choose %xn1% in %qq1% ways") { Equals(Amount(Choice(it.xn(1))), it.qq(1)) },
    template("%xn1% can be matched up with %xn2% in %qq1% ways") { Equals(Amount(Matching(it.xn(1), it.xn(2))), it.qq(1)) },
    template("%qq1% (is|are) %qq2%") { Equals(it.qq(1), it.qq(2)) },
    template("each %xn1% contains %xn2%") { Each(it.xn(1)) { xn -> Contains(xn, it.xn(2)) } },
    template("to choose %xn1% we can [instead] choose %xn2%") { Equals(Amount(Choice(it.xn(1))), Amount(Choice(it.xn(2)))) },
    template("there are %qq1% %xn1%") { Equals(Amount(it.xn(1)), it.qq(1)) },
    template("%qq1% counts %qq2%") { Equals(it.qq(1), it.qq(2)) },
    template("%xn1% contains %xn2%") { Contains(it.xn(1), it.xn(2)) },
    template("%xn1% includes %xn2%") { Contains(it.xn(1), it.xn(2)) },
    template("there are %qq1% other elements in %xn1%") { Equals(it.qq(1), Amount(RemainingElements(it.xn(1)))) },
    template("%xn1% must be chosen from %xn2%") { Contains(it.xn(2), it.xn(1)) },
    template("%qq1% form* %qq2%") { ObjectEquals(it.xn(1), it.xn(2)) },
    template("there are %qq1% to do this") { Equals(it.qq(1), Amount(HangingWays(Unit))) },
    template("both sides of the equation count %qq1%") { And(Equals(SideOfEquation(Side.LEFT), it.qq(1)), Equals(SideOfEquation(Side.RIGHT), it.qq(1))) },
    // meta
    template("since %st1% %st2%") { Since(it.st(1), it.st(2)) },
    template("let %xn1% be %xn2%") { DefineMathObject(it.xn(1), it.xn(2)) },
    template("therefore %st1%") { HangingTherefore(it.st(1)) },
    template("(by definition %st1%|%st1% by definition)") { By(ByConcept.DEFINITION, it.st(1)) },
    template("(by multiplication principle %st1%|%st1% by multiplication principle)") { By(ByConcept.MULTIPLICATION_PRINCIPLE, it.st(1)) },
    template("%st1% so %st2%") { Since(it.st(1), it.st(2)) }
)
val QUANTITY_TEMPLATES = listOf<SyntaxTemplate<Quantity>>(
    template("number of %xn1%") { Amount(it.xn(1)) },
    template("amount of %xn1%") { Amount(it.xn(1)) },
    template("amount") { HangingAmount(Unit) },
    template("at most %qq1%") { Bounded(Infinity(-1), it.qq(1)) },
    template("at least %qq1%") { Bounded(it.qq(1), Infinity(1)) },
    template("[sum on] right [hand] side") { SideOfEquation(Side.RIGHT) },
    template("[sum on] left [hand] side") { SideOfEquation(Side.LEFT) },
    template("(this|desired quantity)") { HangingAmount(Unit) },
    template("how many %xn1% include %xn2%") { Amount(SubjectToCondition(it.xn(1)) { xn -> Contains(xn, it.xn(2)) }) },
    template("how many %xn1% do not include %xn2%") { Amount(SubjectToCondition(it.xn(1)) { xn -> Not(Contains(xn, it.xn(2))) }) },
    template("size* of %xn1%") { Size(it.xn(1)) }
)
val MATH_OBJECT_TEMPLATES = listOf<SyntaxTemplate<MathObject>>(
    template("%qq1% %xn1%") { Multiple(it.qq(1), it.xn(1)) },
    template("%xn1% which will contain %xn2%") { SubjectToCondition(it.xn(1)) { xn -> Contains(xn, it.xn(2)) } },
    template("labelled %xn1%") { LabelledMathObject(it.xn(1)) },
    template("ways to place %xn1% into %xn2%") { Placement(it.xn(1), it.xn(2)) },
    template("%xn1% to exclude from %xn2%") { ForPurpose(it.xn(1)) { xn -> Not(Contains(it.xn(2), xn)) } },
    template("%xn1% of size %qq1%") { OfSize(it.xn(1), it.qq(1)) },
    template("ways to do this") { HangingWays(Unit) },
    template("remaining (elements|portion) of %xn1%") { RemainingElements(it.xn(1)) },
    template("ways to choose %xn1% from %xn2%") { ChoiceFrom(it.xn(1), it.xn(2)) },
    template("%xn1% where %st1%") { Qualified(it.xn(1), it.st(1)) },
    template("%qq1% of %xn1%") { Multiple(it.qq(1), Element(it.xn(1))) },
    template("part of %xn1%") { Part(it.xn(1)) },
    template("subset* of %xn1%") { Subset(it.xn(1)) },
    template("largest %xn1%") { Largest(it.xn(1)) }
)
val INTENT_TEMPLATES = listOf<SyntaxTemplate<Intent>>(
    template("we will show that %st1%") { Show(it.st(1)) },
    template("we will count %qq1%") { Determine(it.qq(1)) },
    template("we choose %xn1% from %xn2% to form %xn3%") { ChooseFor(ChoiceFrom(it.xn(1), it.xn(2)), it.xn(3)) },
    template("we choose %qq1% of %xn2% to form %xn3%") { ChooseFor(Choice(Multiple(it.qq(1), it.xn(2))), it.xn(3)) }
)

val ARBITRARY_MATH_OBJECT_WORDS = listOf("ball", "urn", "subset", "student", "committee", "subcommittee", "body", "element")