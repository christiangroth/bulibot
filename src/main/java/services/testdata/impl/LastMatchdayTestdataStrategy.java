package services.testdata.impl;

import com.google.inject.Singleton;

import services.testdata.LastMatchdaysTestdataStrategy;

@Singleton
public class LastMatchdayTestdataStrategy extends LastMatchdaysTestdataStrategy {

    private static final String CODE = "lastMatchday";

    public LastMatchdayTestdataStrategy() {
        super(1);
    }

    @Override
    public String code() {
        return CODE;
    }
}
