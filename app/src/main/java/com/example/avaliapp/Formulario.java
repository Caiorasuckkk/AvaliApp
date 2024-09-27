package com.example.avaliapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formulario extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private LinearLayout formContainer;
    private ImageView profile_image;// Container para os formulários disponíveis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        // Inicializa o Firebase Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializa o container do layout
        formContainer = findViewById(R.id.form_container);
        profile_image = findViewById(R.id.profile_image);
        carregarImagemPerfil();

        // Verifica se há formulários disponíveis no Firebase
        verificarFormularios();
    }

    private void verificarFormularios() {
        mDatabase.child("Formulario").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Verifica se há formulários disponíveis
                if (dataSnapshot.exists()) {
                    for (DataSnapshot formSnapshot : dataSnapshot.getChildren()) {
                        // Pega os dados do formulário
                        String titulo = formSnapshot.child("titulo").getValue(String.class);
                        String data = formSnapshot.child("data").getValue(String.class);
                        String formId = formSnapshot.getKey(); // ID do formulário

                        // Verifica se a data é menor que a data atual
                        if (!isDataExpirada(data)) {
                            // Infla o layout do formulário disponível
                            View formView = getLayoutInflater().inflate(R.layout.formulario_disponivel, formContainer, false);

                            // Atualiza o título no layout inflado
                            TextView tituloTextView = formView.findViewById(R.id.txt_form_disp);
                            tituloTextView.setText(titulo);

                            // Acessa o TextView para exibir a data
                            TextView dataTextView = formView.findViewById(R.id.txt_disponivel_ate);
                            dataTextView.setText("Disponível até: " + data);

                            // Adiciona o layout inflado ao container
                            formContainer.addView(formView);

                            // Adiciona o OnClickListener ao botão
                            Button responderButton = formView.findViewById(R.id.button_responder);
                            responderButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Inicie a Activity que contém as perguntas
                                    Intent intent = new Intent(Formulario.this, responder_form.class);
                                    // Passe dados necessários
                                    intent.putExtra("form_id", formId); // Passa o ID do formulário
                                    intent.putExtra("titulo", titulo); // Passa o título
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                } else {
                    // Exibe uma mensagem se não houver formulários
                    Toast.makeText(Formulario.this, "Nenhum formulário disponível", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Formulario.this, "Erro ao carregar formulários.", Toast.LENGTH_SHORT).show();
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
                            Glide.with(Formulario.this)
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
                Toast.makeText(Formulario.this, "Erro ao carregar a imagem de perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isDataExpirada(String dataStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        try {
            Date dataFormulario = sdf.parse(dataStr);
            Date dataAtual = new Date();
            return dataFormulario != null && dataFormulario.before(dataAtual);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
