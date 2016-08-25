package gq.optimalorange.account.authentication;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.AuthenticationService.AuthenticateFailureCause;
import gq.optimalorange.account.AuthenticationService.ChangeCertificateFailureCause;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import rx.Single;
import rx.SingleSubscriber;

public class ChangeCertificateOnSubscribe
    implements Single.OnSubscribe<Result<Void, ChangeCertificateFailureCause>> {

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
      SingleSubscriber<? super Result<Void, ChangeCertificateFailureCause>> subscriber) {
    //TODO SafeSubscriber
    if (!oldCertificate.getType().equals(newCertificate.getType())) {
      fail(subscriber, ChangeCertificateFailureCause.NOT_SAME_CERTIFICATE_TYPE);
      return;
    }

    final AuthenticateSingleSubscriber parent = new AuthenticateSingleSubscriber(subscriber);
    subscriber.add(parent);
    authenticationService.authenticate(identifier, forAuthenticate).subscribe(parent);
  }

  private void fail(
      SingleSubscriber<? super Result<Void, ChangeCertificateFailureCause>> subscriber,
      ChangeCertificateFailureCause cause) {
    if (!subscriber.isUnsubscribed()) {
      final Result<Void, ChangeCertificateFailureCause> fail = Results.fail(cause);
      subscriber.onSuccess(fail);
    }
  }

  private class AuthenticateSingleSubscriber
      extends SingleSubscriber<Result<Void, AuthenticateFailureCause>> {

    private final SingleSubscriber<? super Result<Void, ChangeCertificateFailureCause>> actual;

    private AuthenticateSingleSubscriber(
        SingleSubscriber<? super Result<Void, ChangeCertificateFailureCause>> actual) {
      this.actual = actual;
    }

    @Override
    public void onSuccess(Result<Void, AuthenticateFailureCause> authResult) {
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
          final Result<Void, ChangeCertificateFailureCause> fail =
              Results.fail(ChangeCertificateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE);
          return Single.just(fail);
        }
      }).subscribe(parent);
    }

    private void onAuthenticateFailed(AuthenticateFailureCause cause) {
      switch (cause) {
        case SUBJECT_NOT_EXIST:
          fail(actual, ChangeCertificateFailureCause.SUBJECT_NOT_EXIST);
          break;
        case WRONG_CERTIFICATE:
          fail(actual, ChangeCertificateFailureCause.WRONG_CERTIFICATE);
          break;
        case NOT_SUPPORTED_CERTIFICATE_TYPE:
          fail(actual, ChangeCertificateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE);
          break;
        default:
          fail(actual, ChangeCertificateFailureCause.WRONG_CERTIFICATE);
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
      extends SingleSubscriber<Result<Void, ChangeCertificateFailureCause>> {

    private final SingleSubscriber<? super Result<Void, ChangeCertificateFailureCause>> actual;

    private ChangeCertificateSingleSubscriber(
        SingleSubscriber<? super Result<Void, ChangeCertificateFailureCause>> actual) {
      this.actual = actual;
    }

    @Override
    public void onSuccess(Result<Void, ChangeCertificateFailureCause> value) {
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
