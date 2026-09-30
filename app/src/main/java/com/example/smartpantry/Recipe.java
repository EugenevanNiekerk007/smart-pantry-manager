package com.example.smartpantry;

// Stores the main information for one recipe
public class Recipe {

    // Recipe details
    public final long id;
    public final String name, method;

    // Create a recipe using the information from the database
    public Recipe(long id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
    }
}