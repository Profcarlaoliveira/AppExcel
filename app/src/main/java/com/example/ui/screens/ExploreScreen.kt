package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: LessonViewModel,
    onBack: () -> Unit
) {
    val levelId by viewModel.currentLevelId.collectAsState()
    val level = LessonData.levels.find { it.id == levelId } ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aprender — Nível $levelId", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Level main title card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6750A4)), // Bento premium purple
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFD0BCFF)), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = level.title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = level.description,
                        color = Color(0xFFEADDFF),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Pedagogical Explanations and Guides
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Conceitos Fundamentais que deves saber:",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF1C1B1F),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    when (levelId) {
                        1 -> ExploreLevel1Details()
                        2 -> ExploreLevel2Details()
                        3 -> ExploreLevel3Details()
                        4 -> ExploreLevel4Details()
                    }
                }
            }

            // Interactive Animated Concept Simulation Playground
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Demonstração Visual Automática:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InteractiveDemoVisualizer(levelId)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Go to Practice Mode for this level
            Button(
                onClick = { viewModel.setMode(AppMode.PRACTICE) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("explore_start_practice")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Iniciar prática")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estou pronto para Praticar!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- EXPLORATORY DETAIL BUILDERS IN PORTUGUESE ---

@Composable
fun ExploreLevel1Details() {
    val items = listOf(
        "**Célula e Endereço**: A intersecção de uma coluna (letras A, B, C...) e uma linha (números 1, 2, 3...) forma uma célula (ex: **A1** ou **B3**).",
        "**Intervalo de Células**: Um bloco retangular de células. Representa-se com dois pontos (**:**). Ex: **A1:B3** inclui as células A1, A2, A3, B1, B2 e B3.",
        "**Largura de colunas**: Podes alargar as colunas quando vires carateres cortados ou omitidos.",
        "**Datas e Moeda €**: Formatar dados evita erros. Podes formatar números para exibirem o símbolo **€** e as datas para o formato estendido.",
        "**Preenchimento Automático**: Escreve 'Janeiro' em A1, arrasta ou ativa o preenchimento, e o Sheets infere 'Fevereiro', 'Março' e subsequentes de forma inteligente!"
    )
    items.forEach { bullet ->
        Text(
            text = "• " + bullet.replace("**", ""),
            fontSize = 13.sp,
            color = Color(0xFF444444),
            lineHeight = 18.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun ExploreLevel2Details() {
    val items = listOf(
        "**Função SOMA**: Calcula o somatório final das células. Escreve-se **=SOMA(B2:B5)**.",
        "**Função MÉDIA**: Calcula o valor médio de uma lista escolar ou de gastos. Fórmula: **=MÉDIA(B2:B5)**, que soma os valores e divide pela quantidade de itens.",
        "**MÁXIMO e MÍNIMO**: Funções que detetam instantaneamente os limites de notas. Ex: **=MÁXIMO(B2:B5)** devolve o maior valor e **=MÍNIMO(B2:B5)** o menor.",
        "**Função CONTAR.SE**: Permite contar apenas as células que atendem a um filtro. Ex: **=CONTAR.SE(B2:B10; \">=10\")** conta os alunos com nota positiva.",
        "**Formatação Condicional**: Muda o estilo da célula consoante a regra definida (ex: se nota < 10, pinta o fundo de vermelho de forma automática)."
    )
    items.forEach { bullet ->
        Text(
            text = "• " + bullet.replace("**", ""),
            fontSize = 13.sp,
            color = Color(0xFF444444),
            lineHeight = 18.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun ExploreLevel3Details() {
    val items = listOf(
        "**Estrutura de Tabelas**: Alinhamento centrado de cabeçalhos e limites escuros ajudam na clareza de relatórios profissionais escolares.",
        "**Operadores Matemáticos**: A multiplicação é o asterisco (***) e a divisão a barra (**/**). Ex: `=B4*C4` para volume x preço unitário.",
        "**Referências Relativas**: Ao copiares uma fórmula `=B4*C4` para baixo, o Excel ajusta automaticamente para `=B5*C5` na nova linha.",
        "**Referências Absolutas (\$)**: Se quiseres multiplicar uma coluna inteira por uma percentagem fixa guardada na célula **B1**, tens de 'trancar' essa célula com cifrões: **\$B\$1**.",
        "Se escreveres **=D4*\$B\$1** e arrastares, na próxima linha ficará **=D5*\$B\$1**. O D4 muda para D5, mas o B1 fica fixo!"
    )
    items.forEach { bullet ->
        Text(
            text = "• " + bullet.replace("**", ""),
            fontSize = 13.sp,
            color = Color(0xFF444444),
            lineHeight = 18.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun ExploreLevel4Details() {
    val items = listOf(
        "**Gráfico de Colunas**: Ideal para comparar valores individuais de artigos ou médias de turmas de forma simples.",
        "**Gráfico Circular (Setores ou Pizza)**: Ideal para demonstrar a representação proporcional de partes de um todo (com percentagens e fatias).",
        "**Rótulos e Título**: Todo o gráfico de excel de alta qualidade deve ter um Título claro no topo e uma Legenda para correlacionar os dados com as cores.",
        "**Interpretação analítica**: Analisar picos lógicos ajuda a tomar decisões fundamentadas no dia a dia da escola."
    )
    items.forEach { bullet ->
        Text(
            text = "• " + bullet.replace("**", ""),
            fontSize = 13.sp,
            color = Color(0xFF444444),
            lineHeight = 18.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

// --- ANIMATED CONCEPT VISUALIZATIONS ---

@Composable
fun InteractiveDemoVisualizer(levelId: Int) {
    var animationState by remember { mutableStateOf(0) }

    LaunchedEffect(levelId) {
        while (true) {
            delay(2500)
            animationState = (animationState + 1) % 3
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        when (levelId) {
            1 -> {
                // Animated Cell selection or auto-fill visualizer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AutoPreenchimento Dinâmico:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnimatedCellSimul("A1", "Janeiro", highlight = true)
                        AnimatedCellSimul(
                            "A2",
                            if (animationState >= 1) "Fevereiro" else "",
                            highlight = animationState >= 1
                        )
                        AnimatedCellSimul(
                            "A3",
                            if (animationState >= 2) "Março" else "",
                            highlight = animationState >= 2
                        )
                    }
                }
            }
            2 -> {
                // Formula math walkthrough: =MÉDIA(15; 18; 8; 11)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Como o Sheets calcula a MÉDIA:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "=MÉDIA(B2:B5)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF107C41)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (animationState) {
                            0 -> "Passo 1: Somar as Notas → 15 + 18 + 8 + 11 = 52.0"
                            1 -> "Passo 2: Dividir pela Contagem → 52.0 / 4"
                            else -> "Resultado Final na célula = 13.0"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B5E20),
                        textAlign = TextAlign.Center
                    )
                }
            }
            3 -> {
                // Absolute cell reference visualizer B1 with $
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Vantagem do $ cifrão em fórmulas:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sem $ (Relativo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Na Linha 4: =D4*B1", fontSize = 11.sp)
                            Text(
                                "Ao arrastar: =D5*B2 ❌ (B2 está vazia, dá erro!)",
                                fontSize = 10.sp,
                                color = Color.Red
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .height(60.dp)
                                .width(1.dp)
                        )
                        Column {
                            Text("Com \$ (Absoluto)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF107C41))
                            Text("Na Linha 4: =D4*\$B\$1", fontSize = 11.sp)
                            Text(
                                "Ao arrastar: =D5*\$B\$1 ✔️ (B1 travado com desconto!)",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
            4 -> {
                // Column chart simulator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gerador Automático de Gráficos:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(70.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AnimatedChartBarSimul("Canetas", 40.dp, animationState == 0)
                        AnimatedChartBarSimul("Cadernos", 75.dp, animationState == 1)
                        AnimatedChartBarSimul("Lápis", 55.dp, animationState == 2)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCellSimul(coord: String, valText: String, highlight: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(coord, fontSize = 9.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(65.dp, 34.dp)
                .background(
                    if (highlight) Color(0xFFE8F5E9) else Color.White,
                    RoundedCornerShape(4.dp)
                )
                .border(
                    width = if (highlight) 1.5.dp else 0.5.dp,
                    color = if (highlight) Color(0xFF107C41) else Color(0xFFCCCCCC),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(valText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AnimatedChartBarSimul(label: String, heightVal: androidx.compose.ui.unit.Dp, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(heightVal)
                .background(
                    if (active) Color(0xFF107C41) else Color(0xFF81C784),
                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}
