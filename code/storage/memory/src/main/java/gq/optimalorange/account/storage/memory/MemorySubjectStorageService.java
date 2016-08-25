package gq.optimalorange.account.storage.memory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import okio.ByteString;
import rx.Single;
import rx.SingleSubscriber;

//TODO thread
@Singleton
public class MemorySubjectStorageService implements SubjectStorageService {

  private final MemoryDatabase database;

  @Inject
  public MemorySubjectStorageService(MemoryDatabase database) {
    this.database = database;
  }

  @Override
  public Single<Result<Identifier, Void>> create() {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        final Result<Identifier, Void> succeed = Results.succeed(Identifier.id(database.create()));
        subscriber.onSuccess(succeed);
      }
    });
  }

  @Override
  public Single<Result<Boolean, ExistFailureCause>> exist(@Nonnull Identifier identifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Single<Result<Identifier, GetIdentifierFailureCause>> getId(
      @Nonnull Identifier identifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Single<Result<Identifier, GetIdentifierFailureCause>> getUserName(
      @Nonnull Identifier identifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Single<Result<Void, FailureCause>> saveValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key,
      @Nonnull ByteString value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Single<Result<Void, FailureCause>> deleteValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Single<Result<Void, FailureCause>> changeValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key,
      @Nonnull ByteString value) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        if (Identifier.TYPE_ID.equals(identifier.getType())) {
          doChangeValue(subscriber, identifier.getValue(), nameSpace, key, value);
        } else if (Identifier.TYPE_USERNAME.equals(identifier.getType())) {
          doChangeValue(subscriber, database.getId(identifier.getValue()), nameSpace, key, value);
        } else {
          final Result<Void, FailureCause> fail =
              Results.fail(FailureCause.UNSUPPORTED_IDENTIFIER_TYPE);
          subscriber.onSuccess(fail);
        }
      }
    });
  }

  private void doChangeValue(
      SingleSubscriber<? super Result<Void, FailureCause>> subscriber,
      String id, String nameSpace, String key, ByteString value) {
    // do operation
    final MemoryDatabase.Failure failure = database.changeValueWithId(id, nameSpace, key, value);
    // emit result
    if (failure == null) {
      final Result<Void, FailureCause> succeed = Results.succeed(null);
      subscriber.onSuccess(succeed);
    } else {
      switch (failure) {
        case SUBJECT_NOT_EXIST: {
          final Result<Void, FailureCause> fail = Results.fail(FailureCause.SUBJECT_NOT_EXIST);
          subscriber.onSuccess(fail);
          break;
        }
        case NOT_EXIST: {
          final Result<Void, FailureCause> fail = Results.fail(FailureCause.NOT_EXIST);
          subscriber.onSuccess(fail);
          break;
        }
        case ALREADY_EXIST:
          throw new AssertionError();
        default:
          throw new UnsupportedOperationException();
      }
    }
  }

  @Override
  public Single<Result<ByteString, FailureCause>> retrieveValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        if (Identifier.TYPE_ID.equals(identifier.getType())) {
          doRetrieveValue(subscriber, identifier.getValue(), nameSpace, key);
        } else if (Identifier.TYPE_USERNAME.equals(identifier.getType())) {
          doRetrieveValue(subscriber, database.getId(identifier.getValue()), nameSpace, key);
        } else {
          final Result<ByteString, FailureCause> fail =
              Results.fail(FailureCause.UNSUPPORTED_IDENTIFIER_TYPE);
          subscriber.onSuccess(fail);
        }
      }
    });
  }

  private void doRetrieveValue(
      SingleSubscriber<? super Result<ByteString, FailureCause>> subscriber,
      String id, String nameSpace, String key) {
    // do operation
    final Result<ByteString, MemoryDatabase.Failure> result =
        database.getValueWithId(id, nameSpace, key);
    // emit result
    if (result.succeeded()) {
      final Result<ByteString, FailureCause> succeed = Results.succeed(result.result());
      subscriber.onSuccess(succeed);
    } else {
      switch (result.cause()) {
        case SUBJECT_NOT_EXIST: {
          final Result<ByteString, FailureCause> f = Results.fail(FailureCause.SUBJECT_NOT_EXIST);
          subscriber.onSuccess(f);
          break;
        }
        case NOT_EXIST: {
          final Result<ByteString, FailureCause> fail = Results.fail(FailureCause.NOT_EXIST);
          subscriber.onSuccess(fail);
          break;
        }
        case ALREADY_EXIST:
          throw new AssertionError();
        default:
          throw new UnsupportedOperationException();
      }
    }
  }

}
