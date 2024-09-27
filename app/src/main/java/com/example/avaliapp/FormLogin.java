package com.example.avaliapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FormLogin extends AppCompatActivity {
     private EditText edit_email,edit_senha;
     private Button button_acesso;
     private ProgressBar progress_bar;
     String[] mensagens ={"Preencha todos os campos","Login efetuado com sucesso"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_login);

        IniciarComponentes();




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        button_acesso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=edit_email.getText().toString();
                String senha=edit_senha.getText().toString();

                if(email.isEmpty() || senha.isEmpty()){
                    Snackbar snackbar =Snackbar.make(v,mensagens[0],Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();

                }else{
                    AutenticarUsuario(v);
                }
            }
        });
    }
    private void AutenticarUsuario(View view){

        String email=edit_email.getText().toString();
        String senha=edit_senha.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                  TelaPrincipal();



                }else{
                    String erro;
                    try {
                         throw task.getException();
                    }catch (Exception e){
                        erro="Erro ao logar usuário";


                        Snackbar snackbar =Snackbar.make(view,erro,Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();}
                }
            }
        });



    }
      private void TelaPrincipal(){
         Intent intent = new Intent(FormLogin.this, Menu.class);
           startActivity(intent);
           finish();



         }

       private void IniciarComponentes() {

         edit_email=findViewById(R.id.edit_email);
        edit_senha=findViewById(R.id.edit_senha);
        button_acesso=findViewById(R.id.button_acesso);
    }

}