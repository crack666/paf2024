package de.vfh.paf;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "de.vfh.paf.entity" })
@EnableAutoConfiguration
@EnableJpaRepositories
@ComponentScan(basePackages = { "de.vfh.paf.api", "de.vfh.paf.service", "de.vfh.paf.component" })
public class Paf2024Application {

	public static void main(String[] args) {

		SpringApplication.run(Paf2024Application.class, args);
	}

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

}
