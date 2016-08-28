package gq.optimalorange.account.benchmark;

import org.apache.commons.lang3.time.StopWatch;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.benchmark.inject.DaggerMainComponent;
import gq.optimalorange.account.benchmark.inject.MainComponent;
import rx.Completable;
import rx.schedulers.Schedulers;

import static gq.optimalorange.account.Certificate.password;
import static gq.optimalorange.account.Identifier.id;

public class Main implements Runnable {

  public static void main(String[] args) {
    new Main().run();
  }

  /**
   * 一批 test suit 的大小。在同一批中的 test suits 会被<strong>同时（并发的）</strong>运行。<br/>
   * 注：此参数影响任务队列的最大长度，与内存消耗有关，应当和 JVM 的 -Xmx 参数相匹配。
   */
//  private static final int BATCH_SIZE = 0x10_00_00; // 1 MB
//  private static final int BATCH_SIZE = 0x08_00_00; // 512 KB
//  private static final int BATCH_SIZE = 0x04_00_00; // 256 KB
//  private static final int BATCH_SIZE = 0x02_00_00; // 128 KB
//  private static final int BATCH_SIZE = 0x01_00_00; // 64 KB
  private static final int BATCH_SIZE = 0x00_04_00; // 1 KB
//  private static final int BATCH_SIZE = 0x00_02_00; // 512
//  private static final int BATCH_SIZE = 0x00_00_10; // 16
//  private static final int BATCH_SIZE = 0x00_00_03;
//  private static final int BATCH_SIZE = 0x00_00_01;

  /**
   * 循环次数。运行多少批 test suit。
   */
//  private static final int ROUNDS = 0x10_00_00; // 1 MB
//  private static final int ROUNDS = 0x01_00_00; // 64 KB
  private static final int ROUNDS = 0x00_20_00; // 8 KB
//  private static final int ROUNDS = 0x00_08_00; // 2 KB
//  private static final int ROUNDS = 0x00_04_00; // 1 KB
//  private static final int ROUNDS = 0x00_00_10; // 16
//  private static final int ROUNDS = 0x00_00_04;
//  private static final int ROUNDS = 0x00_00_01;

  final MainComponent mainComponent = DaggerMainComponent.create();

  @Override
  public void run() {
    final AuthenticationService authentication = mainComponent.getAuthenticationService();
    final Completable allTest = getAllTest(authentication);

    StopWatch stopWatch = new StopWatch();
    System.out.println("start benchmark");
    stopWatch.start();

    final Throwable error = allTest.get();

    stopWatch.stop();
    System.out.println("error: " + error);
    System.out.println("all test run in: " + stopWatch);
    final double tsPerS = (double) ((long) BATCH_SIZE * ROUNDS * 1000) / stopWatch.getTime();
    System.out.println("testsuit/s: " + tsPerS);
  }

  private Completable getAllTest(AuthenticationService authentication) {
    final Completable testSuitBatch = getTestSuitBatch(authentication);

    Completable[] testSuitBatches = new Completable[ROUNDS];
    for (int i = 0; i < testSuitBatches.length; i++) {
      testSuitBatches[i] = testSuitBatch;
    }
    return Completable.concat(testSuitBatches);
  }

  private Completable getTestSuitBatch(AuthenticationService authentication) {
    final Completable testSuit = getTestSuit(authentication);

    Completable[] testSuits = new Completable[BATCH_SIZE];
    for (int i = 0; i < testSuits.length; i++) {
      testSuits[i] = testSuit;
    }
    return Completable.mergeDelayError(testSuits);
  }

  private Completable getTestSuit(AuthenticationService authentication) {
//    return authentication.authenticate(id("1"), password("test")).toCompletable();
    return authentication.authenticate(id("1"), password("test"))
//        .subscribeOn(Schedulers.io())
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.computation())
        .toCompletable();
  }


}
