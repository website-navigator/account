package gq.optimalorange.account.internalapi;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService;
import okio.ByteString;
import rx.Single;

public interface SubjectStorageService extends StorageService, SubjectService {

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
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

}
