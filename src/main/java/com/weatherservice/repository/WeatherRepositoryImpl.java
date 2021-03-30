package com.weatherservice.repository;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;


import com.weatherservice.model.Weather;

@Repository
public class WeatherRepositoryImpl implements WeatherRepository {

    private static final Logger logger = LogManager.getLogger(WeatherRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WeatherRepositoryImpl(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;

        // Build a SimpleJdbcInsert object from the specified data source
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("weather_data")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Weather findById(String city,String country) {
        try {
            Weather weather = jdbcTemplate.queryForObject("SELECT * FROM weather_data WHERE city = ? and country = ?",
                    new Object[]{city,country},
                    (rs, rowNum) -> {
                        Weather w = new Weather();
                        w.setId(rs.getInt("id"));
                        w.setDetails(rs.getString("weather_details"));
                        //w.setCreatedDate(rs.getDate("created_date").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                        w.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
                        
                        return w;
                    });
            return weather;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    @Override
    public boolean update(Weather weather,String city,String country,LocalDateTime now) {
        return jdbcTemplate.update("UPDATE weather_data SET weather_details = ? , created_date = ? WHERE country = ? AND city = ?",
            weather.getDescription(),
            Timestamp.valueOf(now),
            country,
            city) == 1;
    }
    
    @Override
    public Weather save(Weather weather,String city,String country,LocalDateTime now) {
        // Build the Weather parameters we want to save
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("country", country);
        parameters.put("city", city);
        parameters.put("weather_details", weather.getDescription());
        parameters.put("created_date", Timestamp.valueOf(now));
        parameters.put("version", 1);
        


        // Execute the query and get the generated key
        Number newId = simpleJdbcInsert.executeAndReturnKey(parameters);

        logger.info("Inserting weather details into database, generated key is: {}", newId);

        // Update the weather ID with the new key
        weather.setId((Integer)newId);

        // Return  inserted weather record
        return weather;
    }

    

    

    

   
}
