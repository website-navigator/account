package gq.optimalorange.account.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Result;
import gq.optimalorange.account.authentication.spi.AuthenticationSpi;
import gq.optimalorange.account.internalapi.Results;
import rx.Single;


public class AuthenticationServiceRegister {

  private Map<String, AuthenticationSpi> authServices = new ConcurrentHashMap<>();

  Single<Result<List<String>, Void>> getSupportedCertificateTypes() {
    return Single.create(subscriber -> {
      if (subscriber.isUnsubscribed()) {
        return;
      }

      List<String> result = new ArrayList<>(authServices.keySet());

      if (subscriber.isUnsubscribed()) {
        return;
      }
      subscriber.onSuccess(Results.<List<String>, Void>succeed(result));
    });
  }

  Single<Result<AuthenticationSpi, GetServiceFailure>> getService(
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
        subscriber.onSuccess(Results.<AuthenticationSpi, GetServiceFailure>succeed(result));
      } else {
        Result<AuthenticationSpi, GetServiceFailure> r =
            Results.fail(GetServiceFailure.NOT_EXIST);
        subscriber.onSuccess(r);
      }
    });
  }

  public void addService(@Nonnull AuthenticationSpi service) {
    authServices.put(service.authenticationType(), service);
  }

  enum GetServiceFailure {
    NOT_EXIST
  }

}
