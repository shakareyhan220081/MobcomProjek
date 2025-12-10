//package com.example.mobcomprojek.data.api
//
//import android.util.Log
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object WeatherRepository {
//    private const val BASE_URL = "https://api.open-meteo.com/"
//
//    private val retrofit = Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    private val apiService = retrofit.create(WeatherApiService::class.java)
//
//    // Simpan data cuaca terakhir di memori
//    var currentTemp: Double? = null
//    var currentWeatherCode: Int? = null
//
//    suspend fun fetchWeather(lat: Double, lon: Double) {
//        try {
//            val response = apiService.getCurrentWeather(lat, lon)
//            currentTemp = response.currentWeather.temperature
//            currentWeatherCode = response.currentWeather.weatherCode
//            Log.d("WeatherRepo", "Success: $currentTemp C, Code: $currentWeatherCode")
//        } catch (e: Exception) {
//            Log.e("WeatherRepo", "Failed to fetch weather", e)
//        }
//    }
//
//    // Helper untuk mengubah Kode Cuaca (WMO Code) menjadi Ikon/Teks
//    fun getWeatherIcon(code: Int?): String {
//        return when (code) {
//            0 -> "☀️" // Clear sky
//            1, 2, 3 -> "⛅" // Partly cloudy
//            45, 48 -> "🌫️" // Fog
//            51, 53, 55 -> "🌦️" // Drizzle
//            61, 63, 65 -> "🌧️" // Rain
//            71, 73, 75 -> "❄️" // Snow
//            95, 96, 99 -> "⛈️" // Thunderstorm
//            else -> "❓"
//        }
//    }
//}