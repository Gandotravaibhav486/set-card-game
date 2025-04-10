package com.example.setgame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setgame.model.Card
import com.example.setgame.model.GameEvent
import com.example.setgame.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the game state and handles game logic.
 */
class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    
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
        
        _state.value = GameState(
            deck = deck,
            cardsOnTable = initialCards,
            remainingCards = remainingDeck,
            selectedCards = emptyList(),
            setsFound = 0,
            isValidSelection = null,
            isGameOver = false
        )
    }
    
    /**
     * Process game events to update the game state.
     */
    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.CardSelected -> handleCardSelection(event.card)
            is GameEvent.DealMoreCards -> dealMoreCards()
            is GameEvent.RestartGame -> startGame()
            is GameEvent.ClearSelection -> clearSelection()
        }
    }
    
    /**
     * Handle a card selection by the player.
     */
    private fun handleCardSelection(card: Card) {
        val currentState = _state.value
        val selectedCards = currentState.selectedCards.toMutableList()
        
        // If card is already selected, deselect it
        if (card in selectedCards) {
            selectedCards.remove(card)
            _state.update { it.copy(
                selectedCards = selectedCards,
                isValidSelection = null
            )}
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
            _state.update { it.copy(
                selectedCards = selectedCards,
                isValidSelection = isValidSet
            )}
            
            // If it's a valid set, handle it after a short delay
            if (isValidSet) {
                viewModelScope.launch {
                    delay(1000) // Give the player a moment to see the valid set
                    handleValidSet(selectedCards)
                }
            }
        } else {
            // Update state with the new selection
            _state.update { it.copy(
                selectedCards = selectedCards,
                isValidSelection = null
            )}
        }
    }
    
    /**
     * Process a valid set - remove the cards, add new ones from the deck.
     */
    private fun handleValidSet(matchedCards: List<Card>) {
        val currentState = _state.value
        val tableCards = currentState.cardsOnTable.toMutableList()
        val remainingDeck = currentState.remainingCards.toMutableList()
        
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
        _state.update { it.copy(
            cardsOnTable = tableCards,
            remainingCards = remainingDeck,
            selectedCards = emptyList(),
            isValidSelection = null,
            setsFound = it.setsFound + 1,
            lastMatchedCards = matchedCards,
            isGameOver = isGameOver
        )}
    }
    
    /**
     * Deal three more cards if possible and if no sets are present on the table.
     */
    private fun dealMoreCards() {
        val currentState = _state.value
        
        // Can only deal more cards if:
        // 1. There are cards left in the deck
        // 2. We have fewer than 15 cards on the table or no valid sets are present
        if (currentState.remainingCards.isEmpty() || 
            (currentState.cardsOnTable.size >= 15 && hasValidSetOnTable(currentState.cardsOnTable))) {
            return
        }
        
        val cardsToAdd = minOf(3, currentState.remainingCards.size)
        val newCards = currentState.remainingCards.take(cardsToAdd)
        val updatedTableCards = currentState.cardsOnTable + newCards
        val updatedDeck = currentState.remainingCards.drop(cardsToAdd)
        
        _state.update { it.copy(
            cardsOnTable = updatedTableCards,
            remainingCards = updatedDeck,
            selectedCards = emptyList(),
            isValidSelection = null
        )}
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
     * Clear the current card selection.
     */
    private fun clearSelection() {
        _state.update { it.copy(
            selectedCards = emptyList(),
            isValidSelection = null
        )}
    }
}