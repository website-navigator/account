package gq.optimalorange.account;

import javax.annotation.Nonnull;

import rx.Single;

public interface AuthenticationService extends Service {

  Single<Result<Void, AddCertificateFailureCause>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate newCertificate);

  enum AddCertificateFailureCause {
    SUBJECT_NOT_EXIST,
    WRONG_CERTIFICATE,
    ALREADY_EXIST,
    NOT_SUPPORTED_CERTIFICATE_TYPE
  }

  Single<Result<Void, RemoveCertificateFailureCause>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate toBeRemoved);

  enum RemoveCertificateFailureCause {
    SUBJECT_NOT_EXIST,
    WRONG_CERTIFICATE,
    NOT_SUPPORTED_CERTIFICATE_TYPE
  }

  Single<Result<Void, ChangeCertificateFailureCause>> changeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate oldCertificate,
      @Nonnull Certificate newCertificate);

  enum ChangeCertificateFailureCause {
    SUBJECT_NOT_EXIST,
    WRONG_CERTIFICATE,
    NOT_SUPPORTED_CERTIFICATE_TYPE,
    NOT_SAME_CERTIFICATE_TYPE
  }

  Single<Result<Boolean, AuthenticateFailureCause>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate);

  enum AuthenticateFailureCause {
    SUBJECT_NOT_EXIST,
    NOT_SUPPORTED_CERTIFICATE_TYPE
  }

}
