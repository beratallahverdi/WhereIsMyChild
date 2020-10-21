package beratdamla.wimchild;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;

public class InfoActivity extends AppCompatActivity {
    FloatingActionButton save;
    TextInputLayout name;
    TextInputLayout surname;
    TextInputLayout cellphone;

    String nameText;
    String surnameText;
    String cellphoneText;
    User user;

    Intent intent;
    StitchAppClient client;
    RemoteMongoClient mongoClient;
    RemoteMongoCollection users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        intent = getIntent();
        user = intent.getParcelableExtra(HomepageActivity.USER_ACCOUNT);

        client = Stitch.getDefaultAppClient();
        setUpViews();
    }

    private void setUpViews() {
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        save = findViewById(R.id.save_info);
        name = findViewById(R.id.name_info);
        surname = findViewById(R.id.surname_info);
        cellphone = findViewById(R.id.cellphone_info);
        name.getEditText().setText(user.getName());
        surname.getEditText().setText(user.getSurname());
        cellphone.getEditText().setText(user.getCellphone());
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClick();
            }
        });
    }

    private void saveClick() {
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<RemoteUpdateResult>>() {
                @Override
                public Task<RemoteUpdateResult> then(@NonNull Task<StitchUser> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Log.e("STITCH", "Login failed!");
                        throw task.getException();
                    }
                    else{
                        mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                        users = mongoClient.getDatabase("wimchildb").getCollection("users");
                        Bson filter = new Document("email", user.getEmail());
                        Bson newValue = new Document("name", name.getEditText().getText().toString());
                        ((Document) newValue).put("surname",surname.getEditText().getText().toString());
                        ((Document) newValue).put("cellphone", cellphone.getEditText().getText().toString());
                        Bson updateOperationDocument = new Document("$set", newValue);
                        return users.updateOne(filter, updateOperationDocument);
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
                @Override
                public void onComplete(@NonNull Task<RemoteUpdateResult> task) {
                    Toast.makeText(getApplicationContext(),"Guncellendi.",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                    Bundle bundle = new Bundle();
                    intent.putExtra(HomepageActivity.USER_ACCOUNT,user);
                    startActivity(intent);
                    finish();
                }
            });
    }
}
