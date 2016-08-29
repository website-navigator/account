package gq.optimalorange.account.storage.memory;

import javax.annotation.Nonnull;

import gq.optimalorange.account.Identifier;
import gq.optimalorange.account.Result;
import gq.optimalorange.account.SubjectService.ExistFailure;
import gq.optimalorange.account.SubjectService.GetIdentifierFailure;
import gq.optimalorange.account.SubjectService.SetIdentifierFailure;
import gq.optimalorange.account.internalapi.SubjectStorageService.AddValueFailure;
import gq.optimalorange.account.internalapi.SubjectStorageService.ChangeValueFailure;
import gq.optimalorange.account.internalapi.SubjectStorageService.DeleteValueFailure;
import gq.optimalorange.account.internalapi.SubjectStorageService.GetValueFailure;
import okio.ByteString;

public interface Database {

  Identifier create();

  Result<Void, ExistFailure> exist(@Nonnull Identifier identifier);

  Result<Void, SetIdentifierFailure> setIdentifier(
      @Nonnull Identifier who,
      @Nonnull Identifier newIdentifier);

  Result<Identifier, GetIdentifierFailure> getIdentifier(
      @Nonnull Identifier identifier,
      @Nonnull String type);


  AddValueFailure addValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value);

  DeleteValueFailure deleteValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

  ChangeValueFailure changeValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key,
      @Nonnull ByteString value);

  Result<ByteString, GetValueFailure> getValue(
      @Nonnull Identifier identifier,
      @Nonnull String nameSpace,
      @Nonnull String key);

}
