package gq.optimalorange.account;

import javax.annotation.Nonnull;

import okio.ByteString;

public class Certificate {

  public static final String TYPE_PASSWORD = "password";

  private final String type;

  private final ByteString value;

  public static Certificate password(@Nonnull String password) {
    return create(TYPE_PASSWORD, ByteString.encodeUtf8(password).sha256());
  }

  private static Certificate create(@Nonnull String type, @Nonnull ByteString value) {
    return new Certificate(type, value);
  }

  protected Certificate(@Nonnull String type, @Nonnull ByteString value) {
    this.type = type;
    this.value = value;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public ByteString getValue() {
    return value;
  }

}
