package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.repository.UserRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: throw Exception("Google ID token not found")

                CoroutineScope(Dispatchers.IO).launch {
                    val userRepository = UserRepository()
                    val result = userRepository.signInWithGoogle(idToken)

                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            val intent = Intent(this@SignupActivity, UsernameActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@SignupActivity,
                                "Google Sign-In failed. Please try signing up again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@SignupActivity,
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            val key = System.currentTimeMillis()
            ChronoSyneTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282828)) {
                    SignupScreen(
                        key = key,
                        onGoogleSignIn = { googleSignInLauncher.launch(googleSignInClient.signInIntent) }
                    )
                }
            }
        }
    }
}


@Composable
fun SignupScreen(key: Long, onGoogleSignIn: () -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var email by remember(key) { mutableStateOf("") }
    var password by remember(key) { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf("") }
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }

    LaunchedEffect(Unit) {
        showForm = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create An Account", style = TextStyle(
            fontSize = 40.sp, fontStyle = FontStyle.Italic, color = Color(0xFFb29985)
        ))

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = showForm, enter = slideInVertically { it } + fadeIn(), exit = fadeOut()) {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        inputError = ""
                    },
                    placeholder = { Text("email@domain.com") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        inputError = ""
                    },
                    placeholder = { Text("password123") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                if (inputError.isNotEmpty()) {
                    Text(text = inputError, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                    if (inputError.contains("already exists")) {
                        TextButton(onClick = {
                            context.startActivity(Intent(context, LoginActivity::class.java))
                        }) {
                            Text("Go to Login", color = Color(0xFFb29985))
                        }
                    }
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            inputError = "Please enter your email and password"
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            inputError = "Please enter a valid email address"
                        } else {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = userRepository.signUpWithEmail(email, password, email.substringBefore("@"))
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    if (result.isSuccess) {
                                        val intent = Intent(context, UsernameActivity::class.java)
                                        context.startActivity(intent)
                                    } else {
                                        val msg = result.exceptionOrNull()?.message ?: "Error"
                                        if (msg.contains("already in use", ignoreCase = true)) {
                                            withContext(Dispatchers.Main) {
                                                val intent = Intent(context, LoginActivity::class.java).apply {
                                                    putExtra("prefilled_email", email)
                                                }
                                                context.startActivity(intent)
                                            }
                                        } else {
                                            inputError = msg
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        Text("SIGN UP", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TermsAndPrivacyText()

                Button(
                    onClick = onGoogleSignIn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp) // Slightly larger
                            .padding(end = 12.dp)
                    )
                    Text(
                        text = "Sign in with Google",
                        color = Color.Black // Explicitly set the text color
                    )
                }

            }
        }
    }
}


@Composable
fun TermsAndPrivacyText() {
    val openDialog = remember { mutableStateOf(false) }
    val dialogText = remember { mutableStateOf("") }

    val annotatedText = buildAnnotatedString {
        append("By clicking continue, you agree to our ")

        // Terms of Service
        pushStringAnnotation(tag = "TOS", annotation = "tos")
        withStyle(style = SpanStyle(color = Color(0xFFb29985))) {
            append("Terms of Service")
        }
        pop()

        append(" and ")

        // Privacy Policy
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(style = SpanStyle(color = Color(0xFFb29985))) {
            append("Privacy Policy")
        }
        pop()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ClickableText(
            text = annotatedText,
            onClick = { offset ->
                annotatedText.getStringAnnotations(offset, offset).firstOrNull()?.let {
                    when (it.tag) {
                        "TOS" -> {
                            dialogText.value = "Here are our Terms of Service..." // Replace with your actual content
                            openDialog.value = true
                        }
                        "PRIVACY" -> {
                            dialogText.value = "Here is our Privacy Policy..."
                            openDialog.value = true
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontFamily = RalewayFontFamily,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text("Info")
            },
            text = {
                Text(dialogText.value)
            },
            confirmButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignupScreenPreview() {
    ChronoSyneTheme {
        SignupScreen(
            key = 123L, // any dummy key for preview
            onGoogleSignIn = {}
        )
    }
}
