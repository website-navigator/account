package gq.optimalorange.account.authentication.password;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import okio.ByteString;
import rx.Single;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

public class PasswordAuthenticationTest {

  @Test(expected = NullPointerException.class)
  public void testAuthenticateWithNull() {
    // given
    SubjectStorageService storage = mock(SubjectStorageService.class);
    given(storage.retrieveValue(any(), any(), any())).willReturn(null);
    TestSubscriber<Result<Void, AuthenticationService.AuthenticateFailureCause>> logger =
        TestSubscriber.create();

    // when
    PasswordAuthentication test = new PasswordAuthentication(storage);
    test.authenticate(Identifier.id("test"), Certificate.password("password")).subscribe(logger);

    // then threw NullPointerException
  }

  @Test
  public void testAuthenticateRetrieveValueOnce() {
    // given
    final Result<ByteString, SubjectStorageService.FailureCause> passwordResult =
        Results.succeed(ByteString.encodeUtf8("test").sha256());

    AtomicLong subscribeCounter = new AtomicLong(0);
    Single<Result<ByteString, SubjectStorageService.FailureCause>> value =
        Single.create(subscriber -> {
          subscribeCounter.incrementAndGet();
          subscriber.onSuccess(passwordResult);
        });

    SubjectStorageService storage = mock(SubjectStorageService.class);
    given(storage.retrieveValue(any(), any(), any())).willReturn(value);

    TestSubscriber<Result<Void, AuthenticationService.AuthenticateFailureCause>> logger =
        TestSubscriber.create();

    // when
    PasswordAuthentication tested = new PasswordAuthentication(storage);
    tested.authenticate(Identifier.id("17"), Certificate.password("test")).subscribe(logger);

    // then
    then(storage).should(times(1)).retrieveValue(any(), any(), any());
    assertEquals(1, subscribeCounter.longValue());
    logger.assertCompleted();
    logger.assertNoErrors();
    logger.assertValueCount(1);
    assertTrue(logger.getOnNextEvents().get(0).succeeded());
  }

}
