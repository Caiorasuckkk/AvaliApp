package com.example.avaliapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class reuniao extends AppCompatActivity {

    private static final String TAG = "ZoomAPI";
    private EditText editTextTema;
    private EditText editTextDataHora;
    private Button buttonCriar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reuniao);

        editTextTema = findViewById(R.id.editTextTema);
        editTextDataHora = findViewById(R.id.editTextDataHora);
        buttonCriar = findViewById(R.id.buttonCriar);

        buttonCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                criarReuniao();
            }
        });
    }

    private void criarReuniao() {
        String tema = editTextTema.getText().toString().trim();
        String dataHora = editTextDataHora.getText().toString().trim();

        if (TextUtils.isEmpty(tema) || TextUtils.isEmpty(dataHora)) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar o formato da dataHora
        if (!isValidIso8601DateTime(dataHora)) {
            Toast.makeText(this, "Data e hora devem estar no formato ISO 8601 (yyyy-MM-dd'T'HH:mm:ss'Z')", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalTema = tema;  // Fazer o tema final
        final String finalDataHora = dataHora;  // Fazer a dataHora final

        ZoomApi zoomApi = new ZoomApi();
        zoomApi.getAccessToken(new ZoomApi.AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                Log.d(TAG, "Access Token: " + accessToken);
                zoomApi.createMeeting(accessToken, finalTema, finalDataHora);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private boolean isValidIso8601DateTime(String dateTime) {
        try {
            OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
