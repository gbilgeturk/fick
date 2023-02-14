package com.gokturk.bilgeturk.fick

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gokturk.bilgeturk.fick.ui.theme.BorderColor
import com.gokturk.bilgeturk.fick.ui.theme.FickTheme
import com.gokturk.bilgeturk.fick.ui.theme.StartTimeColor
import com.gokturk.bilgeturk.fick.ui.theme.TextColor
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FickTheme {
                var staticList = shuffleList()

                val context = LocalContext.current
                var ch = Chronometer(context)
                ch.isCountDown = false
                ch.format = "hh:mm:ss"


                //var wanted by remember { mutableStateOf(generateWantedNumber(staticList)) }

                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = "Splash") {
                        composable(route = "Splash") {
                            /* Using composable function */
                            ch = Chronometer(context)
                            ch.isCountDown = false
                            ch.format = "hh:mm:ss"
                            SplashScreen(navController)
                        }
                        composable(route = "Game") {
                            /* Using composable function */
                            ch.start()
                            staticList = shuffleList()
                            GameScreen(navController, staticList, ch)
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.start_time)), contentAlignment = Alignment.Center
    ) {
        Button(colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.border_color)),
            shape = RoundedCornerShape(64),
            onClick = { navController.navigate("Game") }) {
            Text(text = " START ", textAlign = TextAlign.Center, fontSize = 24.sp, color = colorResource(R.color.start_time))
        }
    }
}

@Composable
fun GameScreen(navController: NavController, staticList: MutableList<ItemData>, chronometer: Chronometer) {
    val test = staticList


    var isGameOver by remember {
        mutableStateOf(false)
    }

    val onBack = {
        navController.popBackStack()
        staticList.forEach {
            it.itemStatus = false
        }
        isGameOver = false
    }
    BackPressHandler(onBackPressed = onBack)


    var wanted by remember {
        mutableStateOf(generateWantedNumber(staticList))
    }

    var finishTime by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StartTimeColor)
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        if (!isGameOver) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Find the number:",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = TextColor,
                fontStyle = FontStyle.Italic
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = wanted,
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            color = TextColor,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(36.dp))
        LazyVerticalGrid(
            modifier = Modifier.padding(16.dp),
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(items = test) { index, cell ->
                Cell(cell, index) {
                    if (!test[it].itemStatus && controlEquality(cell.itemValue, wanted.toInt())) {
                        test[it].itemStatus = true
                        wanted = generateWantedNumber(test) {
                            chronometer.stop()
                            var time = SystemClock.elapsedRealtime() - chronometer.base
                            val h = (time / 3600000).toInt()
                            val m = (time - h * 3600000).toInt() / 60000
                            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
                            val t = (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
                            finishTime = String.format("%02d:%02d:%02d", h, m, s)
                            isGameOver = true

                        }
                        Log.e("Clicked:", "item: $cell index: $index")
                        Log.e("New List:", test.toString())
                    }
                }
            }
        }
        if (isGameOver) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = finishTime,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = TextColor
            )
            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = BorderColor),
                shape = RoundedCornerShape(64),
                onClick = {
                    navController.popBackStack()
                    staticList.forEach {
                        it.itemStatus = false
                    }
                    isGameOver = false
                }) {
                Text(text = " you can do better ", textAlign = TextAlign.Center, fontSize = 16.sp, color = StartTimeColor)
            }
        }

    }
}

private fun controlEquality(first: Int, second: Int): Boolean {
    Log.e("First", "$first")
    Log.e("Second", "$second")
    return first == second
}

private fun generateWantedNumber(list: List<ItemData>, onFinish: (() -> Unit)? = null): String {
    list.filter { !it.itemStatus }.apply {
        if (this.size != 0) {
            val randomIndex = Random(System.currentTimeMillis()).nextInt(this.size)
            val randomElement = this[randomIndex]
            return randomElement.itemValue.toString()
        } else {
            onFinish?.invoke()
            return "Game Over"
        }
    }
}

@Composable
fun Cell(itemData: ItemData, index: Int, onCellClicked: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .clickable {
                    Log.e("cell index", "$index")
                    onCellClicked.invoke(index)
                }
                .width(64.dp)
                .height(64.dp),
            backgroundColor = Color(0xFFE95C4B),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (text) = createRefs()
                Text(
                    modifier = Modifier.constrainAs(text) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                    text = itemData.itemValue.toString(),
                    color = StartTimeColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center

                )
            }


        }
    }


}

private fun shuffleList(): MutableList<ItemData> {
    return mutableListOf(
        ItemData(1, false),
        ItemData(2, false),
        ItemData(3, false),
        ItemData(4, false),
        ItemData(5, false),
        ItemData(6, false),
        ItemData(7, false),
        ItemData(8, false),
        ItemData(9, false),
        ItemData(10, false),
        ItemData(11, false),
        ItemData(12, false),
        ItemData(13, false),
        ItemData(14, false),
        ItemData(15, false),
        ItemData(16, false)
    ).apply {
        this.shuffle()
    }
}

data class ItemData(
    val itemValue: Int, var itemStatus: Boolean = false
)

@Composable
fun BackPressHandler(
    backPressedDispatcher: OnBackPressedDispatcher? =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    onBackPressed: () -> Unit
) {
    val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
    }

    DisposableEffect(key1 = backPressedDispatcher) {
        backPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.remove()
        }
    }
}




