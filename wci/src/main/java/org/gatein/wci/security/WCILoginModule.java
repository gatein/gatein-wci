/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.gatein.wci.security;

import org.gatein.wci.authentication.GenericAuthentication;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */

public class WCILoginModule implements LoginModule {
   private Subject subject;
   private CallbackHandler callbackHandler;
   private Map sharedState;
   private Map options;
   
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
      this.subject = subject;
      this.callbackHandler = callbackHandler;
      this.sharedState = sharedState;
      this.options = options;
   }

   /**
    * @see javax.security.auth.spi.LoginModule#login()
    */
   @SuppressWarnings("unchecked")
   public boolean login() throws LoginException {
      Callback[] callbacks = new Callback[2];
      callbacks[0] = new NameCallback("Username");
      callbacks[1] = new PasswordCallback("Password", false);

      try
      {
         callbackHandler.handle(callbacks);
         String password = new String(((PasswordCallback)callbacks[1]).getPassword());

         Credentials credentials = GenericAuthentication.TICKET_SERVICE.validateToken(password, true);
         sharedState.put("javax.security.auth.login.name", credentials.getUsername());
         sharedState.put("javax.security.auth.login.password", credentials.getPassword());
      }
      catch (Exception e)
      {
         LoginException le = new LoginException();
         le.initCause(e);
         throw le;
      }
      return true;
   }

   public boolean commit() throws LoginException {
      return true;
   }

   public boolean abort() throws LoginException {
      return true;
   }

   public boolean logout() throws LoginException {
      return true;
   }
}
