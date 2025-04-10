package com.example.setgame.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.setgame.model.Card as GameCard
import com.example.setgame.ui.theme.InvalidSetBorder
import com.example.setgame.ui.theme.SelectedBorder
import com.example.setgame.ui.theme.ValidSetBorder

/**
 * Composable that displays a single card from the Set game.
 */
@Composable
fun CardView(
    card: GameCard,
    isSelected: Boolean,
    isPartOfValidSet: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine border color based on selection state
    val borderColor = when {
        isPartOfValidSet == true -> ValidSetBorder
        isPartOfValidSet == false -> InvalidSetBorder
        isSelected -> SelectedBorder
        else -> Color.Transparent
    }
    
    val borderWidth = if (isSelected || isPartOfValidSet != null) 3.dp else 0.dp
    
    Card(
        modifier = modifier
            .aspectRatio(0.7f)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Calculate positions for 1, 2, or 3 shapes
                val shapePositions = when (card.number) {
                    GameCard.Number.ONE -> listOf(Offset(width / 2, height / 2))
                    GameCard.Number.TWO -> listOf(
                        Offset(width / 2, height / 3),
                        Offset(width / 2, height * 2 / 3)
                    )
                    GameCard.Number.THREE -> listOf(
                        Offset(width / 2, height / 4),
                        Offset(width / 2, height / 2),
                        Offset(width / 2, height * 3 / 4)
                    )
                }
                
                // Draw shapes at calculated positions
                for (position in shapePositions) {
                    drawShape(
                        position = position,
                        size = Size(width * 0.6f, height * 0.2f),
                        card = card
                    )
                }
            }
        }
    }
}

/**
 * Extension function to draw the shape on the canvas based on card attributes.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShape(
    position: Offset,
    size: Size,
    card: GameCard
) {
    val shapeColor = card.color.toComposeColor()
    
    // Determine how to draw based on shading type
    val paint = when (card.shading) {
        GameCard.Shading.SOLID -> {
            shapeColor
        }
        GameCard.Shading.STRIPED -> {
            // For striped, we'll use a path effect to create stripes
            val strokePathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            Stroke(width = 2f, pathEffect = strokePathEffect)
        }
        GameCard.Shading.OPEN -> {
            // For open, we'll just use the outline
            Stroke(width = 3f)
        }
    }
    
    // Calculate shape dimensions
    val x = position.x - size.width / 2
    val y = position.y - size.height / 2
    
    when (card.shape) {
        GameCard.Shape.OVAL -> {
            if (card.shading == GameCard.Shading.SOLID) {
                drawOval(
                    color = shapeColor,
                    topLeft = Offset(x, y),
                    size = size
                )
            } else {
                drawOval(
                    color = shapeColor,
                    topLeft = Offset(x, y),
                    size = size,
                    style = paint as Stroke
                )
            }
        }
        GameCard.Shape.DIAMOND -> {
            val diamondPath = Path().apply {
                moveTo(x + size.width / 2, y) // Top
                lineTo(x + size.width, y + size.height / 2) // Right
                lineTo(x + size.width / 2, y + size.height) // Bottom
                lineTo(x, y + size.height / 2) // Left
                close()
            }
            
            if (card.shading == GameCard.Shading.SOLID) {
                drawPath(diamondPath, color = shapeColor)
            } else {
                drawPath(diamondPath, color = shapeColor, style = paint as Stroke)
            }
        }
        GameCard.Shape.SQUIGGLE -> {
            // Simplified squiggle as a rounded rectangle with wavy edges
            if (card.shading == GameCard.Shading.SOLID) {
                drawRoundRect(
                    color = shapeColor,
                    topLeft = Offset(x, y),
                    size = size,
                    cornerRadius = CornerRadius(size.height / 3, size.height / 3)
                )
            } else {
                drawRoundRect(
                    color = shapeColor,
                    topLeft = Offset(x, y),
                    size = size,
                    cornerRadius = CornerRadius(size.height / 3, size.height / 3),
                    style = paint as Stroke
                )
            }
        }
    }
}