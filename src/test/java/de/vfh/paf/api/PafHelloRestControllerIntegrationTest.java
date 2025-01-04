package de.vfh.paf.api;

import de.vfh.paf.component.hello.HelloComponent;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Test for PafHelloAPI TEST Controller (integration test)
 *
 * @see <a href="https://www.baeldung.com/spring-boot-testing"></a>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(PafHelloRestController.class)
@Import(PafHelloRestController.class) // This should not be necessary
@ContextConfiguration(classes = {HelloComponent.class} )
class PafHelloRestControllerIntegrationTest {

//  @MockBean
  @Autowired
  private HelloComponent paFHelloComponent;

  @Autowired
  private MockMvc mvc;

  @Test
  public void testPafHello() throws Exception {
    // given
    String respStringExpected = "Hello PaF controller";
    // when
    mvc.perform(get("/hello/paf")
        .contentType(MediaType.TEXT_PLAIN))
        .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
        .andExpect(status().isOk())
//      .andExpect(jsonPath("$.message").value("Hello World!!!"))
      .andExpect(result ->
    // then
          assertTrue(result.getResponse().getContentAsString().contains(respStringExpected)));
  }

  @Test
  public void testPafComponentHello() throws Exception {
    // given
    String respStringExpected = "Hello PaF 2024 from Component";
    // when
    mvc.perform(get("/hello/component/hello")
        .contentType(MediaType.TEXT_PLAIN))
      .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
      .andExpect(status().isOk())
//      .andExpect(jsonPath("$.message").value("Hello World!!!"))
      .andExpect(result ->
        // then
        assertTrue(result.getResponse().getContentAsString().contains(respStringExpected)));
  }

}
