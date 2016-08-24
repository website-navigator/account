package gq.optimalorange.account;

import javax.annotation.Nonnull;

import rx.Single;

public interface SubjectService extends Service {

  /**
   * @return the ID of new created Subject
   */
  Single<Result<Identifier, Void>> create();

  Single<Result<Void, Void>> exist(@Nonnull Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getId(@Nonnull Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getUserName(@Nonnull Identifier identifier);

  enum GetIdentifierFailureCause {
    NOT_EXIST
  }

}
