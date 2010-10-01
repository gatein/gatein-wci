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

package org.gatein.wci.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class AuthenticationListenerSupport
{
  public enum EventType
  {
    LOGIN, LOGOUT
  }

  private List<AuthenticationListener> authenticationListeners = new ArrayList<AuthenticationListener>();

  public void addAuthenticationListener(AuthenticationListener listener)
  {
    authenticationListeners.add(listener);
  }

  protected List<AuthenticationListener> getAuthenticationListeners()
  {
    return authenticationListeners;
  }

  public void fireEvent(EventType type, AuthenticationEvent ae)
  {
    String methodName = String.format(
      "on%1%2",
      type.toString().substring(0, 1).toUpperCase(),
      type.toString().substring(1)
    );
    for (AuthenticationListener currentListener : authenticationListeners)
    {
      try
      {
        currentListener.getClass().getMethod(methodName, AuthenticationEvent.class).invoke(currentListener, ae);
      }
      catch (Exception ignore)
      {
      }
    }
  }
}
