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
package org.gatein.wci.test.glassfish3.authentication;

/*
import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
*/

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 10/29/12
 */
public class GFTestRealm{ /*extends AppservRealm
{
   @Override
   protected void init(Properties props) throws BadRealmException, NoSuchRealmException
   {
      super.init(props);
      String jaasCtx = props.getProperty(AppservRealm.JAAS_CONTEXT_PARAM);
      this.setProperty(AppservRealm.JAAS_CONTEXT_PARAM, jaasCtx);
   }

   @Override
   public Enumeration<Object> getGroupNames(String user) throws InvalidOperationException, NoSuchUserException
   {
      return Collections.enumeration(new ArrayList<Object>());
   }

   @Override
   public String getAuthType()
   {
      return "GFTestRealm";
   }
   */
}
