package com.example.avaliapp;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class profile extends AppCompatActivity {

    private ImageView imageView;
    private Button uploadButton,btnequipe,btnhist2;
    private DatabaseReference root;
    private StorageReference reference;
    private Uri imageUri;
    private String currentImageUrl;
    private String currentImageKey;

    private TextView userNameTextView;  // Nome do usuário
    private TextView userCargoTextView; // Cargo do usuário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        btnhist2=findViewById(R.id.btn_hist2);
        btnequipe=findViewById(R.id.btn_equipe);
        uploadButton = findViewById(R.id.buttonTeste);
        imageView = findViewById(R.id.icon_profile);
        userNameTextView = findViewById(R.id.user_name);  // Inicializa o TextView de nome
        userCargoTextView = findViewById(R.id.user_cargo); // Inicializa o TextView de cargo

        // Inicializa Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        root = FirebaseDatabase.getInstance().getReference("users");
        reference = FirebaseStorage.getInstance().getReference();

        // Carrega a imagem de perfil atual
        carregarImagemPerfil();

        // Carrega o nome e o cargo do usuário
        carregarDadosUsuario(userId);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentImageUrl != null) {
                    mostrarDialogoAlteracaoImagem();
                } else {
                    abrirGaleria();
                }
            }
        });
        btnequipe.setOnClickListener(view -> GerenciarEquipe());
        btnhist2.setOnClickListener(view -> Feedback());

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadToFirebase(imageUri);
                } else {
                    Toast.makeText(profile.this, "Por favor, selecione uma imagem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Feedback() {
        Intent Feed = new Intent(profile.this, historicofeedback.class); // Certifique-se de usar o nome correto da sua Activity de formulário
        startActivity(Feed);
    }

    private void GerenciarEquipe() {
        Intent equipe = new Intent(profile.this, gerenciamento_grupo.class); // Certifique-se de usar o nome correto da sua Activity de formulário
        startActivity(equipe);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadToFirebase(Uri uri) {
        Log.d("Profile", "Iniciando o upload...");

        final StorageReference fileRef = reference.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(uri));

        if (currentImageUrl != null) {
            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl);
            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Profile", "Imagem antiga deletada.");
                    realizarUpload(fileRef, uri);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Profile", "Erro ao deletar imagem: " + e.getMessage());
                    realizarUpload(fileRef, uri);
                }
            });
        } else {
            realizarUpload(fileRef, uri);
        }
    }

    private void realizarUpload(StorageReference fileRef, Uri uri) {
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Model model = new Model(downloadUri.toString());
                        DatabaseReference userImagesRef = root.child(userId).child("imagens").child(currentImageKey);

                        userImagesRef.setValue(model).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(profile.this, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show();
                                currentImageUrl = downloadUri.toString();
                                carregarImagemPerfil();
                            } else {
                                Toast.makeText(profile.this, "Erro ao salvar imagem: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Log.e("Profile", "Erro no upload: " + e.getMessage());
            Toast.makeText(profile.this, "Erro no upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void carregarImagemPerfil() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        root.child(userId).child("imagens").limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    currentImageUrl = imageUrl;
                    currentImageKey = snapshot.getKey();
                    Glide.with(this).load(imageUrl).into(imageView);
                }
            }
        });
    }

    private void mostrarDialogoAlteracaoImagem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deseja alterar sua foto de perfil?")
                .setPositiveButton("Sim", (dialog, id) -> abrirGaleria())
                .setNegativeButton("Não", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void abrirGaleria() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 2);
    }

    private void carregarDadosUsuario(String userId) {
        root.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Carregar nome completo do usuário
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    userNameTextView.setText(fullName);

                    // Carregar a área do usuário
                    String area = dataSnapshot.child("area").getValue(String.class);

                    // Verifica se o usuário tem o campo 'nível'
                    if (dataSnapshot.hasChild("nivel")) {
                        String nivel = dataSnapshot.child("nivel").getValue(String.class);
                        userCargoTextView.setText(nivel); // Exibe apenas o nível
                    } else {
                        userCargoTextView.setText(area); // Se não houver nível, exibe a área
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Profile", "Erro ao carregar dados: " + databaseError.getMessage());
            }
        });
    }


}
