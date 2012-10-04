/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.wci.exo.security;

import org.exoplatform.services.security.jaas.DefaultLoginModule;
import org.exoplatform.services.security.jaas.RolePrincipal;
import java.security.Principal;
import java.util.Set;
import javax.security.auth.login.LoginException;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 10/4/12
 */
public class Jetty8LoginModule extends DefaultLoginModule
{

   @Override
   public boolean commit() throws LoginException
   {
      if(super.commit())
      {
         Set<Principal> principals = subject.getPrincipals();
         for(String role : identity.getRoles())
         {
            principals.add(new RolePrincipal(role));
         }
         return true;
      }
      else
      {
         return false;
      }
   }
}
