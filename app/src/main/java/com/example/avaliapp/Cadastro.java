package com.example.avaliapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Cadastro extends AppCompatActivity {

    private EditText fullName, email, password;
    private Spinner spinnerCargo, spinnerNivel, spinnerArea;
    private Button buttonRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicializando Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Inicializando os componentes
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        spinnerArea = findViewById(R.id.spinnerArea);
        spinnerCargo = findViewById(R.id.spinnerCargo);
        spinnerNivel = findViewById(R.id.spinnerNivel);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Preencher Spinner de Cargo
        ArrayAdapter<String> cargoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Manufacturing and Supply Engineering", "Financas Projetos", "Maintenance Manager", "Maintenance Planning and Control", "Projects", "Systems and Maintenance Control - SCM"});
        cargoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCargo.setAdapter(cargoAdapter);

        // Configurar áreas baseado no cargo selecionado
        spinnerCargo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cargoSelecionado = parent.getItemAtPosition(position).toString();
                updateAreaSpinner(cargoSelecionado); // Atualizar as áreas com base no cargo
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Ação do botão Registrar
        buttonRegister.setOnClickListener(v -> {
            String nome = fullName.getText().toString();
            String emailInput = email.getText().toString();
            String senha = password.getText().toString();
            String cargo = spinnerCargo.getSelectedItem().toString();
            String area = spinnerArea.getSelectedItem().toString(); // Adicionado o campo de área

            // Verifica se o nível está visível antes de capturá-lo
            String nivel = null;
            if (spinnerNivel.getVisibility() == View.VISIBLE) {
                nivel = spinnerNivel.getSelectedItem().toString();
            }

            if (nome.isEmpty() || emailInput.isEmpty() || senha.isEmpty() || cargo.isEmpty() || area.isEmpty()) {
                Toast.makeText(Cadastro.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validação de e-mail
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                Toast.makeText(Cadastro.this, "Email inválido!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validação de senha
            if (senha.length() < 6) {
                Toast.makeText(Cadastro.this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Passando o nível como argumento também
            registerUser(nome, emailInput, senha, cargo, area, nivel);
        });

    }

    private void updateAreaSpinner(String cargoSelecionado) {
        String[] areas;

        // Definir as áreas baseadas no cargo selecionado
        if (cargoSelecionado.equals("Manufacturing and Supply Engineering")) {
            areas = new String[]{"Site Engineering Head", "Project Specialist", "Maintenance Lead", "Engineer", "Project Coordinator", "Engineer I", "Analista de Engenharia Jr", "Jovem Aprendiz"};
        } else if (cargoSelecionado.equals("Financas Projetos")) {
            areas = new String[]{"Project Specialist", "Intern", "Technician II"};
        } else if (cargoSelecionado.equals("Maintenance Manager")) {
            areas = new String[]{"Maintenance Lead", "Maintenance Coordinator", "Coordenador de Utilidades", "Engenheiro Especialista", "Analista Financeiro Jr"};
        } else if (cargoSelecionado.equals("Maintenance Planning and Control")) {
            areas = new String[]{"I Engineer"};
        } else if (cargoSelecionado.equals("Projects")) {
            areas = new String[]{"Project Coordinator","Engineer I", "Intern", "Specialized Engineer", "Designer", "Engineer II "};
        } else if (cargoSelecionado.equals("Systems and Maintenance Control - SCM")) {
            areas = new String[]{"Maintenance Technician", "Intern", "Técnico de manutenção"};
        } else {
            areas = new String[]{};
        }

        // Adicionar as áreas no spinnerArea
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, areas);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(areaAdapter);

        // Definir o comportamento de atualização de níveis com base na área selecionada
        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String areaSelecionada = parent.getItemAtPosition(position).toString();
                updateNivelSpinner(areaSelecionada); // Atualizar os níveis com base na área
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateNivelSpinner(String areaSelecionada) {
        String[] niveis;

        // Verificar se a área selecionada é uma das duas definidas
        if (areaSelecionada.equals("Maintenance Coordinator")) {
            niveis = new String[]{"Electric Technician II", "Mechanical Technician", "Electric Technician I", "Electric Technician II", "Intern"};
            spinnerNivel.setVisibility(View.VISIBLE); // Tornar o spinner visível
        } else if (areaSelecionada.equals("Coordenador de Utilidades")) {
            niveis = new String[]{"Intern", "Utilities Technician", "Engineer II"};
            spinnerNivel.setVisibility(View.VISIBLE); // Tornar o spinner visível
        } else {
            spinnerNivel.setVisibility(View.INVISIBLE); // Tornar o spinner invisível se não for uma das opções
            return; // Encerrar o método, pois não há níveis a configurar
        }

        // Se uma das duas opções foi selecionada, configurar o spinner com os níveis
        ArrayAdapter<String> nivelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, niveis);
        nivelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNivel.setAdapter(nivelAdapter);
    }

    private void registerUser(String fullName, String email, String senha, String cargo, String area, String nivel) {
        // Define "nivel" como final para evitar alterações após a sua atribuição
        final String nivelFinal;

        // Se o spinner for visível, captura o nível selecionado
        if (spinnerNivel.getVisibility() == View.VISIBLE) {
            nivelFinal = spinnerNivel.getSelectedItem().toString();
        } else {
            nivelFinal = null; // Caso contrário, defina como null
        }

        mAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Passa o "nivelFinal" (null se não for visível)
                    User newUser = new User(fullName, email, cargo, area, nivelFinal);
                    mDatabase.child(user.getUid()).setValue(newUser).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(Cadastro.this, "Registro concluído!", Toast.LENGTH_SHORT).show();
                            // Redirecionar para a tela Gestor
                            startActivity(new Intent(Cadastro.this, gestor.class));
                            finish(); // Finalizar a tela de cadastro
                        } else {
                            Toast.makeText(Cadastro.this, "Erro ao salvar os dados", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(Cadastro.this, "Falha ao registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Classe modelo para usuários
    public static class User {
        public String fullName;
        public String email;
        public String cargo;
        public String area;
        public String nivel; // Pode ser null

        public User() {
            // Necessário para o Firebase
        }

        public User(String fullName, String email, String cargo, String area, String nivel) {
            this.fullName = fullName;
            this.email = email;
            this.cargo = cargo;
            this.area = area;
            this.nivel = nivel; // Pode ser null
        }
    }
}
