package gq.optimalorange.account.benchmark.inject;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.storage.memory.MemoryDatabase;
import okio.ByteString;

import static gq.optimalorange.account.Identifier.id;

@Module
public class MockDataModule {

  @Provides
  static MemoryDatabase provideMemoryDatabase() {
    MemoryDatabase data = new MemoryDatabase();
    data.create();
    data.addValue(id("1"), "password", "password", ByteString.encodeUtf8("test").sha256());
    return data;
  }

}
