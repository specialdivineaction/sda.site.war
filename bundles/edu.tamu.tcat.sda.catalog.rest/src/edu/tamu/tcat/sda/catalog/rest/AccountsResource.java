package edu.tamu.tcat.sda.catalog.rest;

import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.AccountException;
import edu.tamu.tcat.account.db.login.DatabaseLoginProvider;
import edu.tamu.tcat.account.jaxrs.bean.ContextBean;
import edu.tamu.tcat.account.jaxrs.bean.TokenProviding;
import edu.tamu.tcat.account.login.AccountLoginException;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.account.login.LoginProvider;
import edu.tamu.tcat.account.store.AccountNotFoundException;
import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.crypto.CryptoProvider;
import edu.tamu.tcat.oss.db.DbExecutor;

@Path("/accounts")
public class AccountsResource
{
   private static final String LOGIN_PROVIDER_DB = "db.basic";
   
   private CryptoProvider crypto;
   private DbExecutor dbExec;
   private AccountStore accountStore;
   
   public void bind(CryptoProvider cp)
   {
      crypto = cp;
   }

   public void bind(DbExecutor db)
   {
      dbExec = db;
   }

   public void bind(AccountStore accounts)
   {
      accountStore = accounts;
   }
   
   @GET
   public String doGet()
   {
      return "Get";
   }
   
   @POST
   @Path ("/authenticate")
   @Produces (MediaType.APPLICATION_JSON)
   @TokenProviding(payloadType=UUID.class)
   public AccountDV authenticate(@FormParam("username") String username, @FormParam("password") String password, @BeanParam ContextBean bean) throws AccountException
   {
      if (username == null || username.length() == 0)
         throw new AccountException("Username not specified");
      if (password == null || password.length() == 0)
         throw new AccountException("Password not specified");
      
      //TODO: later, allow the user to select a Login Provider
      String providerId = LOGIN_PROVIDER_DB;
      
      LoginProvider loginProvider = null;
      if (providerId.equals(LOGIN_PROVIDER_DB))
      {
         DatabaseLoginProvider db = new DatabaseLoginProvider();
         db.init(providerId, username, password, crypto, dbExec);
         loginProvider = db;
      }
      else
      {
         throw new BadRequestException("Unknown login provider ["+providerId+"]");
      }

      try
      {
         // provider encapsulates everything, so try to log in (or fail)
         LoginData data = loginProvider.login();
         Account account = accountStore.lookup(data);
         
         bean.set(account.getId());
         return new AccountDV(account);//, getAccountUri(account));
      }
      catch (AccountLoginException | AccountNotFoundException ae)
      {
         throw new ForbiddenException();
      }
   }
   
   /** a serialization data vehicle for {@link Account} */
   static class AccountDV
   {
      public UUID uuid;
      
      public AccountDV(Account acct)
      {
         uuid = acct.getId();
      }
   }

}
