package com.weatherservice.web;

import static java.time.temporal.ChronoUnit.HOURS;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherservice.common.AuthenticationException;
import com.weatherservice.common.ConstantsIfc;
import com.weatherservice.common.NotFoundException;
import com.weatherservice.common.RateLimitException;
import com.weatherservice.service.WeatherService;

@RestController
public class WeatherController {

    private static final Logger logger = LogManager.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    
    @Value("#{'${api.keys}'.split(',')}")
    private List<String> apiKeyList;

    @Value("${max_number_of_requests_per_api_key}")
    private int alloweNoOfRequests;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }	
    @CrossOrigin   
    @GetMapping("/api/v1/weather")
    public ResponseEntity<?> getWeather(@RequestHeader("API_KEY") String clientId,@RequestParam String city,@RequestParam String country) {
        try {
            logger.log(Level.INFO, "clientId list is {} and api key is {}", apiKeyList,clientId);

            /** validate API_KEY is not null or empty and is valid **/
            if(!isApiKeyValid(clientId)) {
                logger.log(Level.ERROR, "Invalid credentials");
                /** invalid API KEY throw a 401 error **/
                return ResponseEntity.status(401).build();
            }
            
            if(areRequestParamsNullOrEmpty(city,country)) {
                return ResponseEntity.status(400).build();
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            if(isRateLimitExceeded(clientId,now)) {
                logger.log(Level.ERROR, "Exceeded {} attempts allowed within the {}",alloweNoOfRequests,"hour");
                /** invalid API KEY throw a 401 error **/
                return ResponseEntity.status(429).build();
            }
            
            /** all good till here go get details **/
            //return ResponseEntity.status(200).body(weatherService.getWeatherDetails(city,country,now));
            
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type",MediaType.APPLICATION_JSON_UTF8_VALUE); 
              

            return ResponseEntity.status(200)
              .headers(responseHeaders)
              .body(weatherService.getWeatherDetails(city,country,now));

        }catch(RateLimitException rte) {
            logger.log(Level.ERROR, "Exceeded allowed attempts ");
            return ResponseEntity.status(429).build();
        }catch(AuthenticationException aue) {
            logger.log(Level.ERROR, "Invalid credentials ");
            return ResponseEntity.status(401).build();
        }catch(NotFoundException nfe) {
            logger.log(Level.ERROR, "No data found ");
            return ResponseEntity.status(404).build();
        }catch(Exception e) {
            logger.log(Level.ERROR, "System Exception contact administrator ");
            return ResponseEntity.status(500).build();
        }


    }

    private boolean isApiKeyValid(String apiKey) {
        return apiKeyList.contains(apiKey);
    }
    
    private boolean areRequestParamsNullOrEmpty(String city,String country) {
        if ((null == city || city.trim().length() <=0 )  || (null == country || country.trim().length() <=0 )) {
            logger.log(Level.ERROR, "Query params city : {}  and or country: {} are null or empty ",city,country);
            return true;
        }else {
            logger.log(Level.INFO, "Query params city : {}  and  country: {} are valid ",city,country);
            return false;
        }
            
        
    }

    private boolean isRateLimitExceeded(String apiKey,LocalDateTime now) {
        List<LocalDateTime> keyUsageCount =  ConstantsIfc.rateLimitCount.get(apiKey);

        if(null != keyUsageCount  && keyUsageCount.size() > 0 ) {
            /** this means the key has been used previously 
             *  lets check if we are exceeding the number of times allowed 
             *  
             */
            logger.log(Level.INFO, "this api key has been previously used  {} times", keyUsageCount.size());
            
            if(keyUsageCount.size() == alloweNoOfRequests && now.truncatedTo(HOURS).isEqual(keyUsageCount.get(0).truncatedTo(HOURS))) {
                /** this means we are exceeding max allowed attempts within the hour **/
                logger.log(Level.ERROR, "api key usage exceeded within the hour,max retries  {} exceeded", alloweNoOfRequests);
                return true;
            }else {
                /** rate limit not exceeded within the hour for specified key 
                 *  lets add time of current attempt to List if this attempt is within the hour
                 *  if this attempt is outside of the hour for which we have attempts then lets clear the old values and reset the count 
                 */
                if(now.truncatedTo(HOURS).isEqual(keyUsageCount.get(0).truncatedTo(HOURS))){
                    /** so this latest attempt is within the hour of other attempts , so simply add this attempt to the list **/
                    logger.log(Level.INFO, "this api key has been used  {} times now within the hour", keyUsageCount.size()+1);
                    keyUsageCount.add(now);
                    ConstantsIfc.rateLimitCount.put(apiKey, keyUsageCount);
                }else {
                    /** this new attempt is outside of the hour of previous attempts , so simply clear entries for previous attempts and then add this new entry **/
                    logger.log(Level.INFO, "this api key is being used for the very first time within the hour !");
                    keyUsageCount.clear();
                    keyUsageCount.add(now);
                    ConstantsIfc.rateLimitCount.put(apiKey, keyUsageCount);
                }
                return false;
            }

        }else {
            /** no request as yet made for this api key so we are good 
             *  simply store the time of the call against this key 
             **/
            logger.log(Level.INFO, "this api key was never used , first time usage !");
            
            keyUsageCount = new ArrayList<LocalDateTime>();
            keyUsageCount.add(now);
            ConstantsIfc.rateLimitCount.put(apiKey, keyUsageCount);
            return false;
        }        
    }
}
