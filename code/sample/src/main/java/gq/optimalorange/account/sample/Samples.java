package gq.optimalorange.account.sample;

import gq.optimalorange.account.AuthenticationService.AuthenticateFailureCause;
import gq.optimalorange.account.AuthenticationService.ChangeCertificateFailureCause;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.CreateFailure;
import gq.optimalorange.account.SubjectService.SetIdentifierFailure;
import gq.optimalorange.account.sample.inject.ServiceComponent;
import rx.Observable;

import static gq.optimalorange.account.Certificate.password;

public class Samples {

  public static Observable<Result<?, ? extends Enum<? extends Enum<?>>>> getUseCase(
      ServiceComponent serviceComponent) {
    final double suffix = Math.random() * 1_000_000;
    final String username = "test username " + suffix;
    final String initPassword = "init test password " + suffix;
    final String changedPassword = "changed test password" + suffix;
    // 1. create account
    final Observable<Result<Identifier, CreateFailure>> signUp =
        serviceComponent.getSubjectService().create(password(initPassword)).toObservable().cache();
    // 2. set user name
    final Observable<Result<Void, SetIdentifierFailure>> setUsername = signUp
        .filter(Result::succeeded)
        .flatMap(r -> serviceComponent.getSubjectService()
            .setIdentifier(r.result(), password(initPassword), Identifier.username(username))
            .toObservable());
    // 3. change password
    final Observable<Result<Void, ChangeCertificateFailureCause>> changePassword = signUp
        .filter(Result::succeeded)
        .flatMap(r -> serviceComponent.getAuthenticationService()
            .changeCertificate(r.result(), password(initPassword),
                               password(initPassword), password(changedPassword))
            .toObservable())
        .cache();
    // 4. authenticate
    final Observable<Result<Void, AuthenticateFailureCause>> authenticate = changePassword
        .map(r -> {
          if (r.succeeded()) {
            return changedPassword;
          } else {
            return initPassword;
          }
        })
        .flatMap(r -> serviceComponent.getAuthenticationService()
            .authenticate(Identifier.username(username), password(r))
            .toObservable());

    return Observable.concat(signUp, setUsername, changePassword, authenticate);
  }

}
