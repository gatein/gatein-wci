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

import org.gatein.common.net.media.MediaType;
import org.gatein.wci.Body;
import org.gatein.wci.IllegalRequestException;
import org.gatein.wci.WebRequest;
import org.gatein.wci.util.RequestDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;

/**
 * Add useful information about an <code>HttpServletRequest</code>.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractWebRequest extends HttpServletRequestWrapper implements WebRequest
{

   /** . */
   public static final MediaType APPLICATION_X_WWW_FORM_URLENCODED_MEDIA_TYPE = MediaType.APPLICATION_X_WWW_FORM_URLENCODED;

   /** . */
   public static final MediaType MULTIPART_FORM_DATA_MEDIA_TYPE = MediaType.create("multipart/form-data");

   /** . */
   public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

   /** . */
   private final Map<String, String[]> queryParameterMap;

   /** . */
   private final Body body;

   /** . */
   private final Verb verb;

   /** . */
   private final MediaType mediaType;

   public AbstractWebRequest(HttpServletRequest req) throws UnsupportedEncodingException, IllegalRequestException
   {
      super(req);

      //
      Verb verb;
      if ("GET".equals(req.getMethod()))
      {
         verb = Verb.GET;
      }
      else if ("POST".equals(req.getMethod()))
      {
         verb = Verb.POST;
      }
      else
      {
         throw new IllegalRequestException("HTTP Method " + req.getMethod() + " not accepted");
      }

      //
      RequestDecoder decoder = new RequestDecoder(req);


      //
      this.verb = verb;
      this.queryParameterMap = decoder.getQueryParameters();
      this.body = decoder.getBody();
      this.mediaType = decoder.getMediaType();
   }

   public Verb getVerb()
   {
      return verb;
   }

   public Map<String, String[]> getQueryParameterMap()
   {
      return queryParameterMap;
   }

   public Body getBody()
   {
      return body;
   }

   public MediaType getMediaType()
   {
      return mediaType;
   }

   public String getQueryParameter(String parameterName)
   {
      String[] values = getQueryParameterValues(parameterName);

      //
      return values != null ? values[0] : null;  
   }

   public String[] getQueryParameterValues(String parameterName)
   {
      if (parameterName == null)
      {
         throw new IllegalArgumentException();
      }

      return queryParameterMap.get(parameterName);
   }

   public String getBodyParameter(String parameterName)
   {
      String[] values = getBodyParameterValues(parameterName);

      //
      return values != null ? values[0] : null;
   }

   public String[] getBodyParameterValues(String parameterName)
   {
      if (parameterName == null)
      {
         throw new IllegalArgumentException();
      }

      //
      return body instanceof Body.Form ? ((Body.Form)body).getParameters().get(parameterName) : null;
   }
}