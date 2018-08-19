package ru.mage.sales.generator

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.util.*
import kotlin.math.absoluteValue

class SaleGenerator(private val offices: Array<Int>): Iterable<Sale> {

    override fun iterator(): Iterator<Sale> {
        assert(offices.isNotEmpty())
        val currentYear = LocalDate.now().year
        val maxDate = LocalDate.of(currentYear, 1, 1)
        val minDate = LocalDate.of(currentYear - 1, 1, 1)
        return SaleIterator(offices, minDate, maxDate)
    }

}

private class SaleIterator(val offices: Array<Int>, minDate: LocalDate, maxDate: LocalDate): Iterator<Sale> {

    private val random = Random()
    private val minEpochDate = minDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    private val timeGap = maxDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - minEpochDate

    override fun hasNext() = true

    override fun next(): Sale {
        val officeId = offices[random.nextInt().absoluteValue % offices.size]
        return Sale(randomDate(), officeId, random.nextLong().absoluteValue, randomSum())
    }

    private fun randomSum(): BigDecimal {
        val sum = random.nextDouble() * (100_000 - 10_000) + 10_000
        return BigDecimal(sum).setScale(2, RoundingMode.HALF_UP)
    }

    private fun randomDate(): LocalDateTime {
        val epochSecond = random.nextLong().absoluteValue % timeGap + minEpochDate
        return Instant.ofEpochSecond(epochSecond).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

}

data class Sale(val date: LocalDateTime, val officeId: Int, val transactionId: Long, val sum: BigDecimal) {
    fun toCsv(): String {
        return "$date,$officeId,$transactionId,$sum"
    }
}