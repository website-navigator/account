package gq.optimalorange.account.authentication;

import javax.annotation.Nonnull;

import gq.optimalorange.account.AuthenticationService;
import gq.optimalorange.account.Certificate;
import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import rx.Single;

public class MultipleTypeAuthenticationService implements AuthenticationService {

  private final AuthenticationServiceRegister serviceRegister;

  public MultipleTypeAuthenticationService(AuthenticationServiceRegister serviceRegister) {
    this.serviceRegister = serviceRegister;
  }

  @Override
  public Single<Result<Void, AddCertificateFailureCause>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate newCertificate) {
    throw new UnsupportedOperationException(); //TODO
  }

  @Override
  public Single<Result<Void, RemoveCertificateFailureCause>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate toBeRemoved) {
    throw new UnsupportedOperationException(); //TODO
  }

  @Override
  public Single<Result<Void, ChangeCertificateFailureCause>> changeCertificate(
      @Nonnull Identifier identifier, @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate oldCertificate, @Nonnull Certificate newCertificate) {
    return Single.create(new ChangeCertificateOnSubscribe(
        this, serviceRegister, identifier, forAuthenticate, oldCertificate, newCertificate));
  }

  @Override
  public Single<Result<Void, AuthenticateFailureCause>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate) {
    return serviceRegister.getService(certificate.getType()).flatMap(service -> {
      if (service.succeeded()) {
        return service.result().authenticate(identifier, certificate);
      } else {
        return Single.just(Results.fail(AuthenticateFailureCause.NOT_SUPPORTED_CERTIFICATE_TYPE));
      }
    });
  }

}
