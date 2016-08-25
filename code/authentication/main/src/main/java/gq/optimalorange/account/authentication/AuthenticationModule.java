package gq.optimalorange.account.authentication;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;

@Module
public class AuthenticationModule {

  @Provides
  static AuthenticationService provideAuthenticationService(MultipleTypeAuthenticationService s) {
    return s;
  }

  @Singleton
  @Provides
  static AuthenticationServiceRegister provideRegister(Set<AuthenticationSpi> spis) {
    final AuthenticationServiceRegister register = new AuthenticationServiceRegister();
    spis.forEach(register::addService);
    return register;
  }

}
