package com.example.demo.service;

import com.example.demo.service.configuration.RestTemplateConfiguration;
import org.junit.jupiter.api.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.Parameter.param;

@Testcontainers
@SpringBootTest(classes = {AuthorRegistryGateway.class, RestTemplateConfiguration.class})
public class AuthorRegistryGatewayTest {
    @Container
    public static final MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("author.registry.service.base.url", mockServer::getEndpoint);
    }

    @Autowired
    AuthorRegistryGateway authorRegistryGateway;

    @Test
    void shouldDoSomething() {
        var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        client.when(request().withPath("/api/authors-check").withQueryStringParameters(param("firstName", "Robert"), param("lastName", "Stevenson"), param("bookTitle", "Treasure Island"))).respond(HttpResponse.response("true").withHeader("Content-Type", "application/json"));

        Boolean authorCheck = authorRegistryGateway.checkAuthor("Robert", "Stevenson", "Treasure Island");

        assertEquals(true, authorCheck);
    }
}
