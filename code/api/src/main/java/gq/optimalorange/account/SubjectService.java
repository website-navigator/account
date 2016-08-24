package gq.optimalorange.account;

import rx.Single;

public interface SubjectService extends Service {

  Single<Result<Void, CreateFailureCause>> create(Identifier identifier);

  enum  CreateFailureCause {
    ALREADY_EXIST
  }

  Single<Result<Void, Void>> exist(Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getId(Identifier identifier);

  Single<Result<Identifier, GetIdentifierFailureCause>> getUserName(Identifier identifier);

  enum GetIdentifierFailureCause {
    NOT_EXIST
  }

}
