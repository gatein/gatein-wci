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

import org.gatein.common.net.media.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Extends the HttpServletRequest interface to add web module concepts.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public interface WebRequest extends HttpServletRequest
{

   public enum Verb
   {
      GET,
      POST
   }

   /** . */
   MediaType APPLICATION_X_WWW_FORM_URLENCODED_MEDIA_TYPE = MediaType.APPLICATION_X_WWW_FORM_URLENCODED;

   /** . */
   MediaType MULTIPART_FORM_DATA_MEDIA_TYPE = MediaType.MULTIPART_FORM_DATA_MEDIA_TYPE;

   /** . */
   Charset UTF_8_CHARSET = Charset.forName("UTF-8");

   /**
    * Returns the an enum instead of a string as returned by
    * {@link javax.servlet.http.HttpServletRequest#getMethod()} which is more convenient to use sometimes.
    *
    * @return the verb
    */
   Verb getVerb();

   /**
    * Returns the query parameter map. If no query string was provided it returns an empty map in order
    * to avoid to return a null object.
    *
    * @return the query parameter map
    */
   Map<String, String[]> getQueryParameterMap();

   /**
    * Returns a parameter value from the query string or null if it cannot be found.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    */
   String getQueryParameter(String parameterName);

   /**
    * Returns a parameter values from the query string or null if it cannot be found.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    */
   String[] getQueryParameterValues(String parameterName);

   /**
    * Returns a parameter value from the body or null if it cannot be found. If the content type of the
    * request is not <code>application/x-www-form-urlencoded</code> then this method will always return null.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    */
   String getBodyParameter(String parameterName);

   /**
    * Returns a parameter values from the body or null if it cannot be found. If the content type of the
    * request is not <code>application/x-www-form-urlencoded</code> then this method will always return null.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    */
   String[] getBodyParameterValues(String parameterName);

   /**
    * Returns the body of the request when the request is of type POST otherwise return null.
    *
    * @return the body
    */
   Body getBody();

   /**
    * Returns the media type or null if none was provided.
    *
    * @return the media type
    */
   MediaType getMediaType();

   /**
    * Returns the web request path which is a consistent value and does not depend on the kind of mapping of the underlying
    * servlet. It is never null and always start with a '/' char.
    *
    * @return the web request path
    */
   String getWebRequestPath();

   /**
    * Returns the web context path. The web context path value is computed such as the value returned by the method
    * {@link javax.servlet.http.HttpServletRequest#getRequestURI()} is a prefix of the concatenation of the web context
    * path and the web request path.
    *
    * @return the web context path
    */
   String getWebContextPath();
}
