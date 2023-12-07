package fr.richoux.pobo.gamescreen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.richoux.pobo.R
import fr.richoux.pobo.engine.*

private const val TAG = "pobotag GameView"

@Composable
fun GameActions(viewModel: GameViewModel = viewModel()) {
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()
    IconButton(
        onClick = { viewModel.goBackMove() },
        enabled = canGoBack
    ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Undo Move")
    }
    IconButton(
        onClick = { viewModel.goForwardMove() },
        enabled = canGoForward
    ) {
        Icon(Icons.Filled.ArrowForward, contentDescription = "Redo Move")
    }
}

@Composable
fun MainView(
    viewModel: GameViewModel,
    onTap: (Position) -> Unit = { _ -> },
    displayGameState: String = ""
) {
    val board = viewModel.currentBoard
    val player = viewModel.currentPlayer
    val promotionable = viewModel.getFlatPromotionable()
    val lastMove = viewModel.lastMovePosition
    val selected = viewModel.piecesToPromote.toList()
    val configuration = LocalConfiguration.current
    var landscapeMode: Boolean
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            landscapeMode = false
            Column(
                Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoardView(
                    board = board,
                    lastMove = lastMove,
                    onTap = onTap,
                    promotionable = promotionable,
                    selected = selected
                )
                Spacer(modifier = Modifier.height(8.dp))
                columnAllMode(viewModel, displayGameState, landscapeMode)
            }
        }
        Configuration.ORIENTATION_LANDSCAPE -> {
            landscapeMode = true
            Row(
                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.
            ) {

                Column(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    columnAllMode(viewModel, displayGameState, landscapeMode)
                }

                BoardView(
                    modifier = Modifier.fillMaxHeight(),
                    board = board,
                    lastMove = lastMove,
                    onTap = onTap,
                    promotionable = promotionable,
                    selected = selected
                )

                Column(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    columnAllMode(viewModel, displayGameState, landscapeMode)
                }
            }
        }
    }
}

@Composable
fun columnAllMode(viewModel: GameViewModel = viewModel(), displayGameState: String, landscapeMode: Boolean) {
    val board = viewModel.currentBoard
    val player = viewModel.currentPlayer
    val modifier = if(landscapeMode) Modifier else Modifier.fillMaxWidth()
    PiecesStocksView(
        pool = board.getPlayerPool(fr.richoux.pobo.engine.Color.Blue),
        color = fr.richoux.pobo.engine.Color.Blue,
        modifier = modifier
    )
    Spacer(modifier = Modifier.height(8.dp))
    PiecesStocksView(
        pool = board.getPlayerPool(fr.richoux.pobo.engine.Color.Red),
        color = fr.richoux.pobo.engine.Color.Red,
        modifier = modifier
    )
    Spacer(modifier = Modifier.height(32.dp))
    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Player's turn: ",
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(horizontal = 2.dp)
        )
        val style = TextStyle(
            color = if (player == fr.richoux.pobo.engine.Color.Blue) Color.Blue else Color.Red,
            fontSize = MaterialTheme.typography.body1.fontSize,
            fontWeight = FontWeight.Bold,
            fontStyle = MaterialTheme.typography.body1.fontStyle
        )
        Text(
            text = player.toString(),
            style = style,
            modifier = Modifier
                .padding(horizontal = 2.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = displayGameState,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .padding(horizontal = 2.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    val hasChoiceOfPiece = viewModel.twoTypesInPool()
    if (hasChoiceOfPiece) {
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButtonPoBo(player, viewModel)
        }
    } else {
        val gameState by viewModel.gameState.collectAsState()
        val completeSelectionForRemoval =
            gameState == GameState.SELECTGRADUATION
                    && (((viewModel.stateSelection == GameViewModelState.SELECT3 || viewModel.stateSelection == GameViewModelState.SELECT1OR3) && viewModel.piecesToPromote.size == 3)
                    || ((viewModel.stateSelection == GameViewModelState.SELECT1 || viewModel.stateSelection == GameViewModelState.SELECT1OR3) && viewModel.piecesToPromote.size == 1))
        if(gameState == GameState.SELECTGRADUATION) {
            Button(
                onClick = { viewModel.validateGraduationSelection() },
                enabled = completeSelectionForRemoval
            ) {
                Text(
                    text = "Promotion",
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
fun GameView(viewModel: GameViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val player = viewModel.currentPlayer
    var lastMove: Position? by remember { mutableStateOf(null) }

    when (gameState) {
        GameState.INIT -> {
//            Log.d(TAG, "INIT ${player}")
            MainView(
                viewModel,
                displayGameState = viewModel.displayGameState
            )
            viewModel.goToNextState()
        }
//        GameState.PLAY -> {
//            Log.d(TAG, "PLAY ${player}")
//            if(viewModel.historyCall)
//                lastMove = null
//
//            MainView(
//                viewModel,
//                lastMove = lastMove,
//                displayGameState = viewModel.displayGameState
//            )
//            if(!viewModel.historyCall)
//                viewModel.nextTurn()
//            viewModel.goToNextState()
//        }
        GameState.SELECTPIECE -> {
//            Log.d(TAG, "SELECTPIECE ${player}")
            MainView(
                viewModel,
//                lastMove = lastMove,
                displayGameState = viewModel.displayGameState
            )
        }
        GameState.SELECTPOSITION -> {
//            Log.d(TAG, "SELECTPOSITION ${player}")
            val onSelect: (Position) -> Unit = {
                if (viewModel.canPlayAt(it)) {
                    lastMove = it
                    viewModel.playAt(it)
                }
            }
            MainView(
                viewModel,
//                lastMove = lastMove,
                onTap = onSelect,
                displayGameState = viewModel.displayGameState
            )
            if( (viewModel.p1IsAI && player == fr.richoux.pobo.engine.Color.Blue)
                ||
                (viewModel.p2IsAI && player == fr.richoux.pobo.engine.Color.Red) )
            {
                if( player == fr.richoux.pobo.engine.Color.Blue )
                    viewModel.makeP1AIMove()
                else
                    viewModel.makeP2AIMove()
            }
        }
        GameState.CHECKGRADUATION -> {
//            Log.d(TAG, "CHECKGRADUATION ${player}")
            MainView(
                viewModel,
                displayGameState = viewModel.displayGameState
            )
            viewModel.checkGraduation()
        }
        GameState.AUTOGRADUATION -> {
//            Log.d(TAG, "AUTOGRADUATION ${player}")
            MainView(
                viewModel,
                displayGameState = viewModel.displayGameState
            )
            viewModel.autograduation()
        }
        GameState.SELECTGRADUATION -> {
//            Log.d(TAG, "SELECTGRADUATION ${player}")
            lastMove = null
            val onSelect: (Position) -> Unit = {
                viewModel.selectForGraduationOrCancel(it)
            }
            MainView(
                viewModel,
//                lastMove = lastMove,
                onTap = onSelect,
                displayGameState = viewModel.displayGameState
            )
        }
        GameState.REFRESHSELECTGRADUATION -> {
//            Log.d(TAG, "REFRESHSELECTGRADUATION ${player}")
            lastMove = null
            MainView(
                viewModel,
//                lastMove = lastMove,
                displayGameState = viewModel.displayGameState
            )
            viewModel.goToNextState()
        }
        GameState.END -> {
//            Log.d(TAG, "END ${player}")
            Log.d(TAG, "Winner: ${player}")
            MainView(
                viewModel,
//                lastMove = lastMove,
                displayGameState = viewModel.displayGameState
            )
            val style = TextStyle(
                color = if(player == fr.richoux.pobo.engine.Color.Blue) Color.Blue else Color.Red,
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
                fontStyle = MaterialTheme.typography.body1.fontStyle
            )
            EndOfGameDialog(player, style, viewModel)
        }
    }
}

fun Modifier.customDialogModifier() = layout { measurable, constraints ->

    val placeable = measurable.measure(constraints);
    layout(constraints.maxWidth, constraints.maxHeight){
        placeable.place((constraints.maxWidth - placeable.width)/2, 9*(constraints.maxHeight - placeable.height)/10, 10f)
    }
}

@Composable
fun EndOfGameDialog(player: fr.richoux.pobo.engine.Color, style: TextStyle, viewModel: GameViewModel) {
    val openAlertDialog = remember { mutableStateOf(true) }
    when {
        // ...
        openAlertDialog.value -> {
            AlertDialog(
                onDismissRequest = { openAlertDialog.value = false },
                buttons = {
                    Row() {
                        Button(
                            {
                                openAlertDialog.value = false
                                viewModel.newGame(viewModel.p1IsAI, viewModel.p2IsAI)
                            }
                        ) {
                            Text(text = "Sure!")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            { openAlertDialog.value = false }
                        ) {
                            Text(text = "Next time")
                        }
                    }
                },
                title = {
                    Row()
                    {
                        Text(
                            text = "$player",
                            style = style
                        )
                        Text(
                            text = " wins!"
                        )
                    }
                },
                text = {
                    Text(
                        text = "New game?"
                    )
                },
                modifier = Modifier.customDialogModifier().background(Color.Transparent)
            )
        }
    }
}


@Composable
fun RadioButtonPoBo(player: fr.richoux.pobo.engine.Color, viewModel: GameViewModel) {
    val iconPo = when (player) {
        fr.richoux.pobo.engine.Color.Blue -> R.drawable.blue_po
        fr.richoux.pobo.engine.Color.Red -> R.drawable.red_po
    }
    val iconBo = when (player) {
        fr.richoux.pobo.engine.Color.Blue -> R.drawable.blue_bo
        fr.richoux.pobo.engine.Color.Red -> R.drawable.red_bo
    }

    val selectedValue by viewModel.selectedValue.collectAsState()
    val items = listOf("Po", "Bo")
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = (selectedValue == item),
                        onClick = {
                            viewModel.cancelPieceSelection()
                            when (item) {
                                "Po" -> viewModel.selectPo()
                                else -> viewModel.selectBo()
                            }
                        },
                        role = Role.RadioButton
                    )
                    .padding(8.dp)
            ) {
                IconToggleButton(
                    checked = selectedValue == item,
                    onCheckedChange = {
                        viewModel.cancelPieceSelection()
                        when (item) {
                            "Po" -> viewModel.selectPo()
                            else -> viewModel.selectBo()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (selectedValue == item) {
                                R.drawable.ic_baseline_check_circle_24
                            } else {
                                R.drawable.ic_baseline_circle_24
                            }
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                }
                Image(
                    painter = painterResource(
                        id = when (item) {
                            "Po" -> iconPo
                            else -> iconBo
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(92.dp)
                )
            }
        }
    }
}