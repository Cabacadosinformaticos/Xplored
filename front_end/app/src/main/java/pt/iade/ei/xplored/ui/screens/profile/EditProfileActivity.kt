package pt.iade.ei.xplored.ui.screens.profile

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.ui.theme.XploredTheme
import java.util.Locale

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XploredTheme {
                EditProfileScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load current data
    val currentName = SessionManager.getUserName(context)
    val currentEmail = SessionManager.getUserEmail(context)
    val currentAbout = SessionManager.getUserAbout(context)
    val currentCountry = SessionManager.getUserCountry(context)

    var name by remember { mutableStateOf(currentName) }
    var country by remember { mutableStateOf(if (currentCountry.isBlank()) "Portugal" else currentCountry) }
    var about by remember { mutableStateOf(currentAbout) }
    var isSaving by remember { mutableStateOf(false) }

    // Dropdown State
    var expanded by remember { mutableStateOf(false) }

    // --- NEW: Dynamic list of ALL countries in the world ---
    val countries = remember {
        Locale.getISOCountries() // Get all 2-letter codes (PT, US, BR...)
            .map { Locale("", it).displayCountry } // Convert to name (Portugal, United States...)
            .filter { it.isNotBlank() }
            .sorted() // Sort alphabetically
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Informação Pública", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome de Utilizador") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Country Dropdown (ExposedDropdownMenuBox)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = country,
                    onValueChange = {},
                    readOnly = true, // User cannot type freely
                    label = { Text("País") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    countries.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                country = selection
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. About Input
            OutlinedTextField(
                value = about,
                onValueChange = { about = it },
                label = { Text("Sobre Mim") },
                placeholder = { Text("Escreve algo sobre ti...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Save Button
            Button(
                onClick = {
                    if (currentEmail.isNotBlank()) {
                        isSaving = true
                        scope.launch {
                            try {
                                val api = ApiClient.instance.create(UserApiService::class.java)
                                val updatedUser = api.updateProfile(currentEmail, name, about, country)

                                // Save Everything Locally
                                SessionManager.saveUserData(context, updatedUser.name, updatedUser.email)
                                SessionManager.saveUserAbout(context, updatedUser.about)
                                SessionManager.saveUserCountry(context, updatedUser.country ?: "")

                                Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Erro ao guardar.", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Alterações")
                }
            }
        }
    }
}