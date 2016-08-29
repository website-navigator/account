package gq.optimalorange.account.internalapi;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.ExistFailure;
import gq.optimalorange.account.SubjectService.GetIdentifierFailure;
import gq.optimalorange.account.SubjectService.SetIdentifierFailure;
import okio.ByteString;
import rx.Single;

public interface SubjectStorageService extends StorageService {

  /**
   * @return the ID of new created Subject
   */
  Single<Result<Identifier, Void>> create();

  Single<Result<Void, ExistFailure>> exist(@Nonnull Identifier identifier);

  Single<Result<Void, SetIdentifierFailure>> setIdentifier(
      @Nonnull Identifier who,
      @Nonnull Identifier newIdentifier);

  Single<Result<Identifier, GetIdentifierFailure>> getIdentifier(
      @Nonnull Identifier identifier,
      @Nonnull String type);

  Single<Result<Void, AddValueFailure>> saveValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value);

  enum AddValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    ALREADY_EXIST
  }

  Single<Result<Void, DeleteValueFailure>> deleteValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  enum DeleteValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

  Single<Result<Void, ChangeValueFailure>> changeValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value
  );

  enum ChangeValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

  Single<Result<ByteString, GetValueFailure>> retrieveValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  enum GetValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

}
