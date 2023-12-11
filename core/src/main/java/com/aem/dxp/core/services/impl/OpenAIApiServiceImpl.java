package com.aem.dxp.core.services.impl;

import com.aem.dxp.core.services.OpenAIApiService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Component(immediate = true, service = OpenAIApiService.class)
public class OpenAIApiServiceImpl implements OpenAIApiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIApiServiceImpl.class);

    @Activate
    protected void activate() {
        // Perform any activation tasks if needed
    }

    @Override
    public String makeApiRequest(String apiUrl, String apiKey) throws IOException {
        // Create the connection
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("GET");

        // Set the authorization header
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);

        // Set additional headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("OpenAI-Beta", "assistants=v1");

        // Enable input and output streams
        connection.setDoOutput(true);

        // Write any request payload, if needed
        // ...

        // Get the response code
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            logger.info("List of assistant files retrieved successfully");
        }

        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Return the response
            return response.toString();
        } finally {
            // Close the connection
            connection.disconnect();
        }
    }


    @Override
    public String uploadFile(String apiKey, File file, String purpose) throws IOException {
        // API endpoint URL
        String apiUrl = "https://api.openai.com/v1/files";

        // Create the connection
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("POST");

        // Set the authorization header
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);

        // Enable input and output streams
        connection.setDoOutput(true);

        // Set the content type to multipart/form-data
        String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        // Create the request body
        try (OutputStream outputStream = connection.getOutputStream()) {
            // Write purpose field
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"purpose\"\r\n\r\n").getBytes());
            outputStream.write((purpose + "\r\n").getBytes());

            // Write file field
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
            outputStream.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

            // Write file content
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
            outputStream.write(fileBytes);
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
        }

        // Get the response code
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            logger.info("Upload asset to Assistant successful");
        }


        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Return the response
            return response.toString();
        } finally {
            // Close the connection
            connection.disconnect();
        }
    }

    @Override
    public String configureAssistant(String apiUrl, String apiKey, String instructions, List<String> fileIds) throws IOException {
        // Create an HTTP client
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create an HTTP PATCH request
            HttpPost httpPost = new HttpPost(apiUrl);

            // Set headers
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("OpenAI-Beta", "assistants=v1");

            // Set request payload
            String payload = buildPayload(fileIds);
            httpPost.setEntity(new StringEntity(payload, "UTF-8"));

            // Execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // Read the response
            return readResponse(response);
        }
    }

    private String buildPayload(List<String> fileIds) {
        // Build the JSON payload
        JsonObject payload = new Gson().fromJson("{'instructions':'You are an AI bot and you use your pdf files only to respond to queries. Don’t give information not mentioned in the CONTEXT INFORMATION. Analyze & process all the translated asset and reply accordingly.','tools':[{'type':'retrieval'}],'model':'gpt-4-1106-preview','file_ids':[]}", JsonObject.class);
        JsonArray fileArray = payload.get("file_ids").getAsJsonArray();
        for (String id : fileIds) {
            fileArray.add(id);
        }
        return String.format(payload.toString());
    }

    private String readResponse(HttpResponse response) throws IOException {
        // Get the response code
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            logger.info("Assistant updated with file successfully");
        }

        // Read the response content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }

            // Return the response
            return responseContent.toString();
        }
    }
}
