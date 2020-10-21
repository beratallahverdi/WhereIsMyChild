package beratdamla.wimchild;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private String email;
    private String name;
    private String surname;
    private String cellphone;
    private String femail;
    private String password;
    private String password_again;
    private boolean is_member = false;

    Button register;
    StitchAppClient client;
    RemoteMongoClient mongoClient;
    RemoteMongoCollection<Document> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        client = Stitch.getDefaultAppClient();
        register = findViewById(R.id.btnsignin_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = ((TextInputLayout) findViewById(R.id.email_register)).getEditText().getText().toString();
                name = ((TextInputLayout) findViewById(R.id.name_register)).getEditText().getText().toString();
                surname = ((TextInputLayout) findViewById(R.id.surname_register)).getEditText().getText().toString();
                cellphone = ((TextInputLayout) findViewById(R.id.phone_register)).getEditText().getText().toString();
                femail = ((TextInputLayout) findViewById(R.id.familypassword_register)).getEditText().getText().toString();
                password = ((TextInputLayout) findViewById(R.id.password_register)).getEditText().getText().toString();
                password_again = ((TextInputLayout) findViewById(R.id.passwordAgain_register)).getEditText().getText().toString();
                if (password.equals(password_again) && !email.equalsIgnoreCase("") && password.length() > 7) {
                    if (femail.equalsIgnoreCase("")) {
                        femail = email;
                        is_member=true;
                    }
                    registerClick();
                } else {
                    Toast.makeText(getApplicationContext(), "Şifreler Uyuşmuyor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerClick() {
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(
                new Continuation<StitchUser, Task<RemoteInsertOneResult>>() {

                    @Override
                    public Task<RemoteInsertOneResult> then(@NonNull Task<StitchUser> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.e("STITCH", "Login failed!");
                            throw task.getException();
                        }
                        mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                        users = mongoClient.getDatabase("wimchildb").getCollection("users");
                        Document newuser = new Document("owner_id", client.getAuth().getUser().getId());
                        newuser.put("email",email);
                        newuser.put("name",name);
                        newuser.put("surname",surname);
                        newuser.put("cellphone",cellphone);
                        newuser.put("femail",femail);
                        newuser.put("password",password);
                        newuser.put("is_member",is_member);
                        return users.insertOne(newuser);
                    }
                }
        ).continueWithTask(new Continuation<RemoteInsertOneResult, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<RemoteInsertOneResult> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e("STITCH", "Insert failed!");
                    throw task.getException();
                }
                mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                users = mongoClient.getDatabase("wimchildb").getCollection("users");
                List<Document> docs = new ArrayList<>();
                return users
                        .find(new Document("owner_id", client.getAuth().getUser().getId()).append("email",email))
                        .limit(100)
                        .into(docs);
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),email+" adresiyle kayit yapildi",Toast.LENGTH_SHORT).show();
                    RegisterActivity.this.finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "E-Posta Zaten Kayitli",Toast.LENGTH_SHORT).show();
                task.getException().printStackTrace();
            }
        });
    }
}