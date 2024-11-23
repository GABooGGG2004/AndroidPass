package com.example.gestionapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionapp.R;
import com.example.gestionapp.model.Password;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;


public class PerfilUsuarioActivity extends AppCompatActivity {


    // Definición de variables
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference passwordsRef;

    private EditText siteEditText, usernameEditText, passwordEditText;
    private Button addPasswordButton, viewPasswordButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        // Inicializar Firebase y referencias
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Verificar si el usuario está autenticado
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sesión no válida. Por favor, inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        passwordsRef = db.collection("users").document(userId).collection("passwords");

        // Inicializar componentes del layout
        siteEditText = findViewById(R.id.siteEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        addPasswordButton = findViewById(R.id.addPasswordButton);
        viewPasswordButton = findViewById(R.id.viewPasswordButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Configurar acciones para los botones
        addPasswordButton.setOnClickListener(v -> addPassword());
        viewPasswordButton.setOnClickListener(v -> openViewPasswordsActivity());
        logoutButton.setOnClickListener(v -> logout());
    }

    // Cerrar sesión y redirigir al Login
    private void logout() {
        auth.signOut();
        startActivity(new Intent(PerfilUsuarioActivity.this, LoginActivity.class));
        finish();
    }

    // Redirigir a la actividad de visualización de contraseñas
    private void openViewPasswordsActivity() {
        String userId = auth.getCurrentUser().getUid();
        Intent intent = new Intent(PerfilUsuarioActivity.this, VerPassActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    // Agregar una nueva contraseña
    private void addPassword() {
        String site = siteEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(site) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(PerfilUsuarioActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el objeto Password sin ID aún
        Password newPassword = new Password(null, site, username, password);

        // Añadir el documento y obtener el ID generado por Firestore
        passwordsRef.add(newPassword)
                .addOnSuccessListener(documentReference -> {
                    newPassword.setId(documentReference.getId());
                    documentReference.update("id", newPassword.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(PerfilUsuarioActivity.this, "Contraseña añadida", Toast.LENGTH_SHORT).show();
                                clearFields();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(PerfilUsuarioActivity.this, "Error al guardar ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PerfilUsuarioActivity.this, "Error al añadir contraseña: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Limpiar los campos una vez utilizados
    private void clearFields() {
        siteEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
    }
}