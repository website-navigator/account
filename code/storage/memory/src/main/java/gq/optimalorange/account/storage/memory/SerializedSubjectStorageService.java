package gq.optimalorange.account.storage.memory;

import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import okio.ByteString;
import rx.Scheduler;
import rx.Single;
import rx.schedulers.Schedulers;

@ThreadSafe
public class SerializedSubjectStorageService implements SubjectStorageService {

  private final Scheduler workerScheduler = Schedulers.from(Executors.newFixedThreadPool(1));

  private final SubjectStorageService actual;

  public SerializedSubjectStorageService(SubjectStorageService actual) {
    this.actual = actual;
  }

  private <R> Single<R> receiveRequest(Single<R> request) {
    return request.subscribeOn(workerScheduler);
  }

  private static <R> Single<R> sendResponse(Single<R> response) {
    return response.observeOn(Schedulers.computation());
  }

  private <R> Single<R> wrap(Single<R> request) {
    return request.to(this::receiveRequest).to(SerializedSubjectStorageService::sendResponse);
  }

  @Override
  public Single<Result<Identifier, Void>> create() {
    return actual.create().to(this::wrap);
  }

  @Override
  public Single<Result<Void, SubjectService.ExistFailure>> exist(
      @Nonnull Identifier identifier) {
    return actual.exist(identifier).to(this::wrap);
  }

  @Override
  public Single<Result<Void, SubjectService.SetIdentifierFailure>> setIdentifier(
      @Nonnull Identifier who, @Nonnull Identifier newIdentifier) {
    return actual.setIdentifier(who, newIdentifier).to(this::wrap);
  }

  @Override
  public Single<Result<Identifier, SubjectService.GetIdentifierFailure>> getIdentifier(
      @Nonnull Identifier identifier, @Nonnull String type) {
    return actual.getIdentifier(identifier, type).to(this::wrap);
  }

  @Override
  public Single<Result<Void, AddValueFailure>> saveValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key,
      @Nonnull ByteString value) {
    return actual.saveValue(identifier, nameSpace, key, value).to(this::wrap);
  }

  @Override
  public Single<Result<Void, DeleteValueFailure>> deleteValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key) {
    return actual.deleteValue(identifier, nameSpace, key).to(this::wrap);
  }

  @Override
  public Single<Result<Void, ChangeValueFailure>> changeValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key,
      @Nonnull ByteString value) {
    return actual.changeValue(identifier, nameSpace, key, value).to(this::wrap);
  }

  @Override
  public Single<Result<ByteString, GetValueFailure>> retrieveValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key) {
    return actual.retrieveValue(identifier, nameSpace, key).to(this::wrap);
  }

}
