package us.tlatoani.comboprover

class Trie<K, V>(val depth: Int) {
    val children = mutableMapOf<K, Trie<K, V>>()
    val terminal = mutableListOf<V>()

    fun add(ks: List<K>, v: V) {
        if (ks.isEmpty()) {
            terminal.add(v)
        } else {
            children.computeIfAbsent(ks[0]) { Trie(depth + 1) }.add(ks.subList(1), v)
        }
    }
}