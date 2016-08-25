package gq.optimalorange.account;

import javax.annotation.Nonnull;

import rx.Single;

public interface SubjectService extends Service {

  /**
   * @return the ID of new created Subject
   */
  Single<Result<Identifier, Void>> create();

  Single<Result<Boolean, ExistFailureCause>> exist(@Nonnull Identifier identifier);

  enum ExistFailureCause {
    NOT_SUPPORTED_IDENTIFIER_TYPE
  }

  Single<Result<Identifier, GetIdentifierFailureCause>> getId(@Nonnull Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getUserName(@Nonnull Identifier identifier);

  enum GetIdentifierFailureCause {
    NOT_EXIST,
    NOT_SUPPORTED_IDENTIFIER_TYPE
  }

}
