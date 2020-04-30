/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.model

import com.madewithlove.daybalance.repository.TransactionsRepository
import java.util.*

interface Cache : TransactionsRepository {

    fun invalidate(dates: Set<Date> = emptySet())

}