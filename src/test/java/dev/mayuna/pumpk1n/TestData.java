package dev.mayuna.pumpk1n;

import com.google.gson.GsonBuilder;
import dev.mayuna.pumpk1n.api.ParentedDataElement;

public class TestData extends ParentedDataElement {

    public int someNumber = 69;

    @Override
    public GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }
}
