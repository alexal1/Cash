/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels.cache

import com.madewithlove.daybalance.repository.entities.Transaction

/**
 * All data that is cached for a single Moment. Moment is simply an index of a day.
 */
data class MomentData(val transactions: List<Transaction>, val realBalance: Double)