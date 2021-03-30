package com.weatherservice.web;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherservice.model.Weather;
import com.weatherservice.service.WeatherService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {
	@MockBean
	private WeatherService service;

	@Autowired
	private MockMvc mockMvc;



	@Test
	@DisplayName("GET /api/v1/weather - Found and Validated response")
	void testGetWeather() throws Exception {
		// Setup our mocked service
		Weather mockWeather = new Weather();
		mockWeather.setDescription("cloudy");
		
		doReturn(mockWeather).when(service).getWeatherDetails("city","country",LocalDateTime.now());
		
		mockMvc.perform(get("/api/v1/weather").header("API_KEY", "K3").param("city", "city").param("country","country"))

		// Validate the response code and content type
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
		
	}
	/**

	@Test
	@DisplayName("GET /api/v1/weather - 429 response")
	void testGetWeather429() throws Exception {
		// Setup our mocked service
		RateLimitException  rte = new RateLimitException("too many requests");
		doThrow(rte).when(service).getWeatherDetails("a","b");


		mockMvc.perform(get("/api/v1/weather"))

		// Validate the response code and content type
		.andExpect(status().is(429));

	}
**/



	static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
