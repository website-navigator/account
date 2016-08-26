package gq.optimalorange.account.integration.inject;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.internalapi.SubjectStorageService;
import gq.optimalorange.account.storage.memory.MemoryDatabase;
import gq.optimalorange.account.storage.memory.MemorySubjectStorageService;
import okio.ByteString;

@Module
public class CustomModule {

  static MemoryDatabase mockMemoryDatabase() {
    MemoryDatabase data = new MemoryDatabase();
    data.create();
    data.saveValueWithId("1", "password", "password", ByteString.encodeUtf8("test").sha256());
    return data;
  }

  @Singleton
  @Provides
  static SubjectStorageService provideSubjectStorageService() {
    return new MemorySubjectStorageService(mockMemoryDatabase());
  }

}
