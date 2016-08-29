package gq.optimalorange.account.storage.memory;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.ExistFailure;
import gq.optimalorange.account.SubjectService.GetIdentifierFailure;
import gq.optimalorange.account.SubjectService.SetIdentifierFailure;
import gq.optimalorange.account.internalapi.Results;
import okio.ByteString;

@NotThreadSafe
public class MemoryDatabase implements Database {

  private volatile long counter = 1;

  private final BidiMap<String, String> usernameIdMap = new DualHashBidiMap<>();

  private final Map<String, Map<String, Map<String, ByteString>>> data = new HashMap<>();

  @Inject
  public MemoryDatabase() {
  }

  @Override
  public Identifier create() {
    String id = null;
    do {
      String nextId = String.valueOf(counter++);
      if (existId(nextId)) {
        continue;
      }
      id = nextId;
    } while (id == null);
    data.put(id, new HashMap<>());
    return Identifier.id(id);
  }

  private boolean existId(String id) {
    return data.containsKey(id);
  }


  @Override
  public Result<Void, ExistFailure> exist(@Nonnull Identifier identifier) {
    switch (identifier.getType()) {
      case Identifier.TYPE_ID:
        return existId(identifier.getValue()) ?
               Results.succeed(null) : Results.fail(ExistFailure.NOT_EXIST);
      case Identifier.TYPE_USERNAME:
        final String id = toId(identifier.getValue());
        return id != null && existId(id) ?
               Results.succeed(null) : Results.fail(ExistFailure.NOT_EXIST);
      default:
        return Results.fail(ExistFailure.UNSUPPORTED_IDENTIFIER_TYPE);
    }
  }

  @Override
  public Result<Void, SetIdentifierFailure> setIdentifier(
      @Nonnull Identifier who, @Nonnull Identifier newIdentifier) {
    String id = null;
    switch (who.getType()) {
      case Identifier.TYPE_ID:
        if (existId(who.getValue())) {
          id = who.getValue();
        }
        break;
      case Identifier.TYPE_USERNAME:
        id = toId(who.getValue());
        break;
      default:
        return Results.fail(SetIdentifierFailure.UNSUPPORTED_LOCATING_IDENTIFIER_TYPE);
    }
    if (id != null) {
      final SetIdentifierFailure result = setIdentifier(id, newIdentifier);
      return result == null ? Results.succeed(null) : Results.fail(result);
    } else {
      return Results.fail(SetIdentifierFailure.SUBJECT_NOT_EXIST);
    }
  }

  /**
   * pre-condition: id exist
   */
  @Nullable
  private SetIdentifierFailure setIdentifier(
      @Nonnull String id, @Nonnull Identifier newIdentifier) {
    switch (newIdentifier.getType()) {
      case Identifier.TYPE_ID:
        if (id.equals(newIdentifier.getValue())) {
          return null;
        } else {
          return SetIdentifierFailure.UNSUPPORTED_MODIFICATION;
        }
      case Identifier.TYPE_USERNAME:
        usernameIdMap.put(newIdentifier.getValue(), id);
        return null;
      default:
        return SetIdentifierFailure.TYPE_OF_NEW_IDENTIFIER_UNSUPPORTED;
    }
  }

  @Override
  public Result<Identifier, GetIdentifierFailure> getIdentifier(
      @Nonnull Identifier identifier, @Nonnull String type) {
    String id = null;
    switch (identifier.getType()) {
      case Identifier.TYPE_ID:
        if (existId(identifier.getValue())) {
          id = identifier.getValue();
        }
        break;
      case Identifier.TYPE_USERNAME:
        id = toId(identifier.getValue());
        break;
      default:
        return Results.fail(GetIdentifierFailure.UNSUPPORTED_LOCATING_IDENTIFIER_TYPE);
    }
    if (id != null) {
      return getIdentifier(id, type);
    } else {
      return Results.fail(GetIdentifierFailure.SUBJECT_NOT_EXIST);
    }
  }

  /**
   * pre-condition: id exist
   */
  @Nonnull
  private Result<Identifier, GetIdentifierFailure> getIdentifier(
      @Nonnull String id, @Nonnull String type) {
    switch (type) {
      case Identifier.TYPE_ID:
        return Results.succeed(Identifier.id(id));
      case Identifier.TYPE_USERNAME:
        final String username = toUsername(id);
        return username != null ?
               Results.succeed(Identifier.username(username)) :
               Results.fail(GetIdentifierFailure.FINDING_IDENTIFIER_NOT_EXIST);
      default:
        return Results.fail(GetIdentifierFailure.TYPE_OF_FINDING_IDENTIFIER_UNSUPPORTED);
    }
  }

  @Nullable
  private String toId(String username) {
    return usernameIdMap.get(username);
  }

  private Result<String, ToIdFailure> toId(@Nonnull Identifier identifier) {
    switch (identifier.getType()) {
      case Identifier.TYPE_ID:
        if (existId(identifier.getValue())) {
          return Results.succeed(identifier.getValue());
        } else {
          return Results.fail(ToIdFailure.SUBJECT_NOT_EXIST);
        }
      case Identifier.TYPE_USERNAME:
        String id = toId(identifier.getValue());
        return id != null ? Results.succeed(id) : Results.fail(ToIdFailure.SUBJECT_NOT_EXIST);
      default:
        return Results.fail(ToIdFailure.UNSUPPORTED_IDENTIFIER_TYPE);
    }
  }

  private enum ToIdFailure {
    UNSUPPORTED_IDENTIFIER_TYPE,
    SUBJECT_NOT_EXIST
  }

  @Nullable
  private String toUsername(String id) {
    return usernameIdMap.getKey(id);
  }

  @Nullable
  private Map<String, ByteString> getNameSpace(@Nonnull String id, @Nonnull String nameSpace) {
    if (!existId(id)) {
      return null;
    }
    final Map<String, Map<String, ByteString>> document = data.get(id);
    assert document != null;
    if (!document.containsKey(nameSpace)) {
      document.put(nameSpace, new HashMap<>());
    }
    return document.get(nameSpace);
  }

  @Override
  public AddValueFailure addValue(@Nonnull Identifier identifier,
                                  @Nonnull String nameSpace, @Nonnull String key,
                                  @Nonnull ByteString value) {
    final Result<String, ToIdFailure> id = toId(identifier);
    if (id.succeeded()) {
      return addValueWithId(id.result(), nameSpace, key, value);
    } else {
      switch (id.cause()) {
        case UNSUPPORTED_IDENTIFIER_TYPE:
          return AddValueFailure.UNSUPPORTED_IDENTIFIER_TYPE;
        case SUBJECT_NOT_EXIST:
          return AddValueFailure.SUBJECT_NOT_EXIST;
        default:
          throw new UnsupportedOperationException("unknown toId failed cause: " + id.cause());
      }
    }
  }

  @Nullable
  private AddValueFailure addValueWithId(
      @Nonnull String id,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return AddValueFailure.SUBJECT_NOT_EXIST;
    }
    if (nameSpaceMap.containsKey(key)) {
      return AddValueFailure.ALREADY_EXIST;
    }
    nameSpaceMap.put(key, value);
    return null;
  }

  @Override
  public DeleteValueFailure deleteValue(@Nonnull Identifier identifier,
                                        @Nonnull String nameSpace, @Nonnull String key) {
    final Result<String, ToIdFailure> id = toId(identifier);
    if (id.succeeded()) {
      return deleteValueWithId(id.result(), nameSpace, key);
    } else {
      switch (id.cause()) {
        case UNSUPPORTED_IDENTIFIER_TYPE:
          return DeleteValueFailure.UNSUPPORTED_IDENTIFIER_TYPE;
        case SUBJECT_NOT_EXIST:
          return DeleteValueFailure.SUBJECT_NOT_EXIST;
        default:
          throw new UnsupportedOperationException("unknown toId failed cause: " + id.cause());
      }
    }
  }

  @Nullable
  private DeleteValueFailure deleteValueWithId(@Nonnull String id,
                                               @Nonnull String nameSpace, @Nonnull String key) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return DeleteValueFailure.SUBJECT_NOT_EXIST;
    }
    if (!nameSpaceMap.containsKey(key)) {
      return DeleteValueFailure.NOT_EXIST;
    }
    nameSpaceMap.remove(key);
    return null;
  }

  @Override
  public ChangeValueFailure changeValue(@Nonnull Identifier identifier,
                                        @Nonnull String nameSpace, @Nonnull String key,
                                        @Nonnull ByteString value) {
    final Result<String, ToIdFailure> id = toId(identifier);
    if (id.succeeded()) {
      return changeValueWithId(id.result(), nameSpace, key, value);
    } else {
      switch (id.cause()) {
        case UNSUPPORTED_IDENTIFIER_TYPE:
          return ChangeValueFailure.UNSUPPORTED_IDENTIFIER_TYPE;
        case SUBJECT_NOT_EXIST:
          return ChangeValueFailure.SUBJECT_NOT_EXIST;
        default:
          throw new UnsupportedOperationException("unknown toId failed cause: " + id.cause());
      }
    }
  }

  @Nullable
  private ChangeValueFailure changeValueWithId(
      @Nonnull String id,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return ChangeValueFailure.SUBJECT_NOT_EXIST;
    }
    if (!nameSpaceMap.containsKey(key)) {
      return ChangeValueFailure.NOT_EXIST;
    }
    nameSpaceMap.put(key, value);
    return null;
  }

  @Override
  public Result<ByteString, GetValueFailure> getValue(@Nonnull Identifier identifier,
                                                      @Nonnull String nameSpace,
                                                      @Nonnull String key) {
    final Result<String, ToIdFailure> id = toId(identifier);
    if (id.succeeded()) {
      return getValueWithId(id.result(), nameSpace, key);
    } else {
      switch (id.cause()) {
        case UNSUPPORTED_IDENTIFIER_TYPE:
          return Results.fail(GetValueFailure.UNSUPPORTED_IDENTIFIER_TYPE);
        case SUBJECT_NOT_EXIST:
          return Results.fail(GetValueFailure.SUBJECT_NOT_EXIST);
        default:
          throw new UnsupportedOperationException("unknown toId failed cause: " + id.cause());
      }
    }
  }

  private Result<ByteString, GetValueFailure> getValueWithId(
      @Nonnull String id, @Nonnull String nameSpace, @Nonnull String key) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return Results.fail(GetValueFailure.SUBJECT_NOT_EXIST);
    }
    if (!nameSpaceMap.containsKey(key)) {
      return Results.fail(GetValueFailure.NOT_EXIST);
    } else {
      return Results.succeed(nameSpaceMap.get(key));
    }
  }

  enum Failure {
    SUBJECT_NOT_EXIST,
    ALREADY_EXIST,
    NOT_EXIST
  }

}
