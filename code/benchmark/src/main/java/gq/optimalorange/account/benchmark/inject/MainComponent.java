package gq.optimalorange.account.benchmark.inject;

import javax.inject.Singleton;

import dagger.Component;
import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.SubjectService;
import gq.optimalorange.account.authentication.AuthenticationModule;
import gq.optimalorange.account.authentication.password.PasswordAuthenticationSpiModule;
import gq.optimalorange.account.storage.memory.MemoryStorageModule;
import gq.optimalorange.account.subject.SubjectModule;

@Singleton
@Component(modules = {
    SubjectModule.class,
    AuthenticationModule.class,
    PasswordAuthenticationSpiModule.class,
    MemoryStorageModule.class,
    MockDataModule.class
})
public interface MainComponent {

  SubjectService getSubjectService();

  AuthenticationService getAuthenticationService();

}
