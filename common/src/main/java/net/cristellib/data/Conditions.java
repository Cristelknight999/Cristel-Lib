package net.cristellib.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.cristellib.CristelLibExpectPlatform;

public class Conditions {

    public static boolean readConditions(JsonObject object){
        if(!object.has("condition")) return true;
        JsonArray array = object.get("condition").getAsJsonArray();
        boolean bl = true;
        for(JsonElement e : array){
            if(e instanceof JsonObject o){
                if(!readCondition(o)) bl = false;
            }
        }

        return bl;
    }

    public static boolean readCondition(JsonObject object){
        String type = object.get("type").getAsString();
        if(type.equals("mod_loaded")){
            return CristelLibExpectPlatform.isModLoaded(object.get("mod").getAsString());
        }

        return false;
    }

}
