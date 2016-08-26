package gq.optimalorange.account.authentication.password;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.AuthenticationService.AuthenticateFailureCause;
import gq.optimalorange.account.AuthenticationService.ChangeCertificateFailureCause;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;
import gq.optimalorange.account.internalapi.Results;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import gq.optimalorange.account.internalapi.SubjectStorageService.FailureCause;
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
  public Single<Result<Void, AuthenticationService.AddCertificateFailureCause>> addCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate newCertificate) {
    // * haven't set
    // * do set
    /*storageService.retrieveValue(identifier, NAMESPACE, KEY)
        .map(retrieve->{
          if
        })*/
    /*return storageService
        .saveValue(identifier, NAMESPACE, KEY, ByteString.encodeUtf8(newCertificate.getType()))
        .map(saveResult->{
          if (saveResult.succeeded()) {
            return Results.succeed(null);
          } else {
            return Results.fail(AuthenticationService.AddCertificateFailureCause.SUBJECT_NOT_EXIST);
          }
        });*/
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public Single<Result<Void, AuthenticationService.RemoveCertificateFailureCause>> removeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate toBeRemoved) {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public Single<Result<Void, ChangeCertificateFailureCause>> changeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate oldCertificate,
      @Nonnull Certificate newCertificate) {
    final Observable<Result<Void, FailureCause>> change =
        storageService.changeValue(identifier, NAMESPACE, KEY, newCertificate.getValue())
            .toObservable().share();

    final Observable<Result<Void, ChangeCertificateFailureCause>> succeeded =
        change.filter(Result::succeeded).map(value -> Results.succeed(null));

    final Observable<Result<Void, ChangeCertificateFailureCause>> noSubject =
        change
            .filter(value -> value.failed() && value.cause() == FailureCause.SUBJECT_NOT_EXIST)
            .map(value -> Results.fail(ChangeCertificateFailureCause.SUBJECT_NOT_EXIST));

    final Observable<Result<Void, ChangeCertificateFailureCause>> notExist =
        change
            .filter(value -> value.failed() && value.cause() == FailureCause.NOT_EXIST)
            .map(value -> Results.fail(ChangeCertificateFailureCause.NOT_EXIST));

    return Observable.merge(succeeded, noSubject, notExist).toSingle();
  }

  @Override
  public Single<Result<Void, AuthenticateFailureCause>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate) {
    // * read password
    // * compare password
    final Observable<Result<ByteString, FailureCause>> retrieve =
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

    final Observable<Result<Void, AuthenticateFailureCause>> noSubject = retrieve
        .filter(value -> value.failed() && value.cause() == FailureCause.SUBJECT_NOT_EXIST)
        .map(value -> Results.fail(AuthenticateFailureCause.SUBJECT_NOT_EXIST));

    final Observable<Result<Void, AuthenticateFailureCause>> notExist = retrieve
        .filter(value -> value.failed() && value.cause() == FailureCause.NOT_EXIST)
        .map(value -> Results.fail(AuthenticateFailureCause.WRONG_CERTIFICATE));

    return Observable.merge(compared, noSubject, notExist).toSingle();
  }

}
