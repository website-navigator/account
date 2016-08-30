package gq.optimalorange.account;

import javax.annotation.Nonnull;

import rx.Single;

public interface SubjectService extends Service {

  /**
   * @return the ID of new created Subject
   */
  Single<Result<Identifier, CreateFailure>> create(@Nonnull Certificate initialCertificate);

  enum CreateFailure {
    CREATE_FAILURE,
    NOT_SUPPORTED_CERTIFICATE_TYPE
  }

  Single<Result<Void, ExistFailure>> exist(@Nonnull Identifier identifier);

  enum ExistFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    NOT_EXIST
  }

  Single<Result<Void, SetIdentifierFailure>> setIdentifier(
      @Nonnull Identifier who,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Identifier newIdentifier);

  enum SetIdentifierFailure {
    UNSUPPORTED_LOCATING_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    UNSUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE,
    TYPE_OF_NEW_IDENTIFIER_UNSUPPORTED,
    UNSUPPORTED_MODIFICATION
  }

  Single<Result<Identifier, GetIdentifierFailure>> getIdentifier(
      @Nonnull Identifier identifier,
      @Nonnull String type);

  enum GetIdentifierFailure {
    UNSUPPORTED_LOCATING_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    TYPE_OF_FINDING_IDENTIFIER_UNSUPPORTED,
    FINDING_IDENTIFIER_NOT_EXIST
  }

}
