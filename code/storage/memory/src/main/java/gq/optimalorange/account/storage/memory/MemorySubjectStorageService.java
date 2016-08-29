package gq.optimalorange.account.storage.memory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.ExistFailure;
import gq.optimalorange.account.SubjectService.GetIdentifierFailure;
import gq.optimalorange.account.SubjectService.SetIdentifierFailure;
import gq.optimalorange.account.internalapi.Results;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import okio.ByteString;
import rx.Single;

@NotThreadSafe
public class MemorySubjectStorageService implements SubjectStorageService {

  private final Database database;

  @Inject
  public MemorySubjectStorageService(Database database) {
    this.database = database;
  }

  @Override
  public Single<Result<Identifier, Void>> create() {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        final Result<Identifier, Void> succeed = Results.succeed(database.create());
        subscriber.onSuccess(succeed);
      }
    });
  }

  @Override
  public Single<Result<Void, ExistFailure>> exist(@Nonnull Identifier identifier) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onSuccess(database.exist(identifier));
      }
    });
  }

  @Override
  public Single<Result<Void, SetIdentifierFailure>> setIdentifier(
      @Nonnull Identifier who, @Nonnull Identifier newIdentifier) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onSuccess(database.setIdentifier(who, newIdentifier));
      }
    });
  }

  @Override
  public Single<Result<Identifier, GetIdentifierFailure>> getIdentifier(
      @Nonnull Identifier identifier, @Nonnull String type) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onSuccess(database.getIdentifier(identifier, type));
      }
    });
  }

  @Override
  public Single<Result<Void, AddValueFailure>> saveValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key,
      @Nonnull ByteString value) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        AddValueFailure addValueFailure = database.addValue(identifier, nameSpace, key, value);
        subscriber.onSuccess(addValueFailure == null ?
                             Results.<Void, AddValueFailure>succeed(null) :
                             Results.<Void, AddValueFailure>fail(addValueFailure));
      }
    });
  }

  @Override
  public Single<Result<Void, DeleteValueFailure>> deleteValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        final DeleteValueFailure delFailure = database.deleteValue(identifier, nameSpace, key);
        subscriber.onSuccess(delFailure == null ?
                             Results.<Void, DeleteValueFailure>succeed(null) :
                             Results.<Void, DeleteValueFailure>fail(delFailure));
      }
    });
  }

  @Override
  public Single<Result<Void, ChangeValueFailure>> changeValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key,
      @Nonnull ByteString value) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        ChangeValueFailure changeFailure = database.changeValue(identifier, nameSpace, key, value);
        subscriber.onSuccess(changeFailure == null ?
                             Results.<Void, ChangeValueFailure>succeed(null) :
                             Results.<Void, ChangeValueFailure>fail(changeFailure));
      }
    });
  }

  @Override
  public Single<Result<ByteString, GetValueFailure>> retrieveValue(
      @Nonnull Identifier identifier, @Nonnull String nameSpace, @Nonnull String key) {
    return Single.create(subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onSuccess(database.getValue(identifier, nameSpace, key));
      }
    });
  }

}
