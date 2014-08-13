package edu.tamu.tcat.sda.catalog.rest.internal;

import java.util.UUID;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.AccountException;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.account.store.AccountStore;

public class CatalogAccountStore implements AccountStore
{
   class CatalogAccount implements Account
   {
      private UUID uuid;
      
      public CatalogAccount(UUID id)
      {
         this.uuid = id;
      }
      
      @Override
      public UUID getId()
      {
         return uuid;
      }
   }
   
   /*
    * Table "account": stores accounts, groups, and roles
    * Table "account_authn": maps accounts that can log in to login provider and login-provider-user-id
    * Table "authn_local": is a "local db" authn provider using "long" for user-id
    * 
    * TODO: create groups for "everyone" and "debug"
    * TODO: create accounts for paul, neal, jesse, matthew
    *       create "account PUT" in the resource, perhaps?
    * TODO: link accounts to local auth in account_authn
    */
   
   @Override
   public Account lookup(LoginData loginData) throws AccountException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Account getAccount(UUID accountId) throws AccountException
   {
      // TODO Auto-generated method stub
      return null;
   }
}
