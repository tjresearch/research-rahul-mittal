package us.tlatoani.comboprover

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.PrintWriter
import java.lang.IllegalArgumentException
import java.util.*

const val FACTORIAL_NAME = "fact"

fun prepareFormula(formula: String): String {
    var res = ""
    var i = 0
    while (i < formula.length) {
        if (formula[i].isDigit()) {
            var k = 0
            while (i < formula.length && formula[i].isDigit()) {
                k = (10 * k) + (formula[i] - '0')
                i++
            }
            res += "(Constant $k)"
        } else {
            if (formula[i] == '=') {
                res += "==="
            } else if (formula[i] == '>') {
                res += ">>>"
            } else if (formula[i] == '<') {
                res += "<<<"
            } else if (formula[i] == '+') {
                res += "+++"
            } else if (formula[i] == '*') {
                res += "***"
            } else if (formula[i] == '-') {
                res += "`Difference`"
            } else if (formula[i] == '/') {
                res += "`Quotient`"
            } else if (formula[i].isLetter() && (!FACTORIAL_NAME.contains(formula[i]) || "----$formula----".substring(
                    4 + i - FACTORIAL_NAME.indexOf(
                        formula[i]
                    )
                ).substring(0, 4) != "fact")
            ) {
                res += "(Variable '${formula[i]}')"
            } else {
                res += formula[i]
            }
            i++
        }
    }
    return res
}

const val FORMULAE_HS = "formulae.hs"

class FormulaParser {
    val process: Process
    val pin: Scanner
    val pout: PrintWriter

    init {
        process = ProcessBuilder("ghci", "formulae.hs").start()
        println("process started")
        pin = Scanner(process.inputStream)
        pout = PrintWriter(process.outputStream)
        //val scanner = Scanner(Thread.currentThread().contextClassLoader.getResourceAsStream(FORMULAE_HS)!!)
        /*while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            println("Found a line: $line")
            pout.println(line)
        }
        println("Done with those lines")*/
        while (pin.hasNextLine()) {
            val line = pin.nextLine()
            println("Found a line over here: $line")
            if (line.contains("Ok, modules")) {
                break
            }
        }
        println("Done with these lines")
        //while (!pin.nextLine().contains("Prelude"));
    }

    fun parseFormula(formula: String): Quantity {
        println("ohayo")
        pout.println("putStrLn $ json $ ${prepareFormula(formula)}")
        pout.flush()
        println("gozaimasu")
        /*while (pin.hasNext()) {
            println("here's the next: ${pin.next()}")
        }*/
        //println("line hopefully: ${pin.nextLine()}")
        val jsonStr = pin.nextLine().substring(7)
        println("jsonStr = $jsonStr")
        return jsonToQuantity(JSONParser().parse(jsonStr))
    }
}

fun jsonToQuantity(json: Any): Quantity = when (json) {
    is Int -> Constant(json.toLong())
    is String -> Variable(json)
    is JSONObject -> when ((json["type"] as String).toLowerCase()) {
        "sum" -> Sum((json["addends"] as JSONArray).map { elem -> jsonToQuantity(elem!!) })
        "product" -> Product((json["mands"] as JSONArray).map { elem -> jsonToQuantity(elem!!) })
        "difference" -> Difference(jsonToQuantity(json["left"]!!), jsonToQuantity(json["right"]!!))
        "quotient" -> Quotient(jsonToQuantity(json["top"]!!), jsonToQuantity(json["bottom"]!!))
        "factorial" -> Factorial(jsonToQuantity(json["of"]!!))
        "combination" -> Combination(jsonToQuantity(json["top"]!!), jsonToQuantity(json["bottom"]!!))
        else -> throw IllegalArgumentException("Invalid type ${json["type"]}")
    }
    else -> throw IllegalArgumentException("Has to be json1!!1!1!! not json: $json")
}