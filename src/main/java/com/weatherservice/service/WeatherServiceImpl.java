package com.weatherservice.service;

import static java.time.temporal.ChronoUnit.HOURS;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherservice.common.AuthenticationException;
import com.weatherservice.common.NotFoundException;
import com.weatherservice.common.RateLimitException;
import com.weatherservice.model.Weather;
import com.weatherservice.repository.WeatherRepository;

@Service
public class WeatherServiceImpl implements WeatherService { 

    private static final Logger logger = LogManager.getLogger(WeatherServiceImpl.class);

    private final WeatherRepository weatherRepository;
    
    @Value("${weather_api_key}")
    private String apiKey;
    
    @Value("${weather_api_uri}")
    private String apiUri;

    public WeatherServiceImpl(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    /** my method **/
    public Weather getWeatherDetails(String city,String country,LocalDateTime now) {
        logger.log(Level.INFO, "getWeatherDetails START");

        /** step 1 - check if db contains this data **/
        Weather weatherFromDb = weatherRepository.findById(city,country);       

        if(null != weatherFromDb) {
            /** step 1.1 - if found in db return the same data if it is within the hour**/
            logger.log(Level.INFO, "getWeatherDetails found some data in DB");

            if(now.truncatedTo(HOURS).isEqual(weatherFromDb.getCreatedDate().truncatedTo(HOURS))) {
                /** date being requested is within the hour so return the same **/
                logger.log(Level.INFO, "getWeatherDetails returning results from data found in db since request is within an hour");
                return weatherFromDb;
            }else {
                logger.log(Level.INFO, "getWeatherDetails found no data within the hour in DB calling API START");
                Weather weather = getWeatherDataFromAPI(city,country);
                logger.log(Level.INFO, "getWeatherDetails found no data within the hour in DB calling API END");
                if(null != weather) {
                    logger.log(Level.INFO, "getWeatherDetails updating data in DB START");
                                     
                    /** update data in db with data which was retrieved from API **/
                    weatherRepository.update(weather, city, country,now);
                    logger.log(Level.INFO, "getWeatherDetails updating data in DB END");
                }
                return weather;
            }
        }else {
            logger.log(Level.INFO, "getWeatherDetails NOT found any data in DB , calling API START");
            Weather weather = getWeatherDataFromAPI(city,country);
            logger.log(Level.INFO, "getWeatherDetails NOT found any data in DB , calling API END");
            
            if(null != weather) {
                logger.log(Level.INFO, "getWeatherDetails inserting data in DB START");
                /** insert data into db which was retrieved from API , here update will not work**/
                weatherRepository.save(weather, city, country,now);
                logger.log(Level.INFO, "getWeatherDetails inserting data in DB END");
            }
            return weather;           
        }
        

    }

    private Weather getWeatherDataFromAPI(String city,String country) {
        logger.log(Level.INFO, "getWeatherDataFromAPI START");
        String uri = apiUri+city+","+country+"&APPID="+apiKey;
        
        /** TODO : replace with WebClient **/
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = null;
        

        List <String> results= new ArrayList<String>();
        try {
            response = restTemplate.getForEntity(uri, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(response.getBody());

            if(null != actualObj) {
                JsonNode jsonNodeForWeather = actualObj.get("weather");
                if(null != jsonNodeForWeather) {
                    for(int i = 0;i<jsonNodeForWeather.size();i++) {
                        JsonNode descriptionNode = jsonNodeForWeather.get(i);
                        if(null != descriptionNode) {
                            String weatherDescription = descriptionNode.get("description").asText();
                            results.add(weatherDescription);
                        }
                    }
                }
            }
        }catch(Exception e) {
            logger.log(Level.ERROR, "Exception while retrieving weather details from API {}",e.getMessage());
            
            if(e.getMessage().contains("401")) {
                throw new AuthenticationException(e.getMessage());
            }else if (e.getMessage().contains("429")){
                throw new RateLimitException(e.getMessage());
            }else if (e.getMessage().contains("404")){
                throw new NotFoundException(e.getMessage());
            }else {
                throw new RuntimeException("unknown error");
            }            
        }
        Weather weather = new Weather();
        weather.setDescription(String.join(", ", results));
        
        logger.log(Level.INFO, "getWeatherDataFromAPI END");
        return weather;
    }
}
