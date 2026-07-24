package com.digitalai.workbench.catalog.model;

import com.google.gson.annotations.SerializedName;

public class PluginAuthor {
    @SerializedName("name")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
