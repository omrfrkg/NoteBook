package com.omrfrkg.notebook;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class GetData {

    public void getData(ArrayList<Notes> notesArrayList,SQLiteDatabase database,NotesAdapter notesAdapter){
        try{
            notesArrayList.clear();
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
