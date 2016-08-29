package gq.optimalorange.account.authentication.password;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import gq.optimalorange.account.AuthenticationService.AuthenticateFailureCause;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import gq.optimalorange.account.internalapi.SubjectStorageService.GetValueFailure;
import okio.ByteString;
import rx.Single;
import rx.observers.TestSubscriber;

import static gq.optimalorange.account.Certificate.password;
import static gq.optimalorange.account.Identifier.id;
import static gq.optimalorange.account.internalapi.Results.succeed;
import static okio.ByteString.encodeUtf8;
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
    TestSubscriber<Result<Void, AuthenticateFailureCause>> logger = TestSubscriber.create();

    // when
    PasswordAuthentication test = new PasswordAuthentication(storage);
    test.authenticate(id("test"), password("password")).subscribe(logger);

    // then threw NullPointerException
  }

  @Test
  public void testAuthenticateRetrieveValueOnce() {
    // given
    final Result<ByteString, GetValueFailure> passwordResult = succeed(encodeUtf8("test").sha256());

    AtomicLong subscribeCounter = new AtomicLong(0);
    Single<Result<ByteString, GetValueFailure>> value = Single.create(subscriber -> {
      subscribeCounter.incrementAndGet();
      subscriber.onSuccess(passwordResult);
    });

    SubjectStorageService storage = mock(SubjectStorageService.class);
    given(storage.retrieveValue(any(), any(), any())).willReturn(value);

    TestSubscriber<Result<Void, AuthenticateFailureCause>> logger = TestSubscriber.create();

    // when
    PasswordAuthentication tested = new PasswordAuthentication(storage);
    tested.authenticate(id("17"), password("test")).subscribe(logger);

    // then
    then(storage).should(times(1)).retrieveValue(any(), any(), any());
    assertEquals(1, subscribeCounter.longValue());
    logger.assertCompleted();
    logger.assertNoErrors();
    logger.assertValueCount(1);
    assertTrue(logger.getOnNextEvents().get(0).succeeded());
  }

}
