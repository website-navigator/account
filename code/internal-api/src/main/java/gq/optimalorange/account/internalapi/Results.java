package gq.optimalorange.account.internalapi;

import java.util.function.Function;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Result;

public class Results {

  @Nonnull
  public static <T, E> Result<T, E> succeed(T result) {
    return new ResultImpl<>(true, result, null);
  }

  @Nonnull
  public static <T, E> Result<T, E> fail(E cause) {
    return new ResultImpl<>(false, null, cause);
  }

  @Nonnull
  public static <S, R, E> Result<R, E> map(
      @Nonnull Result<S, E> source, @Nonnull Function<S, R> mapFunction) {
    if (source.succeeded()) {
      return succeed(mapFunction.apply(source.result()));
    } else {
      return fail(source.cause());
    }
  }

  private static class ResultImpl<T, E> implements Result<T, E> {

    private final boolean succeeded;

    private final T result;

    private final E cause;

    private ResultImpl(boolean succeeded, T result, E cause) {
      this.succeeded = succeeded;
      this.result = result;
      this.cause = cause;
    }

    @Override
    public boolean succeeded() {
      return succeeded;
    }

    @Override
    public T result() {
      return result;
    }

    @Override
    public E cause() {
      return cause;
    }

  }

}
