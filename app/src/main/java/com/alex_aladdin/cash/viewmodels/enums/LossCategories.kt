package com.alex_aladdin.cash.viewmodels.enums

import com.alex_aladdin.cash.R

enum class LossCategories : Categories {

    REAL_ESTATE_PURCHASE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_real_estate_purchase
    },

    FURNITURE_AND_RENOVATION {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_furniture_and_renovation
    },

    CAR_PURCHASE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_car_purchase
    },

    JEWELRY_AND_ACCESSORIES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_jewelry_and_accessories
    },

    DEVICES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_devices
    },

    HOUSEHOLD_STUFF {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_household_stuff
    },

    TAXES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_taxes
    },

    BANKING_SERVICE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_banking_service
    },

    CLOTHES_AND_SHOES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_clothes_and_shoes
    },

    SOFTWARE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_software
    },

    YOU_LENT_SOME_MONEY {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_you_lent_some_money
    },

    YOU_PAID_A_LOAN_BACK {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_you_paid_a_loan_back
    },

    EDUCATION {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_education
    },

    EXPENDABLE_MATERIALS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_expendable_materials
    },

    TECHNICAL_SERVICE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_technical_service
    },

    BOOKS_FILMS_GAMES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_books_films_games
    },

    SPORT {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_sport
    },

    ACCOMMODATION {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_accommodation
    },

    HOUSEKEEPING_SERVICE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_housekeeping_service
    },

    INTERNET_AND_COMMUNICATION {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_internet_and_communication
    },

    PUBLIC_TRANSPORT_PASSES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_public_transport_passes
    },

    BUSINESS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_business
    },

    CREDIT_INTEREST {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_credit_interest
    },

    TRIPS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_trips
    },

    CAR_RENTAL {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_car_rental
    },

    HEALTH_AND_BODY_CARE {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_health_and_bodu_care
    },

    FOODSTUFF {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_foodstuff
    },

    MUSEUMS_AND_EXHIBITIONS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_museums_and_exhibitions
    },

    PHILANTHROPY {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_philantropy
    },

    GIFTS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_gifts
    },

    TRAVEL_TICKETS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_travel_tickets
    },

    TAXI_AND_CARSHARING {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_taxi_and_carsharing
    },

    CAFES_AND_RESTAURANTS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_cafes_and_restaurants
    },

    FASTFOOD {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_fastfood
    },

    ENTERTAINMENT {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_enterntainment
    },

    OTHER {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_other
    }

}