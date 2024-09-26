package com.example.avaliapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;

    public GroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
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
        holder.textViewTitle.setText(group.getTitle());

        // Exibir o nome do criador
        holder.textViewCreator.setText("Gestor: " + group.getCreatorFullName());

        // Exibir a lista de nomes de usuários no TextView
        if (group.getUserNames() != null && !group.getUserNames().isEmpty()) {
            StringBuilder usersString = new StringBuilder("Usuários: ");
            for (String userName : group.getUserNames()) {
                usersString.append(userName).append(", "); // Adiciona o nome do usuário
            }
            // Remove a última vírgula e espaço
            usersString.setLength(usersString.length() - 2);
            holder.textViewUsers.setText(usersString.toString());
        } else {
            holder.textViewUsers.setText("Nenhum usuário"); // Caso não haja usuários
        }
    }



    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewUsers;
        TextView textViewCreator; // Adicione esta linha

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle); // ID do título
            textViewUsers = itemView.findViewById(R.id.textViewUsers); // ID dos usuários
            textViewCreator = itemView.findViewById(R.id.textViewCreator); // Inicialize o TextView do criador
        }
    }
}
