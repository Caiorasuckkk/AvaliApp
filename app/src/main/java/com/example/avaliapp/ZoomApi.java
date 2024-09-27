package com.example.avaliapp;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ZoomApi {

    private final String clientId = "uDbYu1GQTHmJafOzZIPzbg"; // Substitua pelo seu Client ID
    private final String clientSecret = "OADC7QlzlBhZGQj5GvUr77L22J9X4R3h"; // Substitua pelo seu Client Secret
    private final String accountId = "XB3sFhk8Sy-O56COni0ABA"; // Substitua pelo seu Account ID
    private final OkHttpClient client = new OkHttpClient();

    public interface AccessTokenCallback {
        void onSuccess(String accessToken);
        void onFailure(String error);
    }

    public void getAccessToken(AccessTokenCallback callback) {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);

        // Definir o corpo da requisição conforme solicitado
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "account_credentials")
                .add("account_id", accountId)
                .build();

        // Montar a requisição
        Request request = new Request.Builder()
                .url("https://zoom.us/oauth/token")
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        // Fazer a requisição
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

    public void createMeeting(String accessToken, String tema, String dataHora) {
        String url = "https://api.zoom.us/v2/users/me/meetings";

        JSONObject meetingDetails = new JSONObject();
        try {
            meetingDetails.put("topic", tema);
            meetingDetails.put("type", 2); // 2 = agendada
            meetingDetails.put("start_time", dataHora); // Data e hora no formato ISO 8601
            meetingDetails.put("duration", 30); // Duração em minutos
            meetingDetails.put("timezone", "UTC");
            meetingDetails.put("settings", new JSONObject().put("host_video", true).put("participant_video", true));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(meetingDetails.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Erro na criação da reunião: " + e.getMessage());
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("Reunião criada com sucesso: " + responseBody);

                    // Extrair a URL de acesso da reunião
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String joinUrl = jsonResponse.getString("join_url");  // URL para entrar na reunião
                        String meetingId = jsonResponse.getString("id");      // ID da reunião
                        System.out.println("Join URL: " + joinUrl);
                        System.out.println("Meeting ID: " + meetingId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Erro ao criar reunião: " + response.message());
                }
            }

        });
    }
}
