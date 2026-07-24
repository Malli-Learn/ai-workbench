package com.digitalai.workbench.catalog.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class PluginManifest {
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("version")
    private String version;
    
    @SerializedName("author")
    private PluginAuthor author;
    
    @SerializedName("agents")
    private JsonElement agentsElement;
    
    @SerializedName("skills")
    private JsonElement skillsElement;
    
    @SerializedName("hooks")
    private JsonElement hooks;
    
    @SerializedName("mcpServers")
    private JsonElement mcpServers;
    
    @SerializedName("lspServers")
    private JsonElement lspServers;
    
    @SerializedName("commands")
    private JsonElement commands;
    
    @SerializedName("extensions")
    private JsonElement extensions;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public PluginAuthor getAuthor() { return author; }
    public void setAuthor(PluginAuthor author) { this.author = author; }

    public List<String> getAgents() {
        return parseStringOrArray(agentsElement, "agents/");
    }

    public List<String> getSkills() {
        return parseStringOrArray(skillsElement, "skills/");
    }

    public boolean hasUnsupportedComponents() {
        return hooks != null || mcpServers != null || lspServers != null || commands != null || extensions != null;
    }

    private List<String> parseStringOrArray(JsonElement element, String defaultValue) {
        List<String> result = new ArrayList<>();
        if (element == null) {
            result.add(defaultValue);
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            result.add(element.getAsString());
        } else if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
                    result.add(e.getAsString());
                }
            }
        } else {
            result.add(defaultValue);
        }
        return result;
    }
}
