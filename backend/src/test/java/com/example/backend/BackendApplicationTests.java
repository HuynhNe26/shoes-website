package com.example.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BackendApplicationTests {

	@Test
	void applicationEntryPointExists() {
		assertDoesNotThrow(() -> BackendApplication.class.getDeclaredMethod("main", String[].class));
	}
}
