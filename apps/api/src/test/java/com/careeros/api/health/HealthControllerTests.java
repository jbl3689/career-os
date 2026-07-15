package com.careeros.api.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthControllerTests {

	private final HealthController healthController = new HealthController();

	@Test
	void returnsApiHealth() {
		HealthResponse response = healthController.health();

		assertThat(response.status()).isEqualTo("ok");
		assertThat(response.message()).isEqualTo("Career OS API is running");
	}
}
