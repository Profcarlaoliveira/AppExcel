package com.example.data

data class Level(
    val id: Int,
    val title: String,
    val description: String,
    val exercises: List<Exercise>,
    val quizQuestions: List<QuizQuestion>
)

data class Exercise(
    val id: String,
    val title: String,
    val instruction: String,
    val initialData: SpreadsheetData,
    val targetDescription: String,
    val verificationType: VerificationType,
    val hint: String,
    val points: Int = 10
)

enum class VerificationType {
    SELECTION,
    SHEET_OP,
    VAL_CELL,
    COL_WIDTH,
    COND_FORMAT,
    CHART
}

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class SpreadsheetData(
    val columns: List<String> = listOf("A", "B", "C", "D", "E"),
    val rows: Int = 8,
    val cells: Map<String, CellContent> = emptyMap(),
    val colWidths: Map<String, Int> = emptyMap(), // column letter -> width dps
    val rowHeights: Map<Int, Int> = emptyMap(), // row number -> height dps
    val currentSheetName: String = "Folha1",
    val sheetColor: Long = 0xFF4CAF50, // default green
    val charts: List<SimulatedChart> = emptyList()
)

data class CellContent(
    val value: String = "",
    val formula: String = "",
    val isBold: Boolean = false,
    val bgCellColor: Long = 0xFFFFFFFF,
    val textColor: Long = 0xFF000000,
    val alignment: CellAlignment = CellAlignment.LEFT,
    val isCurrency: Boolean = false,
    val isDate: Boolean = false,
    val decimalPlaces: Int = -1
)

enum class CellAlignment {
    LEFT, CENTER, RIGHT
}

data class SimulatedChart(
    val title: String = "",
    val isPie: Boolean = false, // false = column, true = pie
    val dataRange: String = "",
    val hasLegend: Boolean = true,
    val seriesColors: List<Long> = listOf(0xFF2196F3, 0xFF4CAF50, 0xFFFFC107, 0xFFE91E63)
)

object LessonData {
    val levels = listOf(
        Level(
            id = 1,
            title = "Nível 1 — Iniciação e Formatação",
            description = "Aprende a navegar na folha, selecionar células/intervalos, e dar estilo aos teus dados com datas, moedas e preenchimento automático.",
            exercises = listOf(
                Exercise(
                    id = "1.1",
                    title = "Intervalo A1:B3",
                    instruction = "Para começares, clica nas células para selecionares exatamente o intervalo retangular de A1 até B3 (células A1, A2, A3, B1, B2, B3 ao mesmo tempo).",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Nome"), "B1" to CellContent("Idade"),
                            "A2" to CellContent("Pedro"), "B2" to CellContent("14"),
                            "A3" to CellContent("Ana"), "B3" to CellContent("15"),
                            "A4" to CellContent("João"), "B4" to CellContent("14")
                        )
                    ),
                    targetDescription = "Seleciona todas as células de A1 a B3 na grelha.",
                    verificationType = VerificationType.SELECTION,
                    hint = "Clica em A1, A2, A3, B1, B2 e B3 para ficarem destacadas a azul e clica em Confirmar!"
                ),
                Exercise(
                    id = "1.2",
                    title = "Selecionar Linha e Coluna Completa",
                    instruction = "Para manipular dados inteiros, podemos selecionar uma linha ou coluna inteira. Clica na letra 'C' no cabeçalho ou no número '4' na lateral esquerda da grelha.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "C1" to CellContent("Cidade"), "C2" to CellContent("Porto"),
                            "C3" to CellContent("Lisboa"), "C4" to CellContent("Faro")
                        )
                    ),
                    targetDescription = "Seleciona a coluna C inteira ou a linha 4.",
                    verificationType = VerificationType.SELECTION,
                    hint = "Podes selecionar uma coluna inteira clicando no cabeçalho superior (C) ou uma linha clicando no cabeçalho lateral esquerdo (4)."
                ),
                Exercise(
                    id = "1.3",
                    title = "Organizar Folhas",
                    instruction = "Renomeia a folha atual para 'Turma9A' no painel das abas e ajuda a distingui-la alterando a sua cor para Azul (0xFF2196F3) usando os comandos de folha.",
                    initialData = SpreadsheetData(
                        currentSheetName = "Folha1",
                        sheetColor = 0xFF4CAF50
                    ),
                    targetDescription = "Renomeia a folha para 'Turma9A' e define a cor como azul.",
                    verificationType = VerificationType.SHEET_OP,
                    hint = "Utiliza o mini-editor de folhas abaixo da grelha para mudar o nome para 'Turma9A' e escolhe a cor Azul!"
                ),
                Exercise(
                    id = "1.4",
                    title = "Largura da Coluna",
                    instruction = "Alguns textos parecem cortados! Seleciona a coluna B e ajusta a largura para 150px (ou clica no botão 'Ajustar Coluna' para alargar a coluna).",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("ID"), "B1" to CellContent("Nome Completo do Aluno do 9º Ano")
                        ),
                        colWidths = mapOf("A" to 60, "B" to 80)
                    ),
                    targetDescription = "Ajusta a largura da coluna B para 150px ou mais.",
                    verificationType = VerificationType.COL_WIDTH,
                    hint = "Seleciona a coluna B clicando na letra B e clica na ferramenta 'Alargar Coluna' para definir 150px."
                ),
                Exercise(
                    id = "1.5",
                    title = "Datas e Dinheiro",
                    instruction = "Formata a data de nascimento em B2 como 'Data Completa' e o preço do lanche em C2 como Moeda (€) com 2 casas decimais.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Artigo"), "B1" to CellContent("Data de Compra"), "C1" to CellContent("Preço"),
                            "A2" to CellContent("Manual"), "B2" to CellContent("22/05/2026"), "C2" to CellContent("24.5")
                        )
                    ),
                    targetDescription = "Aplica estilo de Data Completa em B2 e Moeda com 2 decimais € em C2.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Seleciona B2 e clica no ícone Calendário. Seleciona C2 e clica no botão '€'. Garante que as casas decimais estão definidas!"
                ),
                Exercise(
                    id = "1.6",
                    title = "Preenchimento de Meses",
                    instruction = "Na célula A2 temos 'Janeiro'. Usa o preenchimento automático inteligente (clica em preencher de A2:A4) para completar automaticamente o mês de Fevereiro em A3 e Março em A4, e alinha-os ao centro.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A2" to CellContent("Janeiro"),
                            "A3" to CellContent(""),
                            "A4" to CellContent("")
                        )
                    ),
                    targetDescription = "Completa os meses Fevereiro e Março em A3 e A4 no centro.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Seleciona A2 e clica em 'AutoPreencher' no menu de ferramentas rápida para o intervalo até A4, ou simplesmente digita 'Fevereiro' em A3 e 'Março' em A4 e alinha-as ao centro!"
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = 101,
                    question = "O que representa uma coordenada como 'B3' numa folha de cálculo?",
                    options = listOf(
                        "Coluna B e Linha 3",
                        "Linha B e Coluna 3",
                        "O valor 3 na folha B"
                    ),
                    correctOptionIndex = 0,
                    explanation = "A coordenada (ou endereço) é constituída pela Letra da Coluna e o Número da Linha respetivos."
                ),
                QuizQuestion(
                    id = 102,
                    question = "Qual dos seguintes símbolos indica um intervalo contínuo de células de A1 até A5?",
                    options = listOf(
                        "A1-A5",
                        "A1:A5",
                        "A1;A5"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Os dois pontos (:) são o operador literário que define um intervalo contínuo entre duas células limite."
                ),
                QuizQuestion(
                    id = 103,
                    question = "Como se chama a funcionalidade que permite arrastar a quadrícula lateral para preencher sequências lógicas (meses, dias, etc.)?",
                    options = listOf(
                        "Cópia Rápida",
                        "Fusão Automática",
                        "Preenchimento Automático"
                    ),
                    correctOptionIndex = 2,
                    explanation = "O Preenchimento Automático analisa o padrão inicial (ex: Janeiro) e preenche as células seguintes com a sequência inteligente (Fevereiro, Março...)."
                )
            )
        ),
        Level(
            id = 2,
            title = "Nível 2 — Funções Matemáticas e Condições",
            description = "Aprende a calcular médias, máximos/mínimos, usar a poderosa função CONTAR.SE e pintar células com Formatação Condicional baseada em notas.",
            exercises = listOf(
                Exercise(
                    id = "2.1",
                    title = "Cálculo da Média Geral",
                    instruction = "Tens as notas de quatro alunos na coluna B. Na célula B6, escreve a fórmula para calcular a MÉDIA de notas dos alunos do intervalo B2:B5.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Aluno"), "B1" to CellContent("Nota Final"),
                            "A2" to CellContent("Pedro"), "B2" to CellContent("15"),
                            "A3" to CellContent("Mariana"), "B3" to CellContent("18"),
                            "A4" to CellContent("Vasco"), "B4" to CellContent("8"),
                            "A5" to CellContent("Filipa"), "B5" to CellContent("11"),
                            "A6" to CellContent("Média Geral:"), "B6" to CellContent("")
                        )
                    ),
                    targetDescription = "Escreve =MÉDIA(B2:B5) na célula B6 para obteres a nota média.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Seleciona B6, abre o teclado de fórmula e digita exatamente =MÉDIA(B2:B5) ou =MEDIA(B2:B5)."
                ),
                Exercise(
                    id = "2.2",
                    title = "Nota Máxima e Mínima",
                    instruction = "Agora descobre a melhor e a pior nota do intervalo B2:B5. Na célula B7 insere a fórmula do maior valor (=MÁXIMO(B2:B5)) e na célula B8 a do menor (=MÍNIMO(B2:B5)).",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Aluno"), "B1" to CellContent("Nota"),
                            "A2" to CellContent("Pedro"), "B2" to CellContent("15"),
                            "A3" to CellContent("Mariana"), "B3" to CellContent("18"),
                            "A4" to CellContent("Vasco"), "B4" to CellContent("8"),
                            "A5" to CellContent("Filipa"), "B5" to CellContent("11"),
                            "A7" to CellContent("Nota Máxima:"), "B7" to CellContent(""),
                            "A8" to CellContent("Nota Mínima:"), "B8" to CellContent("")
                        )
                    ),
                    targetDescription = "Insere =MÁXIMO(B2:B5) em B7 e =MÍNIMO(B2:B5) em B8.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Clica em B7 e coloca =MÁXIMO(B2:B5). Clica em B8 e coloca =MÍNIMO(B2:B5). Não te esqueças do '=' na fórmula!"
                ),
                Exercise(
                    id = "2.3",
                    title = "Total de Notas Positivas (CONTAR.SE)",
                    instruction = "Determina quantos alunos obtiveram nota positiva (maior ou igual a 10). Na célula B9, insere a função: =CONTAR.SE(B2:B5; \">=10\") para contar.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Aluno"), "B1" to CellContent("Nota"),
                            "A2" to CellContent("Pedro"), "B2" to CellContent("15"),
                            "A3" to CellContent("Mariana"), "B3" to CellContent("18"),
                            "A4" to CellContent("Vasco"), "B4" to CellContent("8"),
                            "A5" to CellContent("Filipa"), "B5" to CellContent("11"),
                            "A9" to CellContent("Positivas:"), "B9" to CellContent("")
                        )
                    ),
                    targetDescription = "Escreve =CONTAR.SE(B2:B5; \">=10\") na célula B9.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "A fórmula usa ponto e vírgula ';' para separar o intervalo do critério. O critério deve estar entre aspas e o operador é '>=10'."
                ),
                Exercise(
                    id = "2.4",
                    title = "Formatação Condicional Dinâmica",
                    instruction = "Queremos realçar as notas de forma visual. Aplica Formatação Condicional no intervalo B2:B5 de modo a que notas >= 15 fiquem em tom Verde e as notas < 10 fiquem Vermelhas.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Aluno"), "B1" to CellContent("Nota"),
                            "A2" to CellContent("Pedro"), "B2" to CellContent("15"),
                            "A3" to CellContent("Mariana"), "B3" to CellContent("18"),
                            "A4" to CellContent("Vasco"), "B4" to CellContent("8"),
                            "A5" to CellContent("Filipa"), "B5" to CellContent("11")
                        )
                    ),
                    targetDescription = "Seleciona B2:B5 e ativa a formatação condicional automática.",
                    verificationType = VerificationType.COND_FORMAT,
                    hint = "Seleciona o intervalo B2:B5 e clica na ferramenta 'Format. Condicional' para as pintar automaticamente segundo a regra escolar!"
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = 201,
                    question = "Qual é a fórmula correta para calcular a média das notas no intervalo B2:B10?",
                    options = listOf(
                        "=MÉDIA(B2:B10)",
                        "=SOMA(B2:B10)/10",
                        "=CALCULAR.MEDIA(B2:B10)"
                    ),
                    correctOptionIndex = 0,
                    explanation = "A fórmula correta em português usa a função MÉDIA. O Excel calcula a soma e divide automaticamente pelo número de registos."
                ),
                QuizQuestion(
                    id = 202,
                    question = "Como se comporta a função CONTAR.SE numa folha de cálculo?",
                    options = listOf(
                        "Soma os valores numéricos com uma condição",
                        "Conta unicamente as células de texto",
                        "Conta as células que cumprem um critério específico (como ser >=10)"
                    ),
                    correctOptionIndex = 2,
                    explanation = "A função CONTAR.SE tem dois argumentos: o intervalo a avaliar e o critério de contagem (ex: \">=10\")."
                ),
                QuizQuestion(
                    id = 203,
                    question = "Qual a vantagem de usar o recurso de Formatação Condicional?",
                    options = listOf(
                        "Bloqueia a folha contra alterações de alunos",
                        "Modifica o design da célula (como cor de fundo) consoante o valor lá guardado",
                        "Adiciona limites pretos grossos em todas as células"
                    ),
                    correctOptionIndex = 1,
                    explanation = "A Formatação Condicional permite-nos automatizar o visual do relatório (ex: pintar de vermelho os valores negativos e verde os positivos)."
                )
            )
        ),
        Level(
            id = 3,
            title = "Nível 3 — Fórmulas e Referências Fixas ($)",
            description = "Aprende a multiplicar tabelas de compras de artigos, arrastar fórmulas, usar a função SOMA e proteger referências fixas com cifrões ($).",
            exercises = listOf(
                Exercise(
                    id = "3.1",
                    title = "Preço Total de Compra",
                    instruction = "Temos materiais escolares com Quantidade na coluna B e Preço Unitário na coluna C. Na célula D4 (Preço Total), calcula o custo total das canetas multiplicando a quantidade pelo preço unitário: =B4*C4",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A3" to CellContent("Artigo"), "B3" to CellContent("Qtd"), "C3" to CellContent("Unitário"), "D3" to CellContent("Total"),
                            "A4" to CellContent("Caneta"), "B4" to CellContent("5"), "C4" to CellContent("1.2"), "D4" to CellContent(""),
                            "A5" to CellContent("Caderno"), "B5" to CellContent("3"), "C5" to CellContent("3.4"), "D5" to CellContent(""),
                            "A6" to CellContent("Mochila"), "B6" to CellContent("1"), "C6" to CellContent("25.0"), "D6" to CellContent("")
                        )
                    ),
                    targetDescription = "Na célula D4 escreve a fórmula: =B4*C4",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Seleciona D4 e escreve =B4*C4 usando o asterisco (*) para as multiplicar."
                ),
                Exercise(
                    id = "3.2",
                    title = "Arrastar Fórumlas e Função SOMA",
                    instruction = "Arrasta as fórmulas de D4 para preencher automaticamente as linhas D5 e D6. Na célula D7, calcula o gasto total final usando a função =SOMA(D4:D6).",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A3" to CellContent("Artigo"), "B3" to CellContent("Qtd"), "C3" to CellContent("Unitário"), "D3" to CellContent("Total"),
                            "A4" to CellContent("Caneta"), "B4" to CellContent("5"), "C4" to CellContent("1.2"), "D4" to CellContent("6", formula = "=B4*C4"),
                            "A5" to CellContent("Caderno"), "B5" to CellContent("3"), "C5" to CellContent("3.4"), "D5" to CellContent(""),
                            "A6" to CellContent("Mochila"), "B6" to CellContent("1"), "C6" to CellContent("25.0"), "D6" to CellContent(""),
                            "A7" to CellContent("Soma de Gastos"), "D7" to CellContent("")
                        )
                    ),
                    targetDescription = "Preenche as linhas vazias e coloca =SOMA(D4:D6) na célula D7.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Podes preencher D5 com =B5*C5, D6 com =B6*C6 e de seguida calcular o somatório em D7 com =SOMA(D4:D6)!"
                ),
                Exercise(
                    id = "3.3",
                    title = "O Cifrão do Desconto (\$B\$1)",
                    instruction = "A célula B1 tem o Desconto fixo de 20% (0.20). Na célula E4, calcula o valor do desconto poupado aplicando a taxa fixa ao preço total. Para fixares a célula B1 ao arrastar, deves usar referências absolutas: =D4*\$B\$1",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Desconto:"), "B1" to CellContent("0.2"),
                            "A3" to CellContent("Artigo"), "D3" to CellContent("Total"), "E3" to CellContent("Desconto (€)"),
                            "A4" to CellContent("Caneta"), "D4" to CellContent("6.0"), "E4" to CellContent(""),
                            "A5" to CellContent("Caderno"), "D5" to CellContent("10.2"), "E5" to CellContent(""),
                            "A6" to CellContent("Mochila"), "D6" to CellContent("25.0"), "E6" to CellContent("")
                        )
                    ),
                    targetDescription = "Escreve =D4*\$B\$1 na célula E4 para calcular o desconto poupado de forma fixa.",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Os cifrões travam a célula. Clica em E4 e introduz =D4*\$B\$1 para que possas arrastar sem perder o valor do desconto."
                ),
                Exercise(
                    id = "3.4",
                    title = "Formatação de Limitados e Moedas",
                    instruction = "O gerente quer este relatório muito apresentável. Coloca as colunas D e E com formato Moeda (€), com 2 casas decimais, e alinha a tabela dos artigos ao centro.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Desconto:"), "B1" to CellContent("0.2"),
                            "A3" to CellContent("Artigo"), "D3" to CellContent("Total"), "E3" to CellContent("Desconto (€)"),
                            "A4" to CellContent("Caneta"), "D4" to CellContent("6.0"), "E4" to CellContent("1.20"),
                            "A5" to CellContent("Caderno"), "D5" to CellContent("10.2"), "E5" to CellContent("2.04"),
                            "A6" to CellContent("Mochila"), "D6" to CellContent("25.0"), "E6" to CellContent("5.00")
                        )
                    ),
                    targetDescription = "Formata os totais e descontos das linhas 4, 5 e 6 para Moeda (€).",
                    verificationType = VerificationType.VAL_CELL,
                    hint = "Seleciona as células das colunas D e E (linhas 4 a 6) e clica no botão '€' de formatação."
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = 301,
                    question = "Se escreveres a fórmula '=\$B\$1' e a arrastares para a célula logo abaixo, o que acontece?",
                    options = listOf(
                        "A referência muda para B2 pois é inteligente",
                        "Dá um erro de sintaxe",
                        "A referência continua fixa em B1 devido aos cifrões (\$)"
                    ),
                    correctOptionIndex = 2,
                    explanation = "Os cifrões indicam uma Referência Absoluta, mantendo as colunas e as linhas travadas ao preencher fórmulas."
                ),
                QuizQuestion(
                    id = 302,
                    question = "Qual é o operador correto para multiplicar dois valores numa folha de cálculo?",
                    options = listOf(
                        "O asterisco (*)",
                        "A letra x",
                        "O símbolo de percentagem (%)"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Em informática e folhas de cálculo, a multiplicação é sempre representada pela estrela ou asterisco (*)."
                ),
                QuizQuestion(
                    id = 303,
                    question = "Qual é a fórmula correta para somar o dinheiro do intervalo D4 a D6?",
                    options = listOf(
                        "=D4+D5+D6",
                        "=SOMA(D4:D6)",
                        "Ambas as opções estão corretas"
                    ),
                    correctOptionIndex = 2,
                    explanation = "Ambas dão o mesmo resultado, embora a função =SOMA() seja muito mais fácil e rápida quando há muitas células a analisar."
                )
            )
        ),
        Level(
            id = 4,
            title = "Nível 4 — Gráficos e Apresentação",
            description = "Dá vida aos teus relatórios! Aprende a gerar gráficos de colunas e pizza, interpretar percentagens e tirar conclusões analíticas.",
            exercises = listOf(
                Exercise(
                    id = "4.1",
                    title = "Inserir Gráfico de Colunas",
                    instruction = "Queremos comparar visualmente a quantidade vendida de materiais. Seleciona a tabela e insere um Gráfico de Colunas com o título 'Vendas por Artigo'.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A3" to CellContent("Artigo"), "B3" to CellContent("Unidades"),
                            "A4" to CellContent("Canetas"), "B4" to CellContent("120"),
                            "A5" to CellContent("Cadernos"), "B5" to CellContent("80"),
                            "A6" to CellContent("Lápis"), "B6" to CellContent("210")
                        )
                    ),
                    targetDescription = "Adiciona um gráfico de colunas.",
                    verificationType = VerificationType.CHART,
                    hint = "Clica na ferramenta rápida de gráficos, escolhe o tipo 'Colunas' e confirma o título correspondente!"
                ),
                Exercise(
                    id = "4.2",
                    title = "Interpretação de Gráfico Pizza",
                    instruction = "O nosso gráfico pizza demonstra a distribuição de despesas: Estudar (60%), Jogos (25%) e Lanches (15%). Qual a despesa correspondente a 1/4 (25%) do orçamento? Seleciona a célula correta na grelha que tem essa resposta.",
                    initialData = SpreadsheetData(
                        cells = mapOf(
                            "A1" to CellContent("Despesa"), "B1" to CellContent("Percentagem"),
                            "A2" to CellContent("Estudar"), "B2" to CellContent("60%"),
                            "A3" to CellContent("Jogos"), "B3" to CellContent("25%"),
                            "A4" to CellContent("Lanches"), "B4" to CellContent("15%")
                        ),
                        charts = listOf(
                            SimulatedChart("Distribuição de Orçamento", isPie = true, dataRange = "A1:B4")
                        )
                    ),
                    targetDescription = "Seleciona a célula A3 (Jogos) para responder.",
                    verificationType = VerificationType.SELECTION,
                    hint = "Analisa a pizza: 25% corresponde à fatia amarela dos 'Jogos'. Seleciona a célula A3 e clica em Confirmar!"
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = 401,
                    question = "Qual é o gráfico ideal para visualizar as partes de um todo (por exemplo, a divisão do teu tempo livre em percentagens)?",
                    options = listOf(
                        "Gráfico de Linhas",
                        "Gráfico de Área Tridimensional",
                        "Gráfico Circular (Setores ou Pizza)"
                    ),
                    correctOptionIndex = 2,
                    explanation = "O gráfico circular/pizza é perfeito para ver proporções e fatias de percentagem que completam o total de 100%."
                ),
                QuizQuestion(
                    id = 402,
                    question = "Para que serve a 'Legenda' na apresentação de um gráfico?",
                    options = listOf(
                        "Para identificar a cor correspondente a cada série de dados",
                        "Para escrever a introdução do trabalho escolar",
                        "Para somar as quadrículas de forma automática"
                    ),
                    correctOptionIndex = 0,
                    explanation = "A legenda correlaciona os códigos de cor visualmente com o rótulo de texto (ex: Azul = Canetas)."
                ),
                QuizQuestion(
                    id = 403,
                    question = "Se vires um pico enorme numa coluna do teu gráfico de vendas, a que corresponde?",
                    options = listOf(
                        "Ao menor valor registado na tabela",
                        "Ao item com o maior número de unidades vendidas",
                        "A um erro de cálculo por introduzir texto"
                    ),
                    correctOptionIndex = 1,
                    explanation = "No gráfico de colunas, a altura da barra é proporcional ao valor numérico. A coluna mais alta é sempre o valor máximo."
                )
            )
        )
    )
}
