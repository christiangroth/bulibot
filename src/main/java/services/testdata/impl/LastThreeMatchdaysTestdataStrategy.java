package services.testdata.impl;

import com.google.inject.Singleton;

import services.testdata.LastMatchdaysTestdataStrategy;

@Singleton
public class LastThreeMatchdaysTestdataStrategy extends LastMatchdaysTestdataStrategy {

    private static final String CODE = "lastThreeMatchdays";

    public LastThreeMatchdaysTestdataStrategy() {
        super(3);
    }

    @Override
    public String code() {
        return CODE;
    }
}
