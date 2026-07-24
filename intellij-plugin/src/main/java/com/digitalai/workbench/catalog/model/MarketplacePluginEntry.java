package com.digitalai.workbench.catalog.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MarketplacePluginEntry {
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("version")
    private String version;
    
    @SerializedName("source")
    private String source;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("keywords")
    private List<String> keywords;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
