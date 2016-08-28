package gq.optimalorange.account.internalapi;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.ExistFailureCause;
import gq.optimalorange.account.SubjectService.GetIdentifierFailureCause;
import okio.ByteString;
import rx.Single;

public interface SubjectStorageService extends StorageService {

  /**
   * @return the ID of new created Subject
   */
  Single<Result<Identifier, Void>> create();

  Single<Result<Boolean, ExistFailureCause>> exist(@Nonnull Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getId(@Nonnull Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getUserName(@Nonnull Identifier identifier);

  Single<Result<Void, FailureCause>> saveValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value);

  Single<Result<Void, FailureCause>> deleteValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  Single<Result<Void, FailureCause>> changeValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value
  );

  Single<Result<ByteString, FailureCause>> retrieveValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  enum FailureCause {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

}
