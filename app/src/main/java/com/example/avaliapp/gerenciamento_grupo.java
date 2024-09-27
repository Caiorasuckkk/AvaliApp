package com.example.avaliapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;

public class gerenciamento_grupo extends AppCompatActivity {

    private static final String TAG = "GerenciamentoGrupoActivity";
    private EditText editTextTitle;
    private Button buttonSaveGroup, buttonMeusGrupos;
    private RecyclerView recyclerViewUsers, recyclerViewGroups;
    private UserAdapter userAdapter;
    private GroupAdapter groupAdapter; // Adapter para os grupos
    private List<User> userList = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();
    private List<Group> groupList = new ArrayList<>(); // Lista para os grupos
    private String currentUserArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciamento_grupo);
        fetchUserGroups();

        editTextTitle = findViewById(R.id.editTextTitle);
        buttonSaveGroup = findViewById(R.id.buttonSaveGroup);
        buttonMeusGrupos = findViewById(R.id.buttonMeusGrupos); // Botão para meus grupos
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewGroups = findViewById(R.id.recyclerViewGroups); // RecyclerView para grupos

        buttonSaveGroup.setEnabled(false); // Desabilita o botão inicialmente

        // Configuração do RecyclerView para usuários
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this::onUserSelected);
        recyclerViewUsers.setAdapter(userAdapter);

        // Configuração do RecyclerView para grupos
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new GroupAdapter(this, groupList); // Passando o contexto
        recyclerViewGroups.setAdapter(groupAdapter);

        // Referência ao Firebase Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        // Listener para buscar os dados dos usuários
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear(); // Limpa a lista anterior
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "Nenhum usuário encontrado.");
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey(); // Obtém a chave do nó filho (potencial ID do usuário)
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userId); // Define o ID do usuário
                        userList.add(user);
                    } else {
                        Log.d(TAG, "Modelo é nulo para: " + snapshot.getKey());
                    }
                }

                // Obtendo a área do usuário atual
                getCurrentUserArea(area -> {
                    currentUserArea = area;
                    filterAndDisplayUsers();
                    userAdapter.notifyDataSetChanged(); // Notifica o adapter que a lista foi atualizada
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Erro ao buscar dados: " + databaseError.getMessage());
            }
        });

        buttonSaveGroup.setOnClickListener(v -> saveGroup());

        buttonMeusGrupos.setOnClickListener(v -> fetchUserGroups()); // Listener para buscar grupos
    }

    private void fetchUserGroups() {
        groupList.clear(); // Limpar a lista antes de buscar os grupos

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    Group group = groupSnapshot.getValue(Group.class);
                    if (group != null) {
                        String creatorId = groupSnapshot.child("creatorId").getValue(String.class);
                        List<String> userIds = (List<String>) groupSnapshot.child("users").getValue();

                        // Verifica se o usuário é o criador ou se faz parte do grupo
                        if (creatorId != null && (creatorId.equals(userId) || (userIds != null && userIds.contains(userId)))) {
                            group.setUsers(userIds); // Define a lista de IDs
                            group.setCreatorFullName(""); // Inicializa o nome do criador

                            // Busca o nome do criador
                            fetchCreatorFullName(creatorId, group, userIds);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log de erro ao buscar grupos
            }
        });
    }

    private void fetchCreatorFullName(String creatorId, Group group, List<String> userIds) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(creatorId).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullName = dataSnapshot.getValue(String.class);
                if (fullName != null) {
                    group.setCreatorFullName(fullName); // Define o nome do criador
                }
                fetchUserNames(userIds, group); // Busca os nomes dos usuários
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log de erro ao buscar nome do criador
            }
        });
    }

    private void fetchUserNames(List<String> userIds, Group group) {
        if (userIds == null || userIds.isEmpty()) {
            // Caso a lista de IDs de usuário esteja vazia ou nula
            group.setUserNames(new ArrayList<>()); // Define uma lista vazia
            groupAdapter.notifyDataSetChanged(); // Atualiza o adapter
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        List<String> userNames = new ArrayList<>();
        int[] completedRequests = {0}; // Usado para contar as requisições

        for (String userId : userIds) {
            usersRef.child(userId).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String fullName = dataSnapshot.getValue(String.class);
                    if (fullName != null) {
                        userNames.add(fullName);
                    }

                    completedRequests[0]++;
                    // Se todos os nomes foram buscados, você pode adicionar a lista ao grupo
                    if (completedRequests[0] == userIds.size()) {
                        group.setUserNames(userNames); // Adiciona os nomes ao grupo
                        groupList.add(group); // Adiciona o grupo à lista
                        groupAdapter.notifyDataSetChanged(); // Notifica o adapter que os dados mudaram
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Log de erro ao buscar nomes dos usuários
                }
            });
        }
    }

    private void filterAndDisplayUsers() {
        List<User> filteredUsers = new ArrayList<>();
        for (User user : userList) {
            if (isUserEligible(user)) {
                filteredUsers.add(user);
            }
        }
        userAdapter.updateUserList(filteredUsers);
        buttonSaveGroup.setEnabled(!selectedUsers.isEmpty()); // Habilita/desabilita botão com base na seleção
    }

    private boolean isUserEligible(User user) {
        String userArea = user.getArea(); // Obtém a área do usuário

        if (userArea == null) {
            Log.d(TAG, "Área do usuário é nula: " + user.getFullName());
            return false; // Retorna false se a área for nula
        }

        switch (currentUserArea) {
            case "Site Engineering Head":
                return userArea.matches("Project Specialist|Maintenance Lead|Engineer|Project Coordinator|Engineer I|Analista de Engenharia Jr|Jovem Aprendiz");
            case "Project Specialist":
                return userArea.matches("Intern|Technician II");
            case "Maintenance Lead":
                return userArea.matches("Maintenance Coordinator|Coordenador de Utilidades|Engenheiro Especialista|Analista Financeiro Jr");
            case "Project Coordinator":
                return userArea.matches("Engineer I|Intern|Specialized Engineer|Designer|Engineer II");
            default:
                return false;
        }
    }

    private void getCurrentUserArea(final CurrentUserAreaCallback callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referência ao Firebase Database onde os dados do usuário estão armazenados
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Listener para buscar a área do usuário atual
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String area = dataSnapshot.child("area").getValue(String.class); // Obtendo a área
                    callback.onAreaReceived(area); // Chamando o callback com a área recebida
                } else {
                    Log.d(TAG, "Usuário não encontrado: " + currentUserId);
                    callback.onAreaReceived(null); // Retornando null se o usuário não for encontrado
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Erro ao buscar a área do usuário: " + databaseError.getMessage());
                callback.onAreaReceived(null); // Retornando null em caso de erro
            }
        });
    }

    private void onUserSelected(User user, boolean isSelected) {
        if (isSelected) {
            selectedUsers.add(user);
        } else {
            selectedUsers.remove(user);
        }
        buttonSaveGroup.setEnabled(!selectedUsers.isEmpty()); // Habilita/desabilita botão com base na seleção
    }

    private void saveGroup() {
        String groupTitle = editTextTitle.getText().toString().trim();

        if (groupTitle.isEmpty() || selectedUsers.isEmpty()) {
            Log.d(TAG, "Título do grupo ou usuários não selecionados.");
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtendo o UID do usuário autenticado
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Criando um novo grupo
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").push();
        String groupId = groupRef.getKey(); // Obtém o ID do grupo que foi gerado
        HashMap<String, Object> groupData = new HashMap<>();
        groupData.put("title", groupTitle);
        groupData.put("creatorId", userId); // Adicionando o ID do criador

        // Criando uma lista de IDs de usuários para salvar
        List<String> selectedUserIds = new ArrayList<>();
        for (User user : selectedUsers) {
            selectedUserIds.add(user.getId());
        }

        groupData.put("users", selectedUserIds); // Adicionando os IDs dos usuários ao grupo
        groupData.put("id", groupId); // Adiciona o ID do grupo aos dados

        // Salvando o grupo no Firebase Database
        groupRef.setValue(groupData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Grupo salvo com sucesso.");
                Toast.makeText(gerenciamento_grupo.this, "Grupo salvo com sucesso!", Toast.LENGTH_SHORT).show();
                editTextTitle.setText(""); // Limpa o campo de título
                selectedUsers.clear(); // Limpa a lista de usuários selecionados
                buttonSaveGroup.setEnabled(false); // Desabilita o botão

                // Inicia a ChatActivity e passa o ID do grupo
                Intent intent = new Intent(gerenciamento_grupo.this, ChatActivity.class);
                intent.putExtra("GROUP_ID", groupId); // Passa o ID do grupo
                startActivity(intent); // Inicia a atividade
            } else {
                Log.d(TAG, "Erro ao salvar grupo: " + task.getException().getMessage());
                Toast.makeText(gerenciamento_grupo.this, "Erro ao salvar grupo.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Callback para receber a área do usuário
    interface CurrentUserAreaCallback {
        void onAreaReceived(String area);
    }
}