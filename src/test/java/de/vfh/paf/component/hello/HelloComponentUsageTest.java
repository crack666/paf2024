package de.vfh.paf.component.hello;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Log
class HelloComponentUsageTest {

  @Autowired
  HelloComponentUsage helloComponentUsage;

  @Test
  void fieldInjection() {
    String hello = helloComponentUsage.getHelloComponentFieldInj().getHello();
    log.info("*** HelloComponentUsageTest: " + hello);
  }

  @Test
  void constructorInjection() {
    String hello = helloComponentUsage.getHelloComponentConstrInj().getHello();
    log.info("*** HelloComponentUsageTest: " + hello);
  }

  @Test
  void setterInjection() {
    String hello = helloComponentUsage.getHelloComponentSetterInj().getHello();
    log.info("*** HelloComponentUsageTest: " + hello);
  }

  @Test
  void testConfig() {
    ApplicationContext context = new AnnotationConfigApplicationContext(HelloComponentUsageConfig.class);
    HelloComponent helloComponent = context.getBean("helloComponentConfig", HelloComponent.class);
    log.info(helloComponentUsage.getHelloComponentFieldInj().getHello());
  }

  @Test
  void getHelloComponent2() {
  }

  @Test
  void getHelloComponent3() {
    log.info(helloComponentUsage.getHelloComponentFieldInj().getHello());
  }
}
