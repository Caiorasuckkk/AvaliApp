package com.example.avaliapp;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ZoomApi {

    private final String clientId = "uDbYu1GQTHmJafOzZIPzbg"; // Substitua pelo seu Client ID
    private final String clientSecret = "OADC7QlzlBhZGQj5GvUr77L22J9X4R3h"; // Substitua pelo seu Client Secret
    private final String accountId = "XB3sFhk8Sy-O56COni0ABA"; // Substitua pelo seu Account ID
    private final OkHttpClient client = new OkHttpClient();

    // Interface para manipular a recuperação do token
    public interface AccessTokenCallback {
        void onSuccess(String accessToken);
        void onFailure(String error);
    }

    // Método para obter o token de acesso da API do Zoom
    public void getAccessToken(AccessTokenCallback callback) {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);

        // Corpo da requisição com grant_type e account_id
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "account_credentials")
                .add("account_id", accountId)
                .build();

        // Criação da requisição com autenticação básica
        Request request = new Request.Builder()
                .url("https://zoom.us/oauth/token")
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        // Executa a requisição
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        String accessToken = jsonObject.getString("access_token");
                        callback.onSuccess(accessToken);
                    } catch (JSONException e) {
                        callback.onFailure("Erro ao processar a resposta JSON");
                    }
                } else {
                    callback.onFailure("Erro ao obter o token: " + response.message());
                }
            }
        });
    }

    // Método para criar uma reunião no Zoom
    public String createMeeting(String accessToken, String tema, String dataHora, List<UserReuni> selectedUsers) {
        String url = "https://api.zoom.us/v2/users/me/meetings";
        String joinUrl = null; // Inicializa a variável para armazenar a URL de acesso

        // Detalhes da reunião em formato JSON
        JSONObject meetingDetails = new JSONObject();
        try {
            meetingDetails.put("topic", tema);
            meetingDetails.put("type", 2); // 2 = reunião agendada
            meetingDetails.put("start_time", dataHora); // Formato ISO 8601
            meetingDetails.put("duration", 30); // Duração em minutos
            meetingDetails.put("timezone", "UTC");
            meetingDetails.put("settings", new JSONObject()
                    .put("host_video", true)
                    .put("participant_video", true));

            // Enviar convites para usuários selecionados
            for (UserReuni user : selectedUsers) {
                if (user.isSelected()) {
                    System.out.println("Convite enviado para: " + user.getEmail());
                    // Aqui você pode adicionar o código para enviar um e-mail se necessário
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Criação da requisição com corpo JSON
        RequestBody body = RequestBody.create(meetingDetails.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        // Executa a requisição para criar a reunião
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("Reunião criada com sucesso: " + responseBody);

                // Extrair URL da reunião
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    joinUrl = jsonResponse.getString("join_url");
                    System.out.println("URL de acesso: " + joinUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Erro ao criar a reunião: " + response.message());
            }
        } catch (IOException e) {
            System.err.println("Erro ao executar a requisição: " + e.getMessage());
        }

        // Retorna a URL da reunião ou null se não foi criada
        return joinUrl;
    }
}
