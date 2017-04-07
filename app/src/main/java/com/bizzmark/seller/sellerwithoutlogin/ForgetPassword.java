package com.bizzmark.seller.sellerwithoutlogin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bizzmark.seller.sellerwithoutlogin.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "";
    TextView FGUSERNAME;
    EditText FGEMAIL;
    Button FGSUBMIT;

    ImageButton FGBACK;

    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        FGUSERNAME=(TextView)findViewById(R.id.tvforgetusername) ;

        FGEMAIL=(EditText)findViewById(R.id.etforgetemail);

        FGSUBMIT=(Button)findViewById(R.id.btforgetsubmit);

        FGBACK=(ImageButton)findViewById(R.id.ibforgetback);

        firebaseAuth=FirebaseAuth.getInstance();

        FGSUBMIT.setOnClickListener(this);

        FGBACK.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view == FGBACK){
            //Closing Current task
            finish();
            //opening login class
            startActivity(new Intent(this, Login.class));

        }

        if(view == FGSUBMIT){
            String emailaddress = FGEMAIL.getText().toString().trim();
            //If Email field is Blank
            if(TextUtils.isEmpty(emailaddress)){

                Toast.makeText(ForgetPassword.this,"Please Enter Registered Email Address",Toast.LENGTH_LONG).show();
            }else {

                firebaseAuth.sendPasswordResetEmail(emailaddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(getApplicationContext(), "Password Reset Email Sent to your Registered Mail Successfully",
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(getApplicationContext(),Login.class));
                                }
                            }
                        });

            }

        }

    }
}
