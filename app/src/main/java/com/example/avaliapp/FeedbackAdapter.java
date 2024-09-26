package com.example.avaliapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<FeedbackRequest> feedbackRequests;
    private OnFeedbackResponseListener responseListener;

    public FeedbackAdapter(List<FeedbackRequest> feedbackRequests, OnFeedbackResponseListener responseListener) {
        this.feedbackRequests = feedbackRequests;
        this.responseListener = responseListener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_resposta, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        FeedbackRequest feedbackRequest = feedbackRequests.get(position);

        // Definir os textos dos TextViews com base no feedbackRequest
        holder.tvUserNameRequester.setText(feedbackRequest.getRequesterFullName());
        holder.tvFeedbackRequest.setText(feedbackRequest.getFeedback());

        // Configurar o botão de envio da resposta
        holder.btnSendFeedbackResponse.setOnClickListener(v -> {
            String response = holder.etFeedbackResponse.getText().toString();
            if (!response.isEmpty() && responseListener != null) {
                // Chamar a resposta quando o botão é clicado
                responseListener.onResponse(feedbackRequest.getRequestId(), response);
                holder.etFeedbackResponse.setText(""); // Limpar o campo após o envio
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedbackRequests.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImageRequester; // Se necessário, adicione uma imagem do solicitante
        public TextView tvUserNameRequester;
        public TextView tvFeedbackRequest;
        public EditText etFeedbackResponse;
        public Button btnSendFeedbackResponse;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicialização dos elementos do layout
            tvUserNameRequester = itemView.findViewById(R.id.tv_user_name_requester);
            tvFeedbackRequest = itemView.findViewById(R.id.tv_feedback_request);
            etFeedbackResponse = itemView.findViewById(R.id.et_feedback_response);
            btnSendFeedbackResponse = itemView.findViewById(R.id.btn_send_feedback_response);
        }
    }

    public interface OnFeedbackResponseListener {
        void onResponse(String feedbackRequestId, String response);
    }
}
