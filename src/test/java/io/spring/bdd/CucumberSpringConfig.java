package io.spring.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * Spring context configuration for Cucumber BDD tests.
 *
 * <p>The {@link CucumberContextConfiguration} annotation marks this class as the entry point for
 * Spring context initialization. Combined with {@link SpringBootTest}, the full application context
 * is started once and shared across all feature files and scenarios, avoiding redundant restarts.
 *
 * <p>The {@code RANDOM_PORT} web environment spins up the embedded server on an available port,
 * which is injected into {@link TestHelper} for HTTP-based integration testing.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfig {}
