package gq.optimalorange.account.internalapi;

import javax.annotation.Nonnull;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import rx.Single;

public interface InternalAuthenticationService extends AuthenticationService {

  Single<Result<Void, AddInitialCertificateFailure>> addInitialCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate initialCertificate);

  enum AddInitialCertificateFailure {
    UNSUPPORTED_CERTIFICATE_TYPE
  }

}
