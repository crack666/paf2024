package de.vfh.paf.component.hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@ComponentScan("de.vfh.paf.component.hello")
public class HelloComponentUsageConfig {

    @Bean
    public HelloComponent helloComponentConfig() {
        return new HelloComponent();
    }
}
