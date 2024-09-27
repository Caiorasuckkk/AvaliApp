package com.example.avaliapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList; // Lista de grupos
    private Context context; // Contexto da Activity

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context; // Inicializa o contexto
        this.groupList = groupList; // Inicializa a lista de grupos
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.textViewTitle.setText(group.getTitle()); // Define o título do grupo

        // Exibe o nome do criador do grupo
        holder.textViewCreator.setText("Gestor: " + group.getCreatorFullName());

        // Exibe a lista de usuários
        if (group.getUserNames() != null && !group.getUserNames().isEmpty()) {
            StringBuilder usersString = new StringBuilder("Usuários: ");
            for (String userName : group.getUserNames()) {
                usersString.append(userName).append(", ");
            }
            // Remove a última vírgula e espaço
            usersString.setLength(usersString.length() - 2);
            holder.textViewUsers.setText(usersString.toString());
        } else {
            holder.textViewUsers.setText("Nenhum usuário"); // Caso não haja usuários
        }

        // Configura o botão de chat para iniciar a ChatActivity
        holder.buttonChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("GROUP_ID", group.getId()); // Passa o ID do grupo para a ChatActivity
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return groupList.size(); // Retorna o número de grupos
    }

    // ViewHolder para cada item do grupo
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewUsers;
        TextView textViewCreator;
        Button buttonChat;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle); // Inicializa o TextView do título
            textViewUsers = itemView.findViewById(R.id.textViewUsers); // Inicializa o TextView dos usuários
            textViewCreator = itemView.findViewById(R.id.textViewCreator); // Inicializa o TextView do criador
            buttonChat = itemView.findViewById(R.id.buttonChat); // Inicializa o botão de chat
        }
    }
}
