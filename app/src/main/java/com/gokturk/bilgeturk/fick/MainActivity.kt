package com.gokturk.bilgeturk.fick

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gokturk.bilgeturk.fick.ui.theme.FickTheme
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

class MainActivity : ComponentActivity() {


    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FickTheme {
                val staticList by remember {
                    mutableStateOf(
                        mutableListOf(
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
                    )
                }
                val context = LocalContext.current
                val ch = Chronometer(context)
                ch.isCountDown = false
                ch.format =  "hh:mm:ss"
               ch.start()


                //var wanted by remember { mutableStateOf(generateWantedNumber(staticList)) }

                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = "Splash") {
                        composable(route = "Splash") {
                            /* Using composable function */
                            SplashScreen(navController)
                        }
                        composable(route = "Game") {
                            /* Using composable function */
                            GameScreen(navController, context,staticList,ch)
                        }
                        composable(route = "GameOver/{time}") {navBackStackEntry->
                            val time = navBackStackEntry.arguments?.getString("time")

                            /* Using composable function */
                            GameOverScreen(navController, time ?: "xx:xx")
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavController){
    
    Button(onClick = {navController.navigate("Game")}) {
            Text(text = "Start Game")
    }

}

@Composable
fun GameScreen(navController: NavController,context: Context,staticList:MutableList<ItemData>, chronometer: Chronometer){
    val test = staticList

    var wanted by remember {
        mutableStateOf(generateWantedNumber(staticList))
    }
    Column() {
        Text(modifier = Modifier.fillMaxWidth(), text = wanted, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(36.dp))
        LazyVerticalGrid(
            modifier = Modifier.padding(16.dp),
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(items = test) { index, cell ->
                Cell(cell, index) {
                    if (!test[it].itemStatus && controlEquality(cell.itemValue,wanted.toInt())) {
                        test[it].itemStatus = true
                        wanted = generateWantedNumber(test){
                            chronometer.stop()
                            navController.navigate("GameOver/${chronometer.base}")
                        }

                        Log.e("Clicked:", "item: $cell index: $index")
                        Log.e("New List:", test.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(navController: NavController, time:String){

    Text(text = convertLongToTime(time.toLong()))
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("HH:mm")
    return format.format(date)

}


private fun controlEquality(first : Int, second:Int):Boolean {
    Log.e("First","$first")
    Log.e("Second","$second")
    return first == second
}

private fun generateWantedNumber(list: List<ItemData>, onFinish: (() -> Unit)? = null): String {
    list.filter { !it.itemStatus }.apply {
        if (this.size != 0) {
            val randomIndex = Random.nextInt(this.size)
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
    Card(
        modifier = Modifier
            .height(64.dp)
            .height(64.dp)
            .clickable {
                Log.e("cell index", "$index")
                onCellClicked.invoke(index)
            },
        border = BorderStroke(2.dp, Color.Red),
        backgroundColor = if (itemData.itemStatus) Color.LightGray else Color.White
    ) {
        Text(modifier = Modifier.fillMaxSize(), text = itemData.itemValue.toString(), textAlign = TextAlign.Center)
    }
}

data class ItemData(
    val itemValue: Int,
    var itemStatus: Boolean = false
)




