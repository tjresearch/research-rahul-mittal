package us.tlatoani.comboprover

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import us.tlatoani.comboprover.better_parser.PLACEHOLDERS
import us.tlatoani.comboprover.better_parser.ParseResult
import us.tlatoani.comboprover.better_parser.parseStatementOrIntent
import us.tlatoani.comboprover.better_parser.parseSyntax
import java.io.File
import java.util.*

val SAMPLE_PROOF_FILENAME = "sample_proof_4.txt"
lateinit var pipeline: StanfordCoreNLP

fun main7() {
    print("Enter filename of plaintext proof: input/")
    val inFilename = "input/" + readLine()!!
    //print("Enter desired output filename: output/")
    //val outFilename = "output/" + readLine()!!
    val scanner = Scanner(File(inFilename))
    val proofJoiner = StringJoiner(" ")
    while (scanner.hasNext()) {
        val line = scanner.nextLine().trim()
        if (line.isEmpty()) {
            proofJoiner.add("\n")
        } else {
            proofJoiner.add(line)
        }
    }
    val proof = proofJoiner.toString().replace(" \n ", "\n")
    val sentences = proof.split(".").toMutableList()
    data class Output(val sentence: String, val totalTokens: Int, val res: Pair<ParseResult<Statement>?, ParseResult<Intent>?>)
    val output = mutableListOf<Output>()
    val formulaParser = FormulaParser()
    val tokenizedSentences = mutableListOf<List<String>>()
    for (rawSentence in sentences) {
        val sentence = rawSentence.trim().toLowerCase()
        if (sentence.isNotEmpty()) {
            var k = 0
            val formulae = mutableMapOf<Char, Quantity>()
            val tokens = mutableListOf<String>()
            var token = ""
            //val builder = StringBuilder()
            var j = 0
            while (j < sentence.length) {
                when {
                    sentence[j] == '$' -> {
                        if (token.isNotEmpty()) {
                            tokens.add(token)
                            token = ""
                        }
                        tokens.add(PLACEHOLDERS[k] + "")
                        val end = sentence.indexOf('$', j + 1)
                        formulae[PLACEHOLDERS[k]] = formulaParser.parseFormula(
                            sentence.substring(j + 1, end))
                        j = end + 1
                        k++
                    }
                    sentence[j].isLetterOrDigit() -> {
                        token += sentence[j]
                        j++
                    }
                    sentence[j].isWhitespace() -> {
                        if (token.isNotEmpty()) {
                            tokens.add(token)
                            token = ""
                        }
                        j++
                    }
                    ",.:;.?!".contains(sentence[j]) -> {
                        if (token.isNotEmpty()) {
                            tokens.add(token)
                            token = ""
                        }
                        tokens.add(sentence[j].toString())
                        j++
                    }
                    else -> j++
                }
            }
            if (token.isNotEmpty()) {
                tokens.add(token)
            }
            //println("bosh")
            //println("tokens = $tokens")
            //println("formulae = $formulae")
            tokenizedSentences.add(tokens)
            output.add(Output(sentence, tokens.size, parseStatementOrIntent(tokens, formulae)))
        }
    }
    println("output = $output")
    val sentenceStatements = mutableListOf<Statement>()
    val tokenizations = mutableListOf<List<String>>()
    for (j in 0 until output.size) {
        val p = output[j]
        if (p.res.first != null && p.res.first!!.unusedWords < p.res.second?.unusedWords ?: Int.MAX_VALUE) {
            sentenceStatements.add(p.res.first!!.s)
            tokenizations.add(tokenizedSentences[j])
        }
    }
    //getStatementsAndImplications(sentenceStatements, tokenizations)
}

fun main() {
    print("Enter filename of plaintext proof: input/")
    val inFilename = "input/" + readLine()!!
    print("Enter desired output filename: output/")
    val outFilename = "output/" + readLine()!!
    val scanner = Scanner(File(inFilename))
    val proofJoiner = StringJoiner(" ")
    while (scanner.hasNext()) {
        val line = scanner.nextLine().trim()
        if (line.isEmpty()) {
            proofJoiner.add("\n")
        } else {
            proofJoiner.add(line)
        }
    }
    val proof = proofJoiner.toString().replace(" \n ", "\n")
    val sentences = proof.split(".").toMutableList()
    data class Output(val sentence: String, val totalTokens: Int, val res: Pair<ParseResult<Statement>?, ParseResult<Intent>?>)
    val output = mutableListOf<Output>()
    val formulaParser = FormulaParser()
    for (rawSentence in sentences) {
        val sentence = rawSentence.trim().toLowerCase()
        if (sentence.isNotEmpty()) {
            var k = 0
            val formulae = mutableMapOf<Char, Quantity>()
            val tokens = mutableListOf<String>()
            var token = ""
            //val builder = StringBuilder()
            var j = 0
            while (j < sentence.length) {
                when {
                    sentence[j] == '$' -> {
                        if (token.isNotEmpty()) {
                            tokens.add(token)
                            token = ""
                        }
                        tokens.add(PLACEHOLDERS[k] + "")
                        val end = sentence.indexOf('$', j + 1)
                        formulae[PLACEHOLDERS[k]] = formulaParser.parseFormula(
                            sentence.substring(j + 1, end))
                        j = end + 1
                        k++
                    }
                    sentence[j].isLetterOrDigit() -> {
                        token += sentence[j]
                        j++
                    }
                    sentence[j].isWhitespace() -> {
                        if (token.isNotEmpty()) {
                            tokens.add(token)
                            token = ""
                        }
                        j++
                    }
                    else -> j++
                }
            }
            if (token.isNotEmpty()) {
                tokens.add(token)
            }
            //println("bosh")
            //println("tokens = $tokens")
            //println("formulae = $formulae")
            output.add(Output(sentence, tokens.size, parseStatementOrIntent(tokens, formulae)))
        }
    }
    val out = StringBuilder()
    for (p in output) {
        out.appendln("sentence = \"${p.sentence}\"")
        if (p.res.first == null && p.res.second == null) {
            out.appendln("Empty sentence")
        } else if (p.res.first?.unusedWords ?: Int.MAX_VALUE < p.res.second?.unusedWords ?: Int.MAX_VALUE) {
            out.appendln("unusedWords = ${p.res.first!!.unusedWords} / ${p.totalTokens}")
            out.appendln("statement = ${dataStringWithNewlines(p.res.first!!.s.toString())}")
        } else {
            out.appendln("unusedWords = ${p.res.second!!.unusedWords} / ${p.totalTokens}")
            out.appendln("intent = ${dataStringWithNewlines(p.res.second!!.s.toString())}")
        }
        out.appendln()
    }
    File(outFilename).printWriter().use { it.print(out) }
}

fun dataStringWithNewlines(string: String): String {
    var builder = StringBuilder()
    var indentation = 0
    var j = 0
    while (j < string.length) {
        val chara = string[j]
        if (chara == ')') {
            indentation--
            builder.append('\n')
            for (l in 1..indentation) {
                builder.append("    ")
            }
        }
        builder.append(chara)
        if (chara == '(') {
            indentation++
            builder.append('\n')
            for (l in 1..indentation) {
                builder.append("    ")
            }
        }
        if (chara == ',') {
            builder.append('\n')
            for (l in 1..indentation) {
                builder.append("    ")
            }
            if (j < string.length - 1 && string[j + 1] == ' ') {
                j++
            }
        }
        j++
    }
    return builder.toString()
}

fun main5() {
    while (true) {
        print("Enter syntax: ")
        val sentence = readLine()!!
        println(parseSyntax(sentence))
    }
}

/*fun main4() {
    while (true) {
        print("Enter stuff (<<<>><>): ")
        val sentence = readLine()!!
        var k = 0
        val formulae = mutableMapOf<Char, Quantity>()
        val builder = StringBuilder()
        var j = 0
        val formulaParser = FormulaParser()
        while (j < sentence.length) {
            if (sentence[j] == '$') {
                builder.append(PLACEHOLDERS[k])
                val end = sentence.indexOf('$', j + 1)
                formulae[PLACEHOLDERS[k]] = formulaParser.parseFormula(
                    sentence.substring(j + 1, end))
                j = end + 1
                k++
            } else {
                builder.append(sentence[j])
                j++
            }
        }
        println("bosh")
        val tokens = builder.toString().replace(Regex("[\\.,;:]"), "").toLowerCase().split(" ")
        println("tokens = $tokens")
        println("formulae = $formulae")
        val output = parseStatement(tokens, formulae).toString()
        var indentation = 0
        for (k in 0 until output.length) {
            if (output[k] == ')') {
                indentation--
                println()
                for (l in 1..indentation) {
                    print("    ")
                }
            }
            print(output[k])
            if (output[k] == '(') {
                indentation++
                println()
                for (l in 1..indentation) {
                    print("    ")
                }
            }
            if (output[k] == ',') {
                println()
                for (l in 1..indentation) {
                    print("    ")
                }
            }
        }
        println("statement = ${parseStatement(tokens, formulae)}")
    }
}*/

/*fun main3() {
    val props = Properties()
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote")
    //props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,kbp")
    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
    props.setProperty("coref.algorithm", "neural")
    // build pipeline
    println("Creating a pipeline...")
    pipeline = StanfordCoreNLP(props)
    println("Created the pipeline")
    while (true) {
        print("ohayo sekai, Enter the sentence to parse: ")
        val sentence = readLine()!!
        if (sentence.toLowerCase().equals("quit") || sentence.toLowerCase().equals("exit")) {
            println("Bai")
            return
        }
        val document = CoreDocument(sentence)
        println("Created a Boredocument")
        // annnotate the document
        println("About to annotate...")
        pipeline.annotate(document)
        println("Finished annotating")

        println("Example: constituency parse")
        val constituencyParse = document.sentences()[0].constituencyParse()
        println(constituencyParse)
        println()
        printConstituencyParse(constituencyParse)
        println()
        println("WOAH STATEMENTNTEWF IDENTIFICATION //|sentence 2/1|")
        //println(identifyActions(listOf(document.sentences()[0].constituencyParse())))
        val statements = getStatements(listOf(document.sentences()[0].constituencyParse()))
        if (statements.isEmpty()) {
            println("No statements =( you should like do stuff and yeah")
        } else {
            println("¡=>Statements~=!")
            for (statementNode in statements) {
                println("${statementNode.statement}: ${statementNode.node}")
            }
        }
    }
}*/

fun main2() {
    val formulaParser = FormulaParser()
    while (true) {
        print("Enter a formula: ")
        val formula = readLine()!!
        println(prepareFormula(formula))
        println(formulaParser.parseFormula(formula))
    }
}

/*fun main1() {
    val props = Properties()
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote")
    //props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,kbp")
    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
    props.setProperty("coref.algorithm", "neural")
    // build pipeline
    println("Creating a pipeline...")
    pipeline = StanfordCoreNLP(props)
    println("Created the pipeline")
    while (true) {
        print("ohayo sekai, Enter the sentence to parse: ")
        val sentence = readLine()!!
        if (sentence.toLowerCase().equals("quit") || sentence.toLowerCase().equals("exit")) {
            println("Bai")
            return
        }
        val document = CoreDocument(sentence)
        println("Created a Boredocument")
        // annnotate the document
        println("About to annotate...")
        pipeline.annotate(document)
        println("Finished annotating")

        println("Example: constituency parse")
        val constituencyParse = document.sentences()[0].constituencyParse()
        /*val constS = getChildWithLabel(constituencyParse, "S")!!
        val constVP = getChildWithLabel(constS, "VP")!!
        val constVerb = constVP.children().find { child -> child.label().value().startsWith("VB") }!!
        println("verb = " + constVerb.childrenAsList[0])*/
        //println("VP dependences = " + constVP.dependencies())
        //println("verb dependencies = " + constVerb.dependencies())
        println(constituencyParse)
        println()
        printConstituencyParse(constituencyParse)
        println()

        //println("Example: dependency parse")
        //println(document.sentences()[0].dependencyParse())
        /*println("Example: roots of dependency parse")
        println(document.sentences()[0].dependencyParse().roots)
        val graph = document.sentences()[0].dependencyParse()
        val bfs = LinkedList<IndexedWord>()
        val visited = mutableSetOf<IndexedWord>()
        bfs.addAll(graph.roots)
        while (!bfs.isEmpty()) {
            val node = bfs.remove()
            if (visited.contains(node)) {
                continue
            }
            visited.add(node)
            if (node.tag().startsWith("VB")) {
                graph.setRoot(node)
                break
            }
            bfs.addAll(graph.getChildren(node))
        }
        println("Example: dependency parse")
        println(document.sentences()[0].dependencyParse())*/

        println("WOAH ACTION IDENTIFICATION //|sentence 2/1|")
        //println(identifyActions(listOf(document.sentences()[0].constituencyParse())))
        val actions = identifyActions(listOf(document.sentences()[0].constituencyParse()))
        if (actions.isEmpty()) {
            println("No actions =( you should like do stuff and yeah")
        }
        for (actionNode in actions) {
            println("${actionNode.action}: ${actionNode.node}")
        }
    }
}

fun main0() {
    val scanner = Scanner(Thread.currentThread().contextClassLoader.getResourceAsStream(SAMPLE_PROOF_FILENAME)!!)
    val proofJoiner = StringJoiner(" ")
    while (scanner.hasNext()) {
        val line = scanner.nextLine().trim()
        if (line.isEmpty()) {
            proofJoiner.add("\n")
        } else {
            proofJoiner.add(line)
        }
    }
    val proof = proofJoiner.toString().replace(" \n ", "\n")
    println("========PROOF========")
    println(proof)
    println("--------oHaYo--------")
    scanner.close()
    //println("oHaYo")
    // set up pipeline properties
    val props = Properties()
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote")
    //props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,kbp")
    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
    props.setProperty("coref.algorithm", "neural")
    // build pipeline
    println("Creating a pipeline...")
    val pipeline = StanfordCoreNLP(props)
    println("Created the pipeline")
    // create a document object
    val document = CoreDocument(proof)
    println("Created a Boredocument")
    // annnotate the document
    println("About to annotate...")
    pipeline.annotate(document)
    println("Finished annotating")

    /*
    // second sentence
    val sentence = document.sentences()[4]

    // list of the part-of-speech tags for the second sentence
    val posTags = sentence.posTags()
    println("Example: pos tags")
    println(posTags)
    println()

    println("Relations: ${sentence.relations()}")
    sentence.*/

    // 10th token of the document
    val token = document.tokens()[10]
    println("Example: token")
    println(token)
    println()

    // text of the first sentence
    val sentenceText = document.sentences()[0].text()
    println("Example: sentence")
    println(sentenceText)
    println()

    // second sentence
    val sentence = document.sentences()[1]

    // list of the part-of-speech tags for the second sentence
    val posTags = sentence.posTags()
    println("Example: pos tags")
    println(posTags)
    println()

    // list of the ner tags for the second sentence
    val nerTags = sentence.nerTags()
    println("Example: ner tags")
    println(nerTags)
    println()

    // constituency parse for the fifth sentence
    val constituencyParse = document.sentences()[1].constituencyParse()
    println("Example: constituency parse")
    println(constituencyParse)
    println()
    printConstituencyParse(constituencyParse)
    println()

    // dependency parse for the fifth sentence
    for (i in 0 until document.sentences().size) {
        println("Dependency parse for sentence ${i + 1}")
        println(document.sentences()[i].dependencyParse())
    }
    println("Example: Tag for root of dependency tree of sentence 2")
    println(document.sentences()[1].dependencyParse().firstRoot.tag())

    println("Example: root verb maybe???? word")
    val dep = sentence.dependencyParse()
    println(dep.getChildList(dep.firstRoot)[1])
    println(dep.getChildList(dep.firstRoot)[1].word())

    /*val dependencyParse = document.sentences()[1].dependencyParse()
    println("Example: dependency parse")
    println(dependencyParse)
    println()*/

    /*
    // kbp relations found in fifth sentence
    val relations = document.sentences()[1].relations()
    println("Example: relation")
    System.out.println(relations[0])
    println()
    */

    /*
    // entity mentions in the second sentence
    val entityMentions = sentence.entityMentions()
    println("Example: entity mentions")
    println(entityMentions)
    println()

    // coreference between entity mentions
    val originalEntityMention = document.sentences()[0].entityMentions()[1]
    println("Example: original entity mention")
    println(originalEntityMention)
    println("Example: canonical entity mention")
    println(originalEntityMention.canonicalEntityMention().get())
    println()

    // get document wide coref info
    val corefChains = document.corefChains()
    println("Example: coref chains for document")
    println(corefChains)
    println()
    */

    /*
    // get quotes in document
    val quotes = document.quotes()
    val quote = quotes[0]
    println("Example: quote")
    println(quote)
    println()

    // original speaker of quote
    // note that quote.speaker() returns an Optional
    println("Example: original speaker of quote")
    println(quote.speaker().get())
    println()

    // canonical speaker of quote
    println("Example: canonical speaker of quote")
    println(quote.canonicalSpeaker().get())
    println()
    */
}

fun printConstituencyParse(tree: Tree) {
    printConstituencyParse(tree, "", "")
}

fun printConstituencyParse(tree: Tree, indentation: String, suffix: String) {
    if (tree.children()[0].children().isEmpty()) {
        println(indentation + tree.toString() + suffix)
    } else {
        println(indentation + "(${tree.label()}")
        for (i in 0..tree.children().size - 2) {
            printConstituencyParse(tree.children()[i], indentation + if (indentation.length % 2 == 0) " | " else " * ", "")
        }
        printConstituencyParse(tree.children().last(), indentation + if (indentation.length % 2 == 0) " | " else " * ", suffix + ")")
    }
}*/