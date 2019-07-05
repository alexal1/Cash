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

    DEVICES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_devices
    },

    BANKS_AND_SERVICES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_banks_and_services
    },

    SOME_STUFF {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_some_stuff
    },

    CLOTHES_AND_SHOES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_clothes_and_shoes
    },

    SPORT {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_sport
    },

    BOOKS_FILMS_GAMES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_books_films_games
    },

    ACCOMMODATION {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_accommodation
    },

    INTERNET_AND_COMMUNICATION {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_internet_and_communication
    },

    TRAVEL_PASSES {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_travel_passes
    },

    BUSINESS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_business
    },

    TRAVELLING {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_travelling
    },

    HEALTH {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_health
    },

    FOODSTUFF {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_foodstuff
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

    GIFTS {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_gifts
    },

    PHILANTHROPY {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_philantropy
    },

    TAXI_AND_CARSHARING {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_taxi_and_carsharing
    },

    ENTERTAINMENT {
        override val id: String = name
        override val isGain = false
        override val stringRes = R.string.loss_enterntainment
    }

}