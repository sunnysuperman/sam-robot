package com.sunnysuperman.samrobot.log;

public interface Logger {

    void log(String msg);

    void logError(Throwable t);

}
