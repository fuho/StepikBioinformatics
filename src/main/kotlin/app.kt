import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val command = args[0]
    val data = File(args[1]).inputStream().bufferedReader().readLines()
    when (command) {
        "k-mers" ->
            println("K-mers of length '${data[1]}' found in dataset: ${data[0].longestKMers(data[1].toInt()).joinToString(separator = " ")}")
        "sequence" ->
            println("Sequence ${data[1]} found in dataset ${data[0].countSequence(data[1])} times.")
        "complement" ->
            println("Complement to:\n${data[0]}\nis:\n${data[0].reverseComplement}")
        "indexes" -> {
            var patternIndexes = setOf<Int>()
            val millis = measureTimeMillis {
                patternIndexes = data[1].patternIndexes(data[0])
            }
            println("""
                |Indexes of :
                |${data[0]}
                |in:
                |${data[1]}
                |are:
                |${patternIndexes.joinToString(separator = " ")}
                |and it took ${millis}ms to calculate.""".trimMargin()
            )
        }
        "clumps" -> {
            var clumps = setOf<String>()
            val sequence: String = data[0]
            val (k, l, t) = data[1].split(" ").map { it.toInt() }
            val millis = measureTimeMillis {
                clumps = sequence.clumps(k, l, t)
            }
            println("""
                |k:$k, L:$l, t:$t clumps of
                |${sequence}
                |are
                |${clumps.joinToString(separator = " ")}
                |and it took ${millis}ms to calculate.""".trimMargin()
            )
        }
        else -> println("Unknown command $command")
    }
}

fun String.patternIndexes(pattern: CharSequence): Set<Int> {
    val indexes = mutableSetOf<Int>()
    for (index in 0..(length - pattern.length)) {
        if (pattern == substring(index..index + pattern.length - 1)) indexes.add(index)
    }
    return indexes
}

fun String.patternCount(pattern: CharSequence): Int = patternIndexes(pattern).size


fun String.countSequence(sequence: CharSequence): Int = patternIndexes(sequence).count()

fun String.longestKMers(k: Int): List<String> =
        List(length - k + 1, { i -> substring(i..i + k - 1) })
                .groupBy { it }
                .map { (key, value) -> Pair(value.count(), key) }
                .groupBy { it.first }
                .maxBy { it.key }
                ?.value
                ?.map { it.second }
                ?.sorted()
                ?: listOf()

fun String.kMers(k: Int): Map<Int, Set<String>> =
        List(length - k + 1, { i -> substring(i..i + k - 1) })
                .groupBy { it } // "ab" to ["ab","ab"]
                .map { it.value.count() to it.key }
                .groupBy { it.first }
                .map { it.key.toInt() to it.value.map { it.second }.toSet() }
                .toMap()

val String.reverseComplement: String
    get() =
        toCharArray()
                .reversed()
                .map { hashMapOf("A" to "T", "G" to "C", "C" to "G", "T" to "A").get(it.toString()) }
                .joinToString(separator = "")


fun String.clumps(k: Int, l: Int, t: Int): Set<String> =
        kMers(k)
                .filter { it.key >= t }
                .flatMap { it.value }
                .filter { kMer ->
                    List(length - l + 1, { i -> substring(i..i + l - 1) })
                            .any { lSection -> lSection.patternCount(kMer) >= t }
                }.toSet()