package com.example.setgame.ui.theme

import androidx.compose.ui.graphics.Color

// Main colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Card colors
val RedCard = Color(0xFFE57373)
val GreenCard = Color(0xFF81C784)
val PurpleCard = Color(0xFF9575CD)

// Border colors for card states
val SelectedBorder = Color(0xFF2196F3)
val ValidSetBorder = Color(0xFF4CAF50)
val InvalidSetBorder = Color(0xFFF44336)

// Extension function to convert game card color to Compose color
fun com.example.setgame.model.Card.Color.toComposeColor(): Color {
    return when (this) {
        com.example.setgame.model.Card.Color.RED -> RedCard
        com.example.setgame.model.Card.Color.GREEN -> GreenCard
        com.example.setgame.model.Card.Color.PURPLE -> PurpleCard
    }
}