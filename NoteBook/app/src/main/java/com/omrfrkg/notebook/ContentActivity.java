package com.omrfrkg.notebook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.omrfrkg.notebook.databinding.ActivityContentBinding;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ContentActivity extends AppCompatActivity {

    int sayac = 0;

    //Gallariye Gitmek İçin
    ActivityResultLauncher<Intent> activityResultLauncher;
    //İzini İstemeke İçin
    ActivityResultLauncher<String> permissionLauncher;
    ActivityContentBinding binding;
    Bitmap selectedImage;
    SQLiteDatabase database;

    EditText[] editTexts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContentBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //binding.imageView.setImageResource(R.drawable.image_not_selected);

        //Create Database
        database = this.openOrCreateDatabase("NotesDB",MODE_PRIVATE,null);

        registerLauncher();

        Intent intent = getIntent();
        String result = intent.getStringExtra("info");

        editTexts = new EditText[] {binding.textNoteContent,binding.textNoteTitle,binding.textNoteDate};



        if (result.equals("new")){
            binding.button.setVisibility(View.VISIBLE);
            binding.textNoteDate.setVisibility(View.INVISIBLE);

            for (EditText editText : editTexts){
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.setClickable(true);
                editText.setCursorVisible(true);
            }
        }
        else{

            for (EditText editText : editTexts){
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setClickable(false);
                editText.setCursorVisible(false);
            }
            binding.imageView.setClickable(false);


            int id = intent.getIntExtra("id",0);
            binding.button.setVisibility(View.INVISIBLE);
            binding.textNoteDate.setVisibility(View.VISIBLE);

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM notes WHERE id = ?",new String[]{String.valueOf(id)});

                int notetitleIx = cursor.getColumnIndex("notetitle");
                int notecontentIx = cursor.getColumnIndex("notecontent");
                int dateIx = cursor.getColumnIndex("date");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.textNoteTitle.setText(cursor.getString(notetitleIx));
                    binding.textNoteContent.setText(cursor.getString(notecontentIx));
                    binding.textNoteDate.setText(cursor.getString(dateIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void save(View view){

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String date = day+"."+month+"."+year;

        String noteTitle = binding.textNoteTitle.getText().toString();
        String noteContent = binding.textNoteContent.getText().toString();

        if (noteTitle.isEmpty() || noteContent.isEmpty()){
            Toast.makeText(ContentActivity.this,"These areas cannot be left empty!",Toast.LENGTH_LONG).show();
        }
        else{
            imageViewIsNull(sayac);
            Bitmap smallImage = makeSmallerImage(selectedImage,300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
            byte[] byteArray = outputStream.toByteArray();

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS notes(id INTEGER PRIMARY KEY,notetitle VARCHAR,notecontent VARCHAR,date VARCHAR,image BLOB)");

                String sqlInsertCommand = "INSERT INTO notes(notetitle,notecontent,date,image) VALUES(?, ?, ?, ?)";
                SQLiteStatement sqLiteStatement = database.compileStatement(sqlInsertCommand);
                sqLiteStatement.bindString(1,noteTitle);
                sqLiteStatement.bindString(2,noteContent);
                sqLiteStatement.bindString(3,date);
                sqLiteStatement.bindBlob(4,byteArray);
                sqLiteStatement.execute();
            }catch (Exception e){
                e.printStackTrace();
            }

            Intent intent = new Intent(ContentActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        }


    }

    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){

        /*

        ------
        |    |    |------------|
  300   |    |    |            | 100
        |    |    |------------|
        ------          300
         100


         */

        //Boyutları Alınıyor
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float)height;

        // 300/100 = 1 100/300 = 0....
        if (bitmapRatio > 1){
            //landscape image
            //300/100
            width = maximumSize;
            height = (int)(width/bitmapRatio);
        }
        else{
            //portrait image
            //100/300
            height = maximumSize;
            width = (int)(height * bitmapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);
    }
    public void selectImage(View view){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU){
            //Android 33+ -> READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }
                else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }
            else{
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
        else {
            //Android 32- -> READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }
                else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            else{
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    public void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == RESULT_OK){
                    Intent intenFromResult = result.getData();
                    if (intenFromResult != null){
                        Uri imageData = intenFromResult.getData();
                        //binding.imageView.setImageURI(imageData);
                        try {

                            if (Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(ContentActivity.this.getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }
                            else {

                                selectedImage = MediaStore.Images.Media.getBitmap(ContentActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);

                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    Toast.makeText(ContentActivity.this,"NULLLLLLLL!!!!!!!",Toast.LENGTH_LONG).show();
                    Uri imageData = Uri.parse("android.resource://com.omrfrkg.notebook/drawable/image_not_selected");

                    try {
                        if (Build.VERSION.SDK_INT >= 28){
                            ImageDecoder.Source source = ImageDecoder.createSource(ContentActivity.this.getContentResolver(),imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);

                        }
                        else {

                            selectedImage = MediaStore.Images.Media.getBitmap(ContentActivity.this.getContentResolver(),imageData);
                            binding.imageView.setImageBitmap(selectedImage);

                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result == true){
                    //Permission Granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else{
                    //Permission Denied
                    Toast.makeText(ContentActivity.this,"Permission Needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void imageViewIsNull(int sayac){
        if (sayac == 0){
            Uri imageData = Uri.parse("android.resource://com.omrfrkg.notebook/drawable/image_not_selected");

            try {
                if (Build.VERSION.SDK_INT >= 28){
                    ImageDecoder.Source source = ImageDecoder.createSource(ContentActivity.this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    binding.imageView.setImageBitmap(selectedImage);

                }
                else {

                    selectedImage = MediaStore.Images.Media.getBitmap(ContentActivity.this.getContentResolver(),imageData);
                    binding.imageView.setImageBitmap(selectedImage);

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void clickCounter(View view){

        sayac++;
    }
}