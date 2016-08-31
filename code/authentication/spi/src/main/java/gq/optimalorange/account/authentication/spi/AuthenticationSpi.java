package gq.optimalorange.account.authentication.spi;

import javax.annotation.Nonnull;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import rx.Single;

public interface AuthenticationSpi {

  @Nonnull
  String authenticationType();

  Single<Result<Void, AuthenticationService.AddCertificateFailure>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate newCertificate);

  Single<Result<Void, AuthenticationService.RemoveCertificateFailure>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate toBeRemoved);

  /**
   * @throws IllegalArgumentException if type of oldCertificate and newCertificate isn't {@link
   *                                  #authenticationType()}
   */
  Single<Result<Void, AuthenticationService.ChangeCertificateFailure>> changeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate oldCertificate,
      @Nonnull Certificate newCertificate);

  Single<Result<Void, AuthenticationService.AuthenticateFailure>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate);

}
