# Weather_SpringBoot_API
This API is developed using Spring boot .


# Software versions required :
Maven 3.2.5+
JDK 1.8+
Spring Boot
Mockito

# To run this example:
First compile run unit tests package install  :
mvn clean install

To start rest service:
mvn spring-boot:run


This will start embedded tomcat on port : 9090
The API endpoint can then be accessed at : http://localhost:9090/api/v1/weather?city=London&country=uk

If this port is already in use please change the port in application.properties 

NOTE : Valid api keys need to be passed as a header attribute: API_KEY
Valid values are : K1,K2,K3,K4,K5


# Rate Limit Implementation
This API is expected to respond with a 429 ( Throttled / Rate Limit response ) 
The condition on when this response is to be generated in this contrived example is as follows :
We allow 5 requests to succeed within an hour and  subsequent request will be served this 429 error response.
Then the counter will be reset to 0 to allow next subsequent 5 requests to succeed and so on ...
This is obviously a contrived and rather simple basic implementation.

# Notes on API Key
Please substitute your own api key in properties file

