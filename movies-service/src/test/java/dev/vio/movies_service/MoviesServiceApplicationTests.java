package dev.vio.movies_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MoviesServiceApplicationTests {

	private final MockMvc mockMvc;

	private static MySQLContainer sqlContainer = new MySQLContainer<>("mysql:latest");

	private static KafkaContainer kafkaContainer;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	public MoviesServiceApplicationTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	static {
		kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
		kafkaContainer.start();
	}

	@BeforeAll
	static void beforeAll() {
		sqlContainer.start();
	}

	@AfterAll
	static void afterAll() {
		sqlContainer.stop();
		kafkaContainer.stop();
	}

	@DynamicPropertySource
	static void configureMockDataBase(DynamicPropertyRegistry registry) {

		registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", sqlContainer::getUsername);
		registry.add("spring.datasource.password", sqlContainer::getPassword);
		registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

	}

	@Test
	void aPostTest() throws Exception {

		String newMovie = "{\"name\": \"Inception\", \"genre\": \"Sci-Fi\"}";

		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(newMovie))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/plain;charset=UTF-8"))
				.andExpect(content().string("Movie Inception has been added to the database!"));
	}


	@Test
	void bGetAllTest() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
					.get("/api/movies")
					.accept("application/json")
					.contentType("application/json"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].id").isNumber())
				.andExpect(jsonPath("$[0].name").isString())
				.andExpect(jsonPath("$[0].genre").isString());
	}

	@Test
	void cProduceKafkaMessageTest() {

		String messageKey = "Inception";
		String messageValue = "Sci-Fi";
		kafkaTemplate.send("movies-topic", messageKey, messageValue);
	}

}
