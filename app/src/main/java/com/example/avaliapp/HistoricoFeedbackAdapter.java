package com.example.avaliapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoricoFeedbackAdapter extends RecyclerView.Adapter<HistoricoFeedbackAdapter.ViewHolder> {

    private List<feedbackHistorico> feedbackList;

    public HistoricoFeedbackAdapter(List<feedbackHistorico> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        feedbackHistorico feedback = feedbackList.get(position);
        holder.textViewQuestion.setText(feedback.getQuestionText()); // Adiciona a pergunta
        holder.textViewResponse.setText(feedback.getFeedbackText()); // Adiciona a resposta
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewQuestion;
        public TextView textViewResponse;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textViewQuestion);
            textViewResponse = itemView.findViewById(R.id.textViewResponse);
        }
    }

    public void updateFeedbackList(List<feedbackHistorico> newList) {
        feedbackList = newList;
        notifyDataSetChanged();
    }
}
