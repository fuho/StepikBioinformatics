import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap

fun main(args: Array<String>) {
    args.forEach { println(it) }
    val inputStream: InputStream = File(args[0]).inputStream()

    val inputs = inputStream.bufferedReader().readLines()
    val dataSet = inputs[0]
    val sequence = inputs[1]
    //println("Sequence '$sequence' found in dataset ${dataSet.countSequence(sequence)} times.")
    println("K-mers of length '$sequence' found in dataset: ${dataSet.longestKMers(sequence.toInt())}")
}

fun String.countSequence(sequence: CharSequence): Int {
    var count = 0
    for (index in 0..(length - sequence.length)) {
        if (sequence == this.substring(index..index + sequence.length - 1)) count++
    }
    return count
}

fun String.longestKMers(k: Int): String =
    List(length - k + 1, { i -> substring(i..i + k - 1) })
            .groupBy { it }
            .map { (key, value) -> Pair(value.count(), key) }
            .groupBy { it.first }
            .maxBy { it.key }
            ?.value
            ?.map { it.second }
            ?.sorted()
            ?.joinToString(separator = " ")
            ?: ""
