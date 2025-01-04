package de.vfh.paf.component.hello;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Component is a class-level annotation.
 * There are several specializations of the @Component annotation:
 * - @Service: business logic component
 * - @Controller / @RestController: component that can work in REST web services
 * - @Repository: interacts with an external data storage / database
 */
@Component
// @ConditionalOnProperty(value="deploy.province",havingValue = "beijing")
@Qualifier("HelloA")
@Primary
//@Scope(value="prototype", proxyMode= ScopedProxyMode.TARGET_CLASS)
//@Scope("request")
//@Scope(value=BeanDefinition.SCOPE_PROTOTYPE, proxyMode=ScopedProxyMode.TARGET_CLASS)
@NoArgsConstructor
@Getter
@Setter
@ToString
//@RequestScope
// @Scope("prototype")
@Slf4j
// @Scope("prototype")
public class HelloComponent implements IHelloComponent {

  private static Logger logger = LoggerFactory.getLogger(HelloComponent.class); // must tbe static (otherwise tests fail)

  private static Integer callCounter = 0;
  private Integer instanceId;
  private Integer instanceValue = 10;


//   public HelloComponent(Integer instanceValue) {
//    this.instanceValue = instanceValue;
//    System.out.println("HelloComponent constructor: instanceValue=" + instanceValue);
//  }

  @Override
  public String getHello() {
    String id = Integer.toHexString(System.identityHashCode(this));
    log.info("*** Lombok slf4f: getHello() called");
    logger.trace("**** import slf4j: getHello() called"); // ToDo: why is this not printed?
    return "Hello PaF 2024 from Component (callCounter:" + ++callCounter
      + ", instanceId:" + instanceId + ", instanceValue:" + instanceValue
      + ", " +  this.hashCode()  + ", " +  id + ")\n";
  }

  @Component
//  @Scope("request")
  @Scope("prototype")
//  @RequestScope
//  @Scope(value=BeanDefinition.SCOPE_PROTOTYPE, proxyMode=ScopedProxyMode.TARGET_CLASS)
//  @Scope(value="prototype", proxyMode= ScopedProxyMode.TARGET_CLASS)
  public static class HelloComponentClient1 {
    @Autowired
    @Getter
    HelloComponent helloComponent1;
    @Autowired
    @Getter
    HelloComponent helloComponent2;

    public void callComponent() {
      helloComponent1.setInstanceId(11);
      helloComponent2.setInstanceId(12);

      System.out.println("helloComponentClient1: " + helloComponent1.getHello());
      System.out.println("helloComponentClient2: " + helloComponent2.getHello());
    }

  }

  @Component
// @Scope("prototype")
  @Scope("singleton")
//  @Scope("request")
//  @RequestScope
//  @Scope(value=BeanDefinition.SCOPE_PROTOTYPE, proxyMode=ScopedProxyMode.TARGET_CLASS)
//  @Scope(value="prototype", proxyMode= ScopedProxyMode.TARGET_CLASS)
  public  static class HelloComponentClient2 {
    @Autowired
    @Getter
    HelloComponent helloComponent;

    public void callComponent() {
      System.out.println("helloComponentClient2: " + helloComponent.getHello());
    }

  }
}
