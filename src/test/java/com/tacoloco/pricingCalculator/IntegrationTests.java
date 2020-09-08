
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import com.tacoloco.model.*;

import org.junit.jupiter.api.Assertions;

import com.tacoloco.repository.PricingCalculatorRepository;

import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper; 

import com.tacoloco.services.JwtUserDetailsService;
import com.tacoloco.services.PricingCalculatorService;

import static org.hamcrest.Matchers.*;

import com.tacoloco.dao.UserDao;

import org.springframework.test.context.TestPropertySource;

import com.tacoloco.TestConfiguration;

import com.jayway.jsonpath.*;

import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

import org.springframework.http.HttpHeaders;

@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles(value="test")
@AutoConfigureMockMvc
class IntegrationTests {


  @Autowired
  private UserDao userDao;
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PricingCalculatorRepository repository;

  @Autowired
  JdbcTemplate jdbcTemplate;


  @Autowired
  private JwtUserDetailsService jwtService;

  @Autowired
  private PricingCalculatorService service;


  @Test
  @DisplayName("get /publicUserDetails valid after registering")
	public void getPublicDetailsForExistingUser() throws Exception{
      
      String mockJson = "{\"username\":\"SirSnoopy7\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";
      mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().is(201));

      String mockUserName = "SirSnoopy7";
      
      mockMvc.perform(get("/publicUserDetails/{username}", mockUserName)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

       UserDTO mockUserDTO = new UserDTO();
      mockUserDTO.setUsername("SirSnoopy");
      mockUserDTO.setFirstName("Joe");
      mockUserDTO.setLastName("Cool");

      ObjectMapper mapper = new ObjectMapper();

      String mockUserDTOJson = mapper.writeValueAsString(mockUserDTO);
    Assertions.assertTrue(jwtService.getPublicUserDetails("SirSnoopy").equals(mockUserDTOJson));
  }
  
  @Test
  @DisplayName("Get public info from invalid username")
  void getPublicInfoFromInvalidUsername() throws Exception{
    String mockUserName = "FakeName";

       
    mockMvc.perform(get("/publicUserDetails/{username}", mockUserName)
    .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().is(422))
    .andExpect(status().reason(containsString("User does not exist with the username provided")));
    


  }

  @Test
  @DisplayName("get /allPublicUserDetails valid")
	public void getPublicDetailsForAllExistingUsers() throws Exception{
      
      userDao.deleteAll();

      String mockJson = "{\"username\":\"SirSnoopy\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";
      mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().is(201));
      
      String mockJson2 = "{\"username\":\"MrSnoopy\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";
      mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson2))
        .andExpect(status().is(201));

      mockMvc.perform(get("/allPublicUserDetails")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username",is("SirSnoopy")))
                    .andExpect(jsonPath("$[1].username",is("MrSnoopy")))
                    .andExpect(jsonPath("$[0].firstName",is("Joe")))
                    .andExpect(jsonPath("$[1].firstName",is("Joe")))
                    .andExpect(jsonPath("$[0].lastName",is("Cool")))
                    .andExpect(jsonPath("$[1].lastName",is("Cool")))
                    .andExpect(jsonPath("$[0].password").doesNotExist())
                    .andExpect(jsonPath("$[1].password").doesNotExist())
                    .andExpect(jsonPath("$.length()",is(2)))
                    .andExpect(jsonPath("$[0].length()",is(4)))
                    .andExpect(jsonPath("$[1].length()",is(4)));

	}

  @Test
  @DisplayName("post /register valid user info")
	public void postRegisterCustomerValid() throws Exception{
      
      String mockJson = "{\"username\":\"SirSnoopy2\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";

      mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().is(201));

  }
  
  @Test
  @DisplayName("post /addNewSession valid session info")
	public void postAddNewSessionValid() throws Exception{
      
    String mockJson = "{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"A\",\"sessionMentor\":null,\"sessionMentee\":\"A\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask1\"}";    

      mockMvc.perform(post("/addNewSession")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().isOk());

  }

  
  @Test
  @DisplayName("post /addNewSession -- valid session info but wrong user")
	public void postAddNewSessionWrongUser() throws Exception{
      
    String mockJson = "{\"username\":\"SirSnoopy2\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";
    
    MvcResult mvcResult = mockMvc.perform(post("/authenticate")
    .contentType(MediaType.APPLICATION_JSON)
    .content(mockJson))
    .andExpect(status().isOk())
    .andReturn();
    
    //System.out.println("************" + JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.token"));
    String token = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.token");
    System.out.println("***********token" + token);
    String mockJson2 = "{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"A\",\"sessionMentor\":null,\"sessionMentee\":\"A\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask1\"}";    

      mockMvc.perform(post("/addNewSession")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson2)
                    .header(HttpHeaders.AUTHORIZATION, token)) 
        .andExpect(status().is(403));

  }

  @Test
  @DisplayName("post /overwriteSession valid session info")
	public void postOverwriteSessionValid() throws Exception{
      
    String mockJson = "[{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"SnoopyJr\",\"sessionMentor\":null,\"sessionMentee\":\"A\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask5\"}]";    

      mockMvc.perform(post("/overwriteSession")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().isOk());

      String mockUsername = "SnoopyJr";

    MvcResult result = mockMvc.perform(get("/sessionFromUsername/{username}", mockUsername))
                        .andExpect(status().isOk())
                        .andReturn();
       
    Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$[0].id");

    String mockJson2 = "[{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"SnoopyJr\",\"sessionMentor\":\"B\",\"sessionMentee\":\"A\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask5\", \"id\":" + id + "}]";    

    mockMvc.perform(post("/overwriteSession")
    .contentType(MediaType.APPLICATION_JSON)
    .content(mockJson2))
    .andExpect(status().isOk());


    mockMvc.perform(get("/sessionFromUsername/{username}", mockUsername)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sessionCreator",is("SnoopyJr")))
                    .andExpect(jsonPath("$[0].sessionMentor", is("B")))
                    .andExpect(jsonPath("$.length()", is(1)))
                    .andExpect(jsonPath("$[0].length()", is(12)));
  }

  @Test
  @DisplayName("post /addNewSession twice with the same storyId info")
	public void postAddNewSessionSameStoryId() throws Exception{
      
    String mockJson = "{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"A\",\"sessionMentor\":null,\"sessionMentee\":\"A\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask2\"}";    

      mockMvc.perform(post("/addNewSession")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().isOk());

      mockMvc.perform(post("/addNewSession")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mockJson))
        .andExpect(status().is(422));

  }

  @Test
  @DisplayName("post /addNewSession with bad time format")
	public void postAddNewSessionBadTimeFormat() throws Exception{
      
    String mockJson = "{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (E)\",\"sessionCreator\":\"A\",\"sessionMentor\":null,\"sessionMentee\":\"A\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask3\"}";    

      mockMvc.perform(post("/addNewSession")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mockJson))
        .andExpect(status().is(422));


  }

  @Test
  @DisplayName("get /sessionFromUsername valid after adding the session")
	public void getSessionFromUsernameValid() throws Exception{
      
    String mockJson = "{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"SirSnoopy\",\"sessionMentor\":null,\"sessionMentee\":\"SirSnoopy\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask3\"}";    
      String mockUsername = "SirSnoopy";
      mockMvc.perform(post("/addNewSession")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(mockJson))
      .andExpect(status().isOk());

      String mockJson2 = "{\"time\":\"Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)\",\"sessionCreator\":\"MrSnoopy1\",\"sessionMentor\":null,\"sessionMentee\":\"SirSnoopy\",\"sessionAction\":\"Debug code for\",\"sessionSubjectMatter\":\"Java\",\"sessionMentorRating\":null,\"sessionMenteeRating\":null,\"sessionMentorComments\":null,\"sessionMenteeComments\":null,\"sessionStoryId\":\"SomeTask33\"}";    
      mockMvc.perform(post("/addNewSession")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(mockJson2))
      .andExpect(status().isOk());

  
      mockMvc.perform(get("/sessionFromUsername/{username}", mockUsername)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sessionCreator",is("SirSnoopy")))
                    .andExpect(jsonPath("$[0].sessionSubjectMatter", is("Java")))
                    .andExpect(jsonPath("$[0].sessionStoryId", is("SomeTask3")))
                    .andExpect(jsonPath("$[0].sessionAction", is("Debug code for")))
                    .andExpect(jsonPath("$.length()", is(2)))
                    .andExpect(jsonPath("$[0].length()", is(12)))
                    .andExpect(jsonPath("$[0].time", is("Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)")))
                    .andExpect(jsonPath("$[1].sessionCreator",is("MrSnoopy1")))
                    .andExpect(jsonPath("$[1].sessionSubjectMatter", is("Java")))
                    .andExpect(jsonPath("$[1].sessionStoryId", is("SomeTask33")))
                    .andExpect(jsonPath("$[1].sessionAction", is("Debug code for")))
                    .andExpect(jsonPath("$[1].length()", is(12)))
                    .andExpect(jsonPath("$[1].time", is("Thu Aug 20 2020 13:08:59 GMT-0400 (EDT)")));

	}

  
  @Test
  @DisplayName("try to post /register same user twice")
	public void postRegisterCustomerTwiceInvalid() throws Exception{
      
      String mockJson = "{\"username\":\"SirSnoopy3\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";

      mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().is(201));


      mockMvc.perform(post("/register")
      .contentType(MediaType.APPLICATION_JSON)
      .content(mockJson))
    .andExpect(status().is(422));

	}

    @Test
  @DisplayName("post /register invalid user info--nonmatching password and returns an exception from post /validate the user")
	public void postRegisterCustomerInvalid() throws Exception{
      
      String mockJson = "{\"username\":\"SirSnoopy\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"fake1\", \"matchingPassword\": \"fake2\"}";
      
      mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().is(422));

      mockMvc.perform(post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().is(401));
	}
  

  @Test
  @DisplayName("post /authenticate valid user info")
	public void postAuthenticateUserValid() throws Exception{


   String mockJson = "{\"username\":\"SirSnoopy\", \"firstName\":\"Joe\", \"lastName\": \"Cool\", \"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";

   //try catch loop in order to prevent multiple registrations of the same user (leads to ambiguous records when searching by username) -- alterantive is I just register another account
   //we can probably change this test by building in restriction from adding the same user twice as well
    try{
         mockMvc.perform(post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().isOk());

    }catch(Exception e){
         mockMvc.perform(post("/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(mockJson))
          .andExpect(status().isOk());

        mockMvc.perform(post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mockJson))
        .andExpect(status().isOk());
    }
  

   
	}

  @Test
  @DisplayName("put /insertCustomer valid user info")
	public void putInsertCustomerValid() throws Exception {
    
    jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
    jdbcTemplate.execute("CREATE TABLE customers(" +
        "id SERIAL, username VARCHAR(255), first_name VARCHAR(255), last_name VARCHAR(255), encoded_password VARCHAR(255))");

    String mockJson = "{\"username\": \"SirSnoopy\", \"firstName\":\"Joe\", \"lastName\": \"Cool\",\"password\": \"SnoopDoDubbaG\", \"matchingPassword\": \"SnoopDoDubbaG\"}";
    
    mockMvc.perform(put("/insertCustomer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(mockJson))
      .andExpect(status().isOk());
    

    String query = "SELECT * FROM customers";
    
    List<Customer> customerlist = new ArrayList<Customer>();
    
    customerlist = jdbcTemplate.query(
        query,
        (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("username"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("encoded_password"))
    );
    
    Assertions.assertTrue(customerlist.size()==1);

    Assertions.assertTrue(customerlist.get(0).getId()==1);
    
    Assertions.assertTrue(customerlist.get(0).getUsername().equals("SirSnoopy"));
    
    Assertions.assertTrue(customerlist.get(0).getFirstName().equals("Joe"));

    Assertions.assertTrue(customerlist.get(0).getLastName().equals("Cool"));

    Assertions.assertTrue(!customerlist.get(0).getEncodedPassword().isEmpty());

	}

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

  @Test
  @DisplayName("get /total/ valid order with discount")
	public void getTotalValidWithDiscount() throws Exception {
      
      String json = "{\"veggie\":1, \"beef\":2, \"chorizo\":1 }";
      
      mockMvc.perform(get("/total")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("9.60")));

	}

  @Test
  @DisplayName("get /total/ invalid")
	public void getTotalInvalid() throws Exception {
      
      String json = "{\"Burger\":1, \"veggie\":1, \"beef\":2, \"chorizo\":1 }";
      
      mockMvc.perform(get("/total")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
        .andExpect(status().is(422))
        .andExpect(status().reason(containsString("No such item or counts not all whole numbers")))
        .andExpect(jsonPath("$.body").doesNotExist());

	}

  @Test
  @DisplayName("get /total/ negative invalid")
	public void getTotalNegativeNumber() throws Exception {
      
      String json = "{\"veggie\":-1, \"beef\":2, \"chorizo\":1 }";
      
      mockMvc.perform(get("/total")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
        .andExpect(status().is(422))
        .andExpect(status().reason(containsString("No such item or counts not all whole numbers")))
        .andExpect(jsonPath("$.body").doesNotExist());

	}

  @Test
  @DisplayName("get /total/ invalid 400")
	public void getTotalInvalid400() throws Exception {
      
      String json = "{";
      
      mockMvc.perform(get("/total")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
        .andExpect(status().is(400))
        .andExpect(jsonPath("$.body").doesNotExist());

	}
  
  
}
