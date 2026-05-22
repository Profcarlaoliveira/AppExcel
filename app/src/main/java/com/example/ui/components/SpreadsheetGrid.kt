package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CellAlignment
import com.example.data.CellContent
import com.example.data.SpreadsheetData

@Composable
fun SpreadsheetGrid(
    data: SpreadsheetData,
    selectedCells: Set<String>,
    selectedCol: String?,
    selectedRow: Int?,
    onCellClick: (String, Boolean) -> Unit,
    onColumnClick: (String) -> Unit,
    onRowClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollStateHorizontal = rememberScrollState()
    val scrollStateVertical = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Column {
            // Top Column headers Row (contains upper-left corner + column letters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollStateHorizontal)
                    .background(Color(0xFFF5F5F5))
            ) {
                // Top-Left static cell corner
                Box(
                    modifier = Modifier
                        .size(40.dp, 32.dp)
                        .border(0.5.dp, Color(0xFFCCCCCC))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "fx",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )
                }

                // Letters (A, B, C, D, E...)
                data.columns.forEach { col ->
                    val isColSelected = selectedCol == col
                    val colWidth = data.colWidths[col]?.dp ?: 95.dp

                    Box(
                        modifier = Modifier
                            .size(colWidth, 32.dp)
                            .border(0.5.dp, Color(0xFFCCCCCC))
                            .background(if (isColSelected) Color(0xFFC8E6C9) else Color(0xFFEEEEEE))
                            .clickable { onColumnClick(col) }
                            .testTag("header_col_$col"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = col,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isColSelected) Color(0xFF2E7D32) else Color(0xFF333333)
                        )
                    }
                }
            }

            // Grid Rows (includes side row numbers + cell grids)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollStateVertical)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollStateHorizontal)
                ) {
                    // Left side Row Numbers (1, 2, 3...)
                    Column(modifier = Modifier.background(Color(0xFFF5F5F5))) {
                        (1..data.rows).forEach { r ->
                            val isRowSelected = selectedRow == r
                            val rowH = data.rowHeights[r]?.dp ?: 36.dp

                            Box(
                                modifier = Modifier
                                    .size(40.dp, rowH)
                                    .border(0.5.dp, Color(0xFFCCCCCC))
                                    .background(if (isRowSelected) Color(0xFFC8E6C9) else Color(0xFFEEEEEE))
                                    .clickable { onRowClick(r) }
                                    .testTag("header_row_$r"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = r.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRowSelected) Color(0xFF2E7D32) else Color(0xFF333333)
                                )
                            }
                        }
                    }

                    // Interactive Spreadsheet Cells Matrix
                    Column {
                        (1..data.rows).forEach { r ->
                            val rowH = data.rowHeights[r]?.dp ?: 36.dp
                            Row {
                                data.columns.forEach { col ->
                                    val coord = "$col$r"
                                    val cell = data.cells[coord] ?: CellContent()
                                    val isSelected = selectedCells.contains(coord)
                                    val colWidth = data.colWidths[col]?.dp ?: 95.dp

                                    // Local style calculations
                                    val align = when (cell.alignment) {
                                        CellAlignment.LEFT -> TextAlign.Left
                                        CellAlignment.CENTER -> TextAlign.Center
                                        CellAlignment.RIGHT -> TextAlign.Right
                                    }

                                    val cellVal = if (cell.isCurrency && cell.value.isNotEmpty()) {
                                        // formats "5" to "5.00 €"
                                        val doubleVal = cell.value.toDoubleOrNull()
                                        if (doubleVal != null) {
                                            String.format("%.2f €", doubleVal).replace(".", ",")
                                        } else {
                                            cell.value
                                        }
                                    } else {
                                        cell.value
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(colWidth, rowH)
                                            .border(
                                                width = if (isSelected) 2.dp else 0.5.dp,
                                                color = if (isSelected) Color(0xFF107C41) else Color(0xFFE0E0E0)
                                            )
                                            .background(
                                                if (isSelected) Color(0xFFE8F5E9) else Color(cell.bgCellColor)
                                            )
                                            .clickable { onCellClick(coord, false) }
                                            .testTag("cell_$coord"),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = cellVal,
                                            fontSize = 12.sp,
                                            fontWeight = if (cell.isBold) FontWeight.Bold else FontWeight.Normal,
                                            color = Color(cell.textColor),
                                            textAlign = align,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
