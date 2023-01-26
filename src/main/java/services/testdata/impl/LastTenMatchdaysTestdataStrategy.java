package services.testdata.impl;

import com.google.inject.Singleton;

import services.testdata.LastMatchdaysTestdataStrategy;

@Singleton
public class LastTenMatchdaysTestdataStrategy extends LastMatchdaysTestdataStrategy {

    private static final String CODE = "lastTenMatchdays";

    public LastTenMatchdaysTestdataStrategy() {
        super(10);
    }

    @Override
    public String code() {
        return CODE;
    }
}
