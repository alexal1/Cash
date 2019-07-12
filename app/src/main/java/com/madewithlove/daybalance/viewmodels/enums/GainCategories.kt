package com.madewithlove.daybalance.viewmodels.enums

import com.madewithlove.daybalance.R

enum class GainCategories : Categories {

    INHERITANCE {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_inheritance
    },

    LOTTERY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_lottery
    },

    GIFT {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_gift
    },

    SALE_OF_A_PROPERTY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_sale_of_a_property
    },

    YOU_BORROWED_SOME_MONEY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_you_borrowed_some_money
    },

    YOUR_LOAN_WAS_PAID_BACK {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_your_loan_was_paid_back
    },

    REWARD {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_reward
    },

    PASSIVE_INCOME {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_passive_income
    },

    SCHOLARSHIP_AND_GRANTS {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_scholarship_and_grants
    },

    ALIMONY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_alimony
    },

    DIVIDENDS {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_dividends
    },

    BUSINESS {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_business
    },

    FINANCIAL_AID {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_financial_aid
    },

    SALARY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_salary
    },

    RENTING_OUT_A_PROPERTY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_renting_out_a_property
    },

    CASHBACK {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_cashback
    },

    DEPOSIT_INTEREST {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_deposit_interest
    },

    TUTORING {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_tutoring
    },

    FREELANCE {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_freelance
    },

    PART_TIME_JOB {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_part_time_job
    },

    OTHER {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_other
    }

}