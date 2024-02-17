package de.verdox.mccreativelab.generator;

import com.google.gson.JsonObject;

public interface JsonConfigurable {
    default JsonObject serializeToJson(){
        return new JsonObject();
    }
    default void deserializeFromJson(JsonObject jsonObject){

    }
}
