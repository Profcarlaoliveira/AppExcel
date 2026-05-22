package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.SpreadsheetGrid
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.LessonViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: LessonViewModel,
    onBack: () -> Unit
) {
    val levelId by viewModel.currentLevelId.collectAsState()
    val level = LessonData.levels.find { it.id == levelId } ?: return

    val exerciseIdx by viewModel.currentExerciseIdx.collectAsState()
    val exercise = level.exercises.getOrNull(exerciseIdx)

    val gridState by viewModel.spreadsheetState.collectAsState()
    val selectedCells by viewModel.selectedCells.collectAsState()
    val selectedCol by viewModel.selectedCol.collectAsState()
    val selectedRow by viewModel.selectedRow.collectAsState()

    val formulaInput by viewModel.formulaInput.collectAsState()
    val showTecladoFormulas by viewModel.showFormulaTeclado.collectAsState()

    val feedbackMsg by viewModel.feedbackMessage.collectAsState()
    val feedbackSuccess by viewModel.feedbackSuccess.collectAsState()

    val focusManager = LocalFocusManager.current

    // Local states for quick Sheet Renaming Dialog
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInputVal by remember { mutableStateOf("") }

    if (exercise == null) {
        // Fallback or completion
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "Success Star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Parabéns!",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color(0xFF107C41)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Concluíste todas as atividades de treino prático deste nível. Faz agora o quiz de nível para avançares!",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Voltar ao Painel", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ficha de Atividade Prática 💻",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Nível $levelId — Atividade ${exerciseIdx + 1}/${level.exercises.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF107C41)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retroceder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FF))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF)) // Bento Background
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Exercise task description frame
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F3E6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, Color(0xFFBDE0BD), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Task outline",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = exercise.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exercise.instruction,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Real Interactive Formula Bar (fx)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(18.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "fx",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF107C41),
                        modifier = Modifier.padding(end = 8.dp, start = 4.dp)
                    )

                    // Text input for formula
                    TextField(
                        value = formulaInput,
                        onValueChange = { viewModel.setFormulaText(it) },
                        placeholder = {
                            Text(
                                text = selectedCells.firstOrNull()?.let { "Digita o valor ou fórmula para $it" }
                                    ?: "Seleciona uma célula primeiro...",
                                fontSize = 12.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("formula_input_field"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    )

                    // Keyboard toggler button
                    IconButton(
                        onClick = { viewModel.toggleFormulaTeclado() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (showTecladoFormulas) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                            contentDescription = "Expand keyboard helper",
                            tint = if (showTecladoFormulas) Color(0xFF107C41) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE0E0E0)))

                    // Save / Apply formula button
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.submitFormula()
                        },
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(36.dp)
                            .testTag("submit_formula_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Ok",
                            tint = Color(0xFF107C41),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Custom floating keyboard bar for fast typing formulas on phone screens
            AnimatedVisibility(
                visible = showTecladoFormulas,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FormulaKeyboardHelper(
                    onKeyPress = { symbol ->
                        if (symbol == "Limpar") {
                            viewModel.setFormulaText("")
                        } else {
                            viewModel.setFormulaText(formulaInput + symbol)
                        }
                    }
                )
            }

            // Real simulated columns A-F, rows 1-8 Spreadsheet Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                SpreadsheetGrid(
                    data = gridState,
                    selectedCells = selectedCells,
                    selectedCol = selectedCol,
                    selectedRow = selectedRow,
                    onCellClick = { coord, isMulti -> viewModel.handleCellClick(coord, isMulti) },
                    onColumnClick = { col -> viewModel.selectEntireColumn(col) },
                    onRowClick = { row -> viewModel.selectEntireRow(row) }
                )
            }

            // Tabs / Sheet bar on the base of the Spreadsheet
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(18.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Active Sheet Tab indicator with customizable color
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(gridState.sheetColor).copy(alpha = 0.15f))
                            .border(1.dp, Color(gridState.sheetColor), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(gridState.sheetColor), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = gridState.currentSheetName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF333333),
                            modifier = Modifier.testTag("active_sheet_tab")
                        )
                    }

                    // Shell Sheet controllers (for Level 1 ex 1.3)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rename text button
                        OutlinedButton(
                            onClick = {
                                renameInputVal = gridState.currentSheetName
                                showRenameDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF107C41))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit name", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Renomear", fontSize = 11.sp, color = Color(0xFF107C41), fontWeight = FontWeight.Bold)
                        }

                        // Colors palette
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(0xFF4CAF50, 0xFF2196F3, 0xFFFF5722).forEach { colorHex ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorHex))
                                        .border(
                                            width = if (gridState.sheetColor == colorHex) 1.5.dp else 0.dp,
                                            color = Color.Black,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.handleSheetColor(colorHex) }
                                )
                            }
                        }
                    }
                }
            }

            // Smart Interactive Excel Toolbelt
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, Color(0xFFD4DAD5), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Barra de Ferramentas de Formatação:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bold
                        ToolButton(icon = { Text("B", fontWeight = FontWeight.Black) }, label = "Negrito") {
                            viewModel.applyBold()
                        }

                        // Alignment
                        ToolButton(icon = { Icon(Icons.Default.FormatAlignCenter, "center") }, label = "Centrar") {
                            viewModel.applyAlignment(CellAlignment.CENTER)
                        }

                        // Currency €
                        ToolButton(icon = { Text("€", fontWeight = FontWeight.Bold, fontSize = 15.sp) }, label = "Moeda (€)") {
                            viewModel.applyCurrencyFormat()
                        }

                        // Date formats
                        ToolButton(icon = { Icon(Icons.Default.CalendarToday, "date") }, label = "Data") {
                            viewModel.applyDateFormat()
                        }

                        // Increase Column Width
                        ToolButton(icon = { Icon(Icons.Default.CompareArrows, "width") }, label = "Alargar") {
                            viewModel.increaseColWidth()
                        }

                        // Auto preencher
                        ToolButton(icon = { Icon(Icons.Default.Bolt, "autofill", tint = Color(0xFFFFA000)) }, label = "Preencher") {
                            viewModel.autoFillSelection()
                        }

                        // Conditional format
                        ToolButton(icon = { Icon(Icons.Default.Palette, "condformat") }, label = "Format. Cond.") {
                            viewModel.applyConditionalFormatting()
                        }

                        // Insert Columns Chart
                        ToolButton(icon = { Icon(Icons.Default.BarChart, "columns") }, label = "Graf. Colunas") {
                            viewModel.insertChart(false)
                        }

                        // Insert Pie Chart
                        ToolButton(icon = { Icon(Icons.Default.PieChart, "pie") }, label = "Graf. Pizza") {
                            viewModel.insertChart(true)
                        }
                    }
                }
            }

            // Real-Time Canvas / Layered charts renderer inside spreadsheet file
            if (gridState.charts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gráficos Gerados na Folha:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )

                gridState.charts.forEach { chart ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, Color(0xFFB5D4BD), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = chart.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF107C41),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (chart.isPie) {
                                // Draw an attractive simplified circular representation of parts of a whole
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Visual Pie Slices representative
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .background(Color(0xFF81C784), CircleShape)
                                                .border(1.5.dp, Color.White, CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(20.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFE57373)))
                                                Text(" Estudar (60%)", fontSize = 10.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFB74D)))
                                                Text(" Jogos (25%)", fontSize = 10.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF64B5F6)))
                                                Text(" Lanches (15%)", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Draw columnar display representation of values
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.width(30.dp).height(80.dp).background(Color(0xFF2196F3)))
                                        Text("A4 (120)", fontSize = 9.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.width(30.dp).height(50.dp).background(Color(0xFF4CAF50)))
                                        Text("A5 (80)", fontSize = 9.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.width(30.dp).height(95.dp).background(Color(0xFFFFC107)))
                                        Text("A6 (210)", fontSize = 9.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dados do Intervalo: ${chart.dataRange}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            // immediate pedagogical Console/Feedback messages indicator
            AnimatedVisibility(
                visible = feedbackMsg != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (feedbackSuccess == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(
                            width = 1.dp,
                            color = if (feedbackSuccess == true) Color(0xFF81C784) else Color(0xFFEF9A9A),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (feedbackSuccess == true) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = "Status logo",
                                tint = if (feedbackSuccess == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (feedbackSuccess == true) "Fabuloso!" else "Atenção Aluno!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (feedbackSuccess == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = feedbackMsg ?: "",
                            fontSize = 12.sp,
                            color = if (feedbackSuccess == true) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Verification Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear state / Hint button
                OutlinedButton(
                    onClick = {
                        viewModel.setFormulaText("")
                        viewModel.selectEntireColumn("") // resets selections
                        viewModel.handleCellClick(exercise.initialData.cells.keys.firstOrNull() ?: "A1", false)
                        // Shows exercise hint immediately
                        viewModel.submitFormula() // resets
                        java.lang.System.out.println("Reseteando inputs")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    border = BorderStroke(1.2.dp, Color(0xFFD32F2F))
                ) {
                    Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Dica")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpar Seleção", fontWeight = FontWeight.Bold)
                }

                // Check answer or Next Buttons
                if (feedbackSuccess == true) {
                    Button(
                        onClick = { viewModel.nextExercise() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                            .testTag("btn_complete_next")
                    ) {
                        Text("Seguinte Atividade ➔", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.verifyCurrentExercise() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                            .testTag("verify_answer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FactCheck, contentDescription = "Validar")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validar Resposta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    // Sheet renaming custom dialog window
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Renomear Folha / Separador", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Introduz o novo nome para esta folha:", fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = renameInputVal,
                        onValueChange = { renameInputVal = it },
                        singleLine = true,
                        placeholder = { Text("Ex: Turma9A") },
                        modifier = Modifier.testTag("rename_sheet_textfield")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleSheetRename(renameInputVal.trim())
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41))
                ) {
                    Text("Submeter", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancelar", color = Color.Red)
                }
            }
        )
    }
}

@Composable
fun ToolButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .testTag("tool_btn_$label"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = BorderStroke(1.dp, Color(0xFFE2E2E2))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FormulaKeyboardHelper(onKeyPress: (String) -> Unit) {
    val basicFormulaSymbols = listOf(
        "=", "(", ")", ";", "\$", ":", "*", "/", "+", "MÉDIA", "SOMA", "CONTAR.SE", "B2", "B5", "D4", "\$B\$1", "Limpar"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE2E8E4))
            .padding(8.dp)
            .border(0.5.dp, Color(0xFFC7D1CB), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Text(
            text = "Teclado Rápido de Fórmulas:",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF107C41),
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            basicFormulaSymbols.forEach { symbol ->
                Button(
                    onClick = { onKeyPress(symbol) },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (symbol == "Limpar") Color(0xFFD32F2F) else Color.White,
                        contentColor = if (symbol == "Limpar") Color.White else Color(0xFF107C41)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .border(1.dp, Color(0xFFCDD5D1), RoundedCornerShape(6.dp))
                        .testTag("kb_key_$symbol")
                ) {
                    Text(text = symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
