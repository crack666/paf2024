package de.vfh.paf.component.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdDatasourceConfig implements DatasourceConfig {
  @Override
  public void setup() {
    System.out.println("Setting up datasource for PROD environment");
  }

}
