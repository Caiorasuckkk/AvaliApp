package com.example.avaliapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class responder_form extends AppCompatActivity {

    private LinearLayout perguntasContainer;
    private DatabaseReference databaseReference;
    private HashMap<String, String> respostas = new HashMap<>();
    private Button btnEnviarRespostas;
    private int totalPerguntas = 10; // Ajuste conforme necessário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder_form);

        perguntasContainer = findViewById(R.id.perguntas_container);
        btnEnviarRespostas = findViewById(R.id.btn_enviar_respostas);
        databaseReference = FirebaseDatabase.getInstance().getReference("Formulario");

        // Carregar perguntas do Firebase
        loadPerguntas();

        btnEnviarRespostas.setOnClickListener(v -> enviarRespostas());
    }

    private void loadPerguntas() {
        String formId = getIntent().getStringExtra("form_id");

        databaseReference.child(formId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String titulo = dataSnapshot.child("titulo").getValue(String.class);
                TextView tituloTextView = findViewById(R.id.tittle_form);
                tituloTextView.setText(titulo);

                for (int i = 1; i <= totalPerguntas; i++) {
                    String pergunta = dataSnapshot.child("pergunta_" + i).getValue(String.class);
                    if (pergunta != null) {
                        addPergunta(pergunta, i); // Passando o índice da pergunta
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(responder_form.this, "Erro ao carregar perguntas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPergunta(String pergunta, int index) {
        View perguntaView = LayoutInflater.from(this).inflate(R.layout.layout_respostas, perguntasContainer, false);
        TextView txtPergunta = perguntaView.findViewById(R.id.txtpergunta_1);
        txtPergunta.setText(pergunta);
        setupButtonListeners(perguntaView, index); // Passando o índice
        perguntasContainer.addView(perguntaView);
    }

    private void setupButtonListeners(View perguntaView, int index) {
        Button btnPessimo = perguntaView.findViewById(R.id.btnpessimo_1);
        Button btnRuim = perguntaView.findViewById(R.id.btnruim_1);
        Button btnMedio = perguntaView.findViewById(R.id.btnmedio_1);
        Button btnBom = perguntaView.findViewById(R.id.btnbom_1);
        Button btnOtimo = perguntaView.findViewById(R.id.btnotimo_1);

        // Obtendo as cores definidas no arquivo colors.xml
        int corOriginalPessimo = getResources().getColor(R.color.pessimo);
        int corOriginalRuim = getResources().getColor(R.color.ruim);
        int corOriginalMedio = getResources().getColor(R.color.medio);
        int corOriginalBom = getResources().getColor(R.color.bom);
        int corOriginalOtimo = getResources().getColor(R.color.otimo);

        View.OnClickListener listener = v -> {
            // Restaurar as cores originais dos botões
            btnPessimo.setBackgroundColor(corOriginalPessimo);
            btnRuim.setBackgroundColor(corOriginalRuim);
            btnMedio.setBackgroundColor(corOriginalMedio);
            btnBom.setBackgroundColor(corOriginalBom);
            btnOtimo.setBackgroundColor(corOriginalOtimo);

            // Destacar o botão clicado com amarelo
            v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));

            // Capturar a resposta
            int valor = 0;
            if (v == btnPessimo) valor = 1;
            else if (v == btnRuim) valor = 2;
            else if (v == btnMedio) valor = 3;
            else if (v == btnBom) valor = 4;
            else if (v == btnOtimo) valor = 5;

            respostas.put("resposta_" + index, String.valueOf(valor)); // Usando o índice para nomear a resposta
            checkAllResponses();
        };

        btnPessimo.setOnClickListener(listener);
        btnRuim.setOnClickListener(listener);
        btnMedio.setOnClickListener(listener);
        btnBom.setOnClickListener(listener);
        btnOtimo.setOnClickListener(listener);
    }

    private void checkAllResponses() {
        // Habilita o botão "Enviar Respostas" apenas se todas as perguntas forem respondidas
        btnEnviarRespostas.setEnabled(respostas.size() == perguntasContainer.getChildCount());
    }

    private void enviarRespostas() {
        if (respostas.size() < perguntasContainer.getChildCount()) {
            Toast.makeText(this, "Por favor, responda todas as perguntas!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obter o email e ID do usuário logado no Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();
        String userEmail = user.getEmail();

        // Obter a data atual
        String formDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String formId = getIntent().getStringExtra("form_id");

        // Referência para as respostas do formulário
        DatabaseReference respostasRef = databaseReference.child(formId).child("respostas").child(userId);

        // Verificar se o usuário já respondeu
        respostasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(responder_form.this, "Você já respondeu a este formulário!", Toast.LENGTH_SHORT).show();
                } else {
                    // Criar um objeto para armazenar as respostas dentro do nó "respostas"
                    HashMap<String, Object> respostaData = new HashMap<>();
                    respostaData.put("data", formDate);
                    respostaData.put("email", userEmail);
                    respostaData.put("respostas", respostas); // Adiciona o HashMap de respostas

                    // Salvar as respostas
                    respostasRef.setValue(respostaData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(responder_form.this, "Respostas enviadas com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(responder_form.this, "Falha ao enviar respostas.", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(responder_form.this, "Erro ao verificar respostas anteriores.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
