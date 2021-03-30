package com.weatherservice.repository;




import java.time.LocalDateTime;

import com.weatherservice.model.Weather;

/**
 * Defines the persistence methods for a WeatherRepository.
 */
public interface WeatherRepository {
    /**
     * Returns the weather data for  specified city and country.
     *
     * @param city        city for which weather is retrieved.
     * @param country     country for which weather is retrieved.
     * @return          The requested Weather detail if found.
     */
    Weather findById(String city,String country);
    
    boolean update(Weather weather,String city,String country,LocalDateTime now);
    
    Weather save(Weather weather,String city,String country,LocalDateTime now);

    

    
}
