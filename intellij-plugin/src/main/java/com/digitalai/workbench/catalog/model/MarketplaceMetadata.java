package com.digitalai.workbench.catalog.model;

import com.google.gson.annotations.SerializedName;

public class MarketplaceMetadata {
    @SerializedName("description")
    private String description;
    
    @SerializedName("version")
    private String version;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
