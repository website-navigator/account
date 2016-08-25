package gq.optimalorange.account.authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Result;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;
import gq.optimalorange.account.internalapi.Results;
import rx.Single;


public class AuthenticationServiceRegister {

  private Map<String, AuthenticationSpi> authServices = new ConcurrentHashMap<>();

  Single<Result<AuthenticationSpi, GetServiceFailureCause>> getService(
      @Nonnull String type) {
    return Single.create(subscriber -> {
      if (subscriber.isUnsubscribed()) {
        return;
      }
      final AuthenticationSpi result = authServices.get(type);
      if (subscriber.isUnsubscribed()) {
        return;
      }
      if (result != null) {
        subscriber.onSuccess(Results.<AuthenticationSpi, GetServiceFailureCause>succeed(result));
      } else {
        Result<AuthenticationSpi, GetServiceFailureCause> r =
            Results.fail(GetServiceFailureCause.NOT_EXIST);
        subscriber.onSuccess(r);
      }
    });
  }

  public void addService(@Nonnull String type, @Nonnull AuthenticationSpi service) {
    authServices.put(type, service);
  }

  enum GetServiceFailureCause {
    NOT_EXIST
  }

}
