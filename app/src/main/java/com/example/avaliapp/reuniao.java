package com.example.avaliapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class reuniao extends AppCompatActivity {

    private static final String TAG = "ReuniaoActivity";

    private EditText editTextTema, editTextData, editTextHora;
    private Button buttonCriar;
    private RecyclerView recyclerViewUsers;
    private List<UserReuni> userList = new ArrayList<>();
    private UserAdapterReuni userAdapterReuni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reuniao);

        editTextTema = findViewById(R.id.editTextTema);
        editTextData = findViewById(R.id.editTextData);
        editTextHora = findViewById(R.id.editTextHora);
        buttonCriar = findViewById(R.id.buttonCriar);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        // Configura o RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        // Carregar usuários do Firebase Realtime Database
        loadUsersFromFirebase();

        // Eventos de clique para abrir seletores de data e hora
        editTextData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        editTextHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        buttonCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                criarReuniao();
            }
        });
    }

    private void loadUsersFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String area = snapshot.child("area").getValue(String.class);

                    UserReuni user = new UserReuni(fullName, email, area);
                    userList.add(user);
                }

                // Atualizar o adapter com a lista de usuários
                userAdapterReuni = new UserAdapterReuni(userList);
                recyclerViewUsers.setAdapter(userAdapterReuni);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Erro ao carregar usuários: " + databaseError.getMessage());
            }
        });
    }

    private void criarReuniao() {
        String tema = editTextTema.getText().toString().trim();
        String data = editTextData.getText().toString().trim();
        String hora = editTextHora.getText().toString().trim();

        if (TextUtils.isEmpty(tema) || TextUtils.isEmpty(data) || TextUtils.isEmpty(hora)) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataHora = data + "T" + hora + ":00Z"; // Formatar para ISO 8601

        // Validar o formato da dataHora
        if (!isValidIso8601DateTime(dataHora)) {
            Toast.makeText(this, "Data e hora devem estar no formato ISO 8601 (yyyy-MM-dd'T'HH:mm:ss'Z')", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalTema = tema;
        final String finalDataHora = dataHora;

        ZoomApi zoomApi = new ZoomApi();
        zoomApi.getAccessToken(new ZoomApi.AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                Log.d(TAG, "Access Token: " + accessToken);
                List<UserReuni> selectedUsers = userAdapterReuni.getSelectedUsers();
                String meetingLink = zoomApi.createMeeting(accessToken, finalTema, finalDataHora, selectedUsers); // Certifique-se de capturar o link da reunião

                // Enviar email aos usuários
                sendEmailToParticipants(meetingLink, selectedUsers); // Passar o link da reunião
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    // Método para enviar email para os usuários selecionados
    private void sendEmailToParticipants(String meetingLink, List<UserReuni> users) {
        EmailSender emailSender = new EmailSender(this); // Passar o contexto
        for (UserReuni user : users) {
            String recipientEmail = user.getEmail(); // O email vem da lista de usuários carregados do Firebase

            // Enviar o e-mail
            emailSender.sendEmail(recipientEmail, "Código de Entrada da Reunião",
                    "Você foi convidado para a reunião. Acesse o link: " + meetingLink);
        }
    }


    private void showDatePicker() {
        // Obter a data atual
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Abrir o DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Corrigir o mês (começa de 0, por isso adiciona +1)
                String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                editTextData.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        // Obter a hora atual
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Abrir o TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Formatar a hora escolhida para HH:mm
                String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                editTextHora.setText(selectedTime);
            }
        }, hour, minute, true); // O último parâmetro define se o formato é 24h ou não

        timePickerDialog.show();
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
