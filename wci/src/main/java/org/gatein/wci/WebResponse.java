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

import org.gatein.common.servlet.URLFormat;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.Writer;
import java.io.IOException;

/**
 * Extends the HttpServletResponse interface to add web module concepts.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public interface WebResponse extends HttpServletResponse
{

   /**
    * <p>Renders an URL and returns the rendered string.</p>
    *
    * <p>The path argument is mandatory and must begin with '/' char. The parameters argument is optional and the
    * wantedURLFormat is also optional.</p>
    *
    * <p>If the parameter map is not null, it must provide a key set with no null elements and the values must be
    * string arrays with no null entries. Any entry with an empty length value will be skipped.</p>
    *
    * @param path the path relative to the web context
    * @param parameters the optional parameter map
    * @param wantedURLFormat the url format needed
    * @return the rendered URL
    * @throws IllegalArgumentException if the path value is not correct or the parameter map is corrupted
    */
   String renderURL(String path, Map<String, String[]> parameters, URLFormat wantedURLFormat) throws IllegalArgumentException;

   /**
    * Renders an URL in the provided writer.
    *
    * @see org.gatein.wci.WebResponse#renderURL(String, java.util.Map, org.gatein.common.servlet.URLFormat)
    * @param writer the writer
    * @param path the path relative to the web context
    * @param parameters the optional parameter map
    * @param wantedURLFormat the url format needed
    * @throws IllegalArgumentException if the path value is not correct or the write is null or the parameter map is corrupted
    * @throws IOException any IOException thrown by the writer
    */
   void renderURL(Writer writer, String path, Map<String, String[]> parameters, URLFormat wantedURLFormat) throws IllegalArgumentException, IOException;

}
