/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wci;

import org.gatein.wci.WebAppListener;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebApp;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class WebAppRegistry implements WebAppListener
{

   /** . */
   final Map<String, WebApp> map = new HashMap<String, WebApp>();

   public void onEvent(WebAppEvent event)
   {
      if (event instanceof WebAppLifeCycleEvent)
      {
         WebApp webApp = event.getWebApp();
         WebAppLifeCycleEvent lfEvent = (WebAppLifeCycleEvent)event;
         if (lfEvent.getType() == WebAppLifeCycleEvent.ADDED)
         {
            map.put(webApp.getContextPath(), webApp);
         }
         else
         {
            map.remove(webApp.getContextPath());
         }
      }
   }

   public WebApp getWebApp(String key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException();
      }
      return map.get(key);
   }

   public Set<String> getKeys()
   {
      return map.keySet();
   }
}
