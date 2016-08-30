package gq.optimalorange.account.storage.memory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.internalapi.SubjectStorageService;

@Module
public class MemoryStorageModule {

  @Provides
  static Database provideDatabase(MemoryDatabase memoryDatabase) {
    return memoryDatabase;
  }

  @Singleton
  @Provides
  static SubjectStorageService provideSubjectStorageService(MemorySubjectStorageService service) {
    return new SerializedSubjectStorageService(service);
  }

}
