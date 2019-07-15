package com.madewithlove.daybalance.viewmodels.enums

interface Categories {

    companion object {

        fun findById(id: String, isGain: Boolean): Categories = if (isGain) {
            GainCategories.values().find { it.id == id } as Categories
        } else {
            LossCategories.values().find { it.id == id } as Categories
        }

    }

    val id: String
    val isGain: Boolean
    val stringRes: Int

}