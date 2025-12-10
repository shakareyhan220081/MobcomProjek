//package com.example.mobcomprojek.data.api
//
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//interface WeatherApiService {
//    // Contoh call: https://api.open-meteo.com/v1/forecast?latitude=-6.2&longitude=106.8&current_weather=true
//    @GET("v1/forecast")
//    suspend fun getCurrentWeather(
//        @Query("latitude") lat: Double,
//        @Query("longitude") lon: Double,
//        @Query("current_weather") current: Boolean = true
//    ): WeatherResponse
//}