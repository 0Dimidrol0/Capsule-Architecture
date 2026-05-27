package io.github.dimidrol.capsule.samples.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.dimidrol.capsule.navigation.compose.rememberCapsuleNavigator
import io.github.dimidrol.capsule.samples.compose.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val navigator = rememberCapsuleNavigator(navController)

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(navigator = navigator)
                        }
                        composable("home") {
                            Text("Home")
                        }
                        composable("forgot") {
                            Text("Forgot password")
                        }
                        composable("register") {
                            Text("Registration")
                        }
                    }
                }
            }
        }
    }
}
