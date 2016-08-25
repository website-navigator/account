package gq.optimalorange.account.authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import rx.Single;


public class AuthenticationServiceRegister {

  private Map<String, AuthenticationService> authServices = new ConcurrentHashMap<>();

  Single<Result<AuthenticationService, GetServiceFailureCause>> getService(
      @Nonnull String type) {
    return Single.create(subscriber -> {
      if (subscriber.isUnsubscribed()) {
        return;
      }
      final AuthenticationService result = authServices.get(type);
      if (subscriber.isUnsubscribed()) {
        return;
      }
      if (result != null) {
        Result<AuthenticationService, GetServiceFailureCause> r = Results.succeed(result);
        subscriber.onSuccess(r);
      } else {
        Result<AuthenticationService, GetServiceFailureCause> r =
            Results.fail(GetServiceFailureCause.NOT_EXIST);
        subscriber.onSuccess(r);
      }
    });
  }

  public void addService(@Nonnull String type, @Nonnull AuthenticationService service) {
    authServices.put(type, service);
  }

  enum GetServiceFailureCause {
    NOT_EXIST
  }

}
