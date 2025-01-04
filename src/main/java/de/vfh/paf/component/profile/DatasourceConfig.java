package de.vfh.paf.component.profile;

import org.springframework.stereotype.Component;

/**
 *
 * @see "https://www.baeldung.com/spring-profiles"
 */

@Component
public interface DatasourceConfig {
  public void setup();
}
