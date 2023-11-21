package com.example.uplodedocument;

import com.google.gson.annotations.SerializedName;

public class GetDocumentTypeResponceList {
    @SerializedName("doctype_id")
    private Integer doctypeId;
    @SerializedName("doctype_name")
    private String doctypeName;

    public Integer getDoctypeId() {
        return doctypeId;
    }

    public void setDoctypeId(Integer doctypeId) {
        this.doctypeId = doctypeId;
    }

    public String getDoctypeName() {
        return doctypeName;
    }

    public void setDoctypeName(String doctypeName) {
        this.doctypeName = doctypeName;
    }

    public String toString() {
        return doctypeName;
    }
}
