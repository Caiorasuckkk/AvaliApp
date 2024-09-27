package com.example.avaliapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private EditText messageEditText;
    private Button sendButton;
    private DatabaseReference messagesRef;
    private String groupId; // ID do grupo atual
    private String senderId; // ID do usuário logado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Recebe o ID do grupo da Intent
        groupId = getIntent().getStringExtra("GROUP_ID"); // Assumindo que você passa o ID do grupo ao iniciar a Activity
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Obtém o ID do usuário logado

        // Verifica se o groupId é nulo
        if (groupId == null) {
            Toast.makeText(this, "ID do grupo não disponível", Toast.LENGTH_SHORT).show();
            finish(); // Fecha a Activity se o ID do grupo não estiver disponível
            return;
        }

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        messageEditText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.sendButton);

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Atualiza a referência do Firebase para o grupo atual
        messagesRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("messages");

        loadMessages();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageContent = messageEditText.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            // Obtém o nome completo do usuário logado
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(senderId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String senderFullName = dataSnapshot.child("fullName").getValue(String.class); // Supondo que o campo seja "fullName"

                    Message message = new Message(senderId, messageContent, senderFullName); // Passando o nome completo
                    messagesRef.push().setValue(message).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageEditText.setText(""); // Limpa o campo de entrada
                            Toast.makeText(ChatActivity.this, "Mensagem enviada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "Erro ao enviar mensagem", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChatActivity.this, "Erro ao obter nome: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ChatActivity.this, "Por favor, digite uma mensagem", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }
                messageAdapter.notifyDataSetChanged(); // Notifica o adaptador sobre as mudanças
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Erro ao carregar mensagens: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
