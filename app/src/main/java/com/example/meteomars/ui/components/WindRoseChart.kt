package com.example.meteomars.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A composable that draws a wind rose chart showing wind direction distribution.
 * 
 * @param windDirectionMap Map of direction index (0-15) to count/value
 * @param maxValue The maximum value in the wind direction map (used for scaling)
 * @param modifier Modifier for the composable
 * @param backgroundColor Background color for the chart
 * @param circleColor Color for the outer circle
 * @param triangleColor Color for the direction triangles
 */
@Composable
fun WindRoseChart(
    windDirectionMap: Map<Int, Double>,
    maxValue: Double,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    circleColor: Color = Color.LightGray.copy(alpha = 0.3f),
    triangleColor: Color = Color(0xFF82B3D6) // Light blue
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(
        modifier = modifier
            .aspectRatio(1f) // Keep it square
            .padding(8.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width.coerceAtMost(size.height) / 2 - 8.dp.toPx()
        
        // Draw the background circle (filled)
        drawCircle(
            color = circleColor,
            radius = radius,
            center = center
        )
        
        // Draw the outer circle
        drawCircle(
            color = Color.Gray,
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        
        // Draw horizontal and vertical axes
        drawLine(
            color = Color.Gray,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Gray,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1.dp.toPx()
        )
        
        // Optional: Draw cardinal direction markers
        val directions = listOf("N", "E", "S", "W")
        val directionOffsets = listOf(
            Pair(center.x, center.y - radius - 20), // North
            Pair(center.x + radius + 20, center.y), // East
            Pair(center.x, center.y + radius + 20), // South
            Pair(center.x - radius - 20, center.y)  // West
        )
        
        for (i in directions.indices) {
            val textLayoutResult = textMeasurer.measure(
                text = directions[i],
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
            
            // Create proper offset for text placement
            val x = directionOffsets[i].first - textLayoutResult.size.width / 2
            val y = directionOffsets[i].second - textLayoutResult.size.height / 2
            
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x, y)
            )
        }
        
        // Draw the triangles for each direction
        windDirectionMap.forEach { (direction, value) ->
            // Calculate the normalized value (0.0 to 1.0)
            val normalizedValue = if (maxValue > 0) value / maxValue else 0.0
            
            // Calculate angle in radians for this direction (0 is North, going clockwise)
            // 16 directions evenly spaced around the circle, with 0 at the top
            val angleInDegrees = (direction * 22.5) // 360 / 16 = 22.5 degrees per direction
            val angleInRadians = Math.toRadians(angleInDegrees)
            
            // Calculate endpoint based on direction and normalized value
            // Ensure all calculations result in Float values
            val normalizedValueFloat = normalizedValue.toFloat()
            val endX = center.x + radius * normalizedValueFloat * sin(angleInRadians.toFloat())
            val endY = center.y - radius * normalizedValueFloat * cos(angleInRadians.toFloat())
            
            // Calculate points for the triangle
            val trianglePath = createTrianglePath(
                center = center,
                endPoint = Offset(endX, endY),
                widthFactor = 0.15f, // Controls width of triangle base
                angleInRadians = angleInRadians
            )
            
            // Draw the triangle
            drawPath(
                path = trianglePath,
                color = triangleColor,
                style = Fill
            )
        }
    }
}

/**
 * Creates a triangle path from center to end point with appropriate width
 */
private fun createTrianglePath(
    center: Offset,
    endPoint: Offset,
    widthFactor: Float,
    angleInRadians: Double
): Path {
    val distance = kotlin.math.sqrt(
        (endPoint.x - center.x) * (endPoint.x - center.x) +
        (endPoint.y - center.y) * (endPoint.y - center.y)
    )
    
    // Calculate perpendicular angle
    val perpAngle = angleInRadians + Math.PI / 2
    
    // Calculate half width at the end point
    val halfWidth = distance * widthFactor
    
    // Calculate the two points that form the base of the triangle
    // Convert Double angles to Float for sin/cos operations
    val perpAngleFloat = perpAngle.toFloat()
    val point1X = endPoint.x + halfWidth * kotlin.math.sin(perpAngleFloat)
    val point1Y = endPoint.y - halfWidth * kotlin.math.cos(perpAngleFloat)
    
    val point2X = endPoint.x - halfWidth * kotlin.math.sin(perpAngleFloat)
    val point2Y = endPoint.y + halfWidth * kotlin.math.cos(perpAngleFloat)
    
    // Create the triangle path
    return Path().apply {
        moveTo(center.x, center.y)
        lineTo(point1X, point1Y)
        lineTo(point2X, point2Y)
        close()
    }
} 