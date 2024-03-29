package org.slowcoders.hyperql.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


@SpringBootApplication
public class SampleHyperQueryApplication {
	public static void main(String[] args) {
		SpringApplication.run(SampleHyperQueryApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void showStartup() {
		System.out.println("########################################################");
		System.out.println("# Sample HyperQuery Application Started                #");
		System.out.println("# Please, see the tutorial and API documentations      #");
		System.out.println("# -- Tutorial: http://localhost:7007                   #");
		System.out.println("# -- Rest API: http://localhost:7007/swagger-ui.html   #");
		System.out.println("########################################################");
	}
}

