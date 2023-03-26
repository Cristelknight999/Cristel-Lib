package net.cristellib.data;

import com.google.gson.JsonObject;
import net.cristellib.CristelLibExpectPlatform;

public class Conditions {

    public static boolean readCondition(JsonObject object){
        if(!object.has("condition")) return true;
        JsonObject object1 = object.get("condition").getAsJsonObject();
        String type = object1.get("type").getAsString();
        if(type.equals("mod_loaded")){
            return CristelLibExpectPlatform.isModLoaded(object1.get("mod").getAsString());
        }

        return false;
    }

}
