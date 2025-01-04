package de.vfh.paf.component.hello;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Test of Spring Component with Hello Component
 */
@Component
@Slf4j
public class HelloComponentUsage {

  @Getter
  private HelloComponent helloComponentConstrInj;
  @Getter
  private HelloComponent helloComponentSetterInj;

  @Autowired
  @Getter
  @Qualifier("HelloA")
  private IHelloComponent helloComponentFieldInj;

  @Autowired
  public HelloComponentUsage(HelloComponent helloComponentConstrInj) {
    // Perform some initialization logic with the injected dependencies
    log.info("HelloComponentUsage initialized with HelloComponent: initialize");
    helloComponentConstrInj.setInstanceValue(20);
    this.helloComponentConstrInj = helloComponentConstrInj;
  }

  @Autowired
  public void setHelloComponentSetterInj(HelloComponent helloComponentSetterInj) {
    log.info("HelloComponentUsage initialized with HelloComponent: setter");
    helloComponentSetterInj.setInstanceValue(30);
    this.helloComponentSetterInj = helloComponentSetterInj;
  }
}
