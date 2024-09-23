package com.example.avaliapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Menu extends AppCompatActivity {

    private Button view_Gestor,view_formulario,view_horarios;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isGestor = false; // Flag para verificar se o usuário é gestor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        isGestor = false;
        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Referência para o botão view_Gestor
        view_Gestor = findViewById(R.id.view_Gestor);
        view_formulario = findViewById(R.id.view_Formulario);
        view_horarios = findViewById(R.id.view_Horarios);

        verificarPermissao();
        // Verifica permissão do usuário ao carregar a Activity
          view_formulario.setOnClickListener(view -> {
              irAoFormulario();
          });
        view_horarios.setOnClickListener(view -> {
            irAoHorarios();
        });



        // Configura o clique no botão view_Gestor
        view_Gestor.setOnClickListener(v -> {
            if (isGestor) {
                irAoGestor(); // Se o usuário for gestor, vai para a tela de gestão
            } else {
                Toast.makeText(Menu.this, "Você não tem permissão para acessar esta área.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void verificarPermissao() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String permission = dataSnapshot.child("permission").getValue(String.class);
                    if ("gestor".equals(permission)) {
                        isGestor = true; // Usuário tem permissão de gestor
                    } else {
                        isGestor = false; // Usuário não é gestor
                        view_Gestor.setVisibility(View.VISIBLE);
                        view_Gestor.setClickable(true); // Deixa o botão visível e clicável
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Menu.this, "Erro ao verificar permissões.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAoGestor() {
        Intent Gestor = new Intent(Menu.this, gestor.class);
        startActivity(Gestor);
    }
    private void irAoFormulario() {
        Intent formulario = new Intent(Menu.this, Formulario.class);
        startActivity(formulario);
    }
    private void irAoHorarios() {
        Intent horario =new Intent(Menu.this, horarios.class);
        startActivity(horario);
    }
}
