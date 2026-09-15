package com.example.smartpantry;

public class NearMatch {
    public final Recipe recipe;
    public final String missing;
    public NearMatch(Recipe recipe, String missing) {
        this.recipe = recipe;
        this.missing = missing;
    }
}
