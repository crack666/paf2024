package de.vfh.paf.api;

import de.vfh.paf.component.hello.HelloComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/hello")
public class PafHelloRestController {

  @Autowired
  private HelloComponent helloComponent;

  @RequestMapping(path = "/paf")
  public String getHello() {
    return "Hello PaF controller\n";
  }

  @RequestMapping(path = "/component/hello")
  public String getComponentHello() {
    return helloComponent.getHello();
  }


}
