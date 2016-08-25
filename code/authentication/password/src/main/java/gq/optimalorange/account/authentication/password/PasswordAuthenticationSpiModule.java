package gq.optimalorange.account.authentication.password;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;

@Module
public class PasswordAuthenticationSpiModule {

  @Provides
  @IntoSet
  static AuthenticationSpi provideAuthenticationSpi(PasswordAuthentication spi) {
    return spi;
  }

}
