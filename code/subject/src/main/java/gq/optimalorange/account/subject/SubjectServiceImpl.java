package gq.optimalorange.account.subject;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import rx.Single;

public class SubjectServiceImpl implements SubjectService {

  private final SubjectStorageService storageService;

  public SubjectServiceImpl(@Nonnull SubjectStorageService storageService) {
    this.storageService = storageService;
  }

  @Override
  public Single<Result<Identifier, Void>> create() {
    return storageService.create();
  }

  @Override
  public Single<Result<Boolean, ExistFailureCause>> exist(@Nonnull Identifier identifier) {
    return storageService.exist(identifier);
  }

  @Override
  public Single<Result<Identifier, GetIdentifierFailureCause>> getId(
      @Nonnull Identifier identifier) {
    return storageService.getId(identifier);
  }

  @Override
  public Single<Result<Identifier, GetIdentifierFailureCause>> getUserName(
      @Nonnull Identifier identifier) {
    return storageService.getUserName(identifier);
  }

}
