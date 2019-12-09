package us.tlatoani.comboprover.archive

val BIT_AMT = IntArray(65536) { mask ->
    var mTemp = mask
    var res = 0
    while (mTemp != 0) {
        mTemp -= mTemp and -mTemp
        res++
    }
    res
}

fun bitAmt(k: Int): Int = BIT_AMT[k and 65535] + BIT_AMT[(k shr 16) and 65535]