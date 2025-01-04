package de.vfh.paf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Paf2024ApplicationIntegrationTest {

  @Autowired
  private Paf2024Application paf2024Application;

	@Test
	void contextLoads() {
    System.out.println("To string:" + paf2024Application.toString());
	}

}
