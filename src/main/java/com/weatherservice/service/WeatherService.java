package com.weatherservice.service;


import java.time.LocalDateTime;

import com.weatherservice.model.Weather;

public interface WeatherService {    
    
    /** my method **/
    Weather getWeatherDetails(String city,String country,LocalDateTime now) ;

    
}
