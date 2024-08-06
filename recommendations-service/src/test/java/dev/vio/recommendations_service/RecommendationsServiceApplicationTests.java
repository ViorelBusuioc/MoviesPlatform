package dev.vio.recommendations_service;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class RecommendationsControllerTest {

	private static KafkaContainer kafkaContainer;

	@Autowired
	private MockMvc mockMvc;

	private KafkaTemplate<String, String> kafkaTemplate;

	static {
		kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
		kafkaContainer.start();
	}

	@DynamicPropertySource
	static void kafkaProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
		registry.add("spring.kafka.consumer.key-deserializer", StringDeserializer.class::getName);
		registry.add("spring.kafka.consumer.value-deserializer", StringDeserializer.class::getName);
	}

	@BeforeEach
	void setUp() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
		kafkaTemplate = new KafkaTemplate<>(producerFactory);
	}

	@Test
	void testGetRecommendationsEndpoint() throws Exception {
		// Arrange: Send some messages to Kafka
		kafkaTemplate.send("movies-topic", "Inception", "Sci-Fi|Thriller");
		kafkaTemplate.send("movies-topic", "The Matrix", "Sci-Fi|Action");
		kafkaTemplate.send("movies-topic", "The Dark Knight", "Action|Crime|Drama");

		// Allow some time for the messages to be processed
		TimeUnit.SECONDS.sleep(5);

		// Act & Assert: Test the endpoint
		mockMvc.perform(MockMvcRequestBuilders
						.get("/api/recommendations")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.['Sci-Fi']").isArray())
				.andExpect(jsonPath("$.['Sci-Fi']").value(org.hamcrest.Matchers.containsInAnyOrder("Inception", "The Matrix")))
				.andExpect(jsonPath("$.Thriller").value(org.hamcrest.Matchers.contains("Inception")))
				.andExpect(jsonPath("$.Action").value(org.hamcrest.Matchers.containsInAnyOrder("The Matrix", "The Dark Knight")))
				.andExpect(jsonPath("$.Crime").value(org.hamcrest.Matchers.contains("The Dark Knight")))
				.andExpect(jsonPath("$.Drama").value(org.hamcrest.Matchers.contains("The Dark Knight")));
	}

	@Test
	void testGetRecommendationsEndpointWithNoData() throws Exception {
		// Act & Assert: Test the endpoint when no data has been processed
		mockMvc.perform(MockMvcRequestBuilders
						.get("/api/recommendations")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isEmpty());
	}
}