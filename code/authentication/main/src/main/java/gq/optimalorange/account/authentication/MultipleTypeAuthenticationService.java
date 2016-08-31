package gq.optimalorange.account.authentication;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.authentication.AuthenticationServiceRegister.GetServiceFailure;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;
import gq.optimalorange.account.internalapi.InternalAuthenticationService;
import gq.optimalorange.account.internalapi.Results;
import rx.Observable;
import rx.Single;

@Singleton
public class MultipleTypeAuthenticationService
    implements AuthenticationService, InternalAuthenticationService {

  private final AuthenticationServiceRegister serviceRegister;

  @Inject
  public MultipleTypeAuthenticationService(AuthenticationServiceRegister serviceRegister) {
    this.serviceRegister = serviceRegister;
  }

  @Override
  public Single<Result<List<String>, Void>> getSupportedCertificateTypes() {
    return serviceRegister.getSupportedCertificateTypes();
  }

  // 1. getService(initialCertificate.type)
  // 2. addCertificate
  @Override
  public Single<Result<Void, AddInitialCertificateFailure>> addInitialCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate initialCertificate) {
    //* 1. getService(initialCertificate.type)
    Observable<Result<AuthenticationSpi, GetServiceFailure>> spi =
        serviceRegister.getService(initialCertificate.getType()).toObservable().cache();
    // [getService failed] return UNSUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, AddInitialCertificateFailure>> unsupportedCertType = spi
        .filter(Result::failed)
        .filter(r -> r.cause() == GetServiceFailure.NOT_EXIST)
        .map(r -> Results.fail(AddInitialCertificateFailure.UNSUPPORTED_CERTIFICATE_TYPE));

    //* [getService succeeded] 2. addCertificate
    final Observable<Result<Void, AddCertificateFailure>> addCert =
        spi
            .filter(Result::succeeded)
            .flatMap(r -> r.result().addCertificate(identifier, initialCertificate).toObservable())
            .cache();
    // [addCertificate failed] shouldn't failed so throw a exception
    final Observable<Result<Void, AddInitialCertificateFailure>> addCertFailed =
        addCert.filter(Result::failed).map(r -> {
          throw new IllegalStateException(r.cause().toString());
        });

    // [addCertificate succeeded] return succeeded
    final Observable<Result<Void, AddInitialCertificateFailure>> succeeded =
        addCert.filter(Result::succeeded).map(r -> Results.succeed(null));

    return Observable.merge(unsupportedCertType, addCertFailed, succeeded).toSingle();
  }

  // 1. authenticate
  // 2. getService(initialCertificate.type)
  // 3. addCertificate
  @Override
  public Single<Result<Void, AddCertificateFailure>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate newCertificate) {
    //* 1. authenticate
    final Observable<Result<Void, AuthenticateFailure>> auth =
        authenticate(identifier, forAuthenticate).toObservable().cache();
    // [auth failed]
    final Observable<Result<Void, AddCertificateFailure>> authFailed =
        auth.filter(Result::failed).map(r -> {
          switch (r.cause()) {
            case UNSUPPORTED_IDENTIFIER_TYPE:
              return Results.fail(AddCertificateFailure.UNSUPPORTED_IDENTIFIER_TYPE);
            case SUBJECT_NOT_EXIST:
              return Results.fail(AddCertificateFailure.SUBJECT_NOT_EXIST);
            case UNSUPPORTED_CERTIFICATE_TYPE:
              return Results
                  .fail(AddCertificateFailure.UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE);
            case CERTIFICATE_NOT_EXIST:
              return Results.fail(AddCertificateFailure.AUTHENTICATE_CERTIFICATE_NOT_EXIST);
            case WRONG_CERTIFICATE:
              return Results.fail(AddCertificateFailure.WRONG_CERTIFICATE);
            default:
              throw new UnsupportedOperationException("unsupported auth fail cause: " + r.cause());
          }
        });

    //* [auth succeeded] 2. getService(initialCertificate.type)
    final Observable<Result<AuthenticationSpi, GetServiceFailure>> spi = auth
        .filter(Result::succeeded)
        .flatMap(r -> serviceRegister.getService(newCertificate.getType()).toObservable())
        .cache();
    // [getService failed] return UNSUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, AddCertificateFailure>> unsupportedCertType = spi
        .filter(Result::failed)
        .filter(r -> r.cause() == GetServiceFailure.NOT_EXIST)
        .map(r -> Results.fail(AddCertificateFailure.UNSUPPORTED_CERTIFICATE_TYPE));

    //* [getService succeeded] 3. addCertificate
    final Observable<Result<Void, AddCertificateFailure>> addCert = spi
        .filter(Result::succeeded)
        .flatMap(r -> r.result().addCertificate(identifier, newCertificate).toObservable());
    // [addCertificate failed] return this cause
    // [addCertificate succeeded] return succeeded
    return Observable.merge(authFailed, unsupportedCertType, addCert).toSingle();
  }

  // 1. authenticate
  // 2. getService(initialCertificate.type)
  // 3. removeCertificate
  @Override
  public Single<Result<Void, RemoveCertificateFailure>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate toBeRemoved) {
    //* 1. authenticate
    final Observable<Result<Void, AuthenticateFailure>> auth =
        authenticate(identifier, forAuthenticate).toObservable().cache();
    // [auth failed]
    final Observable<Result<Void, RemoveCertificateFailure>> authFailed =
        auth.filter(Result::failed).map(r -> {
          switch (r.cause()) {
            case UNSUPPORTED_IDENTIFIER_TYPE:
              return Results.fail(RemoveCertificateFailure.UNSUPPORTED_IDENTIFIER_TYPE);
            case SUBJECT_NOT_EXIST:
              return Results.fail(RemoveCertificateFailure.SUBJECT_NOT_EXIST);
            case UNSUPPORTED_CERTIFICATE_TYPE:
              return Results
                  .fail(RemoveCertificateFailure.UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE);
            case CERTIFICATE_NOT_EXIST:
              return Results.fail(RemoveCertificateFailure.AUTHENTICATE_CERTIFICATE_NOT_EXIST);
            case WRONG_CERTIFICATE:
              return Results.fail(RemoveCertificateFailure.WRONG_CERTIFICATE);
            default:
              throw new UnsupportedOperationException("unsupported auth fail cause: " + r.cause());
          }
        });

    //* [auth succeeded] 2. getService(initialCertificate.type)
    final Observable<Result<AuthenticationSpi, GetServiceFailure>> spi = auth
        .filter(Result::succeeded)
        .flatMap(r -> serviceRegister.getService(toBeRemoved.getType()).toObservable())
        .cache();
    // [getService failed] return UNSUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, RemoveCertificateFailure>> unsupportedCertType = spi
        .filter(Result::failed)
        .filter(r -> r.cause() == GetServiceFailure.NOT_EXIST)
        .map(r -> Results.fail(RemoveCertificateFailure.UNSUPPORTED_CERTIFICATE_TYPE));

    //* [getService succeeded] 3. removeCertificate
    final Observable<Result<Void, RemoveCertificateFailure>> removeCert = spi
        .filter(Result::succeeded)
        .flatMap(r -> r.result().removeCertificate(identifier, toBeRemoved).toObservable());
    // [addCertificate failed] return this cause
    // [addCertificate succeeded] return succeeded
    return Observable.merge(authFailed, unsupportedCertType, removeCert).toSingle();
  }

  // 1. authenticate
  // 2. getService(initialCertificate.type)
  // 3. changeCertificate
  @Override
  public Single<Result<Void, ChangeCertificateFailure>> changeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate oldCertificate, @Nonnull Certificate newCertificate) {
    return Single.create(new ChangeCertificateOnSubscribe(
        this, serviceRegister, identifier, forAuthenticate, oldCertificate, newCertificate));
  }

  @Override
  public Single<Result<Void, AuthenticateFailure>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate) {
    return serviceRegister.getService(certificate.getType()).flatMap(service -> {
      if (service.succeeded()) {
        return service.result().authenticate(identifier, certificate);
      } else {
        return Single.just(Results.fail(AuthenticateFailure.UNSUPPORTED_CERTIFICATE_TYPE));
      }
    });
  }

}
