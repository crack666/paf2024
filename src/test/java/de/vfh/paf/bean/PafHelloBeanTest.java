package de.vfh.paf.bean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of PafHelloBean
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties="spring.main.lazy-initialization=true",
        classes=PafHelloBean.class)
//@ComponentScan(basePackages = {"de.vfh.paf.bean"})
//@ExtendWith(MockitoExtension.class)
// @TestPropertySource(properties = "project-name=PaF 2024 (Test)")
class PafHelloBeanTest {

  @Autowired
  private Environment env;

  @Mock // Bean(answer = CALLS_REAL_METHODS) // not working
  PafHelloBean paFHelloBeanMockBean;

  @Autowired
  private String getProjectName;

  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void getHello() {
    // give
    PafHelloBean paFHelloBean = new PafHelloBean();
    ApplicationContext context = new AnnotationConfigApplicationContext(PafHelloBean.class);
    // when
    String respManual = paFHelloBean.getHello();
    String respContext = context.getBean("getHello", String.class);
    // then
    assertTrue(respManual.contains("Hello PaF 2024 from Bean"));
    assertTrue(respContext.contains("Hello PaF 2024 from Bean"));
  }

  @Test
  void getHelloMockBean() {
    // given
    Mockito.when(paFHelloBeanMockBean.getHello()).thenReturn("Hello PaF 2024 from Bean");
    // when
    String resp = paFHelloBeanMockBean.getHello();
    // then
    assertTrue(resp.contains("Hello PaF 2024 from Bean"));
  }

  @Test
  void testGetProjectName() {
    String projectName = env.getProperty("project-name");
    System.out.println("Project Name: " + projectName);
    assertTrue(projectName.contains("PaF 2024 (Test)"));
  }

  @Test
  void getProjectName() {
    // given
    ApplicationContext context = new AnnotationConfigApplicationContext(PafHelloBean.class);
    String respContext = null;
    // when
    try { // ToDo this does not work
      // respContext = context.getBean("getProjectName", String.class);
    } catch (BeansException e) {
      e.printStackTrace();
      // fail();
    }
    // then
    // assertTrue(respContext.contains("PaF 2024 (Test)"));
    assertTrue(getProjectName.contains("PaF 2024 (Test)"));
  }
}
