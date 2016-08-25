package gq.optimalorange.account.storage.memory;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import gq.optimalorange.account.Result;
import gq.optimalorange.account.internalapi.Results;
import okio.ByteString;

@NotThreadSafe
public class MemoryDatabase {

  volatile long counter = 1;

  BidiMap<String, String> idUsernameMap = new DualHashBidiMap<>();

  Map<String, Map<String, Map<String, ByteString>>> data = new HashMap<>();

  @Inject
  public MemoryDatabase() {
  }

  String create() {
    String id = null;
    do {
      String nextId = String.valueOf(counter++);
      if (exist(nextId)) {
        continue;
      }
      id = nextId;
    } while (id == null);
    data.put(id, new HashMap<>());
    return id;
  }

  boolean exist(String id) {
    return data.containsKey(id);
  }

  String getId(String username) {
    return idUsernameMap.getKey(username);
  }

  String getUsername(String id) {
    return idUsernameMap.get(id);
  }

  @Nullable
  private Map<String, ByteString> getNameSpace(@Nonnull String id, @Nonnull String nameSpace) {
    if (!exist(id)) {
      return null;
    }
    final Map<String, Map<String, ByteString>> document = data.get(id);
    assert document != null;
    if (!document.containsKey(nameSpace)) {
      document.put(nameSpace, new HashMap<>());
    }
    return document.get(nameSpace);
  }

  Failure saveValueWithId(@Nonnull String id, @Nonnull String nameSpace, @Nonnull String key,
                          @Nonnull ByteString value) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return Failure.SUBJECT_NOT_EXIST;
    }
    if (nameSpaceMap.containsKey(key)) {
      return Failure.ALREADY_EXIST;
    }
    nameSpaceMap.put(key, value);
    return null;
  }

  Failure saveValueWithUsername(@Nonnull String username, @Nonnull String nameSpace,
                                @Nonnull String key,
                                @Nonnull ByteString value) {
    return saveValueWithId(getId(username), nameSpace, key, value);
  }

  Failure changeValueWithId(@Nonnull String id, @Nonnull String nameSpace, @Nonnull String key,
                            @Nonnull ByteString value) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return Failure.SUBJECT_NOT_EXIST;
    }
    if (!nameSpaceMap.containsKey(key)) {
      return Failure.NOT_EXIST;
    }
    nameSpaceMap.put(key, value);
    return null;
  }

  Failure changeValueWithUsername(@Nonnull String username, @Nonnull String nameSpace,
                                  @Nonnull String key,
                                  @Nonnull ByteString value) {
    return changeValueWithId(getId(username), nameSpace, key, value);
  }

  Result<ByteString, Failure> getValueWithId(@Nonnull String id, @Nonnull String nameSpace,
                                             @Nonnull String key) {
    Map<String, ByteString> nameSpaceMap = getNameSpace(id, nameSpace);
    if (nameSpaceMap == null) {
      return Results.fail(Failure.SUBJECT_NOT_EXIST);
    }
    if (!nameSpaceMap.containsKey(key)) {
      return Results.fail(Failure.NOT_EXIST);
    } else {
      return Results.succeed(nameSpaceMap.get(key));
    }
  }

  Result<ByteString, Failure> getValueWithUsername(@Nonnull String username,
                                                   @Nonnull String nameSpace, @Nonnull String key) {
    return getValueWithId(getId(username), nameSpace, key);
  }

  enum Failure {
    SUBJECT_NOT_EXIST,
    ALREADY_EXIST,
    NOT_EXIST
  }

}
