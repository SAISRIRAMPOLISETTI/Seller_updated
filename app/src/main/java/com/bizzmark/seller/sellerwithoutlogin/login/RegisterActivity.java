package com.bizzmark.seller.sellerwithoutlogin.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bizzmark.seller.sellerwithoutlogin.R;
import com.bizzmark.seller.sellerwithoutlogin.WifiDirectReceive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    //Defining view objects
    private Button BTREGISTER;
    private EditText ETEMAIL;
    private EditText ETPASSWORD;
    private TextView TVSIGNIN;
    private ImageButton BACKBUTTON;

    private ProgressDialog progressDialog;

    //defining Firebase Auth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        //intializing Views
        BTREGISTER=(Button)findViewById(R.id.btregister);

        ETEMAIL=(EditText)findViewById(R.id.etnewusername);

        ETPASSWORD=(EditText)findViewById(R.id.etnewpassword);

        TVSIGNIN=(TextView) findViewById(R.id.tvsignin);

        BACKBUTTON=(ImageButton)findViewById(R.id.backbutton);

        //attaching listner to button
        BTREGISTER.setOnClickListener(this);

        TVSIGNIN.setOnClickListener(this);

        BACKBUTTON.setOnClickListener(this);

        //intializing Firebase auth Object
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        if(firebaseAuth.getCurrentUser() != null){
            //Wifi direct recive activity here
            finish();
            startActivity(new Intent(getApplicationContext(), WifiDirectReceive.class));

        }

    }

    private void registerUser(){
        //getting email and passwords from Edittext fields
        String email = ETEMAIL.getText().toString();
        String password = ETPASSWORD.getText().toString();

        //checking if email and password are empty

        if(TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_LONG).show();
            //Stopping the function execution further
            return;
        }
        if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_LONG).show();
            //Stopping the execution Further
            return;
        }
        //if validations(Username and password) are ok
        //we will first show progressbar
        progressDialog.setMessage("Registering User .....");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Checking if success
                        if(task.isSuccessful()){
                            //User is succesfully registered and logged in
                            //we will start the Wifi direct recive activity here
                            //right now let display a toast only
                            Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_LONG).show();
                            ETEMAIL.setText("");
                            ETPASSWORD.setText("");
                            //Finishing currrnt activity
                            finish();
                          //  startActivity(new Intent(getApplicationContext(),Login.class));
                        }else{
                            //Showing Some Toast message
                            Toast.makeText(RegisterActivity.this, "Could not Register please try again", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(view == BTREGISTER){
            registerUser();
        }

        if(view == TVSIGNIN){
            startActivity(new Intent(this, Login.class));
        }
        if(view == BACKBUTTON){
            startActivity(new Intent(this,Login.class));
        }
    }
}
