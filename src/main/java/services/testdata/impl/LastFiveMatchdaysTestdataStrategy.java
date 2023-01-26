package services.testdata.impl;

import com.google.inject.Singleton;

import services.testdata.LastMatchdaysTestdataStrategy;

@Singleton
public class LastFiveMatchdaysTestdataStrategy extends LastMatchdaysTestdataStrategy {

    private static final String CODE = "lastFiveMatchdays";

    public LastFiveMatchdaysTestdataStrategy() {
        super(5);
    }

    @Override
    public String code() {
        return CODE;
    }
}
