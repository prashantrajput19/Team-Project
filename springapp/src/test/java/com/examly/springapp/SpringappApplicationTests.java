package com.examly.springapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringappApplicationTests {

    private String usertoken;
    private String admintoken;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper; // To parse JSON responses

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @Order(1)
    void backend_testRegisterAdmin() {
    
        String requestBody = "{"
                + "\"userId\": 1,"
                + "\"email\": \"demoadmin@gmail.com\","
                + "\"password\": \"admin@1234\","
                + "\"username\": \"admin123\","
                + "\"userRole\": \"Admin\","
                + "\"mobileNumber\": \"9876543210\""
                + "}";
    
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/register",
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );
    
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
    

    @Test
    @Order(2)
    void backend_testRegisterUser() {
    
        String requestBody = "{"
                + "\"userId\": 2,"
                + "\"email\": \"demouser@gmail.com\","
                + "\"password\": \"user@1234\","
                + "\"username\": \"user123\","
                + "\"userRole\": \"User\","
                + "\"mobileNumber\": \"1122334455\""
                + "}";
    
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/register",
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );
    
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
    

    @Test
    @Order(3)
    void backend_testLoginAdmin() throws Exception {
        String requestBody = "{\"email\": \"demoadmin@gmail.com\", \"password\": \"admin@1234\"}";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/login",
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );

        Assertions.assertNotNull(response.getBody(), "Response body is null!");

        JsonNode responseBody = objectMapper.readTree(response.getBody());
        String token = responseBody.get("token").asText();
        admintoken = token;

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(token);
    }

    @Test
    @Order(4)
    void backend_testLoginUser() throws Exception {
        String requestBody = "{\"email\": \"demouser@gmail.com\", \"password\": \"user@1234\"}";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/login",
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );

        JsonNode responseBody = objectMapper.readTree(response.getBody());
        String token = responseBody.get("token").asText();
        usertoken = token;

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(token);
    }



    @Test
    @Order(5)
    void backend_testAdminAddArchitect() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
        Assertions.assertNotNull(usertoken, "User token should not be null");
    
        String requestBody = "{"
            + "\"architectId\": 1,"
            + "\"name\": \"John Architects\","
            + "\"expertise\": \"Residential\","
            + "\"location\": \"Chennai\","
            + "\"rating\": 4.5,"
            + "\"portfolioLink\": \"https://portfolio.com/john\","
            + "\"status\": \"Active\""
            + "}";
    
        // ================= ADMIN TOKEN (201 CREATED) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<String> adminRequest = new HttpEntity<>(requestBody, adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange("/api/architects", HttpMethod.POST, adminRequest, String.class);
    
        JsonNode responseBody = objectMapper.readTree(adminResponse.getBody());
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin adding architect");
    
        Assertions.assertEquals(HttpStatus.CREATED, adminResponse.getStatusCode());
        Assertions.assertEquals("John Architects", responseBody.get("name").asText());
        Assertions.assertEquals("Residential", responseBody.get("expertise").asText());
        Assertions.assertEquals("Chennai", responseBody.get("location").asText());
        Assertions.assertEquals("Active", responseBody.get("status").asText());
    
        // ================= USER TOKEN (403 FORBIDDEN) =================
            HttpHeaders userHeaders = createHeaders();
            userHeaders.set("Authorization", "Bearer " + usertoken);
            HttpEntity<String> userRequest = new HttpEntity<>(requestBody, userHeaders);

            ResponseEntity<String> userResponse =
                    restTemplate.exchange("/api/architects", HttpMethod.POST, userRequest, String.class);

            System.out.println(userResponse.getStatusCode() + " Status code for User trying to add architect");

            Assertions.assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());

    }
    
    @Test
    @Order(6)
    void backend_testGetArchitectByIdWithRoleValidation() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
        Assertions.assertNotNull(usertoken, "User token should not be null");
    
        long architectId = 1;
        String url = "/api/architects/" + architectId;
    
        // ================= ADMIN TOKEN (200 OK) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<Void> adminRequest = new HttpEntity<>(adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange(url, HttpMethod.GET, adminRequest, String.class);
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin retrieving architect");
        Assertions.assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
    
        // Parse and validate JSON response
        JsonNode responseBody = objectMapper.readTree(adminResponse.getBody());
    
        Assertions.assertEquals(1, responseBody.get("architectId").asLong());
        Assertions.assertEquals("John Architects", responseBody.get("name").asText());
        Assertions.assertEquals("Residential", responseBody.get("expertise").asText());
        Assertions.assertEquals("Chennai", responseBody.get("location").asText());
        Assertions.assertEquals("Active", responseBody.get("status").asText());
    
        System.out.println("Admin Response JSON: " + responseBody.toString());
    
        // ================= USER TOKEN(403 FORBIDDEN) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        
        ResponseEntity<String> userResponse =
                restTemplate.exchange(url, HttpMethod.GET, userRequest, String.class);
        
        System.out.println(userResponse.getStatusCode() + " Status code for User retrieving architect");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
    }
    
    
   
    @Test
    @Order(7)
    void backend_testGetAllArchitects() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
        Assertions.assertNotNull(usertoken, "User token should not be null");
    
        // ================= ADMIN TOKEN (200 OK) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<String> adminRequest = new HttpEntity<>(adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange("/api/architects", HttpMethod.GET, adminRequest, String.class);
    
        JsonNode responseBody = objectMapper.readTree(adminResponse.getBody());
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin getting all architects");
    
        Assertions.assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        Assertions.assertTrue(responseBody.isArray());
    
        // ================= USER TOKEN (200 OK) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
    
        ResponseEntity<String> userResponse =
                restTemplate.exchange("/api/architects", HttpMethod.GET, userRequest, String.class);
    
        JsonNode userResponseBody = objectMapper.readTree(userResponse.getBody());
    
        System.out.println(userResponse.getStatusCode() + " Status code for User getting all architects");
    
        Assertions.assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        Assertions.assertTrue(userResponseBody.isArray());
    }
    
    
    @Test
    @Order(8)
    void backend_testUpdateArchitectWithRoleValidation() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
        Assertions.assertNotNull(usertoken, "User token should not be null");
    
        long architectId = 1;
        String url = "/api/architects/" + architectId;
    
        String updateRequestBody = "{"
            + "\"architectId\": " + architectId + ","
            + "\"name\": \"Updated Architects\","
            + "\"expertise\": \"Commercial\","
            + "\"location\": \"Bangalore\","
            + "\"rating\": 4.8,"
            + "\"portfolioLink\": \"https://portfolio.com/updated\","
            + "\"status\": \"Active\""
            + "}";
    
        // ================= ADMIN TOKEN (200 OK) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<String> adminRequest = new HttpEntity<>(updateRequestBody, adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange(url, HttpMethod.PUT, adminRequest, String.class);
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin updating architect");
        Assertions.assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
    
        JsonNode responseBody = objectMapper.readTree(adminResponse.getBody());
    
        Assertions.assertEquals(architectId, responseBody.get("architectId").asLong());
        Assertions.assertEquals("Updated Architects", responseBody.get("name").asText());
        Assertions.assertEquals("Commercial", responseBody.get("expertise").asText());
        Assertions.assertEquals("Bangalore", responseBody.get("location").asText());
        Assertions.assertEquals("Active", responseBody.get("status").asText());
    
        System.out.println("Admin Update Response JSON: " + responseBody.toString());
    
        // ================= USER TOKEN (403 FORBIDDEN) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<String> userRequest = new HttpEntity<>(updateRequestBody, userHeaders);
    
        ResponseEntity<String> userResponse =
                restTemplate.exchange(url, HttpMethod.PUT, userRequest, String.class);
    
        System.out.println(userResponse.getStatusCode() + " Status code for User trying to update architect");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
    }
    

    @Test
    @Order(9)
    void backend_testUserCanAddArchitectInquiry() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(usertoken, "User token should not be null");
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
    
        String requestBody = "{"
            + "\"message\": \"I need a residential architect for a villa project\","
            + "\"status\": \"Pending\","
            + "\"priority\": \"High\","
            + "\"contactDetails\": \"user@email.com, 9876543210\","
            + "\"user\": {\"userId\": 2},"
            + "\"architect\": {\"architectId\": 1}"
            + "}";
    
        // ================= USER TOKEN (201 CREATED) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<String> userRequest = new HttpEntity<>(requestBody, userHeaders);
    
        ResponseEntity<String> userResponse =
                restTemplate.exchange("/api/inquiries", HttpMethod.POST, userRequest, String.class);
    
        JsonNode responseBody = objectMapper.readTree(userResponse.getBody());
    
        System.out.println(userResponse.getStatusCode() + " Status code for User adding inquiry");
        Assertions.assertEquals(HttpStatus.CREATED, userResponse.getStatusCode());
        Assertions.assertEquals("Pending", responseBody.get("status").asText());
        Assertions.assertEquals("High", responseBody.get("priority").asText());
        Assertions.assertEquals(
            "I need a residential architect for a villa project",
            responseBody.get("message").asText()
        );
    
        // ================= ADMIN TOKEN (403 FORBIDDEN) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<String> adminRequest = new HttpEntity<>(requestBody, adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange("/api/inquiries", HttpMethod.POST, adminRequest, String.class);
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin trying to add inquiry");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, adminResponse.getStatusCode());
    }
    
    @Test
    @Order(10)
    void backend_testRoleBasedAccessForViewingAllInquiries() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
        Assertions.assertNotNull(usertoken, "User token should not be null");
    
        String url = "/api/inquiries";
    
        // ================= ADMIN TOKEN (200 OK) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<Void> adminRequest = new HttpEntity<>(adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange(url, HttpMethod.GET, adminRequest, String.class);
    
        JsonNode adminResponseBody = objectMapper.readTree(adminResponse.getBody());
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin viewing all inquiries");
        Assertions.assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        Assertions.assertTrue(adminResponseBody.isArray());
    
        System.out.println("Admin View Inquiries Response JSON: " + adminResponseBody.toString());
    
        // ================= USER TOKEN (403 FORBIDDEN) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
    
        ResponseEntity<String> userResponse =
                restTemplate.exchange(url, HttpMethod.GET, userRequest, String.class);
    
        System.out.println(userResponse.getStatusCode() + " Status code for User trying to view all inquiries");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
    }
    

    @Test
    @Order(11)
    void backend_testUserCanViewOwnInquiries() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(usertoken, "User token should not be null");
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
    
        long userId = 2;
        String url = "/api/inquiries/user/" + userId;
    
        // ================= USER TOKEN (200 OK) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
    
        ResponseEntity<String> userResponse =
                restTemplate.exchange(url, HttpMethod.GET, userRequest, String.class);
    
        JsonNode userResponseBody = objectMapper.readTree(userResponse.getBody());
    
        System.out.println(userResponse.getStatusCode() + " Status code for User viewing own inquiries");
        Assertions.assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        Assertions.assertTrue(userResponseBody.isArray());
    
        System.out.println("User View Own Inquiries Response JSON: " + userResponseBody.toString());
    
        // ================= ADMIN TOKEN (403 FORBIDDEN) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<Void> adminRequest = new HttpEntity<>(adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange(url, HttpMethod.GET, adminRequest, String.class);
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin trying to view user inquiries");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, adminResponse.getStatusCode());
    }
    

    @Test
    @Order(12)
    void backend_testAdminCanUpdateInquiry_UserCannot() throws Exception {
    
        // Ensure tokens are available
        Assertions.assertNotNull(usertoken, "User token should not be null");
        Assertions.assertNotNull(admintoken, "Admin token should not be null");
    
        long inquiryId = 1;
    
        // Sample inquiry update payload
        String updateRequestBody = "{"
            + "\"inquiryId\": " + inquiryId + ","
            + "\"message\": \"Updated inquiry message\","
            + "\"status\": \"Resolved\","
            + "\"priority\": \"Medium\","
            + "\"contactDetails\": \"updated@email.com\","
            + "\"user\": {\"userId\": 2},"
            + "\"architect\": {\"architectId\": 1}"
            + "}";
    
        // ================= ADMIN TOKEN (200 OK) =================
        HttpHeaders adminHeaders = createHeaders();
        adminHeaders.set("Authorization", "Bearer " + admintoken);
        HttpEntity<String> adminRequest = new HttpEntity<>(updateRequestBody, adminHeaders);
    
        ResponseEntity<String> adminResponse =
                restTemplate.exchange(
                    "/api/inquiries/" + inquiryId,
                    HttpMethod.PUT,
                    adminRequest,
                    String.class
                );
    
        System.out.println(adminResponse.getStatusCode() + " Status code for Admin updating inquiry");
        Assertions.assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
    
        JsonNode adminResponseBody = objectMapper.readTree(adminResponse.getBody());
        Assertions.assertEquals("Resolved", adminResponseBody.get("status").asText());
        Assertions.assertEquals("Medium", adminResponseBody.get("priority").asText());
        Assertions.assertEquals("Updated inquiry message", adminResponseBody.get("message").asText());
    
        // ================= USER TOKEN (403 FORBIDDEN) =================
        HttpHeaders userHeaders = createHeaders();
        userHeaders.set("Authorization", "Bearer " + usertoken);
        HttpEntity<String> userRequest = new HttpEntity<>(updateRequestBody, userHeaders);
    
        ResponseEntity<String> userResponse =
                restTemplate.exchange(
                    "/api/inquiries/" + inquiryId,
                    HttpMethod.PUT,
                    userRequest,
                    String.class
                );
    
        System.out.println(userResponse.getStatusCode() + " Status code for User trying to update inquiry");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
    }

    @Test
@Order(13)
void backend_testAddFeedbackWithRoleValidation() throws Exception {

    // Ensure tokens are available
    Assertions.assertNotNull(usertoken, "User token should not be null");
    Assertions.assertNotNull(admintoken, "Admin token should not be null");

    String requestBody = "{"
        + "\"feedbackText\": \"Excellent architectural service\","
        + "\"date\": \"2025-02-27\","
        + "\"category\": \"Service\","
        + "\"user\": { \"userId\": 2 },"
        + "\"architect\": { \"architectId\": 1 }"
        + "}";

    // ================= USER TOKEN (201 CREATED) =================
    HttpHeaders userHeaders = createHeaders();
    userHeaders.set("Authorization", "Bearer " + usertoken);
    HttpEntity<String> userRequest = new HttpEntity<>(requestBody, userHeaders);

    ResponseEntity<String> userResponse =
            restTemplate.exchange("/api/feedback", HttpMethod.POST, userRequest, String.class);

    JsonNode responseBody = objectMapper.readTree(userResponse.getBody());

    System.out.println(userResponse.getStatusCode() + " Status code for User adding feedback");
    Assertions.assertEquals(HttpStatus.CREATED, userResponse.getStatusCode());
    Assertions.assertEquals(
        "Excellent architectural service",
        responseBody.get("feedbackText").asText()
    );
    Assertions.assertEquals(
        "Service",
        responseBody.get("category").asText()
    );

    // ================= ADMIN TOKEN (403 FORBIDDEN) =================
    HttpHeaders adminHeaders = createHeaders();
    adminHeaders.set("Authorization", "Bearer " + admintoken);
    HttpEntity<String> adminRequest = new HttpEntity<>(requestBody, adminHeaders);

    ResponseEntity<String> adminResponse =
            restTemplate.exchange("/api/feedback", HttpMethod.POST, adminRequest, String.class);

    System.out.println(adminResponse.getStatusCode() + " Status code for Admin trying to add feedback");
    Assertions.assertEquals(HttpStatus.FORBIDDEN, adminResponse.getStatusCode());
}


@Test
@Order(14)
void backend_testGetAllFeedbackWithRoleValidation() throws Exception {

    Assertions.assertNotNull(admintoken, "Admin token should not be null");
    Assertions.assertNotNull(usertoken, "User token should not be null");

    // ================= ADMIN TOKEN (200 OK) =================
    HttpHeaders adminHeaders = createHeaders();
    adminHeaders.set("Authorization", "Bearer " + admintoken);
    HttpEntity<String> adminRequest = new HttpEntity<>(adminHeaders);

    ResponseEntity<String> adminResponse =
            restTemplate.exchange("/api/feedback", HttpMethod.GET, adminRequest, String.class);

    Assertions.assertEquals(HttpStatus.OK, adminResponse.getStatusCode());

    // ================= USER TOKEN (403 FORBIDDEN) =================
    HttpHeaders userHeaders = createHeaders();
    userHeaders.set("Authorization", "Bearer " + usertoken);
    HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

    ResponseEntity<String> userResponse =
            restTemplate.exchange("/api/feedback", HttpMethod.GET, userRequest, String.class);

    Assertions.assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
}

@Test
@Order(15)
void backend_testGetFeedbackByUserIdWithRoleValidation() throws Exception {

    Assertions.assertNotNull(admintoken, "Admin token should not be null");
    Assertions.assertNotNull(usertoken, "User token should not be null");

    String url = "/api/feedback/user/2";

    // ================= USER TOKEN (200 OK) =================
    HttpHeaders userHeaders = createHeaders();
    userHeaders.set("Authorization", "Bearer " + usertoken);
    HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

    ResponseEntity<String> userResponse =
            restTemplate.exchange(url, HttpMethod.GET, userRequest, String.class);

    Assertions.assertEquals(HttpStatus.OK, userResponse.getStatusCode());

    // ================= ADMIN TOKEN (403 FORBIDDEN) =================
    HttpHeaders adminHeaders = createHeaders();
    adminHeaders.set("Authorization", "Bearer " + admintoken);
    HttpEntity<String> adminRequest = new HttpEntity<>(adminHeaders);

    ResponseEntity<String> adminResponse =
            restTemplate.exchange(url, HttpMethod.GET, adminRequest, String.class);

    Assertions.assertEquals(HttpStatus.FORBIDDEN, adminResponse.getStatusCode());
}

}
