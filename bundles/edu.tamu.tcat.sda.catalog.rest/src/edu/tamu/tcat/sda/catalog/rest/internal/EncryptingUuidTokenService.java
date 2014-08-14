package edu.tamu.tcat.sda.catalog.rest.internal;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import edu.tamu.tcat.account.token.AccountTokenException;
import edu.tamu.tcat.account.token.TokenService;
import edu.tamu.tcat.crypto.CryptoProvider;
import edu.tamu.tcat.crypto.SecureToken;
import edu.tamu.tcat.crypto.TokenException;

public class EncryptingUuidTokenService implements TokenService<UUID>
{
   final String keyb64_128 = "blahDiddlyBlahSchmacko";
   final String keyb64_256 = "blahDiddlyBlahSchmackety+ABitLongerThanThat+";
   private SecureToken secureToken;
   
   private CryptoProvider crypto;
   
   public void bind(CryptoProvider cp)
   {
      crypto = cp;
   }
   
   public void activate() throws AccountTokenException
   {
      byte[] key;
      try
      {
         key = Base64.getDecoder().decode(keyb64_128);
      }
      catch (Exception e)
      {
         throw new AccountTokenException("Could not decode token key", e);
      }
      try
      {
         secureToken = crypto.getSecureToken(key);
      }
      catch (Exception e)
      {
         throw new AccountTokenException("Could not construct secure token", e);
      }
   }
   
   @Override
   public TokenService.TokenData<UUID> createTokenData(UUID id, long expiresIn, TimeUnit expiresInUnit) throws AccountTokenException
   {
      ByteBuffer buffer = ByteBuffer.allocate(4 + 8 + 16);
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime expires = now.plus(2, ChronoUnit.WEEKS);
      buffer.putInt(1);
      buffer.putLong(Instant.from(expires).toEpochMilli());
      buffer.putLong(id.getMostSignificantBits());
      buffer.putLong(id.getLeastSignificantBits());
      buffer.flip();
      try
      {
         String stok = secureToken.getToken(buffer);
         String exp = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(expires);
         return new UuidTokenData(stok, id, exp);
      }
      catch (TokenException e)
      {
         throw new AccountTokenException("Could not create token", e);
      }
   }
   
   @Override
   public UUID unpackToken(String token) throws AccountTokenException
   {
      return UUID.fromString(token);
   }
   
   @Override
   public Class<UUID> getPayloadType()
   {
      return UUID.class;
   }
   
   static class UuidTokenData implements TokenService.TokenData<UUID>
   {
      private String token;
      private String expireStr;
      private UUID uuid;

      public UuidTokenData(String t, UUID uuid, String expStr)
      {
         token = t;
         this.uuid = uuid;
         expireStr = expStr;
      }
      
      @Override
      public String getToken()
      {
         return token;
      }
      
      @Override
      public UUID getPayload()
      {
         return uuid;
      }

      @Deprecated
      @Override
      public String getExpireStr()
      {
         return expireStr;
      }
   }
}
