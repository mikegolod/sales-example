package ru.mage.sales.aggregator

import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: <sales file> <daily stats file> <office stats file>")
        exitProcess(0)
    }

    val sales = checkSailsFile(args)

    val dailyStats = TreeMap<Long, BigDecimal>()
    val officeStats = HashMap<Int, BigDecimal>()

    sales.bufferedReader().useLines {
        it.map(::parseLine).forEach { sale ->
            dailyStats[sale.date] = dailyStats.getOrDefault(sale.date, BigDecimal.ZERO) + sale.amount
            officeStats[sale.point] = officeStats.getOrDefault(sale.point, BigDecimal.ZERO) + sale.amount
        }
    }

    writeDailyStats(args, dailyStats)
    writeOfficeStats(args, officeStats)
}

fun checkSailsFile(args: Array<String>): File {
    val sales = File(args[0])
    if (!sales.exists()) {
        println("${sales.canonicalPath} does not exists")
        exitProcess(-1)
    }
    if (!sales.canRead()) {
        println("${sales.canonicalPath} cannot be read")
        exitProcess(-1)
    }
    return sales
}

fun writeDailyStats(args: Array<String>, dailyStats: TreeMap<Long, BigDecimal>) {
    val out = File(args[1])
    out.parentFile?.mkdirs()
    out.bufferedWriter().use { out ->
        dailyStats.forEach { date, sum ->
            out.write("${LocalDate.ofEpochDay(date)} $sum\n")
        }
    }
}

fun writeOfficeStats(args: Array<String>, officeStats: HashMap<Int, BigDecimal>) {
    val out = File(args[2])
    out.parentFile?.mkdirs()
    out.bufferedWriter().use { out ->
        officeStats.asSequence().sortedByDescending { it.value }.forEach {
            out.write("${it.key} ${it.value}\n")
        }
    }
}

fun parseLine(line: String): Sale {
    val fields = line.split(',')
    val date = LocalDateTime.parse(fields[0])
    val point = fields[1].toInt()
    val amount = fields[3].toBigDecimal()

    return Sale(date.toLocalDate().toEpochDay(), point, amount)
}

data class Sale(val date: Long, val point: Int, val amount: BigDecimal)

