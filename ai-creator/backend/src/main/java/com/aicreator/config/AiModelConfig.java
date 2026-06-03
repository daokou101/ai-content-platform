package com.aicreator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiModelConfig {

    private String defaultModel;
    private List<ModelInfo> models;

    public String getDefaultModel() { return defaultModel; }
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }
    public List<ModelInfo> getModels() { return models; }
    public void setModels(List<ModelInfo> models) { this.models = models; }

    public ModelInfo getModelByName(String name) {
        return models.stream()
                .filter(m -> m.getName().equals(name) || m.getModel().equals(name))
                .findFirst()
                .orElse(null);
    }

    public ModelInfo getDefaultModelInfo() {
        ModelInfo info = getModelByName(defaultModel);
        if (info == null && !models.isEmpty()) {
            return models.get(0);
        }
        return info;
    }

    public static class ModelInfo {
        private String name;
        private String apiUrl;
        private String model;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
