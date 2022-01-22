package com.bcebhagalpur.cheashu.remote

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getClient(baseUrl: String): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}