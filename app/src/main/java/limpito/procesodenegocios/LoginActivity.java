package limpito.procesodenegocios;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            final String userProp = PropertiesUtil.getProperty("user",getApplicationContext());
            final String passProp = PropertiesUtil.getProperty("pass",getApplicationContext());

            final Button button = findViewById(R.id.id_login_button);
            final EditText user_et = findViewById(R.id.id_user);
            final EditText pass_et = findViewById(R.id.id_password);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    String user = user_et.getText().toString();
                    String password = pass_et.getText().toString();

                    if(userProp.equals(user) && passProp.equals(password)){
                        startActivity(new Intent(LoginActivity.this, Approve_cc.class));
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("Incorrect user or password.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    user_et.setText("");
                    pass_et.setText("");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
