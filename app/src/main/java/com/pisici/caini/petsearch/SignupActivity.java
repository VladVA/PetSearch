package com.pisici.caini.petsearch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    EditText mpassEt;
    EditText memailEt;
    EditText mpassrepeatEt;
    EditText mfirstnameEt;
    EditText mlastnameEt;
    EditText mphoneEt;
    Button msignupBtn;
    Button mphotoBtn;
    Button msigninBtn;
    CheckBox checkBox;
    Uri photouri;
    Uri file;
    FirebaseUser user;
    User muser;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        memailEt = (EditText) findViewById(R.id.signup_emailEt);
        mpassEt = (EditText) findViewById(R.id.signup_passwordEt);
        msignupBtn = (Button) findViewById(R.id.signupBtn);
        mphotoBtn = (Button) findViewById(R.id.signup_upload_photo);
        msigninBtn = (Button) findViewById(R.id.signup_signinBtn);
        mpassrepeatEt = (EditText) findViewById(R.id.signup_password_repeatEt);
        mfirstnameEt = (EditText) findViewById(R.id.signup_first_nameEt);
        mlastnameEt = (EditText) findViewById(R.id.signup_last_nameEt);
        mphoneEt = (EditText) findViewById(R.id.signup_phoneEt);
        checkBox = (CheckBox) findViewById(R.id.signup_checkbox);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        msignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isEmailValid(memailEt.getText().toString().trim()) && mpassEt.getText().toString().trim().length() >= 6 &&
                        mpassEt.getText().toString().trim().equals(mpassrepeatEt.getText().toString().trim()) && checkdata()
                        && mphoneEt.getText().toString().trim().length()==10)
                    //Daca mailul e valid si are parola de minim 6 caractere si parolele se potrivesc
                    signup();
                else if (!isEmailValid(memailEt.getText().toString().trim()))
                    //Daca mailul nu e valid
                    Toast.makeText(SignupActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                else if (mpassEt.getText().toString().trim().length() < 6)
                    //Daca parola e prea scurta
                    Toast.makeText(SignupActivity.this, "Passwords should be at least 6 characters long", Toast.LENGTH_SHORT).show();
                else if (!mpassEt.getText().toString().trim().equals(mpassrepeatEt.getText().toString().trim()))
                    //Daca parolele nu se potrivesc
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                else if (!checkdata())
                    //Daca nu e completat numele sau parola
                    Toast.makeText(SignupActivity.this, "You should fill in all the fields", Toast.LENGTH_SHORT).show();
                else if (mphoneEt.getText().toString().trim().length()!=10)
                    //Daca numarul de telefon nu pare bun
                    Toast.makeText(SignupActivity.this, "Phone number is not a valid format", Toast.LENGTH_SHORT).show();
            }
        });
        mphotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, 1);

            }
        });
    }
    protected void signup() {
        mAuth.createUserWithEmailAndPassword(memailEt.getText().toString(), mpassEt.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Daca a fost inregistrat cu succes
                            user = mAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, "Signup succeded",
                                    Toast.LENGTH_SHORT).show();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this,
                                                        "Verification email sent to " + user.getEmail(),
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(SignupActivity.this,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            setvalues();
                            returntologin();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException existEmail) {
                                //Daca mailul e deja folosit
                                Toast.makeText(SignupActivity.this, "Email already exists",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //Daca nu a fost inregistrat cu succes din alt motiv
                            Toast.makeText(SignupActivity.this, "Signup failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //Verifica daca datele sunt goale
    boolean checkdata() {
        if (mfirstnameEt.getText().toString().trim().equals("") ||
                mlastnameEt.getText().toString().trim().equals("") ||
                mphoneEt.getText().toString().trim().equals("")
                )
            return false;
        else return true;
    }
    //pune ce mai trebuie in database
    void setvalues() {
        muser = new User(mfirstnameEt.getText().toString().trim(), mlastnameEt.getText().toString().trim(),
                memailEt.getText().toString(), mphoneEt.getText().toString(),"null");

        if(checkBox.isChecked())
        {StorageReference ref = mStorageRef.child(user.getUid());
            ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    photouri = taskSnapshot.getUploadSessionUri();

                }
            });}

        mDatabase.child("id").child(user.getUid()).
                setValue(muser);
    }
    //Verifica validitatea mailului
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    //in mod surprinzator, se intoarce in login si completeaza automat casutele
    public void returntologin() {
        Intent i = new Intent();
        i.putExtra("email", memailEt.getText().toString());
        i.putExtra("pass", mpassEt.getText().toString());
        setResult(Activity.RESULT_OK, i);
        mAuth.signOut();
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //pentru cand vine din galerie
        if (requestCode == 1 && resultCode == RESULT_OK) {
            file = data.getData();
            //timerul e doar ca sa se bifeze casuta in fata userului
            CountDownTimer count = new CountDownTimer(1500, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    checkBox.setChecked(true);
                }
            }.start();

        }
    }
}
