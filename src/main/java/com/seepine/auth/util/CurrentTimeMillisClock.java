package com.seepine.auth.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CurrentTimeMillisClock {
  private volatile long now;

  private CurrentTimeMillisClock() {
    this.now = System.currentTimeMillis();
    scheduleTick();
  }

  private void scheduleTick() {
    new ScheduledThreadPoolExecutor(
            1,
            runnable -> {
              Thread thread = new Thread(runnable, "current-time-millis");
              thread.setDaemon(true);
              return thread;
            })
        .scheduleAtFixedRate(() -> now = System.currentTimeMillis(), 1, 1, TimeUnit.MILLISECONDS);
  }

  public static long now() {
    return getInstance().now;
  }

  private static CurrentTimeMillisClock getInstance() {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final CurrentTimeMillisClock INSTANCE = new CurrentTimeMillisClock();
  }
}
