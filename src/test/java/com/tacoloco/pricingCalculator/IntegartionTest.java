package com.tacoloco.pricingCalculator;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("get /total/ valid order no discount")
	public void getTotalValid() throws Exception {
      
      String json = "{\"veggie\":1, \"beef\":2}";
      
      mockMvc.perform(get("/total")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("8.50")));

	}
  
}
