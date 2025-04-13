package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.ui.theme.ChronoSyneTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronoSyneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1C1C1C) // Dark background
                ) {
                    WelcomeScreen()
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD2BBA0)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.chronologo),
            contentDescription = "Hourglass Icon",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2BBA0)),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("SIGN IN", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, SignupActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2BBA0)),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("SIGN UP", color = Color.Black)
        }
    }
}
