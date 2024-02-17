package de.verdox.mccreativelab.generator.datapack.wrapper;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;

public class JsonMinecraftWrapper implements JsonBase{
    protected final JsonObjectBuilder jsonObjectBuilder;
    public JsonMinecraftWrapper(JsonObject jsonObject){
        this(JsonObjectBuilder.create(jsonObject));
    }

    public JsonMinecraftWrapper(JsonObjectBuilder jsonObjectBuilder){
        this.jsonObjectBuilder = jsonObjectBuilder;
    }

    @Override
    public JsonObjectBuilder getJsonBuilder() {
        return jsonObjectBuilder;
    }
}
