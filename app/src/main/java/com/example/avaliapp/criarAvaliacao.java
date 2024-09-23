package com.example.avaliapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class criarAvaliacao extends AppCompatActivity {

    private LinearLayout layoutPerguntasContainer;
    private ImageView btnAdicionar, btnRemover;
    private EditText tituloEditText, editTextPergunta;
    private TextView textViewDataSelecionada;
    private Button btnEnviarForm;

    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_avaliacao);

        // Inicialize os componentes
        layoutPerguntasContainer = findViewById(R.id.layout_perguntas_container);
        btnAdicionar = findViewById(R.id.Adicionar);
        btnRemover = findViewById(R.id.Remover);
        tituloEditText = findViewById(R.id.editTextTitulo);
        textViewDataSelecionada = findViewById(R.id.textViewDataSelecionada);
        btnEnviarForm = findViewById(R.id.btn_enviar_form); // Adicionei o botão de enviar

        // Inicializar o Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Formulario");

        // Configurando os listeners para os botões de título
        configurarTituloListeners();


        // Configurando o botão para adicionar uma nova pergunta
        btnAdicionar.setOnClickListener(v -> adicionarNovaPergunta());

        // Configurando o botão para remover a última pergunta
        btnRemover.setOnClickListener(v -> removerUltimaPergunta());

        // Configurando o listener para o TextView da data
        textViewDataSelecionada.setOnClickListener(v -> abrirCalendario());

        // Configurando o listener do botão de enviar
        btnEnviarForm.setOnClickListener(v -> enviarFormulario());
    }


    private void configurarTituloListeners() {
        ImageView verificarTituloImageView = findViewById(R.id.imageViewVerificarTitulo);
        ImageView apagarTituloImageView = findViewById(R.id.imageViewApagarTitulo);

        // Configurando o comportamento do botão verificar
        verificarTituloImageView.setOnClickListener(v -> {
            tituloEditText.setEnabled(false);  // Desabilita o EditText
            tituloEditText.setTextColor(Color.GRAY);  // Muda a cor do texto para cinza
        });

        // Configurando o comportamento do botão apagar
        apagarTituloImageView.setOnClickListener(v -> {
            tituloEditText.setEnabled(true);  // Habilita o EditText
            tituloEditText.setText("");  // Limpa o texto
            tituloEditText.setTextColor(Color.BLACK);  // Volta a cor original
        });
    }

    // Método para abrir o DatePicker
    private void abrirCalendario() {
        final Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dataSelecionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                    textViewDataSelecionada.setText(dataSelecionada);  // Atualiza o TextView com a data selecionada
                }, ano, mes, dia);

        datePickerDialog.show();
    }

    // Método para adicionar um novo bloco de perguntas
    private void adicionarNovaPergunta() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View novaPergunta = inflater.inflate(R.layout.layout_pergunta, null);

        EditText perguntaEditText = novaPergunta.findViewById(R.id.editText);
        ImageView verificarImageView = novaPergunta.findViewById(R.id.imageViewVerificar);
        ImageView apagarImageView = novaPergunta.findViewById(R.id.imageViewApagar);

        // Adiciona margem esquerda ao EditText
        perguntaEditText.setPadding(16, 0, 0, 0); // 16dp de margem esquerda

        verificarImageView.setOnClickListener(v -> {
            perguntaEditText.setEnabled(false);
            perguntaEditText.setTextColor(Color.GRAY);
        });

        apagarImageView.setOnClickListener(v -> {
            perguntaEditText.setEnabled(true);
            perguntaEditText.setText("");
            perguntaEditText.setTextColor(Color.BLACK);
        });

        // Definindo margens
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        novaPergunta.setLayoutParams(params);

        layoutPerguntasContainer.addView(novaPergunta);
    }

    // Método para remover a última pergunta
    private void removerUltimaPergunta() {
        int count = layoutPerguntasContainer.getChildCount();
        if (count > 0) {
            layoutPerguntasContainer.removeViewAt(count - 1);
        }
    }

    // Função para enviar o formulário
    private void enviarFormulario() {
        String titulo = tituloEditText.getText().toString().trim();
        String data = textViewDataSelecionada.getText().toString().trim();
        ArrayList<String> perguntas = new ArrayList<>();

        // Coletar perguntas do layout
        for (int i = 0; i < layoutPerguntasContainer.getChildCount(); i++) {
            View view = layoutPerguntasContainer.getChildAt(i);
            EditText perguntaEditText = view.findViewById(R.id.editText);

            // Adiciona a pergunta apenas se foi confirmada
            if (!perguntaEditText.isEnabled()) {
                String pergunta = perguntaEditText.getText().toString().trim();
                if (pergunta.isEmpty()) {
                    Toast.makeText(this, "Por favor, preencha todas as perguntas obrigatórias!", Toast.LENGTH_SHORT).show();
                    return;
                }
                perguntas.add(pergunta);
            } else {
                // Se a pergunta não foi confirmada, exibe um aviso
                Toast.makeText(this, "Por favor, confirme todas as perguntas!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Verificar se os campos obrigatórios estão preenchidos
        if (titulo.isEmpty() || perguntas.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Exibir um diálogo de confirmação
        new AlertDialog.Builder(this)
                .setTitle("Salvar Formulário")
                .setMessage("Você deseja salvar este formulário?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Salvar no Firebase
                    salvarFormulario(titulo, data, perguntas);
                })
                .setNegativeButton("Não", null)
                .show();
    }

    // Função para salvar no Firebase
    // Função para salvar no Firebase
    private void salvarFormulario(String titulo, String data, ArrayList<String> perguntas) {
        String formularioId = databaseReference.push().getKey();  // Gera um ID único

        HashMap<String, Object> formularioData = new HashMap<>();
        formularioData.put("titulo", titulo);
        formularioData.put("data", data);

        for (int i = 0; i < perguntas.size(); i++) {
            formularioData.put("pergunta_" + (i + 1), perguntas.get(i));
        }

        databaseReference.child(formularioId).setValue(formularioData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Formulário salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, gestor.class)); // Volta para a tela Gestor
                        finish();  // Fecha a tela atual
                    } else {
                        Toast.makeText(this, "Erro ao salvar formulário!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
