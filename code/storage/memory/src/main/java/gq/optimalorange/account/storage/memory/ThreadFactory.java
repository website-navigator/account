package gq.optimalorange.account.storage.memory;

import java.util.concurrent.atomic.AtomicLong;

public class ThreadFactory implements java.util.concurrent.ThreadFactory {

  private final AtomicLong counter = new AtomicLong();

  private final String prefix;

  public ThreadFactory(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    final Thread thread = new Thread(r, prefix + counter.incrementAndGet());
    thread.setDaemon(true);
    return thread;
  }

}
