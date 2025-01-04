package de.vfh.paf.api;

import de.vfh.paf.component.hello.HelloComponent;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for PafHelloAPI TEST Controller (integration test)
 *
 * @see <a href="https://www.baeldung.com/spring-boot-testing"></a>
 * https://docs.spring.io/spring-boot/docs/1.5.2.RELEASE/reference/html/boot-features-testing.html
 * https://reflectoring.io/spring-boot-web-controller-test/
 */
// Alternative 1: WebMvcTest
@RunWith(SpringRunner.class)
@WebMvcTest(PafHelloRestController.class)
//@ExtendWith(SpringExtension.class)
//@Import(HelloRestController.class) // This should not be necessary
@ContextConfiguration(classes = {PafHelloRestController.class} )

// Alternative 2: SpringBootTest mit AutoConfigureMockMvc
//@SpringBootTest
//@AutoConfigureMockMvc

//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest(PafHelloRestController.class)
//@Import(PafHelloRestController.class) // This should not be necessary
//@ContextConfiguration(classes = {PafHelloRestController.class} )
class PafHelloRestControllerUnitTest {

//  @MockBean
  @MockBean
  private HelloComponent pafHelloComponent;

  @Autowired
  private MockMvc mvc;

  @Test
  public void testPafHello() throws Exception {
    // given
    String respStringExpected = "Hello Mock PaF 2024";
    Mockito.when(pafHelloComponent.getHello()).thenReturn("Hello Mock PaF 2024");

    // when
    mvc.perform(get("/hello/component/hello")
        .accept(MediaType.APPLICATION_JSON) // ToDo No error?
        .contentType(MediaType.TEXT_PLAIN))
        .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
       .andExpect(status().isOk())
//      .andExpect(jsonPath("$.message").value("Hello World!!!"))
      .andExpect(result ->
    // then
          assertTrue(result.getResponse().getContentAsString().equals(respStringExpected)));
  }


  @Test
  public void testPafHelloWithJson() throws Exception {
    // given
    String respStringExpected = "Hello Mock PaF 2024";
    Mockito.when(pafHelloComponent.getHello()).thenReturn("Hello Mock PaF 2024");

    // when
    mvc.perform(get("/hello/component/hello")
        .accept(MediaType.APPLICATION_JSON) // ToDo No error?
        .contentType(MediaType.TEXT_PLAIN))
      .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
      .andExpect(status().isOk())
//      .andExpect(jsonPath("$.message").value("Hello World!!!"))
      .andExpect(result ->
        // then
        assertTrue(result.getResponse().getContentAsString().equals(respStringExpected)));
  }

}
