package com.example.setgame.model

/**
 * Represents the current state of the Set game.
 */
data class GameState(
    // The full deck of cards
    val deck: List<Card> = Card.generateDeck(),
    // Cards currently displayed on the table
    val cardsOnTable: List<Card> = emptyList(),
    // Cards currently selected by the player
    val selectedCards: List<Card> = emptyList(),
    // Indicates if the selected cards form a valid set
    val isValidSelection: Boolean? = null,
    // Total number of valid sets found by the player
    val setsFound: Int = 0,
    // Cards that were just matched (for animation/highlighting)
    val lastMatchedCards: List<Card> = emptyList(),
    // Remaining cards in the deck not yet dealt
    val remainingCards: List<Card> = deck,
    // Game completion status
    val isGameOver: Boolean = false
)

/**
 * Game events that can modify the game state
 */
sealed class GameEvent {
    data class CardSelected(val card: Card) : GameEvent()
    object DealMoreCards : GameEvent()
    object RestartGame : GameEvent()
    object ClearSelection : GameEvent()
}