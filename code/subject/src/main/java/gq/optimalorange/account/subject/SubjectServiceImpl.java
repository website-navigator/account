package gq.optimalorange.account.subject;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService;
import gq.optimalorange.account.internalapi.InternalAuthenticationService;
import gq.optimalorange.account.internalapi.InternalAuthenticationService.AddInitialCertificateFailure;
import gq.optimalorange.account.internalapi.Results;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import gq.optimalorange.account.subject.utils.Pair;
import rx.Observable;
import rx.Single;

@Singleton
public class SubjectServiceImpl implements SubjectService {

  private final InternalAuthenticationService internalAuthenticationService;

  private final SubjectStorageService storageService;

  @Inject
  public SubjectServiceImpl(
      @Nonnull InternalAuthenticationService internalAuthenticationService,
      @Nonnull SubjectStorageService storageService) {
    this.internalAuthenticationService = internalAuthenticationService;
    this.storageService = storageService;
  }

  // 1. create subject
  // 2. addInitialCertificate
  @Override
  public Single<Result<Identifier, CreateFailure>> create(@Nonnull Certificate initialCertificate) {
    //* 0. check is't initialCertificate.type supported
    final Observable<Result<List<String>, Void>> supportedTypes =
        internalAuthenticationService.getSupportedCertificateTypes().toObservable().cache();
    // [query supportedTypes failed] return NOT_SUPPORTED_CERTIFICATE_TYPE and TODO log
    final Observable<Result<Identifier, CreateFailure>> queryCertTypesFailed = supportedTypes
        .filter(Result::failed)
        .map(r -> Results.fail(CreateFailure.NOT_SUPPORTED_CERTIFICATE_TYPE));
    // [query succeeded][initialCertificate.type not in supported list] return unsupported
    final Observable<Result<Identifier, CreateFailure>> certTypeNotInSupportedList = supportedTypes
        .filter(Result::succeeded)
        .filter(r -> !r.result().contains(initialCertificate.getType()))
        .map(r -> Results.fail(CreateFailure.NOT_SUPPORTED_CERTIFICATE_TYPE));

    //* [query succeeded][initialCertificate.type in supported list] 1. create subject
    Observable<Result<Identifier, Void>> create = supportedTypes
        .filter(Result::succeeded)
        .filter(r -> r.result().contains(initialCertificate.getType()))
        .flatMap(r -> storageService.create().toObservable())
        .cache();
    // [create failed] return CREATE_FAILURE
    final Observable<Result<Identifier, CreateFailure>> createFailed =
        create.filter(Result::failed).map(result -> Results.fail(CreateFailure.CREATE_FAILURE));

    //* [create succeeded] 2. addInitialCertificate
    final Observable<Pair<Result<Identifier, Void>, Result<Void, AddInitialCertificateFailure>>>
        addInitCert = create
        .filter(Result::succeeded)
        .flatMap(result -> internalAuthenticationService
            .addInitialCertificate(result.result(), initialCertificate)
            .toObservable(), Pair::new)
        .cache();
    // [add failed][NOT_SUPPORTED_CERTIFICATE_TYPE] return NOT_SUPPORTED_CERTIFICATE_TYPE
    // because we have check it at 0., shouldn't goto here. so TODO log this error to admin now
    // TODO maybe this cert type is removed after check at 0., so we should undo create here
    final Observable<Result<Identifier, CreateFailure>> unknownCertType = addInitCert
        .filter(result -> result.b.failed())
        .filter(r -> r.b.cause() == AddInitialCertificateFailure.NOT_SUPPORTED_CERTIFICATE_TYPE)
        .map(r -> Results.fail(CreateFailure.NOT_SUPPORTED_CERTIFICATE_TYPE));

    // [add succeeded] return Identifier
    final Observable<Result<Identifier, CreateFailure>> succeeded =
        addInitCert.filter(result -> result.b.succeeded()).map(r -> Results.succeed(r.a.result()));
    return Observable.merge(
        queryCertTypesFailed, certTypeNotInSupportedList, createFailed, unknownCertType, succeeded)
        .toSingle();
  }

  @Override
  public Single<Result<Void, ExistFailure>> exist(@Nonnull Identifier identifier) {
    return storageService.exist(identifier);
  }

  @Override
  public Single<Result<Void, SetIdentifierFailure>> setIdentifier(
      @Nonnull Identifier who, @Nonnull Identifier newIdentifier) {
    return storageService.setIdentifier(who, newIdentifier);
  }

  @Override
  public Single<Result<Identifier, GetIdentifierFailure>> getIdentifier(
      @Nonnull Identifier identifier, @Nonnull String type) {
    return storageService.getIdentifier(identifier, type);
  }

}
