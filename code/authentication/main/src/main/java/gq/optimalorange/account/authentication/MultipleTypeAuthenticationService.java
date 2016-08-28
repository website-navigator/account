package gq.optimalorange.account.authentication;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.authentication.AuthenticationServiceRegister.GetServiceFailureCause;
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
    Observable<Result<AuthenticationSpi, GetServiceFailureCause>> spi =
        serviceRegister.getService(initialCertificate.getType()).toObservable().cache();
    // [getService failed] return NOT_SUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, AddInitialCertificateFailure>> unsupportedCertType = spi
        .filter(Result::failed)
        .filter(r -> r.cause() == GetServiceFailureCause.NOT_EXIST)
        .map(r -> Results.fail(AddInitialCertificateFailure.NOT_SUPPORTED_CERTIFICATE_TYPE));

    //* [getService succeeded] 2. addCertificate
    final Observable<Result<Void, AddCertificateFailureCause>> addCert =
        spi
            .filter(Result::succeeded)
            .flatMap(r -> r.result().addCertificate(identifier, initialCertificate).toObservable())
            .cache();
    // [addCertificate failed] shouldn't failed so TODO log it

    // [addCertificate succeeded] return succeeded
    final Observable<Result<Void, AddInitialCertificateFailure>> succedded =
        addCert.filter(Result::succeeded).map(r -> Results.succeed(null));

    return Observable.merge(unsupportedCertType, succedded).toSingle();
  }

  // 1. authenticate
  // 2. getService(initialCertificate.type)
  // 3. addCertificate
  @Override
  public Single<Result<Void, AddCertificateFailureCause>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate newCertificate) {
    //* 1. authenticate
    final Observable<Result<Void, AuthenticateFailureCause>> auth =
        authenticate(identifier, forAuthenticate).toObservable().cache();
    // [auth failed][SUBJECT_NOT_EXIST] return SUBJECT_NOT_EXIST
    // [auth failed][WRONG_CERTIFICATE] return WRONG_CERTIFICATE
    // [auth failed][NOT_SUPPORTED_CERTIFICATE_TYPE] return NOT_SUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, AddCertificateFailureCause>> authFailed =
        auth.filter(Result::failed).map(r -> {
          switch (r.cause()) {
            case SUBJECT_NOT_EXIST:
              return Results.fail(AddCertificateFailureCause.SUBJECT_NOT_EXIST);
            case WRONG_CERTIFICATE:
              return Results.fail(AddCertificateFailureCause.WRONG_CERTIFICATE);
            case NOT_SUPPORTED_CERTIFICATE_TYPE:
              return Results.fail(AddCertificateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE);
            default:
              throw new UnsupportedOperationException("unsupported auth fail cause: " + r.cause());
          }
        });

    //* [auth succeeded] 2. getService(initialCertificate.type)
    final Observable<Result<AuthenticationSpi, GetServiceFailureCause>> spi = auth
        .filter(Result::succeeded)
        .flatMap(r -> serviceRegister.getService(newCertificate.getType()).toObservable())
        .cache();
    // [getService failed] return NOT_SUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, AddCertificateFailureCause>> unsupportedCertType = spi
        .filter(Result::failed)
        .filter(r -> r.cause() == GetServiceFailureCause.NOT_EXIST)
        .map(r -> Results.fail(AddCertificateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE));

    //* [getService succeeded] 3. addCertificate
    final Observable<Result<Void, AddCertificateFailureCause>> addCert = spi
        .filter(Result::succeeded)
        .flatMap(r -> r.result().addCertificate(identifier, newCertificate).toObservable());
    // [addCertificate failed][SUBJECT_NOT_EXIST] return SUBJECT_NOT_EXIST
    // [addCertificate failed][ALREADY_EXIST] return ALREADY_EXIST
    // [addCertificate succeeded] return succeeded
    return Observable.merge(authFailed, unsupportedCertType, addCert).toSingle();
  }

  // 1. authenticate
  // 2. getService(initialCertificate.type)
  // 3. removeCertificate
  @Override
  public Single<Result<Void, RemoveCertificateFailureCause>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate toBeRemoved) {
    //* 1. authenticate
    final Observable<Result<Void, AuthenticateFailureCause>> auth =
        authenticate(identifier, forAuthenticate).toObservable().cache();
    // [auth failed][SUBJECT_NOT_EXIST] return SUBJECT_NOT_EXIST
    // [auth failed][WRONG_CERTIFICATE] return WRONG_CERTIFICATE
    // [auth failed][NOT_SUPPORTED_CERTIFICATE_TYPE] return NOT_SUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, RemoveCertificateFailureCause>> authFailed =
        auth.filter(Result::failed).map(r -> {
          switch (r.cause()) {
            case SUBJECT_NOT_EXIST:
              return Results.fail(RemoveCertificateFailureCause.SUBJECT_NOT_EXIST);
            case WRONG_CERTIFICATE:
              return Results.fail(RemoveCertificateFailureCause.WRONG_CERTIFICATE);
            case NOT_SUPPORTED_CERTIFICATE_TYPE:
              return Results.fail(RemoveCertificateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE);
            default:
              throw new UnsupportedOperationException("unsupported auth fail cause: " + r.cause());
          }
        });

    //* [auth succeeded] 2. getService(initialCertificate.type)
    final Observable<Result<AuthenticationSpi, GetServiceFailureCause>> spi = auth
        .filter(Result::succeeded)
        .flatMap(r -> serviceRegister.getService(toBeRemoved.getType()).toObservable())
        .cache();
    // [getService failed] return NOT_SUPPORTED_CERTIFICATE_TYPE
    final Observable<Result<Void, RemoveCertificateFailureCause>> unsupportedCertType = spi
        .filter(Result::failed)
        .filter(r -> r.cause() == GetServiceFailureCause.NOT_EXIST)
        .map(r -> Results.fail(RemoveCertificateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE));

    //* [getService succeeded] 3. removeCertificate
    final Observable<Result<Void, RemoveCertificateFailureCause>> removeCert = spi
        .filter(Result::succeeded)
        .flatMap(r -> r.result().removeCertificate(identifier, toBeRemoved).toObservable());
    // [addCertificate failed][SUBJECT_NOT_EXIST] return SUBJECT_NOT_EXIST
    // [addCertificate failed][ALREADY_EXIST] return ALREADY_EXIST
    // [addCertificate succeeded] return succeeded
    return Observable.merge(authFailed, unsupportedCertType, removeCert).toSingle();
  }

  // 1. authenticate
  // 2. getService(initialCertificate.type)
  // 3. changeCertificate
  @Override
  public Single<Result<Void, ChangeCertificateFailureCause>> changeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate oldCertificate, @Nonnull Certificate newCertificate) {
    return Single.create(new ChangeCertificateOnSubscribe(
        this, serviceRegister, identifier, forAuthenticate, oldCertificate, newCertificate));
  }

  @Override
  public Single<Result<Void, AuthenticateFailureCause>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate) {
    return serviceRegister.getService(certificate.getType()).flatMap(service -> {
      if (service.succeeded()) {
        return service.result().authenticate(identifier, certificate);
      } else {
        return Single.just(Results.fail(AuthenticateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE));
      }
    });
  }

}
