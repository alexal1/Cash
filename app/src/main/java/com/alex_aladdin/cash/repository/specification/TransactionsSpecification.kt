package com.alex_aladdin.cash.repository.specification

import com.alex_aladdin.cash.repository.entities.Transaction

interface TransactionsSpecification {

    fun isSatisfiedBy(transaction: Transaction): Boolean

}