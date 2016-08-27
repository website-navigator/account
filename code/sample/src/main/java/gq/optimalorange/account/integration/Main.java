package gq.optimalorange.account.integration;

import java.util.concurrent.CountDownLatch;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.integration.inject.DaggerMainComponent;
import gq.optimalorange.account.integration.inject.MainComponent;
import gq.optimalorange.account.integration.utils.Debugger;
import rx.schedulers.Schedulers;

import static gq.optimalorange.account.Certificate.password;
import static gq.optimalorange.account.Identifier.id;

public class Main implements Runnable {

  public static void main(String[] args) {
    new Main().run();
  }

  static final boolean DEBUG = false;

  MainComponent mainComponent;

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
    mainComponent = DaggerMainComponent.create();
    println(mainComponent.getSubjectService());
    println(mainComponent.getAuthenticationService());

    System.out.println();
    authenticateUsingMainThread();
    System.out.println();
    authenticate();
    System.out.println();
    authenticateBlocking();
  }

  private void authenticateUsingMainThread() {
    mainComponent.getAuthenticationService()
        .authenticate(id("1"), password("test"))
        .subscribe(result -> {
          if (result.succeeded()) {
            println("authenticate succeeded!");
          } else {
            println("authenticate failed");
            println("cause: " + result.cause());
          }
        });
  }

  private void authenticate() {
    CountDownLatch latch = new CountDownLatch(1);
    mainComponent.getAuthenticationService().authenticate(id("1"), password("test"))
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        // start authenticate
        .subscribe(result -> {
          if (result.succeeded()) {
            println("authenticate succeeded!");
          } else {
            println("authenticate failed");
            println("cause: " + result.cause());
          }
          latch.countDown();
        });
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

  private void authenticateBlocking() {
    final Result<Void, AuthenticationService.AuthenticateFailureCause> result =
        mainComponent.getAuthenticationService().authenticate(id("1"), password("test"))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .toBlocking()
            .value();
    if (result.succeeded()) {
      println("blocking authenticate succeeded!");
    } else {
      println("blocking authenticate failed");
      println("cause: " + result.cause());
    }
  }

  private static void println(Object x) {
    System.out.println("[" + Thread.currentThread() + "]" + x);
  }

}
