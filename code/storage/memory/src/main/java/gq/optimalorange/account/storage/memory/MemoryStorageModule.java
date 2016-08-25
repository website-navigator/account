package gq.optimalorange.account.storage.memory;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.internalapi.SubjectStorageService;

@Module
public class MemoryStorageModule {

  @Provides
  static SubjectStorageService provideSubjectStorageService(MemorySubjectStorageService service) {
    return service;
  }

}
