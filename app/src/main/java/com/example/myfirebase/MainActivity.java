package com.example.myfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText nametxt,surnametxt,name,surname;
    Button save,del,update;
    RadioButton r1,r2,r3,r4;
    RecyclerView recyclerView;



    Model data;

    private DatabaseReference mdatabase;
    private FirebaseAuth mauth;
    private FirebaseRecyclerAdapter adapter;

    private String key,getname,getsurname,getgender,date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nametxt=findViewById(R.id.name);
        surnametxt=findViewById(R.id.surname);
        save=findViewById(R.id.save);
        r1=findViewById(R.id.radiomale);
        r2=findViewById(R.id.radiofemale);


        recyclerView=findViewById(R.id.recyclerview);

        fetch();


        mauth=FirebaseAuth.getInstance();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("NameList");//.child(uID);
        String uID=mauth.getUid();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=nametxt.getText().toString().trim();
                String surname=surnametxt.getText().toString().trim();
                insertData(name,surname);
            }
        });


    }

    boolean emptyCheck(String name, String surname)
    {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(surname)) {
            Toast.makeText(getApplicationContext(), "Enter surname!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return  true;
    }

    String radiocheck(RadioButton r1, RadioButton r2)
    {
        if(r1.isChecked()) return  r1.getText().toString();
        else return r2.getText().toString();
    }
//insert value
    void insertData(String name, String surname)
    {
        if(emptyCheck(name,surname))
        {
            String id=mdatabase.push().getKey();
            date= DateFormat.getDateInstance().format(new Date(String.valueOf(Calendar.getInstance().getTime())));
            data=new Model(id,date,name,surname,radiocheck(r1,r2));
            mdatabase.child(id).setValue(data);
            Toast.makeText(getBaseContext(),"Succesful insert data!",Toast.LENGTH_SHORT).show();
            nametxt.setText("");
            surnametxt.setText("");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

   @Override
    public void onBackPressed() {
       super.onBackPressed();
       startActivity(new Intent(MainActivity.this,Login.class));
       mauth.signOut();
    }

    //recycler view list
    private void fetch() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("NameList");

        FirebaseRecyclerOptions<Model> options =
                new FirebaseRecyclerOptions.Builder<Model>()
                        .setQuery(query, new SnapshotParser<Model>() {
                            @NonNull
                            @Override
                            public Model parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Model(snapshot.child("name").getValue().toString(),
                                        snapshot.child("surname").getValue().toString(),
                                        snapshot.child("gender").getValue().toString());
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.custom_list_item, parent, false);

                return new ViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, final Model model) {



                holder.setName("Name:"+model.getName());
                holder.setSurname("Surname:"+model.getSurname());
                holder.setGender("Gender:"+model.getGender());
                holder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key=getRef(position).getKey();
                        getname=model.getName();
                        getsurname=model.getSurname();
                        getgender=model.getGender();

                        mydialog();
                    }
                });

                            }

        };
        recyclerView.setAdapter(adapter);
    }


static class ViewHolder extends RecyclerView.ViewHolder {
        View myview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myview=itemView;
        }
        public void setName(String name)
        {
            TextView nametxt=myview.findViewById(R.id.nametxt);
            nametxt.setText(name);
        }
        public void setSurname(String surname)
        {
            TextView surnametxt=myview.findViewById(R.id.surnametxt);
            surnametxt.setText(surname);
        }
        public void setGender(String gender)
        {
            TextView gendertxt=myview.findViewById(R.id.gendertxt);
            gendertxt.setText(gender);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
//logout
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                mauth.signOut();
                Toast.makeText(getApplicationContext(),"Logout Succesful",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),Login.class));
        }
        return super.onOptionsItemSelected(item);
    }


    void  mydialog(){


        AlertDialog.Builder mydialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater layoutInflater =LayoutInflater.from(MainActivity.this);

        View myview = layoutInflater.inflate(R.layout.mydialogbox,null);
        mydialog.setView(myview);


        del = myview.findViewById(R.id.del);
        update=myview.findViewById(R.id.update);
        name=myview.findViewById(R.id.editName);
        surname=myview.findViewById(R.id.editSurname);
        r3=myview.findViewById(R.id.radiomale);
        r4=myview.findViewById(R.id.radiofemale);

        final AlertDialog dialog = mydialog.create();

        dialog.show();


        name.setText(getname);

        surname.setText(getsurname);

//delete value
        del.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mdatabase.child(key).removeValue();
                        dialog.dismiss();
                        key="";
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
//update value
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        getname=name.getText().toString().trim();
                        getsurname=surname.getText().toString().trim();
                        getgender=radiocheck(r3,r4);
                        date= DateFormat.getDateInstance().format(new Date(String.valueOf(Calendar.getInstance().getTime())));
                        Model model = new Model(key,date,getname,getsurname,getgender);
                        mdatabase.child(key).setValue(model);
                        dialog.dismiss();
                        key="";
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

            }
        });

    }
}
