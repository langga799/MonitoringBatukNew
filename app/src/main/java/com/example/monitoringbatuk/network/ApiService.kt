package com.example.monitoringbatuk.network

import com.example.monitoringbatuk.helper.Constant
import com.example.monitoringbatuk.model.RawResultResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    //    @GET("v1/api/83092/raw-data/79304402")
//    fun getRawResult(
//        @Header("x-api-key") apiKey: String,
//        @Header("Accept") json: String
//    ): Call<RawResultResponse>
//
    @GET("v1/api/82128/raw-data/80571655?limitPayloadValues=10000")
    fun getRawResult(
        @Header("x-api-key") apiKey: String,
        @Header("Accept") json: String,
    ): Call<RawResultResponse>


    @Headers(
        "x-api-key: ${Constant.API_KEY}",
        "Content-Type: application/json",
        "x-file-name: tes.wav"
    )

    @FormUrlEncoded
    @POST("https://ingestion.edgeimpulse.com/api/training/data")
    fun uploadSample(
        @Field("attachments") type: String = "application/json",
        @Field("attachments") file: String = "tes.wav",
    ): Call<ResponseBody>


}