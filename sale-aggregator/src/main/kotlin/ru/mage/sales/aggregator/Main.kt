package ru.mage.sales.aggregator

import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: <sales file> <daily stats file> <office stats file>")
        exitProcess(0)
    }

    val sales = checkSailsFile(args)

    val dailyStats = HashMap<Long, BigDecimal>()
    val officeStats = HashMap<Int, BigDecimal>()

    sales.bufferedReader().useLines {
        it.map(::parseLine).forEach { sale ->
            dailyStats.merge(sale.date, sale.amount, BigDecimal::add)
            officeStats.merge(sale.point, sale.amount, BigDecimal::add)
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

fun writeDailyStats(args: Array<String>, dailyStats: Map<Long, BigDecimal>) {
    val out = File(args[1])
    out.parentFile?.mkdirs()
    out.bufferedWriter().use { writer ->
        dailyStats.toSortedMap().forEach { date, sum ->
            writer.write("${LocalDate.ofEpochDay(date)} $sum\n")
        }
    }
}

fun writeOfficeStats(args: Array<String>, officeStats: Map<Int, BigDecimal>) {
    val out = File(args[2])
    out.parentFile?.mkdirs()
    out.bufferedWriter().use { writer ->
        officeStats.asSequence().sortedByDescending { it.value }.forEach { (key, value) ->
            writer.write("$key $value\n")
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

