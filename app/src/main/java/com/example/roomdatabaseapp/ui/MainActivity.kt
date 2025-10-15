package com.example.roomdatabaseapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roomdatabaseapp.data.User
import com.example.roomdatabaseapp.ui.theme.RoomDatabaseAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val userViewModel: UserViewModel by viewModels()
        super.onCreate(savedInstanceState)
        setContent {
            RoomDatabaseAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UserApp(userViewModel)
                }
            }
        }
    }
}

@Composable
fun UserApp(userViewModel: UserViewModel = viewModel()) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    val users by userViewModel.users.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    var userToDelete by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("ID (para actualizar/eliminar)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Edad") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                if (name.isNotBlank() && age.isNotBlank()) {
                    val ageInt = age.toIntOrNull() ?: return@Button
                    userViewModel.insertUser(name.trim(), ageInt)
                    name = ""
                    age = ""
                }
            }) {
                Text("Insertar")
            }

            Spacer(Modifier.width(12.dp))

            Button(onClick = {
                val idInt = userId.toIntOrNull() ?: return@Button
                coroutineScope.launch {
                    val existing = users.find { it.id == idInt }
                    if (existing != null) {
                        val newName = if (name.isBlank()) existing.name else name.trim()
                        val newAge = if (age.isBlank()) existing.age else (age.toIntOrNull() ?: existing.age)
                        userViewModel.updateUser(idInt, newName, newAge)
                        userId = ""
                        name = ""
                        age = ""
                    }
                }
            }) {
                Text("Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Usuarios registrados:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(users) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("ID: ${user.id} - ${user.name} (${user.age} años)")
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        userToDelete = user
                        showDeleteDialog = true
                    }) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }

    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    userViewModel.deleteUser(userToDelete!!)
                    showDeleteDialog = false
                    userToDelete = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    userToDelete = null
                }) { Text("Cancelar") }
            },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Deseas eliminar a ${userToDelete?.name}?") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserUI() {
    val fakeUsers = listOf(
        User(id = 1, name = "Alice", age = 25),
        User(id = 2, name = "Bob", age = 30),
        User(id = 3, name = "Carlos", age = 28)
    )

    RoomDatabaseAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Usuarios (vista previa):", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            fakeUsers.forEach { user ->
                Text("ID: ${user.id} - ${user.name} (${user.age} años)")
            }
        }
    }
}