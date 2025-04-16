package com.example.chronosyne

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import android.content.Intent
import androidx.compose.ui.platform.LocalContext


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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

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
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("email@domain.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.White,
                        focusedBorderColor = colorResource(id = R.color.main_color),
                        unfocusedBorderColor = colorResource(id = R.color.lighter_main_color)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.White,
                        focusedBorderColor = colorResource(id = R.color.main_color),
                        unfocusedBorderColor = colorResource(id = R.color.lighter_main_color)
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, WelcomeActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        "BACK",
                        style = TextStyle(
                            fontFamily = RalewayFontFamily,
                            fontSize = 12.sp
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
