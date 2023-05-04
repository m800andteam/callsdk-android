package com.m800.sdk.core

import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface InternalService {
    @POST("/v1/notify/{eId}")
    fun sendNotification(@Path("eId") eId: String, @Body body: RequestBody): Deferred<ResponseBody>
}