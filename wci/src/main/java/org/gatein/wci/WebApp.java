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

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.io.IOException;

/**
 * Abstraction of a web application.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public interface WebApp
{

   /**
    * Returns the servlet context of the web application.
    *
    * @return the servlet context
    */
   ServletContext getServletContext();

   /**
    * Returns the class loader of the web application.
    *
    * @return the web application class loader
    */
   ClassLoader getClassLoader();

   /**
    * Returns the context path of the web application.
    *
    * @return the web application context path
    */
   String getContextPath();

   /**
    * Import a file in the war file. The file could not be created for some reasons which are :
    * <ul>
    *   <li>The parent dir exists and is a file</li>
    *   <li>The parent dir does not exist and its creation failed</li>
    *   <li>An underlying exception occurs when writing bytes from the source <code>Inputstream</code> to the target <code>OutputStream</code></li>
    * </ul>
    *
    * @param parentDirRelativePath the parent relative path in the web app starting from the app root
    * @param name                  the name the created file should have
    * @param source                the data of the target file
    * @param overwrite             if false and the file already exists nothing is done
    * @return true if the file has been created
    * @throws IOException if the file cannot be created
    */
   boolean importFile(String parentDirRelativePath, String name, InputStream source, boolean overwrite) throws IOException;
}
