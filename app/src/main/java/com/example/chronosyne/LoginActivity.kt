package com.example.chronosyne

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.repository.UserRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.*
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val auth = FirebaseAuth.getInstance()
        if (sessionManager.isRemembered() && auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return  // Exit onCreate early to skip login screen
        }

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
    Font(R.font.playfairdisplay_regular)
)

val RalewayFontFamily = FontFamily(
    Font(R.font.raleway_regular)
)

val RobotoFontFamily = FontFamily(
    Font(R.font.robotomono_regular)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var showForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accountNotFound by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }
    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Ensure this string exists
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken!!

            CoroutineScope(Dispatchers.IO).launch {
                userRepository.signInWithGoogle(idToken).onSuccess { user ->
                    // Redirect to MainActivity if user exists

                    if (rememberMe) {
                        SessionManager(context).saveRememberMeState(true)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                }.onFailure { e ->
                    // Handle error
                    withContext(Dispatchers.Main) {
                        inputError = "Google sign-in failed: ${e.message}"
                    }
                }
            }
        } catch (e: Exception) {
            inputError = "Google sign-in failed"
        }
    }



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
        Text(
            "Sign In",
            style = TextStyle(
                fontFamily = PlayfairFontFamily,
                fontSize = 48.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFb29985)
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
                    onValueChange = {
                        email = it
                        inputError = ""
                    },
                    placeholder = { Text(
                        "Enter Email Address",
                        fontFamily = RobotoFontFamily
                        ) },
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
                    onValueChange = {
                        password = it
                        inputError = ""
                    },
                    placeholder = { Text(
                        "Enter Password",
                        fontFamily = RobotoFontFamily
                    ) },
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

                if (inputError.isNotEmpty()) {
                    Text(
                        text = inputError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontFamily = RalewayFontFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (accountNotFound) {
                    Text(
                        "Account does not exist",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    TextButton(onClick = {
                        context.startActivity(Intent(context, SignupActivity::class.java))
                    }) {
                        Text(
                            "Sign Up",
                            style = TextStyle(
                                fontFamily = RalewayFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFFb29985)
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFb29985),
                            uncheckedColor = Color.LightGray
                        )
                    )
                    Text(
                        "Remember Me",
                        color = Color.White,
                        fontFamily = RalewayFontFamily,
                        fontSize = 14.sp
                    )
                }


                TextButton(onClick = { /* TODO: Forgot Password Logic */ }) {
                    Text(
                        "Forgot Password?",
                        style = TextStyle(
                            fontFamily = RalewayFontFamily,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFb29985)
                        )
                    )
                }


                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            inputError = "Please enter your email and password"
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            inputError = "Please enter a valid email address"
                        } else {
                            isLoading = true
                            accountNotFound = false
                            inputError = ""

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // 1. Authenticate with email/password
                                    userRepository.loginWithEmail(email, password)

                                    // 2. Check if user exists in Firestore
                                    val existingUser = userRepository.getUserByEmail(email)

                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        if (existingUser != null) {
                                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                            // Redirect to MainActivity
                                            if (rememberMe) {
                                                SessionManager(context).saveRememberMeState(true)
                                            }
                                            val intent = Intent(context, MainActivity::class.java)
                                            context.startActivity(intent)
                                            (context as Activity).finish()
                                        } else {
                                            // This should rarely happen (user deleted from Firestore but not Auth)
                                            inputError = "Account not found"
                                        }
                                    }
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        accountNotFound = true
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        inputError = "Login failed: ${e.message}"
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 17.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        Text(
                            "CONTINUE",
                            style = TextStyle(
                                fontFamily = RalewayFontFamily,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Or",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontFamily = RalewayFontFamily
                    )
                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Launch Google Sign-In Intent (triggers account picker)
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.google_logo), // Add Google icon
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Google", fontFamily = RalewayFontFamily)
                    }
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
