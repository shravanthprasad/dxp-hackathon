package com.aem.dxp.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.aem.dxp.core.services.OpenAIApiService;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component(immediate = true)
public class CustomPdfAiWorkflowProcess implements WorkflowProcess {

    @Reference
    OpenAIApiService openAIApiService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        try {
            WorkflowData workflowData = workItem.getWorkflowData();

            // Get the asset path
            String assetPath = workflowData.getPayload().toString();

            // Get the ResourceResolver
            ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);

            // Get the asset
            Asset asset = resourceResolver.getResource(assetPath).adaptTo(Asset.class);


            // Get the PDF rendition
            Rendition pdfRendition = asset.getOriginal();

            // Get the InputStream of the PDF
            InputStream pdfInputStream = pdfRendition != null ? pdfRendition.getStream() : null;

            // Call OpenAI API to process the PDF (replace this with your actual OpenAI API call)
            String openAiApiResponse = sendPdfToOpenAi(pdfInputStream, asset);
            JsonObject jsonObject = new Gson().fromJson(openAiApiResponse, JsonObject.class);
            String newFileId = jsonObject.get("id").getAsString();
            // List file IDs
            String listFilesResponse = openAIApiService.makeApiRequest("https://api.openai.com/v1/assistants/asst_SYgA7NtuVgRDMM0vs2QUqItS/files", "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");
            JsonObject listFilesResponseJson = new Gson().fromJson(listFilesResponse, JsonObject.class);
            JsonArray jsonArray = listFilesResponseJson.get("data").getAsJsonArray();
            List<String> fileIds = new ArrayList<>();
            // add new file id
            fileIds.add(newFileId);
            for (int i = 0; i < jsonArray.size(); i++) {
                fileIds.add(jsonArray.get(i).getAsJsonObject().get("id").getAsString());
            }
            openAIApiService.configureAssistant("https://api.openai.com/v1/assistants/asst_SYgA7NtuVgRDMM0vs2QUqItS", "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", "You are an AI bot and you use your pdf files only to respond to queries.Don’t give information not mentioned in the CONTEXT INFORMATION.", fileIds);

            // Perform any additional logic based on the OpenAI API response

        } catch (Exception e) {
            throw new WorkflowException("Error processing PDF in workflow", e);
        }
    }

    // Replace this method with your actual OpenAI API implementation
    private String sendPdfToOpenAi(InputStream pdfInputStream, Asset asset) throws Exception {
        // Your implementation to send PDF to OpenAI API and get response
        // Example: Use HttpURLConnection or an HTTP client library to send the PDF to the OpenAI API
        // and receive the response.

        // For simplicity, this example returns a dummy response.
        // Create a temporary file
        String name = asset.getName().substring(0, asset.getName().indexOf(".pdf"));

        File tempFile = File.createTempFile(name, ".pdf");

        // Write the InputStream content to the temporary file
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = pdfInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        // Close the InputStream
        if (pdfInputStream != null) {
            pdfInputStream.close();
        }

        return openAIApiService.uploadFile("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", tempFile, "assistants");
    }
}

