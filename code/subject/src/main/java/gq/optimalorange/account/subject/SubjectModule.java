package gq.optimalorange.account.subject;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.SubjectService;

@Module
public class SubjectModule {

  @Provides
  static SubjectService provideSubjectService(SubjectServiceImpl service) {
    return service;
  }

}
