package com.example.avaliapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class responder_form extends AppCompatActivity {

    private LinearLayout perguntasContainer;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder_form);

        perguntasContainer = findViewById(R.id.perguntas_container);
        databaseReference = FirebaseDatabase.getInstance().getReference("Formulario");

        // Carregar perguntas do Firebase
        loadPerguntas();
    }

    private void loadPerguntas() {
        String formId = getIntent().getStringExtra("form_id"); // Recebe o ID do formulário

        databaseReference.child(formId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String titulo = dataSnapshot.child("titulo").getValue(String.class);
                // Atualiza o título na Activity
                TextView tituloTextView = findViewById(R.id.tittle_form); // Certifique-se de que há um TextView com esse ID
                tituloTextView.setText(titulo);

                // Carrega as perguntas
                for (int i = 1; i <= 10; i++) { // Ajuste conforme o número máximo de perguntas
                    String pergunta = dataSnapshot.child("pergunta_" + i).getValue(String.class);
                    if (pergunta != null) {
                        addPergunta(pergunta);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Tratar erro
            }
        });
    }

    private void addPergunta(String pergunta) {
        // Inflar o layout da pergunta
        View perguntaView = LayoutInflater.from(this).inflate(R.layout.layout_respostas, perguntasContainer, false);

        // Definir o texto da pergunta
        TextView txtPergunta = perguntaView.findViewById(R.id.txtpergunta_1);
        txtPergunta.setText(pergunta);

        // Adicionar listener aos botões
        setupButtonListeners(perguntaView);

        // Adicionar a pergunta ao container
        perguntasContainer.addView(perguntaView);
    }

    private void setupButtonListeners(View perguntaView) {
        Button btnPessimo = perguntaView.findViewById(R.id.btnpessimo_1);
        Button btnRuim = perguntaView.findViewById(R.id.btnruim_1);
        Button btnMedio = perguntaView.findViewById(R.id.btnmedio_1);
        Button btnBom = perguntaView.findViewById(R.id.btnbom_1);
        Button btnOtimo = perguntaView.findViewById(R.id.btnotimo_1);

        // Configurar o que acontece quando um botão é clicado
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Capturar a resposta e processar (salvar, exibir, etc.)
                String resposta = ((Button) v).getText().toString();
                // Aqui você pode armazenar a resposta no Firebase ou em uma lista
            }
        };

        btnPessimo.setOnClickListener(listener);
        btnRuim.setOnClickListener(listener);
        btnMedio.setOnClickListener(listener);
        btnBom.setOnClickListener(listener);
        btnOtimo.setOnClickListener(listener);
    }
}
