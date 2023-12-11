package com.aem.dxp.core.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

// OpenAIApiService.java
public interface OpenAIApiService {
    String uploadFile(String apiKey, File file, String purpose) throws IOException;

    String makeApiRequest(String apiUrl, String apiKey) throws IOException;

    String configureAssistant(String apiUrl, String apiKey, String instructions, List<String> fileIds) throws IOException;
}

