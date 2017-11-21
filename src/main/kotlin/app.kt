import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val command = args[0]
    when (command) {
        "k-mers old" -> {
            val data = File(args[1]).inputStream().bufferedReader().readLines()
            println("K-mers of length '${data[1]}' found in dataset: ${data[0].longestKMers(data[1].toInt()).joinToString(separator = " ")}")
        }
        "k-mers" -> {
            if (args.size < 5) {
                println("Invalid input: ${args.joinToString { " " }}")
                return
            }
            val fileName = args[1]
            val k = args[2].toInt()
            val l = args[3].toInt()
            val t = args[4].toInt()
            val dataset: ByteArray = File(fileName).readBytes()
            var kMers: Map<String, List<Int>> = mapOf()
            var clumps: Map<String, List<Int>> = mapOf()
            val kMersMs = measureTimeMillis { kMers = dataset.kMersOfTPlus(k, t) }
            println("Found ${kMers.size} ${k}-mers with t >= ${t} in ${kMersMs}ms.")
            val clumpsMs = measureTimeMillis { clumps = kMers.clumps(l, t) }
            println("Found ${clumps.size} ${l}-clumped ${k}-mers with t >= ${t} in ${clumpsMs}ms.")
        }
        "sequence" -> {
            val data = File(args[1]).inputStream().bufferedReader().readLines()
            println("Sequence ${data[1]} found in dataset ${data[0].countSequence(data[1])} times.")
        }
        "complement" -> {
            val data = File(args[1]).inputStream().bufferedReader().readLines()
            println("Complement to:\n${data[0]}\nis:\n${data[0].reverseComplement}")
        }
        "indexes" -> {
            val data = File(args[1]).inputStream().bufferedReader().readLines()
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
            val data = File(args[1]).inputStream().bufferedReader().readLines()
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
        kMers(k).filter { it.key >= t }
                .flatMap { it.value }
                .filter { kMer ->
                    List(length - l + 1, { i -> substring(i..i + l - 1) })
                            .any { lSection -> lSection.patternCount(kMer) >= t }
                }.toSet()

fun ByteArray.kMers(k: Int): Map<String, List<Int>> {
    val result: TreeMap<String, MutableList<Int>> = TreeMap()
    var start = 0
    var end = k - 1
    while (end <= size) {
        val indexes: MutableList<Int> = result.getOrPut(String(sliceArray(start..end))) { mutableListOf() }
        indexes.add(start)
        start++
        end++
    }
    return result
}

fun ByteArray.kMersOfTPlus(k: Int, t: Int): Map<String, List<Int>> = kMers(k).filterValues { it.size >= t }

fun Map<String, List<Int>>.clumps(l: Int, t: Int) = filter { (kMer, indexes) ->
    val maxIndex = indexes.size - 1
    val filterResult = indexes.withIndex().any { (indexI, indexV) ->
        val shiftedIndexI = indexI + t - 1
        if (shiftedIndexI > maxIndex) return@any false
        val shiftedIndexV = indexes[shiftedIndexI]
        shiftedIndexV - indexV + kMer.length <= l
    }
    return@filter filterResult
}