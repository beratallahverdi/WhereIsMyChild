package beratdamla.wimchild;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {
    FloatingActionButton save;
    TextInputLayout name;
    TextInputLayout surname;
    TextInputLayout cellphone;

    String nameText;
    String surnameText;
    String cellphoneText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpViews();
    }

    private void setUpViews() {
        save = findViewById(R.id.save_settings);
        name = findViewById(R.id.name_settings);
        surname = findViewById(R.id.surname_settings);
        cellphone = findViewById(R.id.cellphone_settings);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClick();
            }
        });
    }

    private void saveClick() {
        nameText = name.getEditText().getText().toString();
        surnameText = surname.getEditText().getText().toString();
        cellphoneText = cellphone.getEditText().getText().toString();
        Toast.makeText(this,nameText+" "+surnameText+" "+cellphoneText+" ",Toast.LENGTH_SHORT).show();
        this.finish();
    }

}
