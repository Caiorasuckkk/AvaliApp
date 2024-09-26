package com.example.avaliapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class historicofeedback extends AppCompatActivity {

    private Spinner spinnerRequests;
    private Button buttonShowResponses;
    private TextView textViewResponses;
    private HashMap<String, String> requestIdMap = new HashMap<>(); // Para mapear requestId e feedback
    private ArrayAdapter<String> requestAdapter;
    private ImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historicofeedback);

        spinnerRequests = findViewById(R.id.spinnerRequests);
        buttonShowResponses = findViewById(R.id.buttonShowResponses);
        textViewResponses = findViewById(R.id.textViewResponses);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        profileImage = findViewById(R.id.profile_image);

        loadRequests();
        loadProfileImage();

        spinnerRequests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Limpa as respostas ao selecionar um novo feedback
                textViewResponses.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não faz nada
            }
        });

        buttonShowResponses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedFeedback = (String) spinnerRequests.getSelectedItem();
                String selectedRequestId = getRequestIdByFeedback(selectedFeedback);

                if (selectedRequestId != null) {
                    loadResponses(selectedRequestId);
                } else {
                    Toast.makeText(historicofeedback.this, "Nenhuma requisição selecionada.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadRequests() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Pega o ID do usuário logado

        DatabaseReference feedbackRequestsRef = FirebaseDatabase.getInstance().getReference("feedback_requests");
        feedbackRequestsRef.orderByChild("requesterId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> feedbackList = new ArrayList<>();
                        requestIdMap.clear();

                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            String requestId = requestSnapshot.child("requestId").getValue(String.class);
                            String feedback = requestSnapshot.child("feedback").getValue(String.class);

                            if (feedback != null && !feedback.isEmpty()) {
                                requestIdMap.put(requestId, feedback);
                                feedbackList.add(feedback);
                            }
                        }

                        // Configura o adapter para o spinner
                        requestAdapter = new ArrayAdapter<>(historicofeedback.this, android.R.layout.simple_spinner_item, feedbackList);
                        requestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRequests.setAdapter(requestAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(historicofeedback.this, "Erro ao carregar requisições.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getRequestIdByFeedback(String feedback) {
        for (Map.Entry<String, String> entry : requestIdMap.entrySet()) {
            if (entry.getValue().equals(feedback)) {
                return entry.getKey();
            }
        }
        return null;
    }
    private void loadProfileImage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase.child("users").child(userId).child("imagens").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                            String imageUrl = imageSnapshot.child("imageUrl").getValue(String.class);
                            if (imageUrl != null) {
                                // Usando Glide para carregar a imagem
                                Glide.with(historicofeedback.this)
                                        .load(imageUrl)
                                        .into(profileImage);
                                return;
                            }
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.icon_user); // Imagem padrão
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(historicofeedback.this, "Erro ao carregar a imagem de perfil.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            profileImage.setImageResource(R.drawable.icon_user); // Imagem padrão
        }
    }

    private void loadResponses(String requestId) {
        DatabaseReference responsesRef = FirebaseDatabase.getInstance().getReference("feedback_requests").child(requestId).child("responses");
        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder responsesDisplay = new StringBuilder();

                for (DataSnapshot responseSnapshot : dataSnapshot.getChildren()) {
                    String responderName = responseSnapshot.child("responderName").getValue(String.class);
                    String response = responseSnapshot.child("response").getValue(String.class);

                    if (responderName != null && response != null) {
                        responsesDisplay.append("").append(responderName).append("\n");
                        responsesDisplay.append("Feedback: ").append(response).append("\n\n");
                    }
                }

                if (responsesDisplay.length() > 0) {
                    textViewResponses.setText(responsesDisplay.toString());
                } else {
                    textViewResponses.setText("Nenhuma resposta encontrada.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(historicofeedback.this, "Erro ao carregar respostas.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
