package ru.mage.sales.generator

import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: <offices file> <sales count> <sales output file>")
        System.exit(-1)
    }

    val options = parseCmdArgs(args)
    val offices = loadOffices(options.offices)
    val saleGenerator = SaleGenerator(offices)
    options.sales.bufferedWriter().use { writer ->
        saleGenerator.asSequence().take(options.totalSales).forEach { sale ->
            writer.write(sale.toCsv())
            writer.newLine()
        }
    }

}

data class Options(val totalSales: Int, val offices: File, val sales: File)

fun parseCmdArgs(args: Array<String>): Options {
    val offices = File(args[0])
    if (!offices.exists()) {
        println("${offices.canonicalPath} does not exists")
        exitProcess(-1)
    }
    val totalSales = try {
         args[1].toInt()
    } catch (e: NumberFormatException) {
        println(e.localizedMessage)
        exitProcess(-1)
    }

    return Options(totalSales, offices, File(args[2]))
}

fun loadOffices(input: File): Array<Int> {
    try {
        return input.readLines().map(String::toInt).toTypedArray()
    } catch (e: IOException) {
        println(e.localizedMessage)
        exitProcess(-1)
    }
}
