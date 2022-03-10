package com.pisici.caini.petsearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
     ArrayList<Announcement> arr = new ArrayList<>();
     ImageView fullImageView;
     Button mProfileBtn;
     User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullImageView=findViewById(R.id.main_fullImage);
        mProfileBtn=(Button) findViewById(R.id.main_profileBtn) ;
        user=(User) getIntent().getSerializableExtra("User");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("announcement");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                arr.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Announcement curr = ds.getValue(Announcement.class);
                    arr.add(curr);
                }
                ListView mListView = (ListView) findViewById(R.id.resultsLV);
                MyListAdapter adapter=new MyListAdapter(MainActivity.this,R.layout.pet_list_item,arr);
                mListView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,ProfileActivity.class);
                i.putExtra("User",user);
                startActivity(i);
            }
        });
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = database;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.child("id").child(uid).getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class ViewHolder {
        TextView mPetNameTv;
        TextView mOwnerTv;
        TextView mLocationTv;
        TextView mBountyTv;
        ImageView mPetImage;
        Button mContactBtn;
    }

    private class MyListAdapter extends ArrayAdapter<Announcement> {
        private int layout;

        public MyListAdapter(@NonNull Context context, int resource, @NonNull List<Announcement> objects) {
            super(context, resource, objects);
            layout = resource;
        }


        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MainActivity.ViewHolder mainViewHolder = null;

            if (convertView == null) {

                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                final MainActivity.ViewHolder viewHolder = new MainActivity.ViewHolder();

                viewHolder.mPetNameTv = (TextView) convertView.findViewById(R.id.ann_PetNameTv);
                viewHolder.mPetNameTv.setText(getItem(position).getPetName());

                viewHolder.mOwnerTv = (TextView) convertView.findViewById(R.id.ann_OwnerNameTv);
                viewHolder.mOwnerTv.setText(getItem(position).getOwnerName());

                viewHolder.mLocationTv = (TextView) convertView.findViewById(R.id.ann_LastLocTv);
                viewHolder.mLocationTv.setText(getItem(position).getLocation());

                viewHolder.mBountyTv= (TextView) convertView.findViewById(R.id.ann_BountyTv);
                viewHolder.mBountyTv.setText(getItem(position).getBounty());

                viewHolder.mContactBtn=(Button) convertView.findViewById(R.id.ann_contactBtn);

                viewHolder.mContactBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i =new Intent(Intent.ACTION_DIAL);
                        String uri="tel:"+getItem(position).getOwnerPhone();
                        i.setData(Uri.parse(uri));
                        startActivity(i);
                    }
                });

                viewHolder.mPetImage= (ImageView) convertView.findViewById(R.id.ann_PetIv);
                final StorageReference storageReference=FirebaseStorage.getInstance().getReference();
                storageReference.child(getItem(position).getPetId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(MainActivity.this)
                                .load(uri)
                                .into(viewHolder.mPetImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                viewHolder.mPetImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        storageReference.child(getItem(position).getPetId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(MainActivity.this)
                                        .load(uri)
                                        .into(fullImageView);
                                fullImageView.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                });
                fullImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fullImageView.setVisibility(View.GONE);
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                mainViewHolder = (MainActivity.ViewHolder) convertView.getTag();
                mainViewHolder.mPetNameTv.setText(getItem(position).getPetName());
                mainViewHolder.mOwnerTv.setText(getItem(position).getOwnerName());
                mainViewHolder.mLocationTv.setText(getItem(position).getLocation());
                mainViewHolder.mBountyTv.setText(getItem(position).getBounty());
            }

            return convertView;
        }
    }
}
