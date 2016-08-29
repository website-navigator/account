package gq.optimalorange.account.authentication.password;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import gq.optimalorange.account.AuthenticationService.AddCertificateFailureCause;
import gq.optimalorange.account.AuthenticationService.AuthenticateFailureCause;
import gq.optimalorange.account.AuthenticationService.ChangeCertificateFailureCause;
import gq.optimalorange.account.AuthenticationService.RemoveCertificateFailureCause;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;
import gq.optimalorange.account.internalapi.Results;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import gq.optimalorange.account.internalapi.SubjectStorageService.GetValueFailure;
import okio.ByteString;
import rx.Observable;
import rx.Single;

@Singleton
public class PasswordAuthentication implements AuthenticationSpi {

  private static final String NAMESPACE = "password";

  private static final String KEY = "password";

  private final SubjectStorageService storageService;

  @Inject
  public PasswordAuthentication(SubjectStorageService storageService) {
    this.storageService = storageService;
  }

  @Nonnull
  @Override
  public String authenticationType() {
    return Certificate.TYPE_PASSWORD;
  }

  @Override
  public Single<Result<Void, AddCertificateFailureCause>> addCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate newCertificate) {
    return storageService
        .saveValue(identifier, NAMESPACE, KEY, newCertificate.getValue())
        .map(result -> {
          if (result.succeeded()) {
            return Results.succeed(null);
          } else {
            switch (result.cause()) {
              case UNSUPPORTED_IDENTIFIER_TYPE:
                return Results.fail(AddCertificateFailureCause.UNSUPPORTED_IDENTIFIER_TYPE);
              case SUBJECT_NOT_EXIST:
                return Results.fail(AddCertificateFailureCause.SUBJECT_NOT_EXIST);
              case ALREADY_EXIST:
                return Results.fail(AddCertificateFailureCause.ALREADY_EXIST);
              default:
                throw new UnsupportedOperationException(
                    "unsupported add value failed cause:" + result.cause());
            }
          }
        });
  }

  @Override
  public Single<Result<Void, RemoveCertificateFailureCause>> removeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate toBeRemoved) {
    return storageService
        .deleteValue(identifier, NAMESPACE, KEY)
        .map(result -> {
          if (result.succeeded()) {
            return Results.succeed(null);
          } else {
            switch (result.cause()) {
              case UNSUPPORTED_IDENTIFIER_TYPE:
                return Results.fail(RemoveCertificateFailureCause.UNSUPPORTED_IDENTIFIER_TYPE);
              case SUBJECT_NOT_EXIST:
                return Results.fail(RemoveCertificateFailureCause.SUBJECT_NOT_EXIST);
              case NOT_EXIST:
                return Results.fail(RemoveCertificateFailureCause.CERTIFICATE_NOT_EXIST);
              default:
                throw new UnsupportedOperationException(
                    "unsupported add value failed cause:" + result.cause());
            }
          }
        });
  }

  @Override
  public Single<Result<Void, ChangeCertificateFailureCause>> changeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate oldCertificate,
      @Nonnull Certificate newCertificate) {
    return storageService
        .changeValue(identifier, NAMESPACE, KEY, newCertificate.getValue())
        .map(result -> {
          if (result.succeeded()) {
            return Results.succeed(null);
          } else {
            switch (result.cause()) {
              case UNSUPPORTED_IDENTIFIER_TYPE:
                return Results.fail(ChangeCertificateFailureCause.UNSUPPORTED_IDENTIFIER_TYPE);
              case SUBJECT_NOT_EXIST:
                return Results.fail(ChangeCertificateFailureCause.SUBJECT_NOT_EXIST);
              case NOT_EXIST:
                return Results.fail(ChangeCertificateFailureCause.CERTIFICATE_NOT_EXIST);
              default:
                throw new UnsupportedOperationException(
                    "unsupported add value failed cause:" + result.cause());
            }
          }
        });
  }

  @Override
  public Single<Result<Void, AuthenticateFailureCause>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate) {
    // * read password
    // * compare password
    final Observable<Result<ByteString, GetValueFailure>> retrieve =
        storageService.retrieveValue(identifier, NAMESPACE, KEY) // read
            .toObservable()
            .cacheWithInitialCapacity(1);
            /*.replay()
            .autoConnect();*/
            /*.publish()
            .autoConnect(3);*/

    final Observable<Result<Void, AuthenticateFailureCause>> compared = retrieve
        .filter(Result::succeeded)
        .map(value -> value.result().equals(certificate.getValue())) // compare
        .map(value -> {
          if (value) {
            return Results.succeed(null);
          } else {
            return Results.fail(AuthenticateFailureCause.WRONG_CERTIFICATE);
          }
        });

    final Observable<Result<Void, AuthenticateFailureCause>> retrieveFailed = retrieve
        .filter(Result::failed)
        .map(result -> {
          switch (result.cause()) {
            case UNSUPPORTED_IDENTIFIER_TYPE:
              return Results.fail(AuthenticateFailureCause.UNSUPPORTED_IDENTIFIER_TYPE);
            case SUBJECT_NOT_EXIST:
              return Results.fail(AuthenticateFailureCause.SUBJECT_NOT_EXIST);
            case NOT_EXIST:
              return Results.fail(AuthenticateFailureCause.CERTIFICATE_NOT_EXIST);
            default:
              throw new UnsupportedOperationException(
                  "unsupported add value failed cause:" + result.cause());
          }
        });

    return Observable.merge(compared, retrieveFailed).toSingle();
  }

}
