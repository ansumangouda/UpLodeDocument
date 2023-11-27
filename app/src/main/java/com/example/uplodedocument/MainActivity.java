package com.example.uplodedocument;
import static android.app.PendingIntent.getActivity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

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
                    choosetextFild.setText(uri.toString());
                    imageView.setImageURI(uri);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
            Uri uri  =  resultData.getData();

            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            String stringUri = uri.toString();
//            choosetextFild.setText(stringUri);
//            getPDF(uri);
            try {
                File file = getInputStreamFromUri(uri);
                System.out.println("File uri name : "+file.getName());
                System.out.println("File uri path is : "+file.getAbsolutePath());
                uploadFile(file);
            }  catch (Exception  e) {
                e.printStackTrace();
            }

        }
    }

    public File getInputStreamFromUri(Uri uri) throws IOException {

        InputStream inputStream= this.getContentResolver().openInputStream(uri);
        File file = new File(this.getFilesDir(), "upload.pdf");

        if (inputStream != null) {
            try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (Exception  e) {
                System.out.println("File throws exception with" + e.getMessage());
            } finally {
                inputStream.close();
            }
        }
        return file;
    }



    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, PICK_PDF_FILE);
    }

    private void upLodeToServer(Uri  uri) {
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
    private void uploadFile(File file) {
            if (file != null) {
                System.out.println("File upload started with  file name: "+file.getName()+ "File path" +file.getAbsolutePath());
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                RequestBody Doc_Type_Id_FK = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(3));
                RequestBody PR_Id_FK = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(2336));
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

                Call<DummyClass> call = apiService.insertDocement(filePart, Doc_Type_Id_FK, PR_Id_FK);
                call.enqueue(new Callback<DummyClass>() {
                    @Override
                    public void onResponse(Call<DummyClass> call, Response<DummyClass> response) {
                        System.out.println("File upload ended successfully with  file name: "+file.getName()+ "File path" +file.getAbsolutePath());

                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "susses", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DummyClass> call, Throwable t) {
                        System.out.println("File upload ended failed with  file name: "+file.getName()+ "File path" +file.getAbsolutePath());

                    }
                });
            }else {
                // Handle the case where the filePath is null
                Toast.makeText(this, "File path is null", Toast.LENGTH_SHORT).show();
            }

    }


    private String getRealPathFromUri(Uri uri){

        String filePath = null;
        if (Build.VERSION.SDK_INT < 19){
            filePath = getRealPathFromUriBeforeKitKat(uri);
        }else {
            filePath = getRealPathFromUriKitKatAndAbove(uri);
        }
        return filePath;


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
                pdfOpen();
            }
            if (v.getId() == R.id.saveBtn) {
//                upLodeToServer(uri);
            }
        }
    }

    private void openMedia() {
        Intent iGallery = new Intent(Intent.ACTION_GET_CONTENT);
        iGallery.setType("image/jpg");
        iGallery.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        activityResultLauncher.launch(iGallery);
    }

    private void pdfOpen() {
        Intent iGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        iGallery.addCategory(Intent.CATEGORY_OPENABLE);
        iGallery.setType("application/pdf");
        iGallery.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        activityResultLauncher.launch(iGallery);
        startActivityForResult(iGallery, 2000);
    }

    private void getPDFFileSize(Uri uri)  {

            // The query, because it only applies to a single document, returns only
            // one row. There's no need to filter, sort, or select fields,
            // because we want all fields for one document.
            Cursor cursor = this.getContentResolver()
                    .query(uri, null, null, null, null, null);

            try {
                // moveToFirst() returns false if the cursor has 0 rows. Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor != null && cursor.moveToFirst()) {

                    // Note it's called "Display Name". This is
                    // provider-specific, and might not necessarily be the file name.
                    @SuppressLint("Range") String displayName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    Log.i(TAG, "Display Name: " + displayName);

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null. But because an
                    // int can't be null, the behavior is implementation-specific,
                    // and unpredictable. So as
                    // a rule, check if it's null before assigning to an int. This will
                    // happen often: The storage API allows for remote files, whose
                    // size might not be locally known.
                    String size = null;
                    if (!cursor.isNull(sizeIndex)) {
                        // Technically the column stores an int, but cursor.getString()
                        // will do the conversion automatically.
                        size = cursor.getString(sizeIndex);
                    } else {
                        size = "Unknown";
                    }
                    Log.i(TAG, "Size: " + size);
                }
            }catch (Exception  e){

            } finally {
                cursor.close();
            }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
}