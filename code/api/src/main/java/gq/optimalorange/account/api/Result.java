package gq.optimalorange.account.api;

public interface Result<T, E> {

  boolean succeeded();

  T result();

  default boolean failed() {
    return !succeeded();
  }

  E cause();

}
