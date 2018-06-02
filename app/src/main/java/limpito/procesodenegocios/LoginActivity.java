package limpito.procesodenegocios;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    static final String USER = "Root";
    static final String PASSWORD = "Root";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button button = findViewById(R.id.id_login_button);
        final EditText user_et = findViewById(R.id.id_user);
        final EditText pass_et = findViewById(R.id.id_password);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String user = user_et.getText().toString();
                String password = pass_et.getText().toString();

                startActivity(new Intent(LoginActivity.this, Approve_cc.class));

                /*if(USER.equals(user) && PASSWORD.equals(password)){
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
                    user_et.setText("");
                    pass_et.setText("");
                }*/
            }
        });
    }
}
