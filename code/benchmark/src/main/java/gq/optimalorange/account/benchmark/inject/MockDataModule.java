package gq.optimalorange.account.benchmark.inject;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.storage.memory.MemoryDatabase;
import okio.ByteString;

@Module
public class MockDataModule {

  @Provides
  static MemoryDatabase provideMemoryDatabase() {
    MemoryDatabase data = new MemoryDatabase();
    data.create();
    data.saveValueWithId("1", "password", "password", ByteString.encodeUtf8("test").sha256());
    return data;
  }

}
