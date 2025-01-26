package de.cristelknight.cristellib.util.jankson;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonGrammar;
import de.cristelknight.cristellib.config.ConfigManager;


public class CommentArray extends JsonArray {

    @Override
    public String toJson(JsonGrammar grammar, int depth) {
        return super.toJson(ConfigManager.JSON_GRAMMAR_BUILDER.get().printWhitespace(false).build(), depth);
    }
}