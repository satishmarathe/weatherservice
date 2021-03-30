package com.weatherservice.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class Weather {

    private String details = null;
    private int id = 0;
    private LocalDateTime createdDate = null;
    
    private String description;
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @JsonIgnore
    public int getId() {
        return id;
    }
   
    public void setId(int id) {
        this.id = id;
    }
    
    @JsonIgnore
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    @JsonIgnore
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }

}
