package com.alex_aladdin.cash.viewmodels.cache

import com.alex_aladdin.cash.repository.entities.Transaction

/**
 * All data that is cached for a single Moment. Moment is simply an index of a day.
 */
data class MomentData(val transactions: List<Transaction>, val realBalance: Double)