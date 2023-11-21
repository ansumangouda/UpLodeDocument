package com.example.uplodedocument;

import static androidx.activity.result.contract.ActivityResultContracts.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private ImageView imageView;
    private TextView choosetextFild, chooseBtn, saveBtn;
    private EditText documenName;
    private Spinner spinner;
    private Toolbar toolbar;
    private int selectedItem;
    private Uri uri;
    private ActivityResultLauncher<String> getContentLauncher;
    private ArrayList<GetDocumentTypeResponceList> getDocumentTypeResponceListArrayList;

    public static int RC_PHOTO_PICKER = 0;
    ActivityResultLauncher<Intent> activityResultLauncher ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFindViewByIds();
        someActivityForResult();

        ImagePickerListenre imagePickerListenre = new ImagePickerListenre();
        chooseBtn.setOnClickListener(imagePickerListenre);
        saveBtn.setOnClickListener(imagePickerListenre);
    }

    private void setFindViewByIds() {
        apiService = RetrofitNetwork.getApiservice();
        choosetextFild = findViewById(R.id.showingText);
        chooseBtn = findViewById(R.id.chooseFile);
        spinner = findViewById(R.id.choooseSpinner);
        documenName = findViewById(R.id.documentName);
        toolbar = findViewById(R.id.record_toolbar);
        saveBtn = findViewById(R.id.saveBtn);
        imageView = findViewById(R.id.action_image);

        

    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_PHOTO_PICKER && data != null) {
                try {
                    Uri uri = data.getData();
                    if (uri != null) {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageView.setImageBitmap(bitmap);

                        // Now you can use the uri or the bitmap as needed
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    public void someActivityForResult(){
       activityResultLauncher  = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    uri = data.getData();
                    imageView.setImageURI(uri);

                }
            }
        });



    }

    private void upLodeToServer(Uri uri) {
        if (uri != null) {
            String filePath = getRealPathFromUri(uri);
            if (filePath != null) {
                File file = new File(getRealPathFromUri(uri));
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                RequestBody Doc_Type_Id_FK = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(3));
                RequestBody PR_Id_FK = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(2336));
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

                Call<DummyClass> call = apiService.insertDocement(filePart, Doc_Type_Id_FK, PR_Id_FK);
                call.enqueue(new Callback<DummyClass>() {
                    @Override
                    public void onResponse(Call<DummyClass> call, Response<DummyClass> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "susses", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DummyClass> call, Throwable t) {

                    }
                });
            }else {
                // Handle the case where the filePath is null
                Toast.makeText(this, "File path is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where the URI is null (e.g., the user didn't select a file)
            Toast.makeText(this, "URI is null", Toast.LENGTH_SHORT).show();
        }


    }


    private String getRealPathFromUri(Uri uri){
       /* String filePath = null;
        try {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                // Handle document URI (e.g., files from Google Drive)
                DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
                filePath = documentFile != null ? documentFile.getUri().getPath() : null;
            } else {
                // Handle other types of URIs
                String[] projection = {MediaStore.Images.Media.DATA};
                try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        filePath = cursor.getString(columnIndex);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;*/
        String filePath = null;
        if (Build.VERSION.SDK_INT < 19){
            filePath = getRealPathFromUriBeforeKitKat(uri);
        }else {
            filePath = getRealPathFromUriKitKatAndAbove(uri);
        }
        return filePath;
    }

    private String getRealPathFromUriBeforeKitKat(Uri uri) {
        String filePath = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = getContentResolver().query(uri , projection ,null ,null,null)){
                if (cursor != null && cursor.moveToFirst()){
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return filePath;
    }
    private String getRealPathFromUriKitKatAndAbove(Uri uri){
        String filepath = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri,projection,null,null,null);
            if (cursor !=null){
                int columIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()){
                    filepath = cursor.getString(columIndex);
                }
                cursor.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return filepath;
    }

    public class ImagePickerListenre implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.chooseFile){
                Intent iGallery = new Intent(Intent.ACTION_GET_CONTENT);
                iGallery.setType("application/pdf");
                iGallery.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                activityResultLauncher.launch(iGallery);
            }
            if (v.getId() == R.id.saveBtn){
                upLodeToServer(uri);
            }
        }
    }
}