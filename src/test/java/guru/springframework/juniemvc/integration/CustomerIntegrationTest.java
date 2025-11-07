package guru.springframework.juniemvc.integration;

import guru.springframework.juniemvc.models.customer.CustomerRequest;
import guru.springframework.juniemvc.models.customer.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/customers";
    }

    @Test
    void create_and_get_customer() {
        CustomerRequest req = CustomerRequest.builder()
                .name("Int Test")
                .addressLine1("123 Integration Ave")
                .city("TestCity")
                .state("TS")
                .postalCode("00000")
                .build();

        ResponseEntity<CustomerResponse> createResp = restTemplate.postForEntity(baseUrl(), req, CustomerResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getHeaders().getLocation()).isNotNull();
        Integer id = createResp.getBody().getId();
        assertThat(id).isNotNull();

        ResponseEntity<CustomerResponse> getResp = restTemplate.getForEntity(baseUrl() + "/" + id, CustomerResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getName()).isEqualTo("Int Test");
    }
}
