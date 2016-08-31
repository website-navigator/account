package gq.optimalorange.account;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Single;

public interface AuthenticationService extends Service {

  Single<Result<List<String>, Void>> getSupportedCertificateTypes();

  Single<Result<Void, AddCertificateFailure>> addCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate newCertificate);

  enum AddCertificateFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    AUTHENTICATE_CERTIFICATE_NOT_EXIST,
    UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE,
    WRONG_CERTIFICATE,
    ALREADY_EXIST,
    UNSUPPORTED_CERTIFICATE_TYPE
  }

  Single<Result<Void, RemoveCertificateFailure>> removeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate toBeRemoved);

  enum RemoveCertificateFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE,
    AUTHENTICATE_CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE,
    UNSUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST
  }

  Single<Result<Void, ChangeCertificateFailure>> changeCertificate(
      @Nonnull Identifier identifier,
      @Nonnull Certificate forAuthenticate,
      @Nonnull Certificate oldCertificate,
      @Nonnull Certificate newCertificate);

  enum ChangeCertificateFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    UNSUPPORTED_AUTHENTICATE_CERTIFICATE_TYPE,
    AUTHENTICATE_CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE,
    UNSUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST,
    NOT_SAME_CERTIFICATE_TYPE
  }

  Single<Result<Void, AuthenticateFailure>> authenticate(
      @Nonnull Identifier identifier, @Nonnull Certificate certificate);

  enum AuthenticateFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST,
    UNSUPPORTED_CERTIFICATE_TYPE,
    CERTIFICATE_NOT_EXIST,
    WRONG_CERTIFICATE
  }

}
