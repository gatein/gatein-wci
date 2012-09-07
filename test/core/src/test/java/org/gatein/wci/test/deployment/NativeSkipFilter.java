/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.gatein.wci.test.deployment;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class NativeSkipFilter implements ServletContextListener
{

   /** . */
   static final HashSet<String> contextPaths = new HashSet<String>();

   @Override
   public void contextInitialized(ServletContextEvent sce)
   {
      contextPaths.add(sce.getServletContext().getContextPath());
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce)
   {
      contextPaths.remove(sce.getServletContext().getContextPath());
   }
}
