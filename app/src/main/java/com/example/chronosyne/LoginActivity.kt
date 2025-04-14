package com.example.chronosyne

import android.os.Bundle
import androidx.compose.animation.*
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282828)) {
                    LoginScreen()
                }
            }
        }
    }
}

val PlayfairFontFamily = FontFamily(
    Font(R.font.playfairdisplay_regular)  // Adjust the file name to match your font
)

val RalewayFontFamily = FontFamily(
    Font(R.font.raleway_regular)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(){
    var showForm by remember { mutableStateOf(false) }

    // Automatically show form on load
    LaunchedEffect(Unit) {
        showForm = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828)) // Replace with your primary_color
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign In", style = TextStyle(
            fontFamily = PlayfairFontFamily, fontSize = 48.sp ,fontStyle = FontStyle.Italic, color = Color(0xFFb29985)
        )
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = showForm,
            enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) + fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("email@domain.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.LightGray,
                        containerColor = Color.Transparent
                    )
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.LightGray,
                        containerColor = Color.Transparent
                    )
                )

                TextButton(onClick = { /* TODO: Forgot Password Logic */ }) {
                    Text("Forgot Password?", style = TextStyle(
                        fontFamily = RalewayFontFamily, fontSize = 12.sp , fontStyle = FontStyle.Italic, color = Color(0xFFb29985))
                    )
                }

                Button(
                    onClick = { /* TODO: Continue login */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 17.dp)
                ) {
                    Text(
                        "CONTINUE",
                        style = TextStyle(
                            fontFamily = RalewayFontFamily,
                            fontSize = 12.sp,
                        )

                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ChronoSyneTheme {
        LoginScreen()
    }
}
