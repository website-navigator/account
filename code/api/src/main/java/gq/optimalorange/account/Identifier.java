package gq.optimalorange.account;

import javax.annotation.Nonnull;

public class Identifier {

  public static final String TYPE_ID = "id";

  public static final String TYPE_USERNAME = "username";

  private final String type;

  private final String value;

  @Nonnull
  public static Identifier create(@Nonnull String type, String value) {
    return new Identifier(type, value);
  }

  public static Identifier id(@Nonnull String id) {
    return create(TYPE_ID, id);
  }

  private Identifier(@Nonnull String type, @Nonnull String value) {
    this.type = type;
    this.value = value;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public String getValue() {
    return value;
  }

}
