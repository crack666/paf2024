package de.vfh.paf.bean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Configuration // leads to execution of getHello() at startup
@Slf4j
// @Profile(value = "dev")
public class PafHelloBean {

    @Autowired
    private Environment env;

    /**
     * @Bean annotation is a method-level annotation
     * and has to be used within a class annotated with @Configuration.
     * There are no specializations of the @Bean annotation,
     */
    @Bean
    public String getHello() {
        log.debug("getHello() called");
        System.out.println(this.getClass().getSimpleName() + ": getHello() called");
        return "Hello PaF 2024 from Bean\n";
    }

    @Bean
    public String getProjectName(@Value("${startup.print:false}") boolean print) {
        String projectName = env.getProperty("project-name");
        if (print) System.out.println("Project name: " + projectName);
        return projectName;
    }

}
