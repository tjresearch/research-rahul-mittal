package us.tlatoani.comboprover

class Debug(val message: String) {

    infix fun <T> out(t: T): T {
        println(message)
        return t
    }
}