package uk.gov.homeoffice.digital.sas.jparest.it;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.homeoffice.digital.sas.demo.EntitiesApplication;

import javax.servlet.ServletContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=EntitiesApplication.class)
@WebAppConfiguration
class SessionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void given_servletContextIsStarted_thenItProvidesSessionsController() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("sessionsController"));
    }

    @Test
    public void giveSessionIdExists_WhenGetOperationForSessionIdCalled_thenVerifyResponse() throws Exception {
        String jsonContent = "{\"session_id\":1,\"session_name\":\"Keynote - The Golden Age of Software\",\"session_description\":\"\",\"session_length\":45}";
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/sessions/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(jsonContent))
                .andReturn();
    }

    @Test
    public void giveSessionsExists_WhenGetOperationForSessionsCalled_thenVerifyResponse() throws Exception {
        String jsonContent = "{\"session_id\":1,\"session_name\":\"Keynote - The Golden Age of Software\",\"session_description\":\"\",\"session_length\":45}";
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/sessions"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        System.out.println(result);
        /*final JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.getJSONArray("");
        final int n = array.length();
        System.out.println(n);*/
    }


    @Test
    public void giveArtistIdExists_WhenGetOperationForArtistIdCalled_thenVerifyResponse() throws Exception {
        String jsonContent = "{\n" +
                "    \"meta\": {\n" +
                "        \"next\": null\n" +
                "    },\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"artist_id\": 1,\n" +
                "            \"profile_id\": 1,\n" +
                "            \"performance_name\": \"Beautiful South\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/resources/artists/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(jsonContent))
                .andReturn();
    }

    @Test
    public void givenArtistsExists_WhenGetOperationForArtistCalled_thenVerifyResponse() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/resources/artists"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        final JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.getJSONArray("items");
        final int count = array.length();
        Assertions.assertEquals(count,6);
    }

    @Test
    public void givenArtistsExists_WhenGetOperationWithFilterIdGreaterthan3_thenVerifyResponse() throws Exception {
        String uri = "/resources/artists?filter=artist_id>3";
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(uri))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        final JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.getJSONArray("items");
        final int count = array.length();
        Assertions.assertEquals(count,3);
    }

    @Test
    public void givenArtistsExists_WhenGetOperationWithFilterIdLessthan5_thenVerifyResponse() throws Exception {
        String uri = "/resources/artists?filter=artist_id<5";
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(uri))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        final JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.getJSONArray("items");
        final int count = array.length();
        Assertions.assertEquals(count,4);
    }

    @Test
    public void givenArtistsExists_WhenGetOperationWithFilterIdEqualTo5_thenVerifyResponse() throws Exception {
        String uri = "/resources/artists?filter=artist_id==5";
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(uri))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        final JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.getJSONArray("items");
        final int count = array.length();
        Assertions.assertEquals(count,1);
    }


}