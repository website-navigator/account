package gq.optimalorange.account;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Single;

public interface AuthenticationService extends Service {

  Single<Result<List<String>, Void>> getSupportedCertificateTypes();

  Single<Result<Void, AddCertificateFailureCause>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate newCertificate);

  enum AddCertificateFailureCause {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    AUTHENTICATE_CERTIFICATE_NOT_EXIST,
    UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE,
    WRONG_CERTIFICATE,
    ALREADY_EXIST,
    NOT_SUPPORTED_CERTIFICATE_TYPE
  }

  Single<Result<Void, RemoveCertificateFailureCause>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate toBeRemoved);

  enum RemoveCertificateFailureCause {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE,
    AUTHENTICATE_CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE,
    NOT_SUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST
  }

  Single<Result<Void, ChangeCertificateFailureCause>> changeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate oldCertificate,
      @Nonnull Certificate newCertificate);

  enum ChangeCertificateFailureCause {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE,
    AUTHENTICATE_CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE,
    NOT_SUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST,
    NOT_SAME_CERTIFICATE_TYPE
  }

  Single<Result<Void, AuthenticateFailureCause>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate);

  enum AuthenticateFailureCause {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    NOT_SUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE
  }

}
