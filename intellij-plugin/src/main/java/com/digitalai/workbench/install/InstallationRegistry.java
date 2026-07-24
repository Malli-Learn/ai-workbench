package com.digitalai.workbench.install;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstallationRegistry {
    @SerializedName("schemaVersion")
    private int schemaVersion = 1;

    @SerializedName("marketplace")
    private MarketplaceInfo marketplace;

    @SerializedName("installedPlugins")
    private Map<String, InstalledPluginInfo> installedPlugins = new HashMap<>();

    @SerializedName("sharedResources")
    private Map<String, SharedResourceInfo> sharedResources = new HashMap<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }

    public MarketplaceInfo getMarketplace() { return marketplace; }
    public void setMarketplace(MarketplaceInfo marketplace) { this.marketplace = marketplace; }

    public Map<String, InstalledPluginInfo> getInstalledPlugins() { return installedPlugins; }
    public void setInstalledPlugins(Map<String, InstalledPluginInfo> installedPlugins) { this.installedPlugins = installedPlugins; }

    public Map<String, SharedResourceInfo> getSharedResources() { return sharedResources; }
    public void setSharedResources(Map<String, SharedResourceInfo> sharedResources) { this.sharedResources = sharedResources; }

    public static class MarketplaceInfo {
        @SerializedName("name")
        private String name;
        @SerializedName("source")
        private String source;
        @SerializedName("branch")
        private String branch;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
    }

    public static class InstalledPluginInfo {
        @SerializedName("version")
        private String version;
        @SerializedName("installedAt")
        private String installedAt;
        @SerializedName("agents")
        private List<ComponentInfo> agents;
        @SerializedName("skills")
        private List<ComponentInfo> skills;

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getInstalledAt() { return installedAt; }
        public void setInstalledAt(String installedAt) { this.installedAt = installedAt; }
        public List<ComponentInfo> getAgents() { return agents; }
        public void setAgents(List<ComponentInfo> agents) { this.agents = agents; }
        public List<ComponentInfo> getSkills() { return skills; }
        public void setSkills(List<ComponentInfo> skills) { this.skills = skills; }
    }

    public static class ComponentInfo {
        @SerializedName("name")
        private String name;
        @SerializedName("path")
        private String path;
        @SerializedName("checksum")
        private String checksum;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
    }

    public static class SharedResourceInfo {
        @SerializedName("resource")
        private String resource;
        @SerializedName("checksum")
        private String checksum;
        @SerializedName("owners")
        private List<String> owners;

        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }
        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
        public List<String> getOwners() { return owners; }
        public void setOwners(List<String> owners) { this.owners = owners; }
    }
}
