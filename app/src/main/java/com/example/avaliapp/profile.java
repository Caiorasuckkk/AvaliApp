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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class profile extends AppCompatActivity {

    private ImageView imageView;
    private Button uploadButton;
    private DatabaseReference root;
    private StorageReference reference;
    private Uri imageUri;
    private String currentImageUrl; // URL da imagem atual
    private String currentImageKey; // Chave da imagem atual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uploadButton = findViewById(R.id.buttonTeste);
        imageView = findViewById(R.id.icon_profile);

        // Inicializa as referências do Firebase
        root = FirebaseDatabase.getInstance().getReference("users");
        reference = FirebaseStorage.getInstance().getReference();

        // Carrega a imagem de perfil atual, se existir
        carregarImagemPerfil();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentImageUrl != null) {
                    mostrarDialogoAlteracaoImagem();
                } else {
                    // Abrir a galeria para selecionar a imagem
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 2);
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar se uma imagem foi selecionada
                if (imageUri != null) {
                    // Fazer upload da imagem para o Firebase
                    uploadToFirebase(imageUri);
                } else {
                    Toast.makeText(profile.this, "Por favor, selecione uma imagem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificar se a imagem foi selecionada com sucesso
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri); // Mostrar a imagem selecionada na ImageView
        }
    }

    private void uploadToFirebase(Uri uri) {
        Log.d("profile", "Iniciando o upload...");

        final StorageReference fileRef = reference.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(uri));

        // Se já existir uma imagem, remover a antiga
        if (currentImageUrl != null) {
            // Obter a referência da imagem antiga
            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl);

            // Deletar a imagem antiga
            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("profile", "Imagem antiga deletada com sucesso.");
                    realizarUpload(fileRef, uri); // Prosseguir com o upload da nova imagem
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("profile", "Falha ao deletar imagem antiga: " + e.getMessage());
                    // Prossegue com o upload mesmo se a exclusão falhar
                    realizarUpload(fileRef, uri);
                }
            });
        } else {
            // Não existe imagem, então apenas faz o upload
            realizarUpload(fileRef, uri);
        }
    }

    private void realizarUpload(StorageReference fileRef, Uri uri) {
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("profile", "Upload realizado com sucesso.");
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // Obter o ID do usuário logado
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Criar um objeto com a URL da nova imagem
                        Model model = new Model(downloadUri.toString());
                        DatabaseReference userImagesRef = root.child(userId).child("imagens").child(currentImageKey); // Atualiza a chave da imagem

                        // Atualiza a URL da nova imagem no nó do usuário
                        userImagesRef.setValue(model).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(profile.this, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show();
                                currentImageUrl = downloadUri.toString(); // Atualiza a URL da imagem atual
                                carregarImagemPerfil(); // Atualiza a imagem de perfil
                            } else {
                                Toast.makeText(profile.this, "Falha ao salvar a imagem: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("profile", "Falha no upload: " + e.getMessage());
                Toast.makeText(profile.this, "Falha no upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        // Obter a extensão do arquivo selecionado
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void carregarImagemPerfil() {
        // Aqui você deve implementar a lógica para carregar a imagem atual do perfil
        // Por exemplo, você pode buscar a URL da imagem no Firebase e carregá-la na ImageView
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        root.child(userId).child("imagens").limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        currentImageUrl = imageUrl; // Armazena a URL da imagem atual
                        currentImageKey = snapshot.getKey(); // Armazena a chave da imagem atual
                        // Carregar a imagem na ImageView (use uma biblioteca como Glide ou Picasso)
                        Glide.with(this).load(imageUrl).into(imageView); // Se usar Glide
                    }
                }
            }
        });
    }

    private void mostrarDialogoAlteracaoImagem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deseja alterar sua foto de perfil?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Abrir a galeria para selecionar a nova imagem
                        Intent galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, 2);
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
