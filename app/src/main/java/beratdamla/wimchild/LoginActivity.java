package beratdamla.wimchild;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoDatabase;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout email;
    TextInputLayout password;
    Button register;
    Button login;
    Button signOutButton;
    Button disconnectButton;
    SignInButton signInButton;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    StitchAppClient client;
    RemoteMongoClient mongoClient;
    RemoteMongoDatabase wimchildb;
    RemoteMongoCollection<Document> users;
    RemoteMongoCollection<Document> locations;
    User user;

    String email_login;
    String femail_login;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        client = Stitch.getDefaultAppClient();
        SharedPreferences sharedPref = getSharedPreferences(HomepageActivity.PREFS, Context.MODE_PRIVATE);
        email_login = sharedPref.getString(HomepageActivity.EMAIL,"NONE");
        femail_login = sharedPref.getString(HomepageActivity.FEMAIL,"NONE");
        if(!email_login.equals("NONE")&& !femail_login.equals("NONE")){
            onNormalLoggedIn();
        }
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        email = findViewById(R.id.email_login);
        password = findViewById(R.id.password_login);
        signInButton = findViewById(R.id.btngsignin_login);
        login = findViewById(R.id.btnsignin_login);
        mStatusTextView = findViewById(R.id.copyright_login);
        register = findViewById(R.id.btnregister_login);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerClick();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClick();
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void loginClick() {
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<StitchUser> task) throws Exception {
                if(!task.isSuccessful()){
                    Log.e("STITCH","Login Failed");
                    throw task.getException();
                }
                else{
                    mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                    users = mongoClient.getDatabase("wimchildb").getCollection("users");
                    List<Document> result = new ArrayList<>();
                    final Document doc = new Document();
                    doc.append("email", email.getEditText().getText().toString());
                    doc.append("password", password.getEditText().getText().toString());
                    return users.find(doc).into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.isSuccessful() && task.getResult().size()>0){
                    String name = task.getResult().get(0).getString("name");
                    String surname = task.getResult().get(0).getString("surname");
                    String email = task.getResult().get(0).getString("email");
                    String cellphone = task.getResult().get(0).getString("cellphone");
                    String femail = task.getResult().get(0).getString("femail");
                    boolean is_member = task.getResult().get(0).getBoolean("is_member");
                    setAccount(new User(name,surname,email,femail,cellphone,is_member,null));
                }
                else{
                    Toast.makeText(getApplicationContext(),"Eposta veya Sifre Hatali",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }
    public static void signOut(){

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 101:
                    try {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        account = task.getResult(ApiException.class);
                        onLoggedIn();
                    } catch (ApiException e) {
                        // The ApiException status code indicates the detailed failure reason.
                        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                    }
                    break;
            }

    }
    public void registerClick(){
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @Override
    protected void onStart(){
        super.onStart();
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            onLoggedIn();
        }
    }

    private void onNormalLoggedIn(){
        mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
        users = mongoClient.getDatabase("wimchildb").getCollection("users");
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<StitchUser> task) throws Exception {
                if(!task.isSuccessful()){
                    Log.e("STITCH","Login Failed");
                    throw task.getException();
                }
                else{
                    List<Document> result = new ArrayList<>();
                    final Document doc = new Document();
                    doc.append("email", email_login);
                    doc.append("femail", femail_login);
                    return users.find(doc).into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.isSuccessful() && task.getResult().size()>0){
                    String name = task.getResult().get(0).getString("name");
                    String surname = task.getResult().get(0).getString("surname");
                    String email = task.getResult().get(0).getString("email");
                    String cellphone = task.getResult().get(0).getString("cellphone");
                    String femail = task.getResult().get(0).getString("femail");
                    boolean is_member = task.getResult().get(0).getBoolean("is_member");
                    setAccount(new User(name,surname,email,femail,cellphone,is_member,null));
                }
            }
        });
    }
    private void onLoggedIn() {
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<StitchUser> task) throws Exception {
                if(!task.isSuccessful()){
                    Log.e("STITCH","Login Failed");
                    throw task.getException();
                }
                else{
                    mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                    users = mongoClient.getDatabase("wimchildb").getCollection("users");
                    List<Document> result = new ArrayList<>();
                    final Document doc = new Document();
                    doc.append("email", account.getEmail());
                    return users.find(doc).into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.isSuccessful() && task.getResult().size()>0){
                    String name = task.getResult().get(0).getString("name");
                    String surname = task.getResult().get(0).getString("surname");
                    String email = task.getResult().get(0).getString("email");
                    String cellphone = task.getResult().get(0).getString("cellphone");
                    String femail = task.getResult().get(0).getString("femail");
                    boolean is_member = task.getResult().get(0).getBoolean("is_member");
                    setAccount(new User(name,surname,email,femail,cellphone,is_member,account.getPhotoUrl()));
                }
                else{
                    Toast.makeText(getApplicationContext(),"Eposta veya Sifre Hatali",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void setAccount(User a){
        user = new User(a.getName(),a.getSurname(),a.getEmail(),a.getFamilyEmail(),a.getCellphone(),a.getMember(),a.getImageUri());
        Intent intent = new Intent(this, HomepageActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra(HomepageActivity.USER_ACCOUNT,user);
        startActivity(intent);
        finish();
    }
}
