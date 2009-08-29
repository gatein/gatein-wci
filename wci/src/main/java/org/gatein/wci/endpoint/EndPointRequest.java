/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.endpoint;

import org.gatein.wci.impl.AbstractWebRequest;
import org.gatein.wci.IllegalRequestException;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @version $Revision: 630 $
 */
public class EndPointRequest extends AbstractWebRequest
{

   /** . */
   private final String webRequestPath;

   /** . */
   private final String webContextPath;

   /** . */
   private final int mappingType;

   public EndPointRequest(
      HttpServletRequest req,
      String webRequestPath,
      String webContextPath,
      int mappingType)
      throws UnsupportedEncodingException, IllegalRequestException
   {
      super(req);

      //
      this.webRequestPath = webRequestPath;
      this.webContextPath = webContextPath;
      this.mappingType = mappingType;
   }

   public String getWebRequestPath()
   {
      return webRequestPath;
   }

   public String getWebContextPath()
   {
      return webContextPath;
   }

   public int getMappingType()
   {
      return mappingType;
   }
}
