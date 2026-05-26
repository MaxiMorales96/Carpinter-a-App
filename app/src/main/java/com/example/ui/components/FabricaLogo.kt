package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
fun FabricaLogo(
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    darkModel: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The 3-panel graphic
        Canvas(
            modifier = Modifier
                .size(width = 160.dp, height = 110.dp)
        ) {
            val W = size.width
            val H = size.height
            
            val gap = W * 0.04f
            val pW = (W - 2 * gap) / 3f
            val strokeWidth = 1.75.dp.toPx()
            val handleRadius = 1.0.dp.toPx()
            
            // 1. LEFT PANEL (Red) - Sideboard/Bajo Mesada
            // Symmetrical slant: Left-shorter, right-taller
            val redTopLeft = H * 0.28f
            val redTopRight = H * 0.20f
            val redBottomLeft = H * 0.72f
            val redBottomRight = H * 0.80f
            
            val redPath = Path().apply {
                moveTo(0f, redTopLeft)
                lineTo(pW, redTopRight)
                lineTo(pW, redBottomRight)
                lineTo(0f, redBottomLeft)
                close()
            }
            drawPath(path = redPath, color = Color(0xFFEF3636)) // Red panel
            
            // Draw Sideboard line-art inside Red panel
            // Bounds centered vertically/horizontally inside Left Panel
            val redMidY = (redTopLeft + redTopRight + redBottomLeft + redBottomRight) / 4f
            val sbW = pW * 0.75f
            val sbH = H * 0.16f
            val sbX = (pW - sbW) / 2f
            val sbY = redMidY - sbH / 2f - H * 0.02f
            
            // Cabinet box outline
            drawRect(
                color = Color.White,
                topLeft = Offset(sbX, sbY),
                size = Size(sbW, sbH),
                style = Stroke(width = strokeWidth)
            )
            
            // Drawer division columns (drawers in the middle, cupboards on the sides)
            val leftCupWidth = sbW * 0.28f
            val rightCupWidth = sbW * 0.28f
            val leftDivX = sbX + leftCupWidth
            val rightDivX = sbX + sbW - rightCupWidth
            
            // Vertical divisions
            drawLine(
                color = Color.White,
                start = Offset(leftDivX, sbY),
                end = Offset(leftDivX, sbY + sbH),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(rightDivX, sbY),
                end = Offset(rightDivX, sbY + sbH),
                strokeWidth = strokeWidth
            )
            
            // Drawers compartment details (horizontal lines dividing center)
            val drawerCount = 4
            val drawerH = sbH / drawerCount
            for (i in 1 until drawerCount) {
                val lineY = sbY + i * drawerH
                drawLine(
                    color = Color.White,
                    start = Offset(leftDivX, lineY),
                    end = Offset(rightDivX, lineY),
                    strokeWidth = strokeWidth
                )
            }
            
            // Draw drawer handles (centered in each center row)
            for (i in 0 until drawerCount) {
                val handleY = sbY + i * drawerH + drawerH / 2f
                drawCircle(
                    color = Color.White,
                    center = Offset((leftDivX + rightDivX) / 2f, handleY),
                    radius = handleRadius * 0.8f,
                    style = Fill
                )
            }
            
            // Cupboard knobs (small doors circles)
            drawCircle(
                color = Color.White,
                center = Offset(sbX + leftCupWidth * 0.35f, sbY + sbH / 2f),
                radius = handleRadius * 1.1f,
                style = Fill
            )
            drawCircle(
                color = Color.White,
                center = Offset(sbX + sbW - rightCupWidth * 0.35f, sbY + sbH / 2f),
                radius = handleRadius * 1.1f,
                style = Fill
            )
            
            // Sideboard angled legs
            drawLine(
                color = Color.White,
                start = Offset(sbX + sbW * 0.15f, sbY + sbH),
                end = Offset(sbX + sbW * 0.05f, sbY + sbH + H * 0.045f),
                strokeWidth = strokeWidth * 1.3f
            )
            drawLine(
                color = Color.White,
                start = Offset(sbX + sbW * 0.85f, sbY + sbH),
                end = Offset(sbX + sbW * 0.95f, sbY + sbH + H * 0.045f),
                strokeWidth = strokeWidth * 1.3f
            )
            
            
            // 2. CENTER PANEL (Green) - Wardrobe/Placard
            // Perfect vertical rectangles, centered height
            val greenXStart = pW + gap
            val greenXEnd = greenXStart + pW
            val greenTop = H * 0.18f
            val greenBottom = H * 0.82f
            
            val greenPath = Path().apply {
                moveTo(greenXStart, greenTop)
                lineTo(greenXEnd, greenTop)
                lineTo(greenXEnd, greenBottom)
                lineTo(greenXStart, greenBottom)
                close()
            }
            drawPath(path = greenPath, color = Color(0xFF006B3E)) // Emerald Green
            
            // Draw Wardrobe inside Center panel
            val wdW = pW * 0.65f
            val wdH = (greenBottom - greenTop) * 0.72f
            val wdX = greenXStart + (pW - wdW) / 2f
            val wdY = greenTop + (greenBottom - greenTop - wdH) / 2f
            
            // Wardrobe outline
            drawRect(
                color = Color.White,
                topLeft = Offset(wdX, wdY),
                size = Size(wdW, wdH),
                style = Stroke(width = strokeWidth)
            )
            
            // Separator between upper double-door and bottom drawers
            val wdLowerY = wdY + wdH * 0.65f
            drawLine(
                color = Color.White,
                start = Offset(wdX, wdLowerY),
                end = Offset(wdX + wdW, wdLowerY),
                strokeWidth = strokeWidth
            )
            
            // Vertical seam down the middle (for upper double doors)
            val wdMidX = wdX + wdW / 2f
            drawLine(
                color = Color.White,
                start = Offset(wdMidX, wdY),
                end = Offset(wdMidX, wdLowerY),
                strokeWidth = strokeWidth
            )
            
            // Vertical long handles on double doors near center seam
            val handleLen = wdH * 0.18f
            val handleStartY = wdY + wdH * 0.2f
            drawLine(
                color = Color.White,
                start = Offset(wdMidX - pW * 0.05f, handleStartY),
                end = Offset(wdMidX - pW * 0.05f, handleStartY + handleLen),
                strokeWidth = strokeWidth * 1.1f
            )
            drawLine(
                color = Color.White,
                start = Offset(wdMidX + pW * 0.05f, handleStartY),
                end = Offset(wdMidX + pW * 0.05f, handleStartY + handleLen),
                strokeWidth = strokeWidth * 1.1f
            )
            
            // Lower drawers section: horizontal split in half
            val lowerHalfY = wdLowerY + (wdY + wdH - wdLowerY) / 2f
            drawLine(
                color = Color.White,
                start = Offset(wdX, lowerHalfY),
                end = Offset(wdX + wdW, lowerHalfY),
                strokeWidth = strokeWidth
            )
            
            // Small drawers knobs
            drawCircle(
                color = Color.White,
                center = Offset(wdMidX, wdLowerY + (lowerHalfY - wdLowerY) / 2f),
                radius = handleRadius * 1.1f,
                style = Fill
            )
            drawCircle(
                color = Color.White,
                center = Offset(wdMidX, lowerHalfY + (wdY + wdH - lowerHalfY) / 2f),
                radius = handleRadius * 1.1f,
                style = Fill
            )
            
            
            // 3. RIGHT PANEL (Brown) - Desk/Escritorio
            // Receding slant: Left-taller, right-shorter
            val brownTopLeft = H * 0.20f
            val brownTopRight = H * 0.28f
            val brownBottomLeft = H * 0.80f
            val brownBottomRight = H * 0.72f
            val brownXStart = (pW + gap) * 2f
            
            val brownPath = Path().apply {
                moveTo(brownXStart, brownTopLeft)
                lineTo(W, brownTopRight)
                lineTo(W, brownBottomRight)
                lineTo(brownXStart, brownBottomLeft)
                close()
            }
            drawPath(path = brownPath, color = Color(0xFF3E1D13)) // Wood Brown
            
            // Draw Desk inside Brown panel
            val brownMidY = (brownTopLeft + brownTopRight + brownBottomLeft + brownBottomRight) / 4f
            val dW = pW * 0.76f
            val dH = H * 0.24f
            val dX = brownXStart + (pW - dW) / 2f
            val dY = brownMidY - dH / 2f - H * 0.015f
            
            // Desk table top board (thin bar at top)
            val tableTopH = H * 0.024f
            drawRect(
                color = Color.White,
                topLeft = Offset(dX, dY),
                size = Size(dW, tableTopH),
                style = Fill
            )
            
            // Left leg supports (H-frame style)
            val leg1X = dX + dW * 0.08f
            val leg2X = dX + dW * 0.16f
            drawLine(
                color = Color.White,
                start = Offset(leg1X, dY + tableTopH),
                end = Offset(leg1X, dY + dH),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(leg2X, dY + tableTopH),
                end = Offset(leg2X, dY + dH),
                strokeWidth = strokeWidth
            )
            
            // Right-side pedestal drawers cabinet unit
            val pedW = dW * 0.32f
            val pedH = dH * 0.72f
            val pedX = dX + dW - pedW - dW * 0.05f
            val pedY = dY + tableTopH
            drawRect(
                color = Color.White,
                topLeft = Offset(pedX, pedY),
                size = Size(pedW, pedH),
                style = Stroke(width = strokeWidth)
            )
            
            // Dividers for 3 drawers inside the pedestal
            val pedDrawerH = pedH / 3f
            for (i in 1 until 3) {
                drawLine(
                    color = Color.White,
                    start = Offset(pedX, pedY + i * pedDrawerH),
                    end = Offset(pedX + pedW, pedY + i * pedDrawerH),
                    strokeWidth = strokeWidth
                )
            }
            
            // Small drawers knobs
            for (i in 0 until 3) {
                drawCircle(
                    color = Color.White,
                    center = Offset(pedX + pedW / 2f, pedY + i * pedDrawerH + pedDrawerH / 2f),
                    radius = handleRadius * 0.8f,
                    style = Fill
                )
            }
            
            // Stretcher support bar connecting left frame with right pedestal
            val footrestY = dY + dH * 0.75f
            drawLine(
                color = Color.White,
                start = Offset(leg2X, footrestY),
                end = Offset(pedX, footrestY),
                strokeWidth = strokeWidth
            )
        }
        
        if (showText) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "FABRICA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = if (darkModel) Color.White else Color(0xFF1D1B20),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "DE MUEBLES A MEDIDA",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = if (darkModel) Color.White.copy(alpha = 0.85f) else Color(0xFF1D1B20).copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
