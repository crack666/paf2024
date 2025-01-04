package de.vfh.paf.config;

import de.vfh.paf.bean.PafHelloBean;
import de.vfh.paf.component.geometry.GeometricShape;
import de.vfh.paf.component.hello.HelloComponent;
import de.vfh.paf.plain.di.IProductPersistence;
import de.vfh.paf.plain.di.ProductMan;
import de.vfh.paf.plain.factory.Phone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Component
@Slf4j
public class StartupRunner implements ApplicationRunner {

  @Autowired
  private HelloComponent paFHelloComponent;

  @Autowired
  HelloComponent.HelloComponentClient1 helloComponentClient1;

  @Value("${product.persistence.class}")
  private String prodctPersistenceClass;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    System.out.println("Hello PaF 2024");

//          while (true) {
//            String resp = paFHelloComponent.getHello();
//            System.out.println(resp);
//            sleep(1000);
//          }

    Class<?> productPersistenceClass = Class.forName(
      "de.vfh.paf.plain.di." + prodctPersistenceClass);
    IProductPersistence productPersistence = (IProductPersistence) productPersistenceClass
      .getConstructors()[0].newInstance();
    ProductMan productMan = new ProductMan(productPersistence);

    productMan.addProduct(new Phone(200f, "SupaPhone"));

  }

  @Autowired
  private PafHelloBean pafHelloBean;
  @Autowired
  @Qualifier("rectangle")
  private GeometricShape geometricShape;

  @EventListener(ApplicationReadyEvent.class)
  public void doSomethingAfterStartup() {
    System.out.println("Application ready: " + pafHelloBean.getProjectName(false));

    geometricShape.setSides(2, 3);
    log.info("Area of " + geometricShape.getClass().getSimpleName() + ": " + (geometricShape.calcArea()));
    helloComponentClient1.callComponent();
  }
}
