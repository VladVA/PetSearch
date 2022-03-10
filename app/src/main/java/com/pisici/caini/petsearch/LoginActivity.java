package com.pisici.caini.petsearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEt;
    EditText mPasswordEt;
    Button mLoginBtn;
    Button mSignupBtn;
    private FirebaseAuth mAuth;
    ImageView gif;
    ImageView logo;
    Button mresend_verificationBtn;
    Button mreset_passwordBtn;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //mAuth = FirebaseAuth.getInstance();
        mEmailEt = (EditText) findViewById(R.id.login_usernameEt);
        mPasswordEt = (EditText) findViewById(R.id.login_passwordEt);
        mLoginBtn = (Button) findViewById(R.id.main_profileBtn);
        mSignupBtn = (Button) findViewById(R.id.login_signupBtn);
        mresend_verificationBtn = (Button) findViewById(R.id.resend_verificationBtn);
        mreset_passwordBtn = (Button) findViewById(R.id.reset_passwordBtn);
        mAuth = FirebaseAuth.getInstance();
        mresend_verificationBtn.setEnabled(false);

        //Verifica daca esti conectat la internet
        if (!verifyInternetConnectivty())
            makeToast("Please connect to the internet");
        //daca esti deja logat te duce direct in menu activity
        if (mAuth.getCurrentUser() != null) {
            retrieve_user();
        }
        //daca nu esti logat
        else {


            //OnClick care duce la activitatea de signup
            mSignupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                    startActivityForResult(i, 1);
                    //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                }
            });

            //OnClick pentru logare
            mLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });

        }
        mreset_passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetpassword();
            }
        });


    }
    //functie care verifica daca esti conectat la net
    protected boolean verifyInternetConnectivty() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    void makeToast(String string){
        Toast.makeText(LoginActivity.this, string,Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //asta e pentru cand vine din signup, completeaza automat campurile
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            mPasswordEt.setText(data.getStringExtra("pass"));
            mEmailEt.setText(data.getStringExtra("email"));
        }
    }
    //practic incearca logarea
    void login() {
        if (!mEmailEt.getText().toString().isEmpty() && !mPasswordEt.getText().toString().isEmpty()) {
            //daca stringurile sunt goale da crash metoda de la firebase
            mAuth.signInWithEmailAndPassword(mEmailEt.getText().toString().trim(),
                    mPasswordEt.getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // A mers
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user.isEmailVerified()) {
                                    retrieve_user();
                                } else {
                                    makeToast("Please verify your email adress before you sign in");
                                    makeresendvisible();
                                }

                            } else {
                                // Nu a mers
                                makeToast("Invalid credentials");
                            }
                        }
                    });
        } else {
            makeToast("Please enter your credentials");
        }
    }
    //pune datele in obiectul user
    public void retrieve_user() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = database;
        user = new User();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v("log", "onDataChange din retrieve_user din loginActivity");
                user = dataSnapshot.child("id").child(uid).getValue(User.class);
                //daca nu, te duce in meniu
                Log.v("log", "trimis in meniu din onDataChange");
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("User", user);
                startActivity(i);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, databaseError.toException().toString(), Toast.LENGTH_LONG).show();
            }

        });

    }
    void resetpassword() {
        String email = mEmailEt.getText().toString().trim();
        if (!isEmailValid(email))
            makeToast("Please enter your email");
        else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                makeToast("Password reset email sent");
                            }
                        }
                    });
        }
    }
    void makeresendvisible() {
        mresend_verificationBtn.setEnabled(true);
        mresend_verificationBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        final FirebaseUser fuser = mAuth.getCurrentUser();
        mresend_verificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fuser.sendEmailVerification()
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    makeToast("Verification email sent to " + fuser.getEmail());
                                } else {
                                    makeToast("Failed to send verification email.");
                                }
                            }
                        });
            }
        });

    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
