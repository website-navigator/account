package gq.optimalorange.account.authentication;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.AuthenticationService.AuthenticateFailure;
import gq.optimalorange.account.AuthenticationService.ChangeCertificateFailure;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import rx.Single;
import rx.SingleSubscriber;

public class ChangeCertificateOnSubscribe
    implements Single.OnSubscribe<Result<Void, ChangeCertificateFailure>> {

  private final AuthenticationService authenticationService;
  private final AuthenticationServiceRegister serviceRegister;
  private final Identifier identifier;
  private final Certificate forAuthenticate;
  private final Certificate oldCertificate;
  private final Certificate newCertificate;

  public ChangeCertificateOnSubscribe(AuthenticationService authenticationService,
                                      AuthenticationServiceRegister serviceRegister,
                                      Identifier identifier, Certificate forAuthenticate,
                                      Certificate oldCertificate, Certificate newCertificate) {
    this.authenticationService = authenticationService;
    this.serviceRegister = serviceRegister;
    this.identifier = identifier;
    this.forAuthenticate = forAuthenticate;
    this.oldCertificate = oldCertificate;
    this.newCertificate = newCertificate;
  }

  @Override
  public void call(
      SingleSubscriber<? super Result<Void, ChangeCertificateFailure>> subscriber) {
    //TODO SafeSubscriber
    if (!oldCertificate.getType().equals(newCertificate.getType())) {
      fail(subscriber, ChangeCertificateFailure.NOT_SAME_CERTIFICATE_TYPE);
      return;
    }

    final AuthenticateSingleSubscriber parent = new AuthenticateSingleSubscriber(subscriber);
    subscriber.add(parent);
    authenticationService.authenticate(identifier, forAuthenticate).subscribe(parent);
  }

  private void fail(
      SingleSubscriber<? super Result<Void, ChangeCertificateFailure>> subscriber,
      ChangeCertificateFailure cause) {
    if (!subscriber.isUnsubscribed()) {
      final Result<Void, ChangeCertificateFailure> fail = Results.fail(cause);
      subscriber.onSuccess(fail);
    }
  }

  private class AuthenticateSingleSubscriber
      extends SingleSubscriber<Result<Void, AuthenticateFailure>> {

    private final SingleSubscriber<? super Result<Void, ChangeCertificateFailure>> actual;

    private AuthenticateSingleSubscriber(
        SingleSubscriber<? super Result<Void, ChangeCertificateFailure>> actual) {
      this.actual = actual;
    }

    @Override
    public void onSuccess(Result<Void, AuthenticateFailure> authResult) {
      if (authResult.succeeded()) {
        onAuthenticateSucceeded();
      } else {
        onAuthenticateFailed(authResult.cause());
      }
    }

    private void onAuthenticateSucceeded() {
      ChangeCertificateSingleSubscriber parent = new ChangeCertificateSingleSubscriber(actual);
      actual.add(parent);

      serviceRegister.getService(oldCertificate.getType()).flatMap(service -> {
        if (service.succeeded()) {
          return service.result().changeCertificate(identifier, oldCertificate, newCertificate);
        } else {
          final Result<Void, ChangeCertificateFailure> fail =
              Results.fail(ChangeCertificateFailure.UNSUPPORTED_CERTIFICATE_TYPE);
          return Single.just(fail);
        }
      }).subscribe(parent);
    }

    private void onAuthenticateFailed(AuthenticateFailure cause) {
      switch (cause) {
        case UNSUPPORTED_IDENTIFIER_TYPE:
          fail(actual, ChangeCertificateFailure.UNSUPPORTED_IDENTIFIER_TYPE);
          break;
        case SUBJECT_NOT_EXIST:
          fail(actual, ChangeCertificateFailure.SUBJECT_NOT_EXIST);
          break;
        case UNSUPPORTED_CERTIFICATE_TYPE:
          fail(actual, ChangeCertificateFailure.UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE);
          break;
        case CERTIFICATE_NOT_EXIST:
          fail(actual, ChangeCertificateFailure.AUTHENTICATE_CERTIFICATE_NOT_EXIST);
          break;
        case WRONG_CERTIFICATE:
          fail(actual, ChangeCertificateFailure.WRONG_CERTIFICATE);
          break;
        default:
          fail(actual, ChangeCertificateFailure.WRONG_CERTIFICATE);
          break;
      }
    }

    @Override
    public void onError(Throwable error) {
      if (!actual.isUnsubscribed()) {
        actual.onError(error);
      }
    }
  }

  private class ChangeCertificateSingleSubscriber
      extends SingleSubscriber<Result<Void, ChangeCertificateFailure>> {

    private final SingleSubscriber<? super Result<Void, ChangeCertificateFailure>> actual;

    private ChangeCertificateSingleSubscriber(
        SingleSubscriber<? super Result<Void, ChangeCertificateFailure>> actual) {
      this.actual = actual;
    }

    @Override
    public void onSuccess(Result<Void, ChangeCertificateFailure> value) {
      if (!actual.isUnsubscribed()) {
        actual.onSuccess(value);
      }
    }


    @Override
    public void onError(Throwable error) {
      if (!actual.isUnsubscribed()) {
        actual.onError(error);
      }
    }
  }


}
