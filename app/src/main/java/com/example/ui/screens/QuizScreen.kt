package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.LessonData
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.LessonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: LessonViewModel,
    onBack: () -> Unit
) {
    val levelId by viewModel.currentLevelId.collectAsState()
    val level = LessonData.levels.find { it.id == levelId } ?: return

    val questionIdx by viewModel.quizQuestionIdx.collectAsState()
    val rawQuestion = level.quizQuestions.getOrNull(questionIdx)

    val selectedOption by viewModel.quizSelectedOption.collectAsState()
    val submitted by viewModel.quizSubmitted.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()
    val completed by viewModel.quizCompleted.collectAsState()

    val feedbackMsg by viewModel.feedbackMessage.collectAsState()
    val feedbackSuccess by viewModel.feedbackSuccess.collectAsState()

    if (completed || rawQuestion == null) {
        // Render detailed game-like results card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Gold Cup",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Parabéns, Inteligência!",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Concluíste com sucesso o Quiz Teórico do Nível $levelId!",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RESUMO DO TEU DESEMPENHO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$quizScore / ${level.quizQuestions.size}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF107C41)
                    )
                    Text(
                        text = "Respostas Corretas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ganhos Escolares: +${quizScore * 10} pontos de mérito 🎉",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("quiz_finish_back_btn")
            ) {
                Text("Desbloquear Próximos Passos ➔", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz — Nível $levelId", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Sair")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F9FF)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Questão ${questionIdx + 1} de ${level.quizQuestions.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "Acertos: $quizScore",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF107C41)
                )
            }

            // Continuous progress bar logic
            LinearProgressIndicator(
                progress = (questionIdx + 1).toFloat() / level.quizQuestions.size.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF6750A4),
                trackColor = Color(0xFFEADDFF)
            )

            // Large Question box
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PERGUNTA DO 9º ANO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6750A4),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rawQuestion.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333),
                        lineHeight = 22.sp
                    )
                }
            }

            // Quiz Options list selector
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rawQuestion.options.forEachIndexed { idx, option ->
                    val isOptionSelected = selectedOption == idx
                    val isCorrectIdx = idx == rawQuestion.correctOptionIndex

                    // Determine background colors for Option Card based on sumbit flags
                    val containerColor = when {
                        submitted && isCorrectIdx -> Color(0xFFE8F5E9) // Green (right option)
                        submitted && isOptionSelected && !isCorrectIdx -> Color(0xFFFFEBEE) // Red (wrong option selected)
                        isOptionSelected -> Color(0xFFF3EDFF) // Bento lavender selected color
                        else -> Color.White
                    }

                    val borderColor = when {
                        submitted && isCorrectIdx -> Color(0xFF81C784)
                        submitted && isOptionSelected && !isCorrectIdx -> Color(0xFFEF9A9A)
                        isOptionSelected -> Color(0xFF6750A4) // Bento purple border
                        else -> Color(0xFFE2E2E6)
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(if (isOptionSelected) 2.dp else 1.dp, borderColor), RoundedCornerShape(18.dp))
                            .clickable(enabled = !submitted) { viewModel.selectQuizOption(idx) }
                            .testTag("quiz_option_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Letter selector A B C represent
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        when {
                                            submitted && isCorrectIdx -> Color(0xFF2E7D32)
                                            submitted && isOptionSelected && !isCorrectIdx -> Color(0xFFC62828)
                                            isOptionSelected -> Color(0xFF107C41)
                                            else -> Color(0xFFF0F0F0)
                                        },
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val charOption = when (idx) {
                                    0 -> "A"
                                    1 -> "B"
                                    else -> "C"
                                }
                                if (submitted && isCorrectIdx) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Ok", tint = Color.White, modifier = Modifier.size(14.dp))
                                } else if (submitted && isOptionSelected && !isCorrectIdx) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Wrong", tint = Color.White, modifier = Modifier.size(14.dp))
                                } else {
                                    Text(
                                        text = charOption,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isOptionSelected) Color.White else Color(0xFF555555)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF444444)
                            )
                        }
                    }
                }
            }

            // Pedagogical immediate answer reason explanations
            AnimatedVisibility(
                visible = submitted && feedbackMsg != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (feedbackSuccess == true) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (feedbackSuccess == true) Color(0xFFC8E6C9) else Color(0xFFFFE0B2), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (feedbackSuccess == true) Icons.Default.CheckCircle else Icons.Default.Lightbulb,
                                contentDescription = "Insight icon",
                                tint = if (feedbackSuccess == true) Color(0xFF2E7D32) else Color(0xFFFFA000)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (feedbackSuccess == true) "Fabuloso! Resposta Certa!" else "Fundamento Pedagógico:",
                                fontWeight = FontWeight.Bold,
                                color = if (feedbackSuccess == true) Color(0xFF2E7D32) else Color(0xFFE65100),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = feedbackMsg ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF444444),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Action buttons
            if (submitted) {
                Button(
                    onClick = { viewModel.nextQuizQuestion() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quiz_next_question_btn")
                ) {
                    Text(
                        text = if (questionIdx == level.quizQuestions.lastIndex) "Finalizar Quiz ➔" else "Próxima Pergunta ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.submitQuizAnswer() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                    enabled = selectedOption != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quiz_submit_answer_btn")
                ) {
                    Text("Confirmar Resposta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
