package likelion14th.lte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LteApplication {

	public static void main(String[] args) {
		SpringApplication.run(LteApplication.class, args);
	}
}
