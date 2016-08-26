package gq.optimalorange.account.integration.utils;

import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;
import rx.plugins.RxJavaObservableExecutionHook;
import rx.plugins.RxJavaPlugins;
import rx.plugins.SimpleDebugNotificationListener;

public class Debugger {

  SimpleDebugNotificationListener logger;

  public void start() {
//    logger = new SimpleDebugNotificationListener();
//    RxJavaObservableExecutionHook hook = new DebugHook<>(logger);

    RxJavaObservableExecutionHook hook = new DebugHook<>(new DebugNotificationListener<Object>() {
      @Override
      public <T> T onNext(DebugNotification<T> n) {
//        println("[onNext]" + n);
        return super.onNext(n);
      }

      @Override
      public <T> Object start(DebugNotification<T> n) {
//        println("[start]" + n);
        switch (n.getKind()) {
          case OnNext: case OnCompleted: case OnError: case Subscribe: case Unsubscribe:
            println(String.format("[%11s]%s", n.getKind(), n));
            break;
          case OnStart: case Request:/* case Subscribe: case Unsubscribe:*/
            break;
        }
        return super.start(n);
      }

      @Override
      public void complete(Object context) {
//        println("[complete]" + context);
        super.complete(context);
      }

      @Override
      public void error(Object context, Throwable e) {
        println("[error]" + context);
        e.printStackTrace();
        super.error(context, e);
      }


    });
    RxJavaPlugins.getInstance().registerObservableExecutionHook(hook);
  }

  public void stop() {
//    println(logger.toString());
  }

  protected void println(Object x) {
    System.out.println(x);
  }

}
