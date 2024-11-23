package com.example.gestionapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;


import com.example.gestionapp.model.Password;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    private Context context;
    private List<Password> passwordList;
    private FirebaseFirestore db;
    private String userId;

    public PasswordAdapter(Context context, List<Password> passwordList, String userId) {
        this.context = context;
        this.passwordList = passwordList;
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    @Override
    public PasswordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PasswordViewHolder holder, int position) {
        Password password = passwordList.get(position);
        holder.siteTextView.setText(password.getSite());
        holder.usernameTextView.setText(password.getUsername());
        holder.passwordTextView.setText(password.getPassword());

        // Acción para el botón de eliminar
        holder.deleteButton.setOnClickListener(v -> {
            db.collection("users").document(userId).collection("passwords").document(password.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        passwordList.remove(position); // Eliminar de la lista
                        notifyItemRemoved(position);  // Actualizar la vista
                        Toast.makeText(context, "Contraseña eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
        });

        // Acción para el botón de editar
        holder.editButton.setOnClickListener(v -> showEditDialog(password, position));
    }

    @Override
    public int getItemCount() {
        return passwordList.size();
    }



    private void showEditDialog(Password password, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_password, null);
        builder.setView(dialogView);

        EditText siteEditText = dialogView.findViewById(R.id.siteEditText);
        EditText usernameEditText = dialogView.findViewById(R.id.usernameEditText);
        EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);

        // Establecer Datos Actuales
        siteEditText.setText(password.getSite());
        usernameEditText.setText(password.getUsername());
        passwordEditText.setText(password.getPassword());

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String updatedSite = siteEditText.getText().toString();
            String updatedUsername = usernameEditText.getText().toString();
            String updatedPassword = passwordEditText.getText().toString();

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("site", updatedSite);
            updatedData.put("username", updatedUsername);
            updatedData.put("password", updatedPassword);

            db.collection("users").document(userId).collection("passwords").document(password.getId())
                    .update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar la lista y notificar al adaptador
                        password.setSite(updatedSite);
                        password.setUsername(updatedUsername);
                        password.setPassword(updatedPassword);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
