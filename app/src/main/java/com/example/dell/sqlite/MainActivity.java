package com.example.dell.sqlite;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private EditText fname;
    private ImageView pic;
    private DatabaseHandler db;
    private String f_name;
    private ListView lv;
    private dataAdapter data;
    private Contact dataModel;
    private Bitmap bp;
    private byte[] photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiate database handler
        db = new DatabaseHandler(this);

        lv = (ListView) findViewById(R.id.list1);
        pic = (ImageView) findViewById(R.id.pic);
        fname = (EditText) findViewById(R.id.txt1);

    }

    public void buttonClicked(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.save:

                if (fname.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Name edit text is empty, Enter name", Toast.LENGTH_LONG).show();
                } else {
                    addContact();
                }

                break;

            case R.id.display:

                ShowRecords();
                break;
            case R.id.pic:
                selectImage();
                break;
        }
    }

    public void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                //boolean result=Utility.checkPermission(Profile.this);
                if (options[item].equals("Take Photo")) {       //  startActivityForResult(intent, REQUEST_CAMERA);

                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

     /* from camera
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
       */

    //from gallery
        /*  Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);
     */
    //  startActivityForResult(intent, 2);


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
                //from camera
                bp = (Bitmap) data.getExtras().get("data");
                pic.setImageBitmap(bp);

            case 2:
                //from gallery

                if(resultCode == RESULT_OK){
                    Uri choosenImage = data.getData();

                        if(choosenImage !=null){

                        bp=decodeUri(choosenImage, 400);
                        pic.setImageBitmap(bp);
                    }
                }


        }
    }

    //COnvert and resize our image to 400dp for faster uploading our images to DB
    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] profileImage(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }

    // function to get values from the Edittext and image
    private void getValues(){
        f_name = fname.getText().toString();
        photo = profileImage(bp);
    }

    //Insert data to the database
    private void addContact(){
        getValues();

        db.addContacts(new Contact(f_name, photo));
        Toast.makeText(getApplicationContext(),"Saved successfully", Toast.LENGTH_LONG).show();
    }

    //Retrieve data from the database and set to the list view
    private void ShowRecords(){
        final ArrayList<Contact> contacts = new ArrayList<>(db.getAllContacts());
        data=new dataAdapter(this, contacts);

        lv.setAdapter(data);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                dataModel = contacts.get(position);

                Toast.makeText(getApplicationContext(),String.valueOf(dataModel.getID()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}