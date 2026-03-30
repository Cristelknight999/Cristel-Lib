package de.cristelknight.cristellib.util.jankson;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonGrammar;
import de.cristelknight.cristellib.config.FileWriter;


public class CommentArray extends JsonArray {

    public static final JsonGrammar JSON_GRAMMAR = FileWriter.JSON_GRAMMAR_BUILDER.get().printWhitespace(false).build();

    @Override
    public String toJson(JsonGrammar grammar, int depth) {
        return super.toJson(JSON_GRAMMAR, depth);
    }
}