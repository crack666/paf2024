package de.vfh.paf.api;

import de.vfh.paf.Paf2024Application;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for pafTestAPI
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.MOCK,
  classes = Paf2024Application.class)
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application-integrationtest.yml")
class PafHelloRestControllerIntegrationTestWithProps {
  @Autowired
  private MockMvc mvc;

  @Test
  public void testPafHello() throws Exception {
    // given
    String respStringExpected = "Hello PaF controller";
    // when
    mvc.perform(get("/hello/paf2024")
        .contentType(MediaType.TEXT_PLAIN))
      .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
      .andExpect(status().isOk())
//      .andExpect(jsonPath("$.message").value("Hello World!!!"))
      .andExpect(result ->
        // then
        assertTrue(result.getResponse().getContentAsString().contains(respStringExpected)));
  }
}
