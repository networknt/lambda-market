
package com.networknt.market.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import com.networknt.config.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.LambdaException;

/**
 * This is the generated BusinessHandler for developers to write business logic for the Lambda function. Once this file
 * is generated, it won't be overwritten with another generation in the same folder.
 */
public class BusinessHandler {
    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);
    private static final String DEFAULT_REGION = "ca-central-1";
    private static final String CONTENT_TYPE = "Content-Type";
    public static final String AWS_REGION = "AWS_REGION";

    private final LambdaClient lambdaClient;

    private BusinessHandler() {

        // get current region if defined
        final String region;
        if (System.getenv().get(AWS_REGION) != null && !System.getenv().get(AWS_REGION).isEmpty())
            region = System.getenv().get(AWS_REGION);

        else region = DEFAULT_REGION;

        lambdaClient = DependencyFactory.lambdaClient(Region.of(region));
    }

    private static final class InstanceHolder {
        private static final BusinessHandler INSTANCE = new BusinessHandler();
    }

    public static BusinessHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        final var queryParams = input.getQueryStringParameters();
        final var serviceId = queryParams.get("service_id");
        final var functionName = queryParams.get("function_name");
        final var functionMethod = queryParams.get("function_method");
        final var encodedFunctionPath = queryParams.get("function_path");

        if (functionMethod == null || functionMethod.isEmpty()
                && serviceId == null || serviceId.isEmpty()
                && functionName == null || functionName.isEmpty()
                && encodedFunctionPath == null || encodedFunctionPath.isEmpty()) {
            return errorResponse("Query params 'service_id', 'function_name', 'function_method', and 'function_path' must be defined.", 400);
        }

        final var functionPath = URLDecoder.decode(encodedFunctionPath, StandardCharsets.UTF_8);
        if (!functionPath.startsWith("/")) {
            return errorResponse("Function path must start with leading '/'", 400);
        }


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("service_id", serviceId);

        APIGatewayProxyRequestEvent petstoreRequestEvent = new APIGatewayProxyRequestEvent();
        petstoreRequestEvent.setHeaders(headers);
        petstoreRequestEvent.setHttpMethod(functionMethod.toUpperCase());
        petstoreRequestEvent.setPath(functionPath);
        petstoreRequestEvent.setQueryStringParameters(new HashMap<>());
        String requestString = JsonMapper.toJson(petstoreRequestEvent);

        logger.trace("petstoreRequestEvent = {} to functionName {}", requestString, functionName);

        String petstoreResponse = null;
        try {
            var payload = SdkBytes.fromUtf8String(requestString);
            var request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(payload)
                    .build();
            var res = lambdaClient.invoke(request);
            if (logger.isDebugEnabled()) {
                logger.debug("lambda call function error:{}", res.functionError());
                logger.debug("lambda logger result:{}", res.logResult());
                logger.debug("lambda call status:{}", res.statusCode());
            }
            petstoreResponse = res.payload().asUtf8String();
        } catch (LambdaException e) {
            return errorResponse("Failed to send a request to the lambda function '" + functionName + "'", 500);
        }
        APIGatewayProxyResponseEvent responseEvent = JsonMapper.fromJson(petstoreResponse, APIGatewayProxyResponseEvent.class);
        ;
        return responseEvent;
    }

    private APIGatewayProxyResponseEvent errorResponse(final String reason, final int statusCode) {
        var response = new APIGatewayProxyResponseEvent();
        response.setBody(reason);
        response.setHeaders(Map.of(CONTENT_TYPE, "text/plain"));
        response.setStatusCode(statusCode);
        return response;
    }
}
