package com.weatherservice.common;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConstantsIfc{
    static final Map<String,List<LocalDateTime>> rateLimitCount = new HashMap();
    
    
}