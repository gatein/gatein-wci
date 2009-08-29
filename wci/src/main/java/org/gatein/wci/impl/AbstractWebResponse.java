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
package org.gatein.wci.impl;

import org.gatein.wci.WebResponse;
import org.gatein.common.servlet.URLFormat;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;

/**
 * todo
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractWebResponse extends HttpServletResponseWrapper implements WebResponse
{
   public AbstractWebResponse(HttpServletResponse resp)
   {
      super(resp);
   }

   /**
    * The implementation renders the URL by delegating to the {@link AbstractWebResponse#renderURL(String, java.util.Map, org.gatein.common.servlet.URLFormat)}
    * methods and then prints it in the specified writer. The method can be overriden in order to provide a customized implementation. 
    */
   public void renderURL(Writer writer, String path, Map<String, String[]> parameters, URLFormat wantedURLFormat) throws IllegalArgumentException, IOException
   {
      if (writer == null)
      {
         throw new IllegalArgumentException("No null writer accepted");
      }

      //
      String value = renderURL(path, parameters, wantedURLFormat);

      //
      writer.write(value);
   }
}