/**
 * Terminal version of the Set card game implemented in Kotlin.
 */

/**
 * Model class representing a card in the Set game.
 * Each card has 4 attributes, each with 3 possible values:
 * - Color: Red, Green, Purple
 * - Shape: Oval, Diamond, Squiggle
 * - Number: One, Two, Three
 * - Shading: Solid, Striped, Open
 */
data class Card(
    val color: Color,
    val shape: Shape,
    val number: Number,
    val shading: Shading
) {
    enum class Color { RED, GREEN, PURPLE }
    enum class Shape { OVAL, DIAMOND, SQUIGGLE }
    enum class Number { ONE, TWO, THREE }
    enum class Shading { SOLID, STRIPED, OPEN }
    
    companion object {
        /**
         * Generate the complete deck of 81 cards (3^4 combinations).
         * @return List of all possible cards in the deck
         */
        fun generateDeck(): List<Card> {
            val deck = mutableListOf<Card>()
            
            for (color in Color.values()) {
                for (shape in Shape.values()) {
                    for (number in Number.values()) {
                        for (shading in Shading.values()) {
                            deck.add(Card(color, shape, number, shading))
                        }
                    }
                }
            }
            
            // Shuffle the deck
            return deck.shuffled()
        }
        
        /**
         * Check if three cards form a valid set.
         * A valid set requires that for each attribute, the values are either all the same or all different.
         * @return true if the three cards form a valid set
         */
        fun isValidSet(card1: Card, card2: Card, card3: Card): Boolean {
            // For each attribute, check if values are all the same or all different
            return isValidAttribute(card1.color, card2.color, card3.color) &&
                   isValidAttribute(card1.shape, card2.shape, card3.shape) &&
                   isValidAttribute(card1.number, card2.number, card3.number) &&
                   isValidAttribute(card1.shading, card2.shading, card3.shading)
        }
        
        /**
         * Helper function to check if three values of an attribute satisfy the Set game rule.
         * @return true if the values are all the same or all different
         */
        private fun <T> isValidAttribute(a: T, b: T, c: T): Boolean {
            // All the same
            if (a == b && b == c) {
                return true
            }
            
            // All different
            if (a != b && b != c && a != c) {
                return true
            }
            
            // Otherwise (two the same, one different)
            return false
        }
    }
    
    override fun toString(): String {
        return "$number $color $shape ($shading)"
    }
}

/**
 * Represents the current state of the Set game.
 */
data class GameState(
    val deck: List<Card> = Card.generateDeck(),
    val cardsOnTable: List<Card> = emptyList(),
    val selectedCards: List<Card> = emptyList(),
    val setsFound: Int = 0,
    val remainingCards: List<Card> = deck,
    val isGameOver: Boolean = false
)

/**
 * Game events that can modify the game state
 */
sealed class GameEvent {
    data class CardSelected(val index: Int) : GameEvent()
    object DealMoreCards : GameEvent()
    object RestartGame : GameEvent()
    object ClearSelection : GameEvent()
}

/**
 * Manages the game state and handles game logic.
 */
class GameController {
    private var state = GameState()
    
    init {
        startGame()
    }
    
    /**
     * Initialize the game with a new shuffled deck and deal 12 cards.
     */
    private fun startGame() {
        val deck = Card.generateDeck()
        val initialCards = deck.take(12)
        val remainingDeck = deck.drop(12)
        
        state = GameState(
            deck = deck,
            cardsOnTable = initialCards,
            remainingCards = remainingDeck,
            selectedCards = emptyList(),
            setsFound = 0,
            isGameOver = false
        )
    }
    
    /**
     * Process game events to update the game state.
     */
    fun onEvent(event: GameEvent): GameState {
        when (event) {
            is GameEvent.CardSelected -> handleCardSelection(event.index)
            is GameEvent.DealMoreCards -> dealMoreCards()
            is GameEvent.RestartGame -> startGame()
            is GameEvent.ClearSelection -> clearSelection()
        }
        return state
    }
    
    /**
     * Handle a card selection by the player.
     */
    private fun handleCardSelection(index: Int) {
        if (index < 0 || index >= state.cardsOnTable.size) {
            println("Invalid card index: $index")
            return
        }
        
        val card = state.cardsOnTable[index]
        val selectedCards = state.selectedCards.toMutableList()
        
        // If card is already selected, deselect it
        if (card in selectedCards) {
            selectedCards.remove(card)
            state = state.copy(selectedCards = selectedCards)
            return
        }
        
        // If already have 3 cards selected, ignore new selection
        if (selectedCards.size >= 3) {
            return
        }
        
        // Add the newly selected card
        selectedCards.add(card)
        
        // If we have 3 cards selected, check if they form a valid set
        if (selectedCards.size == 3) {
            val isValidSet = Card.isValidSet(selectedCards[0], selectedCards[1], selectedCards[2])
            
            // If it's a valid set, handle it
            if (isValidSet) {
                println("Valid set found!")
                handleValidSet(selectedCards)
            } else {
                println("Not a valid set. Try again!")
                state = state.copy(selectedCards = emptyList())
            }
        } else {
            // Update state with the new selection
            state = state.copy(selectedCards = selectedCards)
        }
    }
    
    /**
     * Process a valid set - remove the cards, add new ones from the deck.
     */
    private fun handleValidSet(matchedCards: List<Card>) {
        val tableCards = state.cardsOnTable.toMutableList()
        val remainingDeck = state.remainingCards.toMutableList()
        
        // Remove the matched cards from the table
        tableCards.removeAll(matchedCards)
        
        // If there are cards left in the deck, replace the matched cards
        if (remainingDeck.isNotEmpty()) {
            val cardsToAdd = minOf(matchedCards.size, remainingDeck.size)
            val newCards = remainingDeck.take(cardsToAdd)
            tableCards.addAll(newCards)
            remainingDeck.removeAll(newCards)
        }
        
        // Check if the game is over
        val isGameOver = remainingDeck.isEmpty() && !hasValidSetOnTable(tableCards)
        
        // Update the game state
        state = state.copy(
            cardsOnTable = tableCards,
            remainingCards = remainingDeck,
            selectedCards = emptyList(),
            setsFound = state.setsFound + 1,
            isGameOver = isGameOver
        )
        
        if (isGameOver) {
            println("Game Over! You found ${state.setsFound} sets.")
        }
    }
    
    /**
     * Deal three more cards if possible.
     */
    private fun dealMoreCards() {
        // Can only deal more cards if there are cards left in the deck
        if (state.remainingCards.isEmpty()) {
            println("No more cards in the deck!")
            return
        }
        
        val cardsToAdd = minOf(3, state.remainingCards.size)
        val newCards = state.remainingCards.take(cardsToAdd)
        val updatedTableCards = state.cardsOnTable + newCards
        val updatedDeck = state.remainingCards.drop(cardsToAdd)
        
        state = state.copy(
            cardsOnTable = updatedTableCards,
            remainingCards = updatedDeck,
            selectedCards = emptyList()
        )
    }
    
    /**
     * Check if there is a valid set among the cards on the table.
     */
    private fun hasValidSetOnTable(cards: List<Card>): Boolean {
        if (cards.size < 3) return false
        
        for (i in 0 until cards.size - 2) {
            for (j in i + 1 until cards.size - 1) {
                for (k in j + 1 until cards.size) {
                    if (Card.isValidSet(cards[i], cards[j], cards[k])) {
                        return true
                    }
                }
            }
        }
        
        return false
    }
    
    /**
     * Find and return a valid set of cards from the table, if any exists.
     * Used for hints and validation.
     */
    fun findValidSetOnTable(): Triple<Card, Card, Card>? {
        val cards = state.cardsOnTable
        
        for (i in 0 until cards.size - 2) {
            for (j in i + 1 until cards.size - 1) {
                for (k in j + 1 until cards.size) {
                    if (Card.isValidSet(cards[i], cards[j], cards[k])) {
                        return Triple(cards[i], cards[j], cards[k])
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Clear the current card selection.
     */
    private fun clearSelection() {
        state = state.copy(selectedCards = emptyList())
    }
    
    /**
     * Get the current game state.
     */
    fun getState(): GameState = state
}

/**
 * Display the current cards on the table with their indices.
 */
fun displayCardsOnTable(cards: List<Card>) {
    println("\nCards on the table:")
    println("-------------------")
    cards.forEachIndexed { index, card ->
        println("[$index] $card")
    }
    println()
}

/**
 * Display the currently selected cards.
 */
fun displaySelectedCards(cards: List<Card>) {
    if (cards.isEmpty()) {
        println("No cards selected.")
    } else {
        println("Selected cards:")
        cards.forEach { println("- $it") }
    }
    println()
}

/**
 * Display help information.
 */
fun displayHelp() {
    println("""
    |Set Game Commands:
    |----------------
    |select <index> - Select a card by its index
    |deal          - Deal three more cards
    |clear         - Clear current selection
    |hint          - Get a hint for a valid set
    |restart       - Start a new game
    |help          - Display this help message
    |quit          - Exit the game
    """.trimMargin())
}

/**
 * Main game loop.
 */
fun main() {
    println("Welcome to the Set Card Game!")
    println("-----------------------------")
    displayHelp()
    
    val gameController = GameController()
    var gameState = gameController.getState()
    
    while (true) {
        // Display game status
        println("\nSets found: ${gameState.setsFound}")
        println("Remaining cards in deck: ${gameState.remainingCards.size}")
        
        displayCardsOnTable(gameState.cardsOnTable)
        displaySelectedCards(gameState.selectedCards)
        
        if (gameState.isGameOver) {
            println("Game Over! You found ${gameState.setsFound} sets.")
            println("Type 'restart' to play again or 'quit' to exit.")
        }
        
        print("> ")
        val input = readLine()?.trim()?.lowercase() ?: ""
        
        when {
            input.startsWith("select") -> {
                val parts = input.split(" ")
                if (parts.size < 2) {
                    println("Please specify a card index: select <index>")
                } else {
                    try {
                        val index = parts[1].toInt()
                        gameState = gameController.onEvent(GameEvent.CardSelected(index))
                    } catch (e: NumberFormatException) {
                        println("Invalid index. Please enter a number.")
                    }
                }
            }
            input == "deal" -> {
                gameState = gameController.onEvent(GameEvent.DealMoreCards)
            }
            input == "clear" -> {
                gameState = gameController.onEvent(GameEvent.ClearSelection)
            }
            input == "hint" -> {
                val validSet = gameController.findValidSetOnTable()
                if (validSet != null) {
                    println("Hint: Look for a set containing these cards:")
                    println("- ${validSet.first}")
                    println("- ${validSet.second}")
                    println("- ${validSet.third}")
                } else {
                    println("No valid sets on the table. Try dealing more cards.")
                }
            }
            input == "restart" -> {
                gameState = gameController.onEvent(GameEvent.RestartGame)
                println("Game restarted!")
            }
            input == "help" -> {
                displayHelp()
            }
            input == "quit" -> {
                println("Thanks for playing Set!")
                return
            }
            else -> {
                println("Unknown command. Type 'help' for a list of commands.")
            }
        }
    }
}