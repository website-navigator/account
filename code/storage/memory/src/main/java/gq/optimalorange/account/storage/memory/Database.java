package gq.optimalorange.account.storage.memory;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import okio.ByteString;

public interface Database {

  Identifier create();

  Result<Void, ExistFailure> exist(@Nonnull Identifier identifier);

  enum ExistFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    NOT_EXIST
  }

  Result<Void, SetIdentifierFailure> setIdentifier(
      @Nonnull Identifier who,
      @Nonnull Identifier newIdentifier);

  enum SetIdentifierFailure {
    UNSUPPORTED_LOCATING_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    TYPE_OF_NEW_IDENTIFIER_UNSUPPORTED,
    UNSUPPORTED_MODIFICATION,
    NEW_IDENTIFIER_ALREAY_EXIST
  }

  Result<Identifier, GetIdentifierFailure> getIdentifier(
      @Nonnull Identifier identifier,
      @Nonnull String type);

  enum GetIdentifierFailure {
    UNSUPPORTED_LOCATING_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    TYPE_OF_FINDING_IDENTIFIER_UNSUPPORTED,
    FINDING_IDENTIFIER_NOT_EXIST
  }

  AddValueFailure addValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value);

  enum AddValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    ALREADY_EXIST
  }

  DeleteValueFailure deleteValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  enum DeleteValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

  ChangeValueFailure changeValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value);

  enum ChangeValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

  Result<ByteString, GetValueFailure> getValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  enum GetValueFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_EXIST
  }

}
