package edu.tamu.tcat.sda.catalog.rest.internal;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import edu.tamu.tcat.account.AccountException;
import edu.tamu.tcat.account.token.TokenService;
import edu.tamu.tcat.crypto.CryptoProvider;
import edu.tamu.tcat.crypto.SecureToken;
import edu.tamu.tcat.crypto.TokenException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

public class EncryptingUuidTokenService implements TokenService<UUID>
{
   private static final String PROP_TOKEN_KEY = "token.key";

   //TODO: remove these default values and require a config property to be set
   @Deprecated
   final String keyb64_128 = "blahDiddlyBlahSchmacko";
   @Deprecated
   final String keyb64_256 = "blahDiddlyBlahSchmackety+ABitLongerThanThat+";

   private SecureToken secureToken;

   private CryptoProvider crypto;
   private ConfigurationProperties props;

   public void bind(CryptoProvider cp)
   {
      crypto = cp;
   }

   public void bind(ConfigurationProperties cp)
   {
      props = cp;
   }

   public void activate() throws AccountException
   {
      byte[] key;
      try
      {
         String encryptionKey = props.getPropertyValue(PROP_TOKEN_KEY, String.class, keyb64_128);

         key = Base64.getDecoder().decode(encryptionKey);
      }
      catch (Exception e)
      {
         throw new AccountException("Could not decode token key", e);
      }
      try
      {
         secureToken = crypto.getSecureToken(key);
      }
      catch (Exception e)
      {
         throw new AccountException("Could not construct secure token", e);
      }
   }

   @Override
   public TokenService.TokenData<UUID> createTokenData(UUID id) throws AccountException
   {
      ByteBuffer buffer = ByteBuffer.allocate(4 + 8 + 16);
      //HACK: allow configuration of the expiration duration
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
         return new UuidTokenData(stok, id, expires);
      }
      catch (TokenException e)
      {
         throw new AccountException("Could not create token", e);
      }
   }

   @Override
   public UUID unpackToken(String token)
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
      private final String token;
      private final UUID uuid;
      private final ZonedDateTime expiration;

      public UuidTokenData(String token, UUID uuid, ZonedDateTime expiration)
      {
         this.token = token;
         this.uuid = uuid;
         this.expiration = expiration;
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

      @Override
      public ZonedDateTime getExpiration()
      {
         return expiration;
      }
   }
}
