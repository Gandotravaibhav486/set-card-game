package com.example.setgame.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.setgame.model.GameEvent
import com.example.setgame.viewmodel.GameViewModel

/**
 * Main game screen that displays the cards and game controls.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set Game",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Sets Found: ${gameState.setsFound}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.onEvent(GameEvent.DealMoreCards) },
                    enabled = gameState.remainingCards.isNotEmpty() && 
                              (gameState.cardsOnTable.size < 15 || !hasAnySet(gameState.cardsOnTable))
                ) {
                    Text("Deal More Cards")
                }
                
                Button(
                    onClick = { viewModel.onEvent(GameEvent.ClearSelection) },
                    enabled = gameState.selectedCards.isNotEmpty()
                ) {
                    Text("Clear Selection")
                }
                
                Button(onClick = { viewModel.onEvent(GameEvent.RestartGame) }) {
                    Text("Restart Game")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (gameState.cardsOnTable.isEmpty()) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Grid of cards
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(gameState.cardsOnTable) { card ->
                            CardView(
                                card = card,
                                isSelected = card in gameState.selectedCards,
                                isPartOfValidSet = if (gameState.selectedCards.size == 3 && card in gameState.selectedCards) 
                                                     gameState.isValidSelection else null,
                                onClick = { viewModel.onEvent(GameEvent.CardSelected(card)) }
                            )
                        }
                    }
                    
                    // Game status messages
                    AnimatedVisibility(
                        visible = gameState.isGameOver,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 500))
                    ) {
                        Text(
                            text = "Game Over! You found ${gameState.setsFound} sets.",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = gameState.selectedCards.size == 3 && gameState.isValidSelection == false,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Text(
                            text = "Not a valid set. Try again!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper function to check if there are any valid sets among the cards on the table.
 * Used to determine if dealing more cards is necessary/allowed.
 */
private fun hasAnySet(cards: List<com.example.setgame.model.Card>): Boolean {
    if (cards.size < 3) return false
    
    for (i in 0 until cards.size - 2) {
        for (j in i + 1 until cards.size - 1) {
            for (k in j + 1 until cards.size) {
                if (com.example.setgame.model.Card.isValidSet(cards[i], cards[j], cards[k])) {
                    return true
                }
            }
        }
    }
    
    return false
}