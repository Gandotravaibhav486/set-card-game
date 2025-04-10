package com.example.setgame.model

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
}