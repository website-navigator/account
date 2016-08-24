package gq.optimalorange.account;

import javax.annotation.Nonnull;

import okio.ByteString;

public abstract class Identifier {

  private final ByteString value;

  protected Identifier(@Nonnull ByteString value) {
    this.value = value;
  }

  @Nonnull
  public abstract String getType();

  @Nonnull
  public ByteString getValue() {
    return value;
  }

}
