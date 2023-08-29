package com.omrfrkg.notebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.omrfrkg.notebook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<Notes> notesArrayList;
    NotesAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        notesArrayList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesArrayList);
        binding.recyclerView.setAdapter(notesAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.note_menu){
            Intent intent = new Intent(this,ContentActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void getData(){
        try{

            SQLiteDatabase database = this.openOrCreateDatabase("NotesDB",MODE_PRIVATE,null);

            Cursor cursor = database.rawQuery("SELECT * FROM notes",null);
            int noteTitleIx = cursor.getColumnIndex("notetitle");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()){
                String noteTitle = cursor.getString(noteTitleIx);
                int id = cursor.getInt(idIx);

                Notes note = new Notes(noteTitle,id);
                notesArrayList.add(note);
            }
            notesAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}