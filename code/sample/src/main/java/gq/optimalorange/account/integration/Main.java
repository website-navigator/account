package gq.optimalorange.account.integration;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.integration.inject.MainComponent;
import gq.optimalorange.account.integration.inject.DaggerMainComponent;
import gq.optimalorange.account.integration.utils.Debugger;

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
    final AuthenticationService auth = mainComponent.getAuthenticationService();
    println(auth);
    auth.authenticate(Identifier.id("1"), Certificate.password("test")).subscribe(result->{
      if (result.succeeded()) {
        println("authenticate succeeded!");
      } else {
        println("authenticate failed");
        println("cause: " + result.cause());
      }
    });
  }

  private static void println(Object x) {
    System.out.println(x);
  }

}
