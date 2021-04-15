package com.example.notebook.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notebook.R;
import com.example.notebook.database.NotesDatabase;
import com.example.notebook.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle,inputNoteSubtitle,inputNoteText;
    private TextView textDatetime;
    private View viewSubtitleIndicator;
    private ImageView imageNote;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;

    private String selectedImagePath;
    private String selectNoteColor;
    private static final int REQUEST_CODE_STORAGE_PERMISSION =1;
    private static final int REQUEST_CODE_SELECT_IMAGE =2;
    private AlertDialog dialogueAddUrl;
    private AlertDialog dialogDeleteNote;
   // private Object GradientDrawable;
    private Note alreadyAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_create_note2);
        ImageView imageBack=findViewById (R.id.imageBack);
        imageBack.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                onBackPressed ();
            }
        });


        inputNoteTitle=findViewById (R.id.inputNoteTitle);
        inputNoteSubtitle=findViewById (R.id.inputNoteSubtitle);
        inputNoteText=findViewById (R.id.inputNote);
        textDatetime=findViewById (R.id.textDateTime);
        viewSubtitleIndicator=findViewById (R.id.viewSubtitleIndicator);
        imageNote=findViewById (R.id.imageNote);
        textWebURL=findViewById (R.id.textWebUrl);
        layoutWebURL=findViewById (R.id.layoutWebUrl);



        textDatetime.setText (
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault ())
        .format (new Date ()));

        ImageView imageSave = findViewById (R.id.imageSave);
        imageSave.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                saveNote ();
            }
        });

        if (getIntent ().getBooleanExtra ("isFromQuickActions",false)){
            String type=getIntent ().getStringExtra ("quickActionType");
            if (type!=null){
                if (type.equals ("image")){
                    selectedImagePath=getIntent ().getStringExtra ("imagePath");
                    imageNote.setImageBitmap (BitmapFactory.decodeFile (selectedImagePath));
                    imageNote.setVisibility (View.VISIBLE);
                    findViewById (R.id.imageRemoveImage).setVisibility (View.VISIBLE);
                }else if (type.equals ("URL")){
                    textWebURL.setText (getIntent ().getStringExtra ("URL"));
                    layoutWebURL.setVisibility (View.VISIBLE);
                }
            }
        }

        initMiscelleneous ();
      //  setSubtitleIndicatorColor();
        selectNoteColor= "#333333";
        selectedImagePath="";

        if (getIntent ().getBooleanExtra ("isViewOrUpdate",false)){
            alreadyAvailable=(Note)getIntent ().getSerializableExtra ("note");
            setViewOrUpdateNote ();
        }
        findViewById (R.id.imageRemoveUrl).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                textWebURL.setText (null);
                layoutWebURL.setVisibility (View.GONE);
            }
        });

        findViewById (R.id.imageRemoveImage).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {

                imageNote.setImageBitmap (null);
                imageNote.setVisibility (View.GONE);
                findViewById (R.id.imageRemoveImage).setVisibility (View.GONE);
                selectedImagePath="";

            }
        });

    }

    private void setViewOrUpdateNote(){

        inputNoteTitle.setText (alreadyAvailable.getTitle ());
        inputNoteSubtitle.setText (alreadyAvailable.getSubtitle ());
        inputNoteText.setText (alreadyAvailable.getNote_text ());
        textDatetime.setText (alreadyAvailable.getDateTime ());

        if (alreadyAvailable.getImage_path () != null && !alreadyAvailable.getImage_path ().trim ().isEmpty ()){
            imageNote.setImageBitmap (BitmapFactory.decodeFile (alreadyAvailable.getImage_path ()));
            imageNote.setVisibility (View.VISIBLE);
            findViewById (R.id.imageRemoveImage).setVisibility (View.VISIBLE);
            selectedImagePath= alreadyAvailable.getImage_path ();
        }
        if (alreadyAvailable.getWeb_link () != null && !alreadyAvailable.getWeb_link ().trim ().isEmpty ()){
            textWebURL.setText (alreadyAvailable.getWeb_link ());
            layoutWebURL.setVisibility (View.VISIBLE);

        }


    }
    private void saveNote(){
        if (inputNoteTitle.getText ().toString ().trim ().isEmpty ()){
            Toast.makeText (this,"Note title can't be empty",Toast.LENGTH_SHORT).show ();
            return;
        }else if (inputNoteSubtitle.getText ().toString ().trim ().isEmpty () && inputNoteText.getText ().toString ().trim ().isEmpty ()){
            Toast.makeText (this,"Note can't be empty",Toast.LENGTH_SHORT).show ();
            return;

        }

        final Note note=new Note ();
        note.setTitle (inputNoteTitle.getText ().toString ());
        note.setSubtitle (inputNoteSubtitle.getText ().toString ());
        note.setNote_text (inputNoteText.getText ().toString ());
        note.setDateTime (textDatetime.getText ().toString ());
        note.setColor (selectNoteColor);
        note.setImage_path (selectedImagePath);

        if (layoutWebURL.getVisibility () == View.VISIBLE){
            note.setWeb_link (textWebURL.getText ().toString ());
        }

        if (alreadyAvailable !=null){
            note.setId (alreadyAvailable.getId ());
        }


        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {

                NotesDatabase.getDatabase (getApplicationContext ()).noteDao ().insertNote (note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute (aVoid);
                Intent intent=new Intent ();
                setResult (RESULT_OK,intent);
                finish ();
            }
        }

        new SaveNoteTask ().execute ();
    }

       private void initMiscelleneous() {
        final LinearLayout layoutMissceleneous=findViewById (R.id.layoutMiscelleneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior=BottomSheetBehavior.from (layoutMissceleneous);
        layoutMissceleneous.findViewById (R.id.textMissceleneous).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState (BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

           final ImageView imaggeColor1=layoutMissceleneous.findViewById (R.id.imageColor1);
           final ImageView imaggeColor2=layoutMissceleneous.findViewById (R.id.imageColor2);
           final ImageView imaggeColor3=layoutMissceleneous.findViewById (R.id.imageColor3);
           final ImageView imaggeColor4=layoutMissceleneous.findViewById (R.id.imageColor4);
           final ImageView imaggeColor5=layoutMissceleneous.findViewById (R.id.imageColor5);

           layoutMissceleneous.findViewById (R.id.viewColor1).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {
                   selectNoteColor = "#333333";
                   imaggeColor1.setImageResource (R.drawable.ic_done);
                   imaggeColor2.setImageResource (0);
                   imaggeColor3.setImageResource (0);
                   imaggeColor4.setImageResource (0);
                   imaggeColor5.setImageResource (0);
                   setSubtitleIndicatorColor ();
               }
           });
           layoutMissceleneous.findViewById (R.id.viewColor2).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {
                   selectNoteColor = "#FDBE3B";
                   imaggeColor1.setImageResource (0);
                   imaggeColor2.setImageResource (R.drawable.ic_done);
                   imaggeColor3.setImageResource (0);
                   imaggeColor4.setImageResource (0);
                   imaggeColor5.setImageResource (0);
                   setSubtitleIndicatorColor ();
               }
           });
           layoutMissceleneous.findViewById (R.id.viewColor3).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {
                   selectNoteColor = "#FF4842";
                   imaggeColor1.setImageResource (0);
                   imaggeColor2.setImageResource (0);
                   imaggeColor3.setImageResource (R.drawable.ic_done);
                   imaggeColor4.setImageResource (0);
                   imaggeColor5.setImageResource (0);
                   setSubtitleIndicatorColor ();
               }
           });
           layoutMissceleneous.findViewById (R.id.viewColor4).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {
                   selectNoteColor = "#3A52Fc";
                   imaggeColor1.setImageResource (0);
                   imaggeColor2.setImageResource (0);
                   imaggeColor3.setImageResource (0);
                   imaggeColor4.setImageResource (R.drawable.ic_done);
                   imaggeColor5.setImageResource (0);
                   setSubtitleIndicatorColor ();
               }
           });
           layoutMissceleneous.findViewById (R.id.viewColor5).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {
                   selectNoteColor = "#000000";
                   imaggeColor1.setImageResource (0);
                   imaggeColor2.setImageResource (0);
                   imaggeColor3.setImageResource (0);
                   imaggeColor4.setImageResource (0);
                   imaggeColor5.setImageResource (R.drawable.ic_done);
                   setSubtitleIndicatorColor ();
               }
           });
           if (alreadyAvailable!= null && alreadyAvailable.getColor ()!=null && !alreadyAvailable.getColor ().trim ().isEmpty ()){
               switch (alreadyAvailable.getColor ()){

                   case "#FDBE3B":
                       layoutMissceleneous.findViewById (R.id.viewColor2).performClick ();
                       break;
                   case "#FF4842":
                       layoutMissceleneous.findViewById (R.id.viewColor3).performClick ();
                       break;
                   case "#3A52Fc":
                       layoutMissceleneous.findViewById (R.id.viewColor4).performClick ();
                       break;
                   case "#000000":
                       layoutMissceleneous.findViewById (R.id.viewColor5).performClick ();
                       break;
               }
           }

           layoutMissceleneous.findViewById (R.id.layoutAddImage).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {

                   bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
                   if (ContextCompat.checkSelfPermission (
                           getApplicationContext (), Manifest.permission.READ_EXTERNAL_STORAGE)
                           != PackageManager.PERMISSION_GRANTED
                   ){
                       ActivityCompat.requestPermissions (
                               CreateNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                               REQUEST_CODE_STORAGE_PERMISSION
                       );
                   } else {
                       selectImage ();
                   }

               }
           });

           layoutMissceleneous.findViewById (R.id.layoutAddUrl).setOnClickListener (new View.OnClickListener () {
               @Override
               public void onClick(View v) {

                   bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
                   showAddUrlDialog ();
               }
           });

           if (alreadyAvailable!= null){
               layoutMissceleneous.findViewById (R.id.layoutDeleteNote).setVisibility (View.VISIBLE);
               layoutMissceleneous.findViewById (R.id.layoutDeleteNote).setOnClickListener (new View.OnClickListener () {
                   @Override
                   public void onClick(View v) {

                       bottomSheetBehavior.setState (BottomSheetBehavior.STATE_COLLAPSED);
                       showDeleteNoteDialog ();
                   }
               });
           }
    }

    private void showDeleteNoteDialog(){

        if (dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder (CreateNoteActivity.this);
            View view=LayoutInflater.from (this).inflate (

                    R.layout.layout_delete_note,
                    (ViewGroup)findViewById (R.id.layoutDeleteNoteContainer)
            );

            builder.setView (view);
            dialogDeleteNote=builder.create ();
            if (dialogDeleteNote.getWindow ()!= null){
                dialogDeleteNote.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
            }
            view.findViewById (R.id.textDeleteNote).setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {

                            NotesDatabase.getDatabase (getApplicationContext ()).noteDao ()
                                    .deleteNote (alreadyAvailable);
                            return null;
                        }

                        protected void onPostExecute(Void avoid){

                            super.onPostExecute (avoid);
                            Intent intent=new Intent ();
                            intent.putExtra ("isNoteDeleted",true);
                            setResult (RESULT_OK,intent);
                            finish ();
                        }
                    }

                    new DeleteNoteTask ().execute ();
                }
            });

            view.findViewById (R.id.textCancel).setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss ();
                }
            });
        }
        dialogDeleteNote.show ();
    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable=(GradientDrawable)viewSubtitleIndicator.getBackground ();
        gradientDrawable.setColor (Color.parseColor (selectNoteColor));
    }

    private void selectImage(){
        Intent intent=new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity (getPackageManager ())!=null){
            startActivityForResult (intent,REQUEST_CODE_SELECT_IMAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);

        if (requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length >0){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage ();
            }else {
                Toast.makeText (this,"Permission Denied",Toast.LENGTH_SHORT).show ();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult (requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data !=null){
                Uri selectImageUri= data.getData ();
                if (selectImageUri !=null){
                    try{

                        InputStream inputStream=getContentResolver ().openInputStream (selectImageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream (inputStream);
                        imageNote.setImageBitmap (bitmap);
                        imageNote.setVisibility (View.VISIBLE);

                        findViewById (R.id.imageRemoveImage).setVisibility (View.VISIBLE);

                        selectedImagePath=getPathFromUri (selectImageUri);

                    }catch (Exception exception){
                        Toast.makeText (this,exception.getMessage (),Toast.LENGTH_SHORT).show ();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contenturi){
        String filePath;
        Cursor cursor=getContentResolver ()
                .query (contenturi,null,null,null,null);
        if (cursor==null){
            filePath = contenturi.getPath ();

        }else{
            cursor.moveToFirst ();
            int index=cursor.getColumnIndex ("_data");
            filePath=cursor.getString (index);
            cursor.close ();
        }
        return filePath;
    }
    private  void showAddUrlDialog(){

        if (dialogueAddUrl==null){
            AlertDialog.Builder builder=new AlertDialog.Builder (CreateNoteActivity.this);
            View view= LayoutInflater.from (this).inflate (
                    R.layout.layout_add_url,
                    (ViewGroup)findViewById (R.id.layoutAddUrlContainer)
            );
            builder.setView (view);
            dialogueAddUrl=builder.create ();
            if (dialogueAddUrl.getWindow ()!=null){
                dialogueAddUrl.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
            }


            final EditText inputUrl = view.findViewById (R.id.inputUrl);
            inputUrl.requestFocus ();

            view.findViewById (R.id.textAdd).setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {

                    if (inputUrl.getText ().toString ().trim ().isEmpty ()){

                        Toast.makeText (CreateNoteActivity.this,"Enter URL",Toast.LENGTH_SHORT).show ();
                    }else if (!Patterns.WEB_URL.matcher (inputUrl.getText ().toString ()).matches ()){

                        Toast.makeText (CreateNoteActivity.this,"Enter Valid URL",Toast.LENGTH_SHORT).show ();
                    }else {

                        textWebURL.setText (inputUrl.getText ().toString ());
                        layoutWebURL.setVisibility (View.VISIBLE);
                        dialogueAddUrl.dismiss ();
                    }
                }
            });

            view.findViewById (R.id.textCancel).setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    dialogueAddUrl.dismiss ();
                }
            });

        }

        dialogueAddUrl.show ();

    }
}