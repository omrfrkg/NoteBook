package com.omrfrkg.notebook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omrfrkg.notebook.databinding.RecyclerRowBinding;

import java.sql.Statement;
import java.util.ArrayList;

public class NotesAdapter  extends RecyclerView.Adapter<NotesAdapter.NotesHolder> {

    ArrayList<Notes> notesArrayList;

    public NotesAdapter(ArrayList<Notes> notesArrayList){
        this.notesArrayList = notesArrayList;
    }

    @NonNull
    @Override
    public NotesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new NotesHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesHolder holder, int position) {

        holder.binding.recyclerRowText.setText(notesArrayList.get(position).noteTitle);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ContentActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("id",notesArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{

                    SQLiteDatabase database = view.getContext().openOrCreateDatabase("NotesDB", Context.MODE_PRIVATE,null);
                    GetData getData = new GetData();
                    database.delete("notes","id" + "=?",new String[]{String.valueOf(notesArrayList.get(position).id)});
                    ArrayList<Notes> arrayList = new ArrayList<>();
                    getData.getData(notesArrayList,database,NotesAdapter.this);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        holder.binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ContentActivity.class);
                intent.putExtra("info","update");
                intent.putExtra("id",notesArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notesArrayList.size();
    }

    public class NotesHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;
        public NotesHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
