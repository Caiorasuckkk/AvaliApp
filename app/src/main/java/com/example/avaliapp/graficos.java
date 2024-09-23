package com.example.avaliapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class graficos extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private BarChart barChart;
    private Spinner spinner;
    private Button verGraficoButton;
    private String formId;
    private List<String> perguntasList = new ArrayList<>(); // Inicialização da lista de perguntas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos);

        // Inicializa o Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializa o BarChart
        barChart = findViewById(R.id.barChart);

        // Inicializa o Spinner e o Button
        spinner = findViewById(R.id.spinnerPerguntas);
        verGraficoButton = findViewById(R.id.verGraficoButton);

        // Obtém o ID do formulário passado pela Intent
        formId = getIntent().getStringExtra("FORM_ID");
        if (formId != null) {
            carregarPerguntas(); // Carrega as perguntas para o Spinner
        } else {
            Toast.makeText(this, "ID do formulário não foi passado.", Toast.LENGTH_SHORT).show();
        }

        // Configura o clique do botão para exibir o gráfico
        verGraficoButton.setOnClickListener(v -> {
            String perguntaSelecionada = (String) spinner.getSelectedItem(); // Obtém a pergunta selecionada
            if (perguntaSelecionada != null) {
                String numeroPergunta = "resposta_" + perguntaSelecionada.split(" ")[1]; // Converte para o formato "resposta_1", "resposta_2", etc.
                // Chama o método para carregar os dados e gerar o gráfico para a pergunta selecionada
                carregarDados(formId, numeroPergunta);
            } else {
                Toast.makeText(this, "Selecione uma pergunta.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Carrega as perguntas para o Spinner
    // Carrega as perguntas para o Spinner
    private void carregarPerguntas() {
        mDatabase.child("Formulario").child(formId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                perguntasList.clear();
                int i = 1;
                while (dataSnapshot.hasChild("pergunta_" + i)) {
                    String pergunta = dataSnapshot.child("pergunta_" + i).getValue(String.class);
                    if (pergunta != null) {
                        perguntasList.add("Pergunta " + i + " (" + pergunta + ")"); // Adiciona perguntas ao Spinner no formato desejado
                    }
                    i++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(graficos.this, android.R.layout.simple_spinner_item, perguntasList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(graficos.this, "Erro ao carregar perguntas.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Carrega os dados de respostas da pergunta selecionada
    private void carregarDados(String formId, String pergunta) {
        mDatabase.child("Formulario").child(formId).child("respostas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int[] contagemRespostas = new int[5]; // Contagem para respostas de 1 a 5

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Acessa o nó "respostas" dentro de cada usuário
                    DataSnapshot respostasSnapshot = userSnapshot.child("respostas");

                    if (respostasSnapshot.exists()) {
                        // Acessa a resposta específica para a pergunta selecionada
                        String resposta = respostasSnapshot.child(pergunta).getValue(String.class);

                        if (resposta != null) {
                            try {
                                int valor = Integer.parseInt(resposta); // Converte a resposta para número
                                if (valor >= 1 && valor <= 5) {
                                    contagemRespostas[valor - 1]++; // Incrementa a contagem da resposta
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace(); // Tratar erro de conversão
                            }
                        }
                    }
                }

                // Log para verificar a contagem
                Log.d("Contagem Respostas", Arrays.toString(contagemRespostas));
                // Plota o gráfico com a contagem das respostas
                plotarGrafico(contagemRespostas);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(graficos.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Plota o gráfico com as respostas carregadas
    private void plotarGrafico(int[] contagemRespostas) {
        if (contagemRespostas == null || contagemRespostas.length < 5) {
            Toast.makeText(this, "Nenhuma resposta encontrada.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < contagemRespostas.length; i++) {
            // Adiciona entradas como inteiros (i + 1 para alinhamento)
            entries.add(new BarEntry(i + 1, contagemRespostas[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Respostas");
        dataSet.setColors(new int[]{R.color.pessimo_grafico, R.color.ruim_grafico, R.color.medio_grafico, R.color.bom_grafico, R.color.otimo_grafico}, this);
        BarData barData = new BarData(dataSet);

        // Configura a legenda
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        LegendEntry[] legendEntries = new LegendEntry[5];
        legendEntries[0] = new LegendEntry("Péssimo", Legend.LegendForm.SQUARE, 10f, 2f, null, getResources().getColor(R.color.pessimo_grafico));
        legendEntries[1] = new LegendEntry("Ruim", Legend.LegendForm.SQUARE, 10f, 2f, null, getResources().getColor(R.color.ruim_grafico));
        legendEntries[2] = new LegendEntry("Médio", Legend.LegendForm.SQUARE, 10f, 2f, null, getResources().getColor(R.color.medio_grafico));
        legendEntries[3] = new LegendEntry("Bom", Legend.LegendForm.SQUARE, 10f, 2f, null, getResources().getColor(R.color.bom_grafico));
        legendEntries[4] = new LegendEntry("Ótimo", Legend.LegendForm.SQUARE, 10f, 2f, null, getResources().getColor(R.color.otimo_grafico));
        legend.setCustom(legendEntries);

        barChart.setData(barData);

        // Configure para o eixo Y ser inteiro
        barChart.getAxisLeft().setGranularity(1f); // Define a granularidade para 1
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf((int) value); // Converte para inteiro
            }
        });

        barChart.invalidate(); // Atualiza o gráfico
    }
}
