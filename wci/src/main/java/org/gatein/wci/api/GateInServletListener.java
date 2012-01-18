/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.api;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.gatein.wci.impl.generic.GenericServletContainerContext;
import org.gatein.wci.impl.generic.GenericWebAppContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class GateInServletListener implements ServletContextListener
{

	public void contextInitialized(ServletContextEvent event)
	{
		ServletContext servletContext = event.getServletContext();
		
		String contextPath = servletContext.getContextPath();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		GenericWebAppContext webAppContext = new GenericWebAppContext(servletContext, contextPath, classLoader);

		GateInServletRegistrations.register(webAppContext, "/PortletWrapper");
	}

	public void contextDestroyed(ServletContextEvent event)
	{
		ServletContext servletContext = event.getServletContext();
		GateInServletRegistrations.unregister(servletContext);
	}

}
