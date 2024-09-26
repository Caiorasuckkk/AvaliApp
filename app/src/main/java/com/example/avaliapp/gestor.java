package com.example.avaliapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class gestor extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private LinearLayout layoutFormGestor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestor);

        // Inicializa o Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializa o container do layout
        layoutFormGestor = findViewById(R.id.layout_form_gestor);
        Button btncadastros = findViewById(R.id.btn_cadastro);
        btncadastros.setOnClickListener(view -> IrAoCadastro());
        // Botão "Acessar Respostas"
        Button btncriarform = findViewById(R.id.btn_criar_avali);
        btncriarform.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Intent intent = new Intent(gestor.this, criarAvaliacao.class);
                startActivity(intent);
            }
        });

        // Verifica se há formulários disponíveis no Firebase
        verificarFormularios();
    }

    private void IrAoCadastro() {
        Intent Cadastro= new Intent(gestor.this,Cadastro.class);
        startActivity(Cadastro);
    }


    private void verificarFormularios() {
        mDatabase.child("Formulario").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot formSnapshot : dataSnapshot.getChildren()) {
                        String titulo = formSnapshot.child("titulo").getValue(String.class);
                        String data = formSnapshot.child("data").getValue(String.class);
                        String formId = formSnapshot.getKey(); // ID do formulário

                        View formView = getLayoutInflater().inflate(R.layout.form_gestor, layoutFormGestor, false);

                        TextView tituloTextView = formView.findViewById(R.id.txt_form_disp);
                        tituloTextView.setText(titulo);

                        TextView dataTextView = formView.findViewById(R.id.txt_disponivel_ate);
                        dataTextView.setText("Disponível até: " + data);

                        ImageView imageViewApagar = formView.findViewById(R.id.image_view_apagar);
                        imageViewApagar.setOnClickListener(v -> showDeleteConfirmationDialog(formId));

                        // Botão para analisar respostas
                        Button analiRespostasButton = formView.findViewById(R.id.anali_respostas);
                        analiRespostasButton.setOnClickListener(v -> {
                            Intent intent = new Intent(gestor.this, graficos.class);
                            intent.putExtra("FORM_ID", formId); // Passando o ID do formulário
                            startActivity(intent);
                        });

                        layoutFormGestor.addView(formView);
                    }
                } else {
                    Toast.makeText(gestor.this, "Nenhum formulário disponível", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(gestor.this, "Erro ao carregar formulários.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(String formId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Você deseja apagar este formulário?")
                .setPositiveButton("Sim", (dialog, which) -> deleteForm(formId))
                .setNegativeButton("Não", null)
                .create()
                .show();
    }

    private void deleteForm(String formId) {
        mDatabase.child("Formulario").child(formId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(gestor.this, "Formulário apagado com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(gestor.this, "Erro ao apagar o formulário!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
