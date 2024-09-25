package com.example.avaliapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Menu extends AppCompatActivity {

    private Button view_Gestor, view_formulario, view_horarios, icon_profile;
    private ImageView profile_image; // Adicionando ImageView para o perfil
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

        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Referência para os elementos da interface
        icon_profile = findViewById(R.id.icon_profile);
        profile_image = findViewById(R.id.profile_image); // Inicializa o ImageView
        view_Gestor = findViewById(R.id.view_Gestor);
        view_formulario = findViewById(R.id.view_Formulario);
        view_horarios = findViewById(R.id.view_Horarios);

        verificarPermissao();
        carregarImagemPerfil(); // Carrega a imagem do perfil

        // Configura os cliques nos botões
        view_formulario.setOnClickListener(view -> irAoFormulario());
        view_horarios.setOnClickListener(view -> irAoHorarios());
        icon_profile.setOnClickListener(view -> irAoProfile());
        view_Gestor.setOnClickListener(v -> {
            if (isGestor) {
                irAoGestor(); // Se o usuário for gestor, vai para a tela de gestão
            } else {
                Toast.makeText(Menu.this, "Você não tem permissão para acessar esta área.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAoProfile() {
        Intent Profile = new Intent(Menu.this, profile.class); // Certifique-se de usar o nome correto da sua Activity de perfil
        startActivity(Profile);
    }

    private void verificarPermissao() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String permission = dataSnapshot.child("permission").getValue(String.class);
                    isGestor = "gestor".equals(permission); // Define a permissão do usuário
                    view_Gestor.setVisibility(isGestor ? View.VISIBLE : View.GONE); // Mostra ou esconde o botão
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Menu.this, "Erro ao verificar permissões.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarImagemPerfil() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("imagens").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Para obter a primeira imagem, você pode usar:
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        String imageUrl = imageSnapshot.child("imageUrl").getValue(String.class);
                        if (imageUrl != null) {
                            // Usando Glide para carregar a imagem
                            Glide.with(Menu.this)
                                    .load(imageUrl)
                                    .into(profile_image);
                            return;
                        }
                    }
                } else {
                    // Se não houver imagem, você pode definir uma imagem padrão
                    profile_image.setImageResource(R.drawable.icon_user); // Imagem padrão
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Menu.this, "Erro ao carregar a imagem de perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void irAoGestor() {
        Intent Gestor = new Intent(Menu.this, gestor.class); // Certifique-se de usar o nome correto da sua Activity de gestor
        startActivity(Gestor);
    }

    private void irAoFormulario() {
        Intent formulario = new Intent(Menu.this, Formulario.class); // Certifique-se de usar o nome correto da sua Activity de formulário
        startActivity(formulario);
    }

    private void irAoHorarios() {
        Intent horario = new Intent(Menu.this, horarios.class); // Certifique-se de usar o nome correto da sua Activity de horários
        startActivity(horario);
    }
}
