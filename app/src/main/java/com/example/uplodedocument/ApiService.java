package com.example.uplodedocument;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("api/PatientDocument/InsertPatientDocument_Temp")
    Call<DummyClass> insertDocement(@Part MultipartBody.Part file,
                                    @Part("Doc_Type_Id_FK") RequestBody doc_Type_Id_FK,
                                    @Part("PR_Id_FK") RequestBody PR_Id_FK);
//                                    @Part("Doc_Type_Id_FK") RequestBody docType,
//                                    @Part("documentname") RequestBody docName,
//                                    @Part("Choose_Document") RequestBody description);
}
