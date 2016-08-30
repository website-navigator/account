package gq.optimalorange.account.sample;

import java.util.concurrent.CountDownLatch;

import gq.optimalorange.account.sample.inject.DaggerServiceComponent;
import gq.optimalorange.account.sample.inject.ServiceComponent;
import gq.optimalorange.account.sample.utils.Debugger;

public class Main implements Runnable {

  public static void main(String[] args) {
    new Main().run();
  }

  static final boolean DEBUG = false;

  ServiceComponent serviceComponent;

  @Override
  public void run() {
    Debugger debugger;
    if (DEBUG) {
      debugger = new Debugger();
      debugger.start();
    }

    doTask();

    if (DEBUG) {
      debugger.stop();
    }
  }

  private void doTask() {
    serviceComponent = DaggerServiceComponent.create();
    println(serviceComponent.getSubjectService());
    println(serviceComponent.getAuthenticationService());

    System.out.println();
    System.out.println("#doAsynchronously");
    doAsynchronously();
    System.out.println();
    System.out.println("#doBlockingly");
    doBlockingly();
  }

  private void doAsynchronously() {
    CountDownLatch latch = new CountDownLatch(1);
    Samples.getUseCase(serviceComponent)
        // start
        .subscribe(result -> {
          if (result.succeeded()) {
            println("succeeded: " + result.result());
          } else {
            println("failed cause: " + result.cause());
          }
        }, e -> {
          println(String.format(
              "error:[%s]%s", e.getClass().getSimpleName(), e.getLocalizedMessage()));
          latch.countDown();
        }, latch::countDown);
    // wait authenticate finished
    while (true) {
      try {
        latch.await();
        break;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void doBlockingly() {
    try {
      Samples.getUseCase(serviceComponent).toBlocking().toIterable().forEach(result -> {
        if (result.succeeded()) {
          println("succeeded: " + result.result());
        } else {
          println("failed cause: " + result.cause());
        }
      });
    } catch (Exception e) {
      println(String.format("error:[%s]%s", e.getClass().getSimpleName(), e.getLocalizedMessage()));
      e.printStackTrace();
    }
  }

  private static void println(Object x) {
    System.out.println("[" + Thread.currentThread() + "]" + x);
  }

}
