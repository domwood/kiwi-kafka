package com.github.domwood.kiwi.utilities;

import java.time.Clock;

public class TimeService {

    private Clock clock;

    public TimeService(){
        this.clock = Clock.systemUTC();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public long now(){
        return clock.instant().toEpochMilli();
    }
}
