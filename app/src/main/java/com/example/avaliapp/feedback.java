package com.example.avaliapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class feedback extends AppCompatActivity {

    private EditText etFeedbackRequest;
    private Button btnSendFeedbackRequest;
    private RecyclerView rvFeedbackRequests;
    private FeedbackAdapter feedbackAdapter;
    private List<FeedbackRequest> feedbackRequests;
    private ImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Inicializa o FirebaseAuth e DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializa as views
        etFeedbackRequest = findViewById(R.id.et_feedback_request);
        btnSendFeedbackRequest = findViewById(R.id.btn_send_feedback_request);
        rvFeedbackRequests = findViewById(R.id.rv_feedback_feed);
        profileImage = findViewById(R.id.profile_image);

        feedbackRequests = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackRequests, this::sendFeedbackResponse);

        rvFeedbackRequests.setLayoutManager(new LinearLayoutManager(this));
        rvFeedbackRequests.setAdapter(feedbackAdapter);

        // Carregar as solicitações de feedback do Firebase e imagem de perfil
        loadFeedbackRequests();
        loadProfileImage();

        btnSendFeedbackRequest.setOnClickListener(v -> {
            String feedback = etFeedbackRequest.getText().toString();
            if (!feedback.isEmpty()) {
                sendFeedbackRequest(feedback);
            } else {
                Toast.makeText(feedback.this, "Digite um feedback", Toast.LENGTH_SHORT).show();
            }
        });
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
                                Glide.with(feedback.this)
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
                    Toast.makeText(feedback.this, "Erro ao carregar a imagem de perfil.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            profileImage.setImageResource(R.drawable.icon_user); // Imagem padrão
        }
    }

    private void loadFeedbackRequests() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference feedbackRequestsRef = mDatabase.child("feedback_requests");

        feedbackRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                feedbackRequests.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FeedbackRequest feedbackRequest = snapshot.getValue(FeedbackRequest.class);
                    // Verifica se o usuário já respondeu
                    if (feedbackRequest != null && (feedbackRequest.getResponses() == null || !feedbackRequest.getResponses().containsKey(currentUserId))) {
                        feedbackRequests.add(feedbackRequest);
                    }
                }
                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(feedback.this, "Erro ao carregar feedbacks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFeedbackRequest(String feedback) {
        String userId = mAuth.getCurrentUser().getUid();
        String requestId = mDatabase.child("feedback_requests").push().getKey();
        String createdAt = String.valueOf(System.currentTimeMillis());

        if (requestId != null) {
            DatabaseReference userRef = mDatabase.child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        // Cria o FeedbackRequest
                        FeedbackRequest feedbackRequest = new FeedbackRequest(requestId, userId, fullName, feedback, createdAt);
                        DatabaseReference feedbackRequestsRef = mDatabase.child("feedback_requests");

                        feedbackRequestsRef.child(requestId).setValue(feedbackRequest).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(feedback.this, "Solicitação de feedback enviada", Toast.LENGTH_SHORT).show();
                                etFeedbackRequest.setText("");
                            } else {
                                Toast.makeText(feedback.this, "Erro ao enviar solicitação", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(feedback.this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(feedback.this, "Erro ao buscar os dados do usuário.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(feedback.this, "Erro ao criar ID da solicitação.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFeedbackResponse(String feedbackRequestId, String response) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String createdAt = String.valueOf(System.currentTimeMillis());

                    FeedbackResponse feedbackResponse = new FeedbackResponse(response, fullName, createdAt);

                    DatabaseReference feedbackRequestRef = mDatabase.child("feedback_requests").child(feedbackRequestId);
                    feedbackRequestRef.child("responses").child(userId).setValue(feedbackResponse)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(feedback.this, "Resposta enviada com sucesso!", Toast.LENGTH_SHORT).show();
                                    loadFeedbackRequests();
                                } else {
                                    Toast.makeText(feedback.this, "Falha ao enviar a resposta.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(feedback.this, "Erro ao buscar os dados do usuário.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
