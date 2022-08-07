package ch.wipfli.microstreamclientplus.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WicketApplication {
	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder()
				.sources(WicketApplication.class)
				.run(args);

	}
}

