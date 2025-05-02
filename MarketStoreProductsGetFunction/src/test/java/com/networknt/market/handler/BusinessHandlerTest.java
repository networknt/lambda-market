
package com.networknt.market.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BusinessHandlerTest {

    @Test
    public void testFunctionQueryParams() {
        App app = new App();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        // Create request with all valid query params
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("service_id", "my-service-id-0.0.1-SNAPSHOT");
        queryParams.put("function_name", "MyFakeLambdaFunction");
        queryParams.put("function_method", "GET");
        queryParams.put("function_path", URLEncoder.encode("/path/to/resource", StandardCharsets.UTF_8));
        request.setQueryStringParameters(queryParams);
        request.setPath("/market/{store}/products");
        request.setHttpMethod("GET");

        // send a request, we should get '500' because the function is not real. We should pass all validation though.
        var result1 = app.handleRequest(request, null);
        System.out.println(result1.getBody());

        assertEquals(500, result1.getStatusCode().intValue());
        assertEquals("text/plain", result1.getHeaders().get("Content-Type"));

        // re-use the previous request, just change the 'function_path' to an invalid path (not starting with /)
        queryParams.put("function_path", URLEncoder.encode("non-valid-path", StandardCharsets.UTF_8));
        request.setQueryStringParameters(queryParams);

        // validation should return 400 here.
        var result2 = app.handleRequest(request, null);
        System.out.println(result1.getBody());
        assertEquals(400, result2.getStatusCode().intValue());

    }

    @Test
    @Disabled
    public void successResponse() {
        App app = new App();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<>();
        // when running unit tests, there is no gateway authorizer and the token is not used.
        headers.put("Authorization", "Bearer ");
        request.setHeaders(headers);
        request.setPath("/market/{store}/products");
        request.setHttpMethod("GET");
        APIGatewayProxyRequestEvent.ProxyRequestContext context = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        context.setAccountId("1234567890");
        context.setStage("Prod");
        context.setApiId("gy415nuibc");
        context.setHttpMethod("POST");
        APIGatewayProxyRequestEvent.RequestIdentity identity = new APIGatewayProxyRequestEvent.RequestIdentity();
        identity.setAccountId("1234567890");
        context.setIdentity(identity);
        request.setRequestContext(context);
        Map<String, Object> authorizerMap = new HashMap<>();
        // Simulate the authorizer to manually inject the primary token scopes for the scope verifier.
        authorizerMap.put("primary_scopes", "read:products");
        context.setAuthorizer(authorizerMap);
        APIGatewayProxyResponseEvent result = app.handleRequest(request, null);
        System.out.println(result.getBody());
        assertEquals(result.getStatusCode().intValue(), 200);
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
    }

    @Test
    @Disabled
    public void failureResponse() {
        App app = new App();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<>();
        // when running unit tests, there is no gateway authorizer and the token is not used.
        headers.put("Authorization", "Bearer ");
        request.setHeaders(headers);
        request.setPath("/market/{store}/products");
        request.setHttpMethod("GET");
        APIGatewayProxyRequestEvent.ProxyRequestContext context = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        context.setAccountId("1234567890");
        context.setStage("Prod");
        context.setApiId("gy415nuibc");
        context.setHttpMethod("POST");
        APIGatewayProxyRequestEvent.RequestIdentity identity = new APIGatewayProxyRequestEvent.RequestIdentity();
        identity.setAccountId("1234567890");
        context.setIdentity(identity);
        request.setRequestContext(context);
        Map<String, Object> authorizerMap = new HashMap<>();
        // Simulate the authorizer to manually inject the primary token scopes for the scope verifier.
        authorizerMap.put("primary_scopes", "read:products");
        context.setAuthorizer(authorizerMap);
        APIGatewayProxyResponseEvent result = app.handleRequest(request, null);
        System.out.println(result.getBody());
        assertEquals(result.getStatusCode().intValue(), 400);
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
    }
}
