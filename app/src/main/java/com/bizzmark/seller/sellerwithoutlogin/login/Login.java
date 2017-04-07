package com.bizzmark.seller.sellerwithoutlogin.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizzmark.seller.sellerwithoutlogin.ForgetPassword;
import com.bizzmark.seller.sellerwithoutlogin.R;
import com.bizzmark.seller.sellerwithoutlogin.WifiDirectReceive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "";
    private EditText ETUSERNAME;
    private EditText ETPASSWORD;
    private TextView TVSIGNUP;
    private Button BTLOGIN;
    private TextView TVFORGET;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    ConnectivityManager cm;
    NetworkInfo info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ETUSERNAME=(EditText)findViewById(R.id.etusername);

        ETPASSWORD=(EditText)findViewById(R.id.etpassword);

        TVSIGNUP=(TextView) findViewById(R.id.tvsignup);

        BTLOGIN=(Button)findViewById(R.id.btlogin);

        TVFORGET=(TextView)findViewById(R.id.tvforget);

        BTLOGIN.setOnClickListener(this);

        TVSIGNUP.setOnClickListener(this);

        TVFORGET.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            //Will open Wifi direct Recive activity here
            startActivity(new Intent(this, WifiDirectReceive.class));
        }
        checkInternet();
    }

    private void checkInternet() {
        cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        info = cm.getActiveNetworkInfo();
        if (info == null){
            alertDialog();
         // setContentView(R.layout.internet_layout);
           // finish();
        }else {
            if (firebaseAuth.getCurrentUser() != null){
                startActivity(new Intent(this,WifiDirectReceive.class));
                finish();
            }
        }
    }

    private void alertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet")
                .setMessage("oops! no internet connection!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).setCancelable(false).create().show();
    }



    private void userLogin() {
        final String email = ETUSERNAME.getText().toString().trim();
        String password = ETPASSWORD.getText().toString().trim();

        //Checking if email and password are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter Username", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_LONG).show();
            return;
        }
        //if the email and password are not empty
        //displaying progress dialog
            progressDialog.setMessage("LogIn Please Wait....");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                //start Wifi direct recive activity
                                finish();
                                startActivity(new Intent(getApplicationContext(), WifiDirectReceive.class));

                            }else {
                                Toast.makeText(Login.this, "Invalid Username or Password",Toast.LENGTH_LONG ).show();
                                ETUSERNAME.setText("");
                                ETPASSWORD.setText("");
                            }
                        }
                    });
    }


    @Override
    public void onClick(View view) {
        if(view == BTLOGIN){
            //Calling userLogin()method
            if (info != null) {
                userLogin();
            }else {
                alertDialog();
            }
        }

        if(view == TVSIGNUP){
            //Closing Current task
            finish();
            //Goto RegisterActivity class
            startActivity(new Intent(this,RegisterActivity.class));

        }


        if(view == TVFORGET){
            //Closing Current Task
            finish();
            //Goto Forget password class
            startActivity(new Intent(this, ForgetPassword.class));

        }
    }

}
