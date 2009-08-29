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

import org.gatein.wci.impl.AbstractWebResponse;
import org.gatein.common.servlet.URLFormat;
import org.gatein.common.text.CharBuffer;
import org.gatein.common.text.FastURLEncoder;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @version $Revision: 630 $
 */
public class EndPointResponse extends AbstractWebResponse
{

   /** The fast url encoder. */
   private static final FastURLEncoder urlEncoder = FastURLEncoder.getUTF8Instance();

   /** . */
   private static final URLFormat nullFormat = URLFormat.create(
      null,
      null,
      null,
      null,
      null);

   /** . */
   private String requestRelativePrefix;

   /** . */
   private String requestPrefix;

   /** . */
   private final EndPointRequest req;

   public EndPointResponse(EndPointRequest req, HttpServletResponse resp)
   {
      super(resp);


      //
      this.req = req;
   }

   private String getRequestRelativePrefix()
   {
      if (requestRelativePrefix == null)
      {
         //
         StringBuilder requestRelativePrefix = new StringBuilder();

         //
         requestRelativePrefix.append(req.getScheme()).append("://").append(req.getServerName());
         if (req.isSecure())
         {
            if (req.getServerPort() != 443)
            {
               requestRelativePrefix.append(":").append(Integer.toString(req.getServerPort()));
            }
         }
         else if (req.getServerPort() != 80)
         {
            requestRelativePrefix.append(":").append(Integer.toString(req.getServerPort()));
         }

         //
         requestRelativePrefix.append(req.getContextPath());

         //
         if (req.getMappingType() != EndPointServlet.DEFAULT_SERVLET_MAPPING)
         {
            requestRelativePrefix.append(req.getServletPath());
         }

         //
         this.requestRelativePrefix = requestRelativePrefix.toString();
      }

      //
      return requestRelativePrefix;
   }

   private String getRequestPrefix()
   {
      if (requestPrefix == null)
      {
         this.requestPrefix = req.getMappingType() != EndPointServlet.DEFAULT_SERVLET_MAPPING ? req.getContextPath() : req.getContextPath() + req.getServletPath();
      }

      //
      return requestPrefix;
   }

   public String renderURL(String path, Map<String, String[]> parameters, URLFormat wantedURLFormat) throws IllegalArgumentException
   {
      if (path == null)
      {
         throw new IllegalArgumentException("No null path accepted");
      }
      if (!path.startsWith("/"))
      {
         throw new IllegalArgumentException("Path value " + path + " should start with a trailing '/'");
      }

      //
      if (wantedURLFormat == null)
      {
         wantedURLFormat = nullFormat;
      }

      //
      Buffer buffer = new Buffer(wantedURLFormat);

      //
      return buffer.toString(path, parameters);
   }

   public class Buffer extends CharBuffer
   {

      /** . */
      private final URLFormat format;

      /** . */
      private final int prefixLength;

      /** . */
      private final String parameterSeparator;

      public Buffer(URLFormat format)
      {
         if (Boolean.FALSE.equals(format.getRelative()))
         {
            append(getRequestPrefix());
         }
         else
         {
            append(getRequestRelativePrefix());
         }

         // Save the prefix length
         this.prefixLength = length;
         this.format = format;
         this.parameterSeparator = Boolean.TRUE.equals(format.getEscapeXML()) ? "&amp;" : "&";
      }

      public String toString(String path, Map<String, String[]> parameters)
      {
         // Reset the prefix length
         this.length = prefixLength;

         // julien : check UTF-8 is ok and should not be dependant on the response charset ????
         // same for xml escape
         append(path);

         //
         boolean first = true;
         if (parameters != null)
         {
            for (Map.Entry parameter: parameters.entrySet())
            {
               String name = (String)parameter.getKey();

               //
               if (name == null)
               {
                  throw new IllegalArgumentException("Null key in the parameter map are not allowed");
               }

               //
               String[] values = (String[])parameter.getValue();
               for (String value : values)
               {
                  if (value == null)
                  {
                     throw new IllegalArgumentException("Null value for the key " + name + " in the parameter map are not allowed");
                  }

                  //
                  append(first ? "?" : parameterSeparator);
                  append(name, urlEncoder);
                  append('=');
                  append(value, urlEncoder);
                  first = false;
               }
            }
         }

         // Stringify
         String s = asString();

         // Let the servlet rewrite the URL if necessary
         if (!Boolean.FALSE.equals(format.getServletEncoded()))
         {
            s = encodeURL(s);
         }

         //
         return s;
      }
   }
}
