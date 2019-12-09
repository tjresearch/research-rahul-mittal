package us.tlatoani.comboprover

class Debug(val message: String) {

    infix fun <T> out(t: T): T {
        println(message)
        return t
    }
}

fun <S> List<S>.subList(from: Int) = subList(from, size)