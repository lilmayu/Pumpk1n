package dev.mayuna.pumpk1n;

import com.google.gson.GsonBuilder;
import dev.mayuna.pumpk1n.api.BackwardsCompatible;
import dev.mayuna.pumpk1n.api.ParentedDataElement;

@BackwardsCompatible(className = "dev.mayuna.pumpk1n.AnotherTestData")
public class TestData extends ParentedDataElement {

    public int someNumber = 69;

    @Override
    public GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }
}
