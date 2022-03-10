package com.pisici.caini.petsearch;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {
    TextView mnameTv;
    TextView memailTv;
    TextView mphoneTv;
    TextView mpetNameTv;
    ImageView petPhoto;
    Button maddpetBtn;
    Button missingBtn;
    Uri file;
    User user;
    Boolean photo;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mnameTv = (TextView) findViewById(R.id.profile_nameTv);
        memailTv= (TextView) findViewById(R.id.profile_emailTv);
        mphoneTv= (TextView) findViewById(R.id.profile_phoneTv);
        mpetNameTv= (TextView) findViewById(R.id.profile_petNameTv);
        maddpetBtn = (Button) findViewById(R.id.addpetBtn);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        missingBtn=(Button) findViewById(R.id.missingBtn);
        user = (User) getIntent().getSerializableExtra("User");
        petPhoto=(ImageView) findViewById(R.id.profile_petPhoto);
        photo = false;

        putData();

        maddpetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dialog add pet
                final Pet new_animal = new Pet();
                AlertDialog.Builder new_pet_dialog = new AlertDialog.Builder(ProfileActivity.this);

                View dialog_view = getLayoutInflater().inflate(R.layout.add_pet_dialog, null);
                final EditText mnameEt = dialog_view.findViewById(R.id.pet_nameEt);
                final EditText mdateEt = dialog_view.findViewById(R.id.pet_dateEt);
                Button mphotoBtn = dialog_view.findViewById(R.id.mpet_photoBtn);
                Button maddPetBtn = dialog_view.findViewById(R.id.dialog_addPetBtn);
                final RadioGroup mspeciesRG = dialog_view.findViewById(R.id.speciesRG);
                final Spinner mraceSpinner = dialog_view.findViewById(R.id.species_spinner);

                new_pet_dialog.setView(dialog_view);

                final AlertDialog dialog = new_pet_dialog.create();

                mspeciesRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.dogRb:
                                spinner_change_dog(mraceSpinner);
                                break;
                            case R.id.catRb:
                                spinner_change_cat(mraceSpinner);
                                break;
                            default:
                                break;
                        }
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

                maddPetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mspeciesRG.getCheckedRadioButtonId() == R.id.dogRb) {
                            Log.v("log", "trimis in add_pet cu dog");
                            add_pet(true, mnameEt.getText().toString().trim(), mdateEt.getText().toString().trim(),
                                    (Dog_breed) mraceSpinner.getSelectedItem(), Cat_breed.Bengal);


                        }
                        else {
                            Log.v("log", "trimis in add_pet cu cat");
                            add_pet(false, mnameEt.getText().toString().trim(), mdateEt.getText().toString().trim(),
                                    Dog_breed.Bulldog, (Cat_breed) mraceSpinner.getSelectedItem());
                        }
                        dialog.cancel();

                    }
                });
                mdateEt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        final int mDay, mYear, mMonth;
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);
                        //aici face chestiuta de calendar
                        DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        if (checkdate(mYear, mMonth + 1, mDay, year, monthOfYear + 1, dayOfMonth))
                                            mdateEt.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                        else
                                            makeToast("This date is not valid");
                                    }
                                }, mYear, mMonth, mDay);
                        datePickerDialog.show();
                    }
                });
                mspeciesRG.check(R.id.dogRb);

                dialog.show();

            }
        });

        missingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.getPetId().equals("null")){
                    makeToast("You have not added your pet yet");
                    return;
                }
                else if(user.isMissing())
                {
                    user.setMissing(false);
                    mDatabase.child("announcement").child(FirebaseAuth.getInstance().getUid()).removeValue();
                    missingBtn.setText("Pet missing");
                    mDatabase.child("id").child(FirebaseAuth.getInstance().getUid()).child("missing").setValue(false);
                }
                else{

                AlertDialog.Builder missing_pet_dialog = new AlertDialog.Builder(ProfileActivity.this);

                View dialog_view = getLayoutInflater().inflate(R.layout.missing_prompt, null);
                final EditText mplaceEt = dialog_view.findViewById(R.id.prompt_placeEt);
                final EditText mdateEt = dialog_view.findViewById(R.id.prompt_dateEt);
                final Button mbutton = dialog_view.findViewById(R.id.promptBtn);
                final EditText mbounty = dialog_view.findViewById(R.id.prompt_bountyEt);
                missing_pet_dialog.setView(dialog_view);
                final AlertDialog dialog = missing_pet_dialog.create();
                dialog.show();

                mdateEt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        final int mDay, mYear, mMonth;
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        if (checkdate(mYear, mMonth + 1, mDay, year, monthOfYear + 1, dayOfMonth))
                                            mdateEt.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                        else
                                            makeToast("This date is not valid");
                                    }
                                }, mYear, mMonth, mDay);
                        datePickerDialog.show();
                    }
                });

                mbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mdateEt.getText().toString().trim().equals("") ||
                                mplaceEt.getText().toString().trim().equals("")
                                || mbounty.getText().toString().trim().equals(""))
                            makeToast("You should fill in all the data");
                        else{
                           final Announcement ann=new Announcement();
                            mDatabase.child("pet").child(user.getPetId()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    setPetName(dataSnapshot.getValue().toString(),ann,dialog);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            ann.date=mdateEt.getText().toString().trim();
                            ann.location=mplaceEt.getText().toString().trim();
                            ann.ownerName=user.getFirst_name();
                            ann.bounty=mbounty.getText().toString().trim();
                            ann.ownerPhone=user.getPhone();
                            user.setMissing(true);
                            mDatabase.child("id").child(FirebaseAuth.getInstance().getUid()).child("missing").setValue(true);
                            missingBtn.setText("I found my pet");
                        }
                    }
                });
            }}
        });
    }

    boolean checkdate(int cyear, int cmonth, int cday, int year, int month, int day){
        if(cyear-year>30)
            return false;
        if(cyear<year)
            return false;
        if(cyear==year && cmonth<month)
            return false;
        if(cyear==year && cmonth==month && cday<day)
            return false;
        return true;
    }

    void spinner_change_dog(Spinner mraceSpinner) {
        mraceSpinner.setAdapter(new ArrayAdapter<Dog_breed>(ProfileActivity.this,
                android.R.layout.simple_spinner_item, Dog_breed.values()));

    }

    void spinner_change_cat(Spinner mraceSpinner) {
        mraceSpinner.setAdapter(new ArrayAdapter<Cat_breed>(ProfileActivity.this,
                android.R.layout.simple_spinner_item, Cat_breed.values()));

    }

    void add_pet(boolean isDog, String name, String birthday, Dog_breed dog_breed, Cat_breed cat_breed) {
        if (name.equals("") || birthday.equals("")) {
            makeToast("You should fill in all the fields");
            return;
        }

       /* if (!isBirthdayValid(birthday)) {
            Log.v("log", "nu e valida saracia");
            makeToast("Birthday is not valid");
            return;
        }*/

        if(!photo) {
            makeToast("Please add a photo of your pet");
            return;
        }

        String Id = Pet.getNewID();
        if (!user.getPetId().equals("null")) {
            mDatabase.child("pet").child(user.getPetId()).removeValue();
            mStorageRef.child(user.getPetId()).delete();
            mDatabase.child("announcement").child(FirebaseAuth.getInstance().getUid()).removeValue();
        }
        user.setPetId(Id);
        mStorageRef.child(Id).putFile(file);
        mDatabase.child("id").child(FirebaseAuth.getInstance().getUid()).child("petId").setValue(Id);
        if (isDog) {
            Pet.Dog doggo = new Pet.Dog(Pet.getNewID(), name, birthday, dog_breed);
            mDatabase.child("pet").child(Id).setValue(doggo);
        } else {
            Pet.Cat cat = new Pet.Cat(Pet.getNewID(), name, birthday, cat_breed);
            mDatabase.child("pet").child(Id).setValue(cat);
        }
    }

    void setPetName(String name, Announcement ann, Dialog dialog){
        ann.petName=name;
        ann.petId=user.getPetId();
        mDatabase.child("announcement").child(FirebaseAuth.getInstance().getUid()).setValue(ann);
        dialog.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //pentru cand vine din galerie
        if (requestCode == 1 && resultCode == RESULT_OK) {
            {
                file = data.getData();
                photo = true;
            }
        }
    }

    void makeToast(String x) {
        Toast.makeText(this, x, Toast.LENGTH_SHORT).show();
    }

    void putData(){
       if(user.isMissing())
           missingBtn.setText("I found my pet");
        StorageReference storageReference=FirebaseStorage.getInstance().getReference();
        storageReference.child(user.getPetId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .into(petPhoto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        mnameTv.setText(user.getFirst_name()+" "+user.getLast_name());
        memailTv.setText(user.getEmail());
        mphoneTv.setText(user.getPhone());
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = database;
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("pet").child(user.getPetId()).child("name").getValue()!=null)
                mpetNameTv.setText(dataSnapshot.child("pet").child(user.getPetId()).child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
