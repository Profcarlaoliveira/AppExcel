package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class LessonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProgressRepository

    // Database user progress state
    val userProgress: StateFlow<UserProgress>

    // Navigation and UI state
    private val _currentLevelId = MutableStateFlow(1)
    val currentLevelId: StateFlow<Int> = _currentLevelId.asStateFlow()

    private val _currentMode = MutableStateFlow<AppMode>(AppMode.DASHBOARD)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Interactive Practice State
    private val _currentExerciseIdx = MutableStateFlow(0)
    val currentExerciseIdx: StateFlow<Int> = _currentExerciseIdx.asStateFlow()

    private val _spreadsheetState = MutableStateFlow(SpreadsheetData())
    val spreadsheetState: StateFlow<SpreadsheetData> = _spreadsheetState.asStateFlow()

    private val _selectedCells = MutableStateFlow<Set<String>>(emptySet())
    val selectedCells: StateFlow<Set<String>> = _selectedCells.asStateFlow()

    private val _selectedCol = MutableStateFlow<String?>(null)
    val selectedCol: StateFlow<String?> = _selectedCol.asStateFlow()

    private val _selectedRow = MutableStateFlow<Int?>(null)
    val selectedRow: StateFlow<Int?> = _selectedRow.asStateFlow()

    private val _formulaInput = MutableStateFlow("")
    val formulaInput: StateFlow<String> = _formulaInput.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _feedbackSuccess = MutableStateFlow<Boolean?>(null)
    val feedbackSuccess: StateFlow<Boolean?> = _feedbackSuccess.asStateFlow()

    private val _showFormulaTeclado = MutableStateFlow(false)
    val showFormulaTeclado: StateFlow<Boolean> = _showFormulaTeclado.asStateFlow()

    // Local Quiz State
    private val _quizQuestionIdx = MutableStateFlow(0)
    val quizQuestionIdx: StateFlow<Int> = _quizQuestionIdx.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _quizSubmitted = MutableStateFlow(false)
    val quizSubmitted: StateFlow<Boolean> = _quizSubmitted.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProgressRepository(db.progressDao())

        // Collect DB updates safely
        userProgress = repository.progressFlow
            .map { it ?: UserProgress() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserProgress()
            )
    }

    fun selectLevel(levelId: Int) {
        _currentLevelId.value = levelId
        setMode(AppMode.DASHBOARD)
    }

    fun setMode(mode: AppMode) {
        _currentMode.value = mode
        _feedbackMessage.value = null
        _feedbackSuccess.value = null

        if (mode == AppMode.PRACTICE) {
            _currentExerciseIdx.value = 0
            loadExerciseForCurrentLevel()
        } else if (mode == AppMode.QUIZ) {
            startQuiz()
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            _currentLevelId.value = 1
            _currentMode.value = AppMode.DASHBOARD
        }
    }

    // --- PRACTICE ENGINE ---

    private fun loadExerciseForCurrentLevel() {
        val level = LessonData.levels.find { it.id == _currentLevelId.value } ?: return
        val exercise = level.exercises.getOrNull(_currentExerciseIdx.value) ?: return

        _spreadsheetState.value = exercise.initialData
        _selectedCells.value = emptySet()
        _selectedCol.value = null
        _selectedRow.value = null
        _formulaInput.value = ""
        _feedbackMessage.value = null
        _feedbackSuccess.value = null
    }

    fun nextExercise() {
        val level = LessonData.levels.find { it.id == _currentLevelId.value } ?: return
        if (_currentExerciseIdx.value < level.exercises.lastIndex) {
            _currentExerciseIdx.value += 1
            loadExerciseForCurrentLevel()
        } else {
            // Completed all exercises for this level
            _feedbackMessage.value = "Fabuloso! Concluíste todos os exercícios do nível! Abre agora o Quiz para testares a tua aprendizagem e desbloqueares o próximo nível!"
            _feedbackSuccess.value = true
        }
    }

    fun handleCellClick(coord: String, isMultiSelect: Boolean = false) {
        _selectedCol.value = null
        _selectedRow.value = null
        val currentSelected = _selectedCells.value.toMutableSet()

        if (isMultiSelect) {
            if (currentSelected.contains(coord)) {
                currentSelected.remove(coord)
            } else {
                currentSelected.add(coord)
            }
        } else {
            currentSelected.clear()
            currentSelected.add(coord)
        }

        _selectedCells.value = currentSelected

        // Update formula input with focused cell's formula or value
        val cell = _spreadsheetState.value.cells[coord]
        _formulaInput.value = cell?.formula.takeIf { !it.isNullOrEmpty() } ?: cell?.value ?: ""
    }

    fun selectEntireColumn(col: String) {
        _selectedCells.value = emptySet()
        _selectedRow.value = null
        _selectedCol.value = col

        // Auto select all cells in this column for simpler execution
        val coords = (1.._spreadsheetState.value.rows).map { "$col$it" }.toSet()
        _selectedCells.value = coords
    }

    fun selectEntireRow(row: Int) {
        _selectedCells.value = emptySet()
        _selectedCol.value = null
        _selectedRow.value = row

        // Auto select all cells in this row
        val coords = _spreadsheetState.value.columns.map { "$it$row" }.toSet()
        _selectedCells.value = coords
    }

    fun setFormulaText(text: String) {
        _formulaInput.value = text
    }

    fun toggleFormulaTeclado() {
        _showFormulaTeclado.value = !_showFormulaTeclado.value
    }

    fun submitFormula() {
        val focusedCell = _selectedCells.value.firstOrNull()
        if (focusedCell == null) {
            showNotice("Clica primeiro numa célula para inserires a fórmula/valor!", false)
            return
        }

        val input = _formulaInput.value.trim()
        val currentCells = _spreadsheetState.value.cells.toMutableMap()
        val existingCell = currentCells[focusedCell] ?: CellContent()

        if (input.startsWith("=")) {
            // It is a formula
            val evalResult = evaluateFormulaString(input, currentCells)
            if (evalResult.error != null) {
                showNotice("Erro na fórmula: ${evalResult.error}", false)
                currentCells[focusedCell] = existingCell.copy(value = "#ERRO!", formula = input)
            } else {
                currentCells[focusedCell] = existingCell.copy(
                    value = evalResult.value,
                    formula = input,
                    isCurrency = evalResult.shouldBeCurrency || existingCell.isCurrency,
                    decimalPlaces = if (evalResult.shouldBeCurrency) 2 else existingCell.decimalPlaces
                )
                showNotice("Fórmula introduzida com sucesso!", true)
            }
        } else {
            // Plain text or numeric
            currentCells[focusedCell] = existingCell.copy(value = input, formula = "")
        }

        _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
    }

    // --- FORMULA PARSER ENGINE ---
    data class EvalResult(val value: String, val error: String? = null, val shouldBeCurrency: Boolean = false)

    private fun evaluateFormulaString(formula: String, cells: Map<String, CellContent>): EvalResult {
        val uppercaseForm = formula.uppercase(Locale.getDefault()).replace(" ", "")
        if (!uppercaseForm.startsWith("=")) return EvalResult("", "Falta o símbolo inicial '='")

        val expr = uppercaseForm.substring(1)

        try {
            // 1. AVERAGE / MÉDIA
            if (expr.startsWith("MÉDIA(") || expr.startsWith("MEDIA(")) {
                val match = Regex("MED[IÍ]A\\((.+)\\)").find(expr) ?: return EvalResult("", "Sintaxe inválida para MÉDIA")
                val rangeStr = match.groupValues[1]
                val rangeCells = expandRange(rangeStr) ?: return EvalResult("", "Intervalo de células inválido (ex: B2:B5)")
                val values = rangeCells.mapNotNull { cells[it]?.value?.toDoubleOrNull() }
                if (values.isEmpty()) return EvalResult("0")
                return EvalResult(String.format(Locale.US, "%.1f", values.average()))
            }

            // 2. MAX / MÁXIMO
            if (expr.startsWith("MÁXIMO(") || expr.startsWith("MAXIMO(") || expr.startsWith("MAX(")) {
                val match = Regex("(M[AÁ]XIMO|MAX)\\((.+)\\)").find(expr) ?: return EvalResult("", "Sintaxe inválida para MÁXIMO")
                val rangeStr = match.groupValues[2]
                val rangeCells = expandRange(rangeStr) ?: return EvalResult("", "Intervalo de células inválido")
                val values = rangeCells.mapNotNull { cells[it]?.value?.toDoubleOrNull() }
                if (values.isEmpty()) return EvalResult("0")
                return EvalResult(String.format(Locale.US, "%.1f", values.maxOrNull() ?: 0.0))
            }

            // 3. MIN / MÍNIMO
            if (expr.startsWith("MÍNIMO(") || expr.startsWith("MINIMO(") || expr.startsWith("MIN(")) {
                val match = Regex("(M[IÍ]NIMO|MIN)\\((.+)\\)").find(expr) ?: return EvalResult("", "Sintaxe inválida para MÍNIMO")
                val rangeStr = match.groupValues[2]
                val rangeCells = expandRange(rangeStr) ?: return EvalResult("", "Intervalo de células inválido")
                val values = rangeCells.mapNotNull { cells[it]?.value?.toDoubleOrNull() }
                if (values.isEmpty()) return EvalResult("0")
                return EvalResult(String.format(Locale.US, "%.1f", values.minOrNull() ?: 0.0))
            }

            // 4. SOMA / SUM
            if (expr.startsWith("SOMA(") || expr.startsWith("SUM(")) {
                val match = Regex("(SOMA|SUM)\\((.+)\\)").find(expr) ?: return EvalResult("", "Sintaxe de SOMA incorreta")
                val rangeStr = match.groupValues[2]
                val rangeCells = expandRange(rangeStr) ?: return EvalResult("", "Intervalo inválido")
                val values = rangeCells.mapNotNull { cells[it]?.value?.toDoubleOrNull() }
                return EvalResult(String.format(Locale.US, "%.1f", values.sum()))
            }

            // 5. COUNTIF / CONTAR.SE
            if (expr.startsWith("CONTAR.SE(") || expr.startsWith("COUNTIF(")) {
                val match = Regex("(CONTAR\\.SE|COUNTIF)\\((.+)\\)").find(expr) ?: return EvalResult("", "Sintaxe de CONTAR.SE incorreta. Exemplo: =CONTAR.SE(B2:B5; \">=10\")")
                val inner = match.groupValues[2]
                val parts = inner.split(";", ",")
                if (parts.size < 2) return EvalResult("", "Sintaxe de CONTAR.SE necessita de intervalo e critério separados por ';'")
                val rangeStr = parts[0]
                val rawCriteria = parts[1].replace("\"", "") // remove quotes

                val rangeCells = expandRange(rangeStr) ?: return EvalResult("", "Intervalo inválido para CONTAR.SE")
                val values = rangeCells.mapNotNull { cells[it]?.value?.toDoubleOrNull() }

                // Parse operator in criteria like ">=10" or "10"
                val criteriaRegex = Regex("([>=<!]+)?([+-]?\\d*\\.?\\d+)")
                val criteriaMatch = criteriaRegex.find(rawCriteria)
                if (criteriaMatch == null) {
                    // Treat as text match
                    val count = rangeCells.count { cells[it]?.value?.uppercase(Locale.getDefault()) == rawCriteria.uppercase(Locale.getDefault()) }
                    return EvalResult(count.toString())
                }

                val op = criteriaMatch.groupValues[1].ifEmpty { "=" }
                val limitNum = criteriaMatch.groupValues[2].toDoubleOrNull() ?: 0.0

                val count = values.count { valNum ->
                    when (op) {
                        ">=" -> valNum >= limitNum
                        "<=" -> valNum <= limitNum
                        ">" -> valNum > limitNum
                        "<" -> valNum < limitNum
                        "!=" -> valNum != limitNum
                        else -> valNum == limitNum
                    }
                }
                return EvalResult(count.toString())
            }

            // 6. Multiplication like =B4*C4 or absolute like =D4*$B$1
            if (expr.contains("*")) {
                val parts = expr.split("*")
                if (parts.size == 2) {
                    val cell1 = parts[0].replace("$", "")
                    val cell2 = parts[1].replace("$", "")

                    val val1 = cells[cell1]?.value?.toDoubleOrNull() ?: cell1.toDoubleOrNull() ?: 0.0
                    val val2 = cells[cell2]?.value?.toDoubleOrNull() ?: cell2.toDoubleOrNull() ?: 0.0

                    val result = val1 * val2
                    // If either is currency or B1 discount multiplying absolute, keep moeda
                    val isMoedaResult = cells[cell1]?.isCurrency == true || cells[cell2]?.isCurrency == true || expr.contains("\$B\$1")
                    return EvalResult(String.format(Locale.US, "%.2f", result), shouldBeCurrency = isMoedaResult)
                }
            }

            // Fallback: evaluate single cell or static math
            val cleanExpr = expr.replace("$", "")
            val singleCellVal = cells[cleanExpr]?.value
            if (singleCellVal != null) {
                return EvalResult(singleCellVal)
            }

            val staticNum = cleanExpr.toDoubleOrNull()
            if (staticNum != null) {
                return EvalResult(String.format(Locale.US, "%.1f", staticNum))
            }

            return EvalResult("", "Fórmula desconhecida ou não suportada nesta versão de treino.")
        } catch (e: Exception) {
            return EvalResult("", "Ocorreu um erro ao calcular os dados.")
        }
    }

    private fun expandRange(rangeStr: String): List<String>? {
        val parts = rangeStr.split(":")
        if (parts.size != 2) return null
        val start = parts[0]
        val end = parts[1]

        val startCol = start.filter { it.isLetter() }
        val startRow = start.filter { it.isDigit() }.toIntOrNull() ?: return null
        val endCol = end.filter { it.isLetter() }
        val endRow = end.filter { it.isDigit() }.toIntOrNull() ?: return null

        if (startCol.length != 1 || endCol.length != 1) return null
        val colCharStart = startCol[0]
        val colCharEnd = endCol[0]

        val colRange = colCharStart..colCharEnd
        val rowRange = if (startRow <= endRow) startRow..endRow else endRow..startRow

        val coords = mutableListOf<String>()
        for (col in colRange) {
            for (row in rowRange) {
                coords.add("$col$row")
            }
        }
        return coords
    }

    private fun showNotice(msg: String, success: Boolean) {
        _feedbackMessage.value = msg
        _feedbackSuccess.value = success
    }

    // Tools for active student practice
    fun applyAlignment(align: CellAlignment) {
        val selected = _selectedCells.value
        val currentCells = _spreadsheetState.value.cells.toMutableMap()
        for (coord in selected) {
            val cell = currentCells[coord] ?: CellContent()
            currentCells[coord] = cell.copy(alignment = align)
        }
        _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
        showNotice("Alinhamento aplicado ao centro das células com sucesso!", true)
    }

    fun applyBold() {
        val selected = _selectedCells.value
        val currentCells = _spreadsheetState.value.cells.toMutableMap()
        for (coord in selected) {
            val cell = currentCells[coord] ?: CellContent()
            currentCells[coord] = cell.copy(isBold = !cell.isBold)
        }
        _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
        showNotice("Estilo Negrito togglado nas células!", true)
    }

    fun applyCurrencyFormat() {
        val selected = _selectedCells.value
        val currentCells = _spreadsheetState.value.cells.toMutableMap()
        for (coord in selected) {
            val cell = currentCells[coord] ?: CellContent()
            // Convert to a format with € representation
            var rawVal = cell.value
            if (rawVal.isNotEmpty() && rawVal.toDoubleOrNull() != null) {
                val d = rawVal.toDouble()
                rawVal = String.format(Locale.US, "%.2f", d)
            }
            currentCells[coord] = cell.copy(isCurrency = true, decimalPlaces = 2, value = rawVal)
        }
        _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
        showNotice("Formato de Moeda (€) com 2 casas decimais configurado!", true)
    }

    fun applyDateFormat() {
        val selected = _selectedCells.value
        val currentCells = _spreadsheetState.value.cells.toMutableMap()
        for (coord in selected) {
            val cell = currentCells[coord] ?: CellContent()
            currentCells[coord] = cell.copy(isDate = true)
        }
        _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
        showNotice("Formato de Data Completa configurado nas células!", true)
    }

    fun increaseColWidth() {
        val col = _selectedCol.value ?: _selectedCells.value.firstOrNull()?.filter { it.isLetter() }
        if (col != null) {
            val currentWidths = _spreadsheetState.value.colWidths.toMutableMap()
            val currentW = currentWidths[col] ?: 80
            currentWidths[col] = currentW + 35
            _spreadsheetState.value = _spreadsheetState.value.copy(colWidths = currentWidths)
            showNotice("Alargaste a coluna $col com sucesso para melhor leitura!", true)
        } else {
            showNotice("Seleciona primeiro uma coluna ou célula para a alargar!", false)
        }
    }

    fun handleSheetRename(newName: String) {
        _spreadsheetState.value = _spreadsheetState.value.copy(currentSheetName = newName)
        showNotice("Folha renomeada para '$newName'!", true)
    }

    fun handleSheetColor(color: Long) {
        _spreadsheetState.value = _spreadsheetState.value.copy(sheetColor = color)
        showNotice("Cor do separador alterada com sucesso!", true)
    }

    fun autoFillSelection() {
        val selected = _selectedCells.value
        if (selected.contains("A2") || selected.contains("A3") || selected.contains("A4")) {
            val currentCells = _spreadsheetState.value.cells.toMutableMap()
            // Simulates Level 1 Ex 1.6 auto filling months Janeiro -> Fevereiro -> Março
            currentCells["A3"] = CellContent(value = "Fevereiro", alignment = CellAlignment.CENTER)
            currentCells["A4"] = CellContent(value = "Março", alignment = CellAlignment.CENTER)
            _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
            _selectedCells.value = setOf("A2", "A3", "A4")
            showNotice("Meses preenchidos automaticamente: Fevereiro, Março!", true)
        } else if (selected.contains("D4")) {
            // Level 3 Ex 3.2: auto fill formulas B*C
            val currentCells = _spreadsheetState.value.cells.toMutableMap()
            currentCells["D5"] = CellContent(value = "10.20", formula = "=B5*C5", isCurrency = true, decimalPlaces = 2)
            currentCells["D6"] = CellContent(value = "25.00", formula = "=B6*C6", isCurrency = true, decimalPlaces = 2)
            _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
            showNotice("Fórmulas copiadas com sucesso de D4 para D5 e D6!", true)
        } else {
            showNotice("Preenchimento automático inteligente: Seleciona 'A2' (Janeiro) ou a fórmula 'D4'!", false)
        }
    }

    fun applyConditionalFormatting() {
        val selected = _selectedCells.value
        if (selected.isEmpty()) {
            showNotice("Seleciona o intervalo de notas B2:B5 primeiro!", false)
            return
        }

        val currentCells = _spreadsheetState.value.cells.toMutableMap()
        for (coord in selected) {
            val cell = currentCells[coord] ?: CellContent()
            val dVal = cell.value.toDoubleOrNull()
            if (dVal != null) {
                if (dVal >= 15.0) {
                    currentCells[coord] = cell.copy(bgCellColor = 0xFFE8F5E9, textColor = 0xFF2E7D32) // Light Green
                } else if (dVal < 10.0) {
                    currentCells[coord] = cell.copy(bgCellColor = 0xFFFFEBEE, textColor = 0xFFC62828) // Light Red
                }
            }
        }
        _spreadsheetState.value = _spreadsheetState.value.copy(cells = currentCells)
        showNotice("Formatação Condicional escolar aplicada: Notas >= 15 a verde, Notas < 10 a vermelho!", true)
    }

    fun insertChart(typeIsPie: Boolean) {
        val title = if (typeIsPie) "Distribuição de Orçamento" else "Vendas por Artigo"
        val newChart = SimulatedChart(
            title = title,
            isPie = typeIsPie,
            dataRange = "A3:B6"
        )
        val currentCharts = _spreadsheetState.value.charts.toMutableList()
        currentCharts.add(newChart)
        _spreadsheetState.value = _spreadsheetState.value.copy(charts = currentCharts)
        showNotice("Lindo! Gráfico de ${if (typeIsPie) "Setores (Pizza)" else "Colunas"} inserido abaixo da grelha!", true)
    }

    fun verifyCurrentExercise() {
        val exerciseIdx = _currentExerciseIdx.value
        val level = LessonData.levels.find { it.id == _currentLevelId.value } ?: return
        val exercise = level.exercises.getOrNull(exerciseIdx) ?: return
        val currentGrid = _spreadsheetState.value

        var isCorrect = false
        var errMsg = "Ainda não concluíste os requisitos do exercício. Tenta novamente!"

        when (exercise.verificationType) {
            VerificationType.SELECTION -> {
                if (exercise.id == "1.1") {
                    // Selecionar A1:B3
                    val target = setOf("A1", "A2", "A3", "B1", "B2", "B3")
                    if (_selectedCells.value == target) {
                        isCorrect = true
                    } else {
                        errMsg = "Precisas de selecionar exatamente as células de A1 a B3 na grelha (6 células a azul)."
                    }
                } else if (exercise.id == "1.2") {
                    // Selecionar coluna C ou linha 4
                    if (_selectedCol.value == "C" || _selectedRow.value == 4) {
                        isCorrect = true
                    } else {
                        errMsg = "Não selecionaste a coluna C (clica na letra 'C' acima) ou a linha 4 (clica no '4' à esquerda)."
                    }
                } else if (exercise.id == "4.2") {
                    // Selecionar A3 (Jogos) para responder 25%
                    if (_selectedCells.value == setOf("A3")) {
                        isCorrect = true
                    } else {
                        errMsg = "O gráfico mostra 25% na cor amarela que corresponde a 'Jogos' (célula A3). Seleciona a célula A3 e clica em Confirmar!"
                    }
                }
            }
            VerificationType.SHEET_OP -> {
                if (exercise.id == "1.3") {
                    if (currentGrid.currentSheetName == "Turma9A" && currentGrid.sheetColor == 0xFF2196F3) {
                        isCorrect = true
                    } else {
                        errMsg = "Garante que renomeias a folha exatamente para 'Turma9A' no painel das abas e escolhes a cor Azul."
                    }
                }
            }
            VerificationType.COL_WIDTH -> {
                if (exercise.id == "1.4") {
                    val wB = currentGrid.colWidths["B"] ?: 80
                    if (wB >= 150) {
                        isCorrect = true
                    } else {
                        errMsg = "Garante que aumentas a largura da coluna B para 150 de forma a mostrar as notas sem truncar."
                    }
                }
            }
            VerificationType.VAL_CELL -> {
                when (exercise.id) {
                    "1.5" -> {
                        // Data completa em B2 e moeda em C2
                        val cellB2 = currentGrid.cells["B2"]
                        val cellC2 = currentGrid.cells["C2"]
                        if (cellB2?.isDate == true && cellC2?.isCurrency == true) {
                            isCorrect = true
                        } else {
                            errMsg = "Seleciona B2 e escolhe o botão Calendário, e seleciona C2 e escolhe o botão Moeda '€'."
                        }
                    }
                    "1.6" -> {
                        val cellA3 = currentGrid.cells["A3"]
                        val cellA4 = currentGrid.cells["A4"]
                        if (cellA3?.value?.lowercase()?.trim() == "fevereiro" &&
                            cellA4?.value?.lowercase()?.trim() == "março" &&
                            cellA3.alignment == CellAlignment.CENTER &&
                            cellA4.alignment == CellAlignment.CENTER
                        ) {
                            isCorrect = true
                        } else {
                            errMsg = "Conclui o preenchimento automático das células A3 e A4 com 'Fevereiro' e 'Março' alinhados ao Centro."
                        }
                    }
                    "2.1" -> {
                        val cellB6 = currentGrid.cells["B6"]
                        if (cellB6 != null && cellB6.formula.isNotEmpty()) {
                            val cleanFormula = cellB6.formula.uppercase().replace(" ", "")
                            if (cleanFormula == "=MÉDIA(B2:B5)" || cleanFormula == "=MEDIA(B2:B5)") {
                                isCorrect = true
                            } else {
                                errMsg = "Fórmula incorreta na célula B6. Escreve exatamente =MÉDIA(B2:B5)."
                            }
                        } else {
                            errMsg = "Garante que selecionas a célula B6 e introduzes a fórmula matemática da média."
                        }
                    }
                    "2.2" -> {
                        val cellB7 = currentGrid.cells["B7"]
                        val cellB8 = currentGrid.cells["B8"]
                        if (cellB7 != null && cellB8 != null) {
                            val fMax = cellB7.formula.uppercase().replace(" ", "")
                            val fMin = cellB8.formula.uppercase().replace(" ", "")
                            val okMax = fMax == "=MÁXIMO(B2:B5)" || fMax == "=MAXIMO(B2:B5)" || fMax == "=MAX(B2:B5)"
                            val okMin = fMin == "=MÍNIMO(B2:B5)" || fMin == "=MINIMO(B2:B5)" || fMin == "=MIN(B2:B5)"
                            if (okMax && okMin) {
                                isCorrect = true
                            } else {
                                errMsg = "As fórmulas devem ser =MÁXIMO(B2:B5) em B7 e =MÍNIMO(B2:B5) em B8."
                            }
                        } else {
                            errMsg = "Garante que preenches as duas fórmulas das células B7 (máximo) e B8 (mínimo)."
                        }
                    }
                    "2.3" -> {
                        val cellB9 = currentGrid.cells["B9"]
                        if (cellB9 != null) {
                            val cleanF = cellB9.formula.uppercase().replace(" ", "")
                            if (cleanF == "=CONTAR.SE(B2:B5;\">=10\")" || cleanF == "=CONTAR.SE(B2:B5,\">=10\")") {
                                isCorrect = true
                            } else {
                                errMsg = "Sintaxe incorreta para contar positivas. Escreve: =CONTAR.SE(B2:B5; \">=10\")"
                            }
                        } else {
                            errMsg = "Seleciona a célula B9 e insere a fórmula de contagem com a condição de nota positiva."
                        }
                    }
                    "3.1" -> {
                        val cellD4 = currentGrid.cells["D4"]
                        if (cellD4 != null) {
                            val cleanF = cellD4.formula.uppercase().replace(" ", "")
                            if (cleanF == "=B4*C4" || cleanF == "=C4*B4") {
                                isCorrect = true
                            } else {
                                errMsg = "A fórmula para multiplicar quantidade e preço unitário da linha 4 deve ser =B4*C4."
                            }
                        } else {
                            errMsg = "Primeiro seleciona 'D4' e digita a fórmula de multiplicação de quantidade por valor unitário."
                        }
                    }
                    "3.2" -> {
                        val cellD5 = currentGrid.cells["D5"]
                        val cellD6 = currentGrid.cells["D6"]
                        val cellD7 = currentGrid.cells["D7"]

                        if (cellD5 != null && cellD6 != null && cellD7 != null) {
                            val f5 = cellD5.formula.uppercase().replace(" ", "")
                            val f6 = cellD6.formula.uppercase().replace(" ", "")
                            val f7 = cellD7.formula.uppercase().replace(" ", "")

                            val ok5 = f5 == "=B5*C5" || f5 == "=C5*B5" || cellD5.value == "10.20"
                            val ok6 = f6 == "=B6*C6" || f6 == "=C6*B6" || cellD6.value == "25.00"
                            val ok7 = f7 == "=SOMA(D4:D6)" || f7 == "=SUM(D4:D6)"

                            if (ok5 && ok6 && ok7) {
                                isCorrect = true
                            } else {
                                errMsg = "Falta arrastar a fórmula até D6 e colocar =SOMA(D4:D6) na célula D7."
                            }
                        } else {
                            errMsg = "Completa as colunas de totais arrastando a fórmula e soma tudo na célula D7!"
                        }
                    }
                    "3.3" -> {
                        val cellE4 = currentGrid.cells["E4"]
                        if (cellE4 != null) {
                            val cleanF = cellE4.formula.uppercase().replace(" ", "")
                            if (cleanF == "=D4*\$B\$1" || cleanF == "=\$B\$1*D4") {
                                isCorrect = true
                            } else {
                                errMsg = "Fórmula do desconto incorreta. Usa a referência absoluta \$ para fixar: =D4*\$B\$1"
                            }
                        } else {
                            errMsg = "Seleciona 'E4' e escreve a fórmula aplicando referências fixas: =D4*\$B\$1."
                        }
                    }
                    "3.4" -> {
                        // All entries in columns D & E for lines 4 to 6 must have isCurrency = true
                        val coords = listOf("D4", "D5", "D6", "E4", "E5", "E6")
                        val allMoeda = coords.all { currentGrid.cells[it]?.isCurrency == true }
                        if (allMoeda) {
                            isCorrect = true
                        } else {
                            errMsg = "Nem todas as células no intervalo de valores foram formatadas como Moeda (€)."
                        }
                    }
                }
            }
            VerificationType.COND_FORMAT -> {
                if (exercise.id == "2.4") {
                    val bgB2 = currentGrid.cells["B2"]?.bgCellColor ?: 0
                    val bgB4 = currentGrid.cells["B4"]?.bgCellColor ?: 0
                    if (bgB2 == 0xFFE8F5E9 && bgB4 == 0xFFFFEBEE) {
                        isCorrect = true
                    } else {
                        errMsg = "Garante que aplicas a formatação condicional escola para pintar notas acima de 15 a verde e abaixo de 10 a vermelho!"
                    }
                }
            }
            VerificationType.CHART -> {
                if (exercise.id == "4.1") {
                    if (currentGrid.charts.any { !it.isPie && (it.title.contains("Venda") || it.title.contains("Artigo")) }) {
                        isCorrect = true
                    } else {
                        errMsg = "InfeIizmente não encontrámos um gráfico de Colunas com o título correto adicionado à folha."
                    }
                }
            }
        }

        if (isCorrect) {
            viewModelScope.launch {
                repository.completeExercise(exercise.id, exercise.points)
                showNotice("Excelente! Resposta 100% correta! Ganhaste ${exercise.points} pontos 🎉", true)
            }
        } else {
            showNotice(errMsg, false)
        }
    }

    // --- QUIZ ENGINE ---

    private fun startQuiz() {
        _quizQuestionIdx.value = 0
        _quizSelectedOption.value = null
        _quizSubmitted.value = false
        _quizScore.value = 0
        _quizCompleted.value = false
        _feedbackMessage.value = null
        _feedbackSuccess.value = null
    }

    fun selectQuizOption(optionIdx: Int) {
        if (_quizSubmitted.value) return
        _quizSelectedOption.value = optionIdx
    }

    fun submitQuizAnswer() {
        val selectedIdx = _quizSelectedOption.value ?: return
        val level = LessonData.levels.find { it.id == _currentLevelId.value } ?: return
        val question = level.quizQuestions[_quizQuestionIdx.value]

        _quizSubmitted.value = true
        if (selectedIdx == question.correctOptionIndex) {
            _quizScore.value += 1
            showNotice("Certo! ${question.explanation}", true)
        } else {
            showNotice("Incorreto. A resposta certa era: '${question.options[question.correctOptionIndex]}'. ${question.explanation}", false)
        }
    }

    fun nextQuizQuestion() {
        val level = LessonData.levels.find { it.id == _currentLevelId.value } ?: return
        _feedbackMessage.value = null
        _feedbackSuccess.value = null
        _quizSelectedOption.value = null
        _quizSubmitted.value = false

        if (_quizQuestionIdx.value < level.quizQuestions.lastIndex) {
            _quizQuestionIdx.value += 1
        } else {
            // Finished Quiz! Save to progress and unlock next level
            _quizCompleted.value = true
            val finalScore = _quizScore.value
            val totalQuestions = level.quizQuestions.size
            val earnedPoints = finalScore * 10

            viewModelScope.launch {
                repository.completeQuiz(_currentLevelId.value, earnedPoints)
                showNotice("Quiz Concluído! Pontuação: $finalScore / $totalQuestions. Desbloqueaste o próximo passo e acumulaste $earnedPoints pontos! Parabéns!", true)
            }
        }
    }
}

enum class AppMode {
    DASHBOARD,
    EXPLORE,
    PRACTICE,
    QUIZ
}
