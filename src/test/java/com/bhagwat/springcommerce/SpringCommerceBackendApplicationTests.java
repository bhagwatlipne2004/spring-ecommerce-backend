
package com.bhagwat.springcommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SpringCommerceBackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void invalidJwtShouldReturn401ForProtectedEndpoint() throws Exception {
		mockMvc.perform(get("/api/v1/cart")
						.header(HttpHeaders.AUTHORIZATION,
								"Bearer invalid.jwt.token"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void requestWithoutJwtShouldReturn401ForProtectedEndpoint()
			throws Exception {

		mockMvc.perform(get("/api/v1/cart"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void productsEndpointShouldBePublicWithoutJwt() throws Exception {
		mockMvc.perform(get("/api/v1/products"))
				.andExpect(status().isOk());
	}
}
