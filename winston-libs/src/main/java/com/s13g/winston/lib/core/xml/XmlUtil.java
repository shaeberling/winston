/*
 * Copyright 2016 The Winston Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.s13g.winston.lib.core.xml;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple XML util class
 */
public final class XmlUtil {
  private static final Logger LOG = Logger.getLogger("XmlUtil");

  private XmlUtil() {
  }

  /**
   * Parses the given XML string and returns the root node.
   *
   * @param xml the XML string.
   * @return The root element, or 'empty' if the XML cannot be parsed.
   */
  public static Optional<Element> getRootFromString(String xml) {
    SAXBuilder builder = new SAXBuilder();
    try {
      return Optional.ofNullable(builder.build(new StringReader(xml)).getRootElement());
    } catch (JDOMException | IOException ex) {
      LOG.log(Level.WARNING, "Error parsing XML", ex);
      return Optional.empty();
    }
  }
}
