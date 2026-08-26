package com.example.smartpantry;

public class Recipe {
    public final long id;
    public final String name, method;

    public Recipe(long id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
    }
}
