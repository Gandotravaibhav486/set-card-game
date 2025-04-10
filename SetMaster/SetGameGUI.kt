import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

/**
 * GUI version of the Set card game implemented in Kotlin with Swing.
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
    val isValidSelection: Boolean? = null,
    val setsFound: Int = 0,
    val lastMatchedCards: List<Card> = emptyList(),
    val remainingCards: List<Card> = deck,
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

/**
 * Game controller that manages the game state and handles game logic.
 */
class GameController {
    private var _state = GameState()
    val state: GameState get() = _state
    
    // Callback for updating the UI when the state changes
    private var onStateChanged: ((GameState) -> Unit)? = null
    
    init {
        startGame()
    }
    
    /**
     * Set a callback to be notified when the game state changes.
     */
    fun setOnStateChangedListener(listener: (GameState) -> Unit) {
        onStateChanged = listener
    }
    
    /**
     * Initialize the game with a new shuffled deck and deal 12 cards.
     */
    private fun startGame() {
        val deck = Card.generateDeck()
        val initialCards = deck.take(12)
        val remainingDeck = deck.drop(12)
        
        _state = GameState(
            deck = deck,
            cardsOnTable = initialCards,
            remainingCards = remainingDeck,
            selectedCards = emptyList(),
            setsFound = 0,
            isValidSelection = null,
            isGameOver = false
        )
        
        onStateChanged?.invoke(_state)
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
        val selectedCards = _state.selectedCards.toMutableList()
        
        // If card is already selected, deselect it
        if (card in selectedCards) {
            selectedCards.remove(card)
            updateState(_state.copy(
                selectedCards = selectedCards,
                isValidSelection = null
            ))
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
            updateState(_state.copy(
                selectedCards = selectedCards,
                isValidSelection = isValidSet
            ))
            
            // If it's a valid set, handle it after a short delay
            if (isValidSet) {
                Timer(1000) { handleValidSet(selectedCards) }.start()
            }
        } else {
            // Update state with the new selection
            updateState(_state.copy(
                selectedCards = selectedCards,
                isValidSelection = null
            ))
        }
    }
    
    /**
     * Process a valid set - remove the cards, add new ones from the deck.
     */
    private fun handleValidSet(matchedCards: List<Card>) {
        val tableCards = _state.cardsOnTable.toMutableList()
        val remainingDeck = _state.remainingCards.toMutableList()
        
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
        updateState(_state.copy(
            cardsOnTable = tableCards,
            remainingCards = remainingDeck,
            selectedCards = emptyList(),
            isValidSelection = null,
            setsFound = _state.setsFound + 1,
            lastMatchedCards = matchedCards,
            isGameOver = isGameOver
        ))
    }
    
    /**
     * Deal three more cards if possible.
     */
    private fun dealMoreCards() {
        // Can only deal more cards if:
        // 1. There are cards left in the deck
        // 2. We have fewer than 15 cards on the table or no valid sets are present
        if (_state.remainingCards.isEmpty() || 
            (_state.cardsOnTable.size >= 15 && hasValidSetOnTable(_state.cardsOnTable))) {
            return
        }
        
        val cardsToAdd = minOf(3, _state.remainingCards.size)
        val newCards = _state.remainingCards.take(cardsToAdd)
        val updatedTableCards = _state.cardsOnTable + newCards
        val updatedDeck = _state.remainingCards.drop(cardsToAdd)
        
        updateState(_state.copy(
            cardsOnTable = updatedTableCards,
            remainingCards = updatedDeck,
            selectedCards = emptyList(),
            isValidSelection = null
        ))
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
     * Find a valid set on the table. Used for hints.
     */
    fun findValidSetOnTable(): Triple<Card, Card, Card>? {
        val cards = _state.cardsOnTable
        
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
        updateState(_state.copy(
            selectedCards = emptyList(),
            isValidSelection = null
        ))
    }
    
    /**
     * Update the state and notify listeners.
     */
    private fun updateState(newState: GameState) {
        _state = newState
        onStateChanged?.invoke(_state)
    }
    
    /**
     * Simple timer implementation for delayed actions.
     */
    private inner class Timer(private val delayMillis: Int, private val action: () -> Unit) {
        fun start() {
            Thread {
                try {
                    Thread.sleep(delayMillis.toLong())
                    SwingUtilities.invokeLater { action() }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}

/**
 * Component that displays a single card in the UI.
 */
class CardView(private val card: Card) : JPanel() {
    private var isSelected = false
    private var isValidSet: Boolean? = null
    
    init {
        preferredSize = Dimension(120, 170)
        border = LineBorder(Color.GRAY, 1)
        isOpaque = true
        background = Color.WHITE
    }
    
    /**
     * Update the card's visual state.
     */
    fun updateState(selected: Boolean, validSet: Boolean? = null) {
        isSelected = selected
        isValidSet = validSet
        
        border = when {
            validSet == true -> LineBorder(java.awt.Color.GREEN, 3)
            validSet == false -> LineBorder(java.awt.Color.RED, 3)
            selected -> LineBorder(java.awt.Color.BLUE, 3)
            else -> LineBorder(Color.GRAY, 1)
        }
        
        repaint()
    }
    
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Set the color based on the card's color attribute
        val cardColor = when (card.color) {
            Card.Color.RED -> java.awt.Color(220, 50, 50)
            Card.Color.GREEN -> java.awt.Color(50, 180, 50)
            Card.Color.PURPLE -> java.awt.Color(150, 50, 200)
        }
        
        // Calculate positions for shapes based on the number
        val shapePositions = when (card.number) {
            Card.Number.ONE -> listOf(Point(width / 2, height / 2))
            Card.Number.TWO -> listOf(
                Point(width / 2, height / 3),
                Point(width / 2, height * 2 / 3)
            )
            Card.Number.THREE -> listOf(
                Point(width / 2, height / 4),
                Point(width / 2, height / 2),
                Point(width / 2, height * 3 / 4)
            )
        }
        
        // Draw the shapes
        for (position in shapePositions) {
            drawShape(g2, position, cardColor)
        }
    }
    
    /**
     * Draw a shape at the specified position with the given color.
     */
    private fun drawShape(g2: Graphics2D, position: Point, color: java.awt.Color) {
        val shapeWidth = 60
        val shapeHeight = 30
        
        val x = position.x - shapeWidth / 2
        val y = position.y - shapeHeight / 2
        
        // Set color
        g2.color = color
        
        // Set stroke for outlines
        g2.stroke = BasicStroke(2f)
        
        // Create the shape based on the card's shape attribute
        val shape = when (card.shape) {
            Card.Shape.OVAL -> {
                java.awt.geom.Ellipse2D.Double(x.toDouble(), y.toDouble(), shapeWidth.toDouble(), shapeHeight.toDouble())
            }
            Card.Shape.DIAMOND -> {
                val diamond = Polygon()
                diamond.addPoint(x + shapeWidth / 2, y)
                diamond.addPoint(x + shapeWidth, y + shapeHeight / 2)
                diamond.addPoint(x + shapeWidth / 2, y + shapeHeight)
                diamond.addPoint(x, y + shapeHeight / 2)
                diamond
            }
            Card.Shape.SQUIGGLE -> {
                java.awt.geom.RoundRectangle2D.Double(
                    x.toDouble(), y.toDouble(), 
                    shapeWidth.toDouble(), shapeHeight.toDouble(), 
                    15.0, 15.0
                )
            }
        }
        
        // Draw based on shading
        when (card.shading) {
            Card.Shading.SOLID -> {
                g2.fill(shape)
            }
            Card.Shading.OPEN -> {
                g2.draw(shape)
            }
            Card.Shading.STRIPED -> {
                // Draw stripes
                val clip = g2.clip
                g2.clip = shape
                
                for (i in 0 until width step 4) {
                    g2.drawLine(i, 0, i, height)
                }
                
                g2.clip = clip
                g2.draw(shape)
            }
        }
    }
}

/**
 * The main GUI application for the Set game.
 */
class SetGameGUI : JFrame("Set Card Game") {
    private val controller = GameController()
    private val cardViewMap = mutableMapOf<Card, CardView>()
    
    // UI components
    private val cardsPanel = JPanel()
    private val statusLabel = JLabel("Sets Found: 0")
    private val deckLabel = JLabel("Cards in Deck: 69")
    private val messageLabel = JLabel("")
    
    init {
        // Set up the frame
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        size = Dimension(800, 600)
        setLocationRelativeTo(null)
        
        // Set up the layout
        val mainPanel = JPanel(BorderLayout())
        mainPanel.border = EmptyBorder(10, 10, 10, 10)
        
        // Top panel for status information
        val topPanel = JPanel(BorderLayout())
        topPanel.add(statusLabel, BorderLayout.WEST)
        topPanel.add(deckLabel, BorderLayout.EAST)
        mainPanel.add(topPanel, BorderLayout.NORTH)
        
        // Cards panel in the center
        cardsPanel.layout = GridLayout(0, 3, 10, 10)
        
        val scrollPane = JScrollPane(cardsPanel)
        scrollPane.border = EmptyBorder(10, 0, 10, 0)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        // Message label
        messageLabel.horizontalAlignment = SwingConstants.CENTER
        messageLabel.font = Font(Font.SANS_SERIF, Font.BOLD, 14)
        mainPanel.add(messageLabel, BorderLayout.SOUTH)
        
        // Bottom panel for buttons
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5))
        
        val dealButton = JButton("Deal More Cards")
        dealButton.addActionListener { controller.onEvent(GameEvent.DealMoreCards) }
        
        val clearButton = JButton("Clear Selection")
        clearButton.addActionListener { controller.onEvent(GameEvent.ClearSelection) }
        
        val hintButton = JButton("Hint")
        hintButton.addActionListener { showHint() }
        
        val restartButton = JButton("Restart Game")
        restartButton.addActionListener { controller.onEvent(GameEvent.RestartGame) }
        
        buttonPanel.add(dealButton)
        buttonPanel.add(clearButton)
        buttonPanel.add(hintButton)
        buttonPanel.add(restartButton)
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)
        
        contentPane = mainPanel
        
        // Set up listener for game state changes
        controller.setOnStateChangedListener { state ->
            updateUI(state)
        }
    }
    
    /**
     * Update the UI based on the current game state.
     */
    private fun updateUI(state: GameState) {
        // Update status labels
        statusLabel.text = "Sets Found: ${state.setsFound}"
        deckLabel.text = "Cards in Deck: ${state.remainingCards.size}"
        
        // Update message label
        messageLabel.text = when {
            state.isGameOver -> "Game Over! You found ${state.setsFound} sets."
            state.isValidSelection == true -> "Valid Set Found!"
            state.isValidSelection == false -> "Not a valid set. Try again!"
            else -> ""
        }
        
        // Handle card views
        if (cardViewMap.isEmpty() || state.cardsOnTable.size != cardViewMap.size) {
            // Rebuild the card view map if the number of cards has changed
            rebuildCardViews(state.cardsOnTable)
        } else {
            // Just update the selection state of existing cards
            for (card in cardViewMap.keys) {
                val isSelected = card in state.selectedCards
                val isPartOfValidSet = if (state.selectedCards.size == 3 && isSelected) state.isValidSelection else null
                cardViewMap[card]?.updateState(isSelected, isPartOfValidSet)
            }
        }
        
        // Revalidate and repaint
        cardsPanel.revalidate()
        cardsPanel.repaint()
    }
    
    /**
     * Rebuild all card views based on the current cards on the table.
     */
    private fun rebuildCardViews(cardsOnTable: List<Card>) {
        cardViewMap.clear()
        cardsPanel.removeAll()
        
        for (card in cardsOnTable) {
            val cardView = CardView(card)
            cardView.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    controller.onEvent(GameEvent.CardSelected(card))
                }
            })
            
            cardViewMap[card] = cardView
            cardsPanel.add(cardView)
        }
    }
    
    /**
     * Show a hint by highlighting a valid set if one exists.
     */
    private fun showHint() {
        val validSet = controller.findValidSetOnTable()
        
        if (validSet != null) {
            // Clear current selection
            controller.onEvent(GameEvent.ClearSelection)
            
            // Highlight the cards in the hint
            messageLabel.text = "Hint: Look for a set containing these cards"
            
            for (card in listOf(validSet.first, validSet.second, validSet.third)) {
                cardViewMap[card]?.updateState(true, null)
            }
            
            // Reset after 3 seconds
            Timer(3000) {
                for (card in listOf(validSet.first, validSet.second, validSet.third)) {
                    cardViewMap[card]?.updateState(false, null)
                }
                messageLabel.text = ""
            }.start()
        } else {
            messageLabel.text = "No valid sets on the table. Try dealing more cards."
            Timer(2000) { messageLabel.text = "" }.start()
        }
    }
    
    /**
     * Simple timer implementation for delayed actions.
     */
    private inner class Timer(private val delayMillis: Int, private val action: () -> Unit) {
        fun start() {
            Thread {
                try {
                    Thread.sleep(delayMillis.toLong())
                    SwingUtilities.invokeLater { action() }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}

/**
 * Main function to start the application.
 */
fun main() {
    // Use system look and feel
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    // Start the GUI on the Event Dispatch Thread
    SwingUtilities.invokeLater {
        val game = SetGameGUI()
        game.isVisible = true
    }
}