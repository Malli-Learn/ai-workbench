package com.digitalai.workbench.catalog.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MarketplaceManifest {
    @SerializedName("name")
    private String name;
    
    @SerializedName("owner")
    private MarketplaceOwner owner;
    
    @SerializedName("metadata")
    private MarketplaceMetadata metadata;
    
    @SerializedName("plugins")
    private List<MarketplacePluginEntry> plugins;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MarketplaceOwner getOwner() { return owner; }
    public void setOwner(MarketplaceOwner owner) { this.owner = owner; }
    public MarketplaceMetadata getMetadata() { return metadata; }
    public void setMetadata(MarketplaceMetadata metadata) { this.metadata = metadata; }
    public List<MarketplacePluginEntry> getPlugins() { return plugins; }
    public void setPlugins(List<MarketplacePluginEntry> plugins) { this.plugins = plugins; }
}
