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

            var patternIndexes = listOf<Int>()
            val millis = measureTimeMillis {
                var patternIndexes = data[1].patternIndexes(data[0])
            }
            println(
                    "Indexes of :\n${data[0]}\nin:\n${data[1]}\nare:"
                            + "\n${patternIndexes.joinToString(separator = " ")}\n"
                            + "and it took ${millis}ms to calculate."
            )

        }
        else -> println("Unknown command $command")
    }
}

private fun String.patternIndexes(pattern: CharSequence): List<Int> {
    val indexes = mutableListOf<Int>()
    for (index in 0..(length - pattern.length)) {
        if (pattern == substring(index..index + pattern.length - 1)) indexes.add(index)
    }
    return indexes
}

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


fun String.clumps(k: Int, l: Int, t: Int): List<String> {
    TODO("Implement this")
}