package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.LessonViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: LessonViewModel,
    progress: UserProgress,
    onNavigate: (AppMode) -> Unit
) {
    val levels = LessonData.levels

    // Detect the highest unlocked level to be selected as active by default if not set
    val selectedLevelId by viewModel.currentLevelId.collectAsState()
    val activeLevelId = if (selectedLevelId in 1..4) selectedLevelId else progress.unlockedLevel.coerceIn(1, 4)

    val activeLevel = levels.find { it.id == activeLevelId } ?: levels.first()

    // Force selecting the default level internally if state is empty
    LaunchedEffect(activeLevelId) {
        if (selectedLevelId != activeLevelId) {
            viewModel.selectLevel(activeLevelId)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FF) // Cool modern bento off-white background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Bento Top App Bar Header
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Aprender Excel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6750A4), // Bento primary purple
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Olá, Aluno 👋",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1B1F),
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Class indicator circle badge "9ºA"
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEADDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "9ºA",
                            color = Color(0xFF21005D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 2. Bento Hero Card (Nível Ativo)
            item {
                // Calculate real active level exercise progress
                val completedForLevel = activeLevel.exercises.count { ex ->
                    progress.completedExercises.split(",").contains(ex.id)
                }
                val totalExercises = activeLevel.exercises.size
                val isLevelQuizDone = progress.isQuizCompleted(activeLevel.id)

                val rawRatio = if (totalExercises > 0) completedForLevel.toFloat() / totalExercises.toFloat() else 0f
                val basePct = (rawRatio * 100).toInt()
                // Quiz gives extra achievement or cap to 100
                val progressPercent = if (isLevelQuizDone) 100 else basePct.coerceIn(0, 95)

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(28.dp))
                        .testTag("level_card_${activeLevel.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nível Selecionado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${activeLevel.id}. ${activeLevel.title}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1D1B20),
                                    lineHeight = 24.sp
                                )
                            }

                            // Dynamic progress percent badge
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3E8FF))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$progressPercent%",
                                    color = Color(0xFF6750A4),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Beautiful animated progress bar
                        LinearProgressIndicator(
                            progress = progressPercent.toFloat() / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF6750A4),
                            trackColor = Color(0xFFE7E0EC)
                        )

                        Text(
                            text = activeLevel.description,
                            fontSize = 12.sp,
                            color = Color(0xFF49454F),
                            lineHeight = 16.sp
                        )

                        // Three adaptive actions row for active learning
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Explorar
                            OutlinedButton(
                                onClick = {
                                    viewModel.selectLevel(activeLevel.id)
                                    viewModel.setMode(AppMode.EXPLORE)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, Color(0xFF6750A4)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6750A4))
                            ) {
                                Text("Explorar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Praticar
                            Button(
                                onClick = {
                                    viewModel.selectLevel(activeLevel.id)
                                    viewModel.setMode(AppMode.PRACTICE)
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("btn_practice_${activeLevel.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Praticar", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            // Quiz
                            Button(
                                onClick = {
                                    viewModel.selectLevel(activeLevel.id)
                                    viewModel.setMode(AppMode.QUIZ)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_quiz_${activeLevel.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (isLevelQuizDone) {
                                    ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32))
                                } else {
                                    ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.White)
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isLevelQuizDone) Icons.Default.CheckCircle else Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Módulos Section Indicator
            item {
                Text(
                    text = "Grelha Bento de Módulos",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF1D1B20),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }

            // 3. 2x2 Bento Levels Grid Column arrangement
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // First Row of Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BentoGridTile(
                                level = levels[0],
                                progress = progress,
                                isActive = activeLevelId == levels[0].id,
                                symbol = "1",
                                itemsDesc = "Quadricula, Células, Altura, Datas",
                                onSelect = {
                                    viewModel.selectLevel(levels[0].id)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BentoGridTile(
                                level = levels[1],
                                progress = progress,
                                isActive = activeLevelId == levels[1].id,
                                symbol = "fx",
                                itemsDesc = "Médias, Máximos, Contar.Se",
                                onSelect = {
                                    viewModel.selectLevel(levels[1].id)
                                }
                            )
                        }
                    }

                    // Second Row of Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BentoGridTile(
                                level = levels[2],
                                progress = progress,
                                isActive = activeLevelId == levels[2].id,
                                symbol = "$",
                                itemsDesc = "Referência Absoluta, Cifrão",
                                onSelect = {
                                    viewModel.selectLevel(levels[2].id)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BentoGridTile(
                                level = levels[3],
                                progress = progress,
                                isActive = activeLevelId == levels[3].id,
                                symbol = "📊",
                                itemsDesc = "Filtros, Critérios, Gráficos",
                                onSelect = {
                                    viewModel.selectLevel(levels[3].id)
                                }
                            )
                        }
                    }
                }
            }

            // 4. Simulador Ativo Interactive Bento Mini-Widget
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F3E6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFFBDE0BD)), RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Miniature Spreadsheet Mockup Column
                        Column(modifier = Modifier.weight(1.2f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF1A7336), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SIMULADOR ATIVO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1A7336),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Tiny Spreadsheet grid
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(BorderStroke(0.5.dp, Color(0xFFBDE0BD)), RoundedCornerShape(8.dp))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    GridCellMock("A1", isHeader = true)
                                    GridCellMock("B1", isHeader = true)
                                    GridCellMock("C1", isHeader = true)
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    GridCellMock("10")
                                    GridCellMock("20")
                                    GridCellMock("=SOMA", isFormula = true)
                                }
                            }
                        }

                        // Right prompt action Column
                        Column(
                            modifier = Modifier.weight(0.9f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Experimenta inserir fórmulas reais!",
                                fontSize = 11.sp,
                                color = Color(0xFF1A7336),
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )

                            Button(
                                onClick = {
                                    // Directly triggers practicing for the current level
                                    viewModel.selectLevel(activeLevelId)
                                    viewModel.setMode(AppMode.PRACTICE)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7336)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text("Tentar", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 5. Daily pedagogical tip
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Dica:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6750A4)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Para fixares uma célula numa fórmula, usa referências com o cifrão: ",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F),
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF3EDFF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }
                }
            }

            // 6. Stats & General Status Dashboard row
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFFD0BCFF)), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RESUMO GERAL DESDE O INÍCIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${progress.score} pts", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF21005D))
                                Text("Mérito", fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFD0BCFF)))

                            val completedEx = progress.completedExercises.split(",").filter { it.isNotEmpty() }.size
                            Column {
                                Text("$completedEx efetuadas", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF21005D))
                                Text("Atividades", fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFD0BCFF)))

                            Column {
                                Text("${progress.unlockedLevel} / 4", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF21005D))
                                Text("Grau Desbloqueio", fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                        }
                    }
                }
            }

            // 7. Reset action at the bottom
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = { viewModel.resetAllProgress() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar percurso escolar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(
                        text = "Apaga o histórico de atividades e pontuações.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoGridTile(
    level: Level,
    progress: UserProgress,
    isActive: Boolean,
    symbol: String,
    itemsDesc: String,
    onSelect: () -> Unit
) {
    val isUnlocked = level.id <= progress.unlockedLevel
    val isFinished = progress.isQuizCompleted(level.id)

    // Base background and border states according to level locking/finish states
    val (bkg, bdr) = when {
        !isUnlocked -> Pair(Color(0xFFF1F1F4), Color(0xFFE1E1E4))
        isFinished -> Pair(Color(0xFFE8F5E9), Color(0xFFBDE0BD)) // Greenish finished look
        isActive -> Pair(Color(0xFFF3EDFF), Color(0xFF6750A4)) // Lavender highlighting
        else -> Pair(Color.White, Color(0xFFCAC4D0))
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bkg),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(
                BorderStroke(if (isActive) 2.dp else 1.dp, bdr),
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = isUnlocked) { onSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon badge for Bento Grid
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                !isUnlocked -> Color.LightGray
                                isFinished -> Color(0xFF2E7D32)
                                else -> Color(0xFF6750A4)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFinished) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else if (!isUnlocked) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(symbol, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Foco", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Column {
                Text(
                    text = "${level.id}. ${level.title.replace("Nível ${level.id} — ", "")}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUnlocked) Color(0xFF1C1B1F) else Color(0xFF79747E),
                    maxLines = 1,
                    lineHeight = 15.sp
                )
                Text(
                    text = itemsDesc,
                    fontSize = 9.sp,
                    color = Color(0xFF79747E),
                    maxLines = 1,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun GridCellMock(text: String, isHeader: Boolean = false, isFormula: Boolean = false) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(20.dp)
            .background(
                when {
                    isHeader -> Color(0xFFF1F3F4)
                    isFormula -> Color(0xFFE8F0FE)
                    else -> Color.White
                }
            )
            .border(0.2.dp, Color(0xFFBDE0BD)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 8.sp,
            fontWeight = if (isFormula) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isFormula) Color(0xFF1967D2) else Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}
