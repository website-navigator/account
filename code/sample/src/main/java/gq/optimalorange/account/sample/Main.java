package gq.optimalorange.account.sample;

import java.util.concurrent.CountDownLatch;

import gq.optimalorange.account.AuthenticationService.AuthenticateFailureCause;
import gq.optimalorange.account.AuthenticationService.ChangeCertificateFailureCause;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.CreateFailure;
import gq.optimalorange.account.SubjectService.SetIdentifierFailure;
import gq.optimalorange.account.sample.inject.DaggerMainComponent;
import gq.optimalorange.account.sample.inject.MainComponent;
import gq.optimalorange.account.sample.utils.Debugger;
import gq.optimalorange.account.subject.utils.Pair;
import rx.Observable;

import static gq.optimalorange.account.Certificate.password;

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
    System.out.println("#doAsynchronously");
    doAsynchronously();
    System.out.println();
    System.out.println("#doBlockingly");
    doBlockingly();
  }

  private void doAsynchronously() {
    CountDownLatch latch = new CountDownLatch(1);
    getUseCase()
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
      getUseCase().toBlocking().toIterable().forEach(result -> {
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

  /*
  1. create account
  2. set user name
  3. change password
  4. authenticate
  * */
  private Observable<Result<?, ? extends Enum<? extends Enum<?>>>> getUseCase() {
    final double suffix = Math.random() * 1_000_000;
    final String initPassword = "init test password " + suffix;
    final String username = "test username " + suffix;
    final String changedPassword = "changed test password" + suffix;

    final Observable<Result<Identifier, CreateFailure>> signUp =
        mainComponent.getSubjectService().create(password(initPassword)).toObservable().cache();
    final Observable<Result<Void, SetIdentifierFailure>> setUsername = signUp
        .filter(Result::succeeded)
        .flatMap(r -> mainComponent.getSubjectService()
            .setIdentifier(r.result(), Identifier.username(username))
            .toObservable());
    final Observable<Result<Void, ChangeCertificateFailureCause>> changePassword = signUp
        .filter(Result::succeeded)
        .flatMap(r -> mainComponent.getAuthenticationService()
            .changeCertificate(r.result(), password(initPassword),
                               password(initPassword), password(changedPassword))
            .toObservable())
        .cache();

    final Observable<Result<Void, AuthenticateFailureCause>> authenticate =
        changePassword
            .map(r -> {
              if (r.succeeded()) {
                return changedPassword;
              } else {
                return initPassword;
              }
            })
            .zipWith(signUp, Pair::new)
            .flatMap(r -> mainComponent.getAuthenticationService()
                .authenticate(r.b.result(), password(r.a))
                .toObservable());

    return Observable.merge(signUp, setUsername, changePassword, authenticate);
  }

  private static void println(Object x) {
    System.out.println("[" + Thread.currentThread() + "]" + x);
  }

}
