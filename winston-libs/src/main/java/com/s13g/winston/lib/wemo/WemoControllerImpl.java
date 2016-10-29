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

package com.s13g.winston.lib.wemo;

import com.s13g.winston.lib.core.net.HttpUtil;
import com.s13g.winston.lib.core.net.HttpUtil.Method;
import com.s13g.winston.lib.core.xml.XmlUtil;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Default implementation of the Wemo controller.
 * <p>
 * See here for details:
 * https://objectpartners.com/2014/03/25/a-groovy-time-with-upnp-and-wemo/
 * </p>
 */
public class WemoControllerImpl implements WemoController {
  private static final Logger LOG = Logger.getLogger("WemoController");
  private static final String SETUP_URL = "http://%s:49153/setup.xml";
  private static final String EVENT_SERVICE_URL = "%s:49153/eventservice.xml";
  private static final String DEVICE_INFO_SERVICE_URL = "%s:49153/deviceinfoservice.xml";
  // TODO: This can be parsed from the XML repsonses we get from the device.
  private static final String SWITCH_EVENT_URL = "http://%s:49153/upnp/control/basicevent1";

  private static final String PAYLOAD_QUERY = getQueryPayload();
  private static final String PAYLOAD_SWITCH_ON = createSwitchOnOffPayload(true);
  private static final String PAYLOAD_SWITCH_OFF = createSwitchOnOffPayload(false);

  public Optional<WemoSwitch> querySwitch(String ipAddress) {
    Element root;
    try {
      String setupResponse = HttpUtil.requestUrl(String.format(SETUP_URL, ipAddress));
      Optional<Element> optRoot = XmlUtil.getRootFromString(setupResponse);
      if (!optRoot.isPresent()) {
        LOG.log(Level.WARNING, "Cannot parse setup XML: " + ipAddress);
        return Optional.empty();
      }
      root = optRoot.get();
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Could not query switch: " + ipAddress, ex);
      return Optional.empty();
    }

    Namespace ns = root.getNamespace();
    Element deviceElement = root.getChild("device", ns);
    String friendlyName = deviceElement.getChildText("friendlyName", ns);
    String manufacturer = deviceElement.getChildText("manufacturer", ns);
    String modelDescription = deviceElement.getChildText("modelDescription", ns);
    String modelNumber = deviceElement.getChildText("modelNumber", ns);
    String firmwareVersion = deviceElement.getChildText("firmwareVersion", ns);
    String serialNumber = deviceElement.getChildText("serialNumber", ns);
    return Optional.of(new WemoSwitchImpl(friendlyName, manufacturer, modelDescription, modelNumber,
        firmwareVersion, serialNumber, v -> getSwitchState(ipAddress),
        on -> sendSwitchOnOffPayload(ipAddress, on)));
  }

  private Optional<Boolean> getSwitchState(String ip) {
    String url = String.format(SWITCH_EVENT_URL, ip);
    Map<String, String> headers = new HashMap<>();
    headers.put("SOAPACTION", "\"urn:Belkin:service:basicevent:1#GetBinaryState\"");
    headers.put("Content-Type", "text/xml; charset=\"utf-8\"");
    headers.put("Accept", "");
    try {
      String response = HttpUtil.requestUrl(url, Method.GET, headers, Optional.of(PAYLOAD_QUERY));
      return parseSwitchQueryResponse(response);
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Cannot send switch query", ex);
      return Optional.empty();
    }
  }

  /**
   * Parse the response payload we get from a switch query request.
   *
   * @param response the response string
   * @return The state of the button. It will be 'empty' if the response could not be parsed.
   */
  private Optional<Boolean> parseSwitchQueryResponse(String response) {
    Optional<Element> rootOpt = XmlUtil.getRootFromString(response);
    if (!rootOpt.isPresent()) {
      return Optional.empty();
    }
    Namespace soapNamespace = rootOpt.get().getNamespace();
    Element body = rootOpt.get().getChild("Body", soapNamespace);
    if (body == null) {
      LOG.warning("No 'body' found in query response.");
      return Optional.empty();
    }
    Element responseElement = body.getChild("GetBinaryStateResponse", Namespace.getNamespace
        ("urn:Belkin:service:basicevent:1"));
    if (responseElement == null) {
      LOG.warning("No 'GetBinaryStateResponse' found in query response.");
      return Optional.empty();
    }

    Element binaryState = responseElement.getChild("BinaryState");
    if (binaryState == null) {
      LOG.warning("No 'BinaryState' found in query response.");
      return Optional.empty();
    }

    try {
      int value = Integer.parseInt(binaryState.getText());
      return Optional.of(value != 0);
    } catch (NumberFormatException ex) {
      LOG.warning("Cannot parse binary state value in query response.");
      return Optional.empty();
    }
  }

  private boolean sendSwitchOnOffPayload(String ip, boolean on) {
    String url = String.format(SWITCH_EVENT_URL, ip);
    String payload = on ? PAYLOAD_SWITCH_ON : PAYLOAD_SWITCH_OFF;
    Map<String, String> headers = new HashMap<>();
    headers.put("SOAPACTION", "\"urn:Belkin:service:basicevent:1#SetBinaryState\"");
    headers.put("Content-Type", "text/xml; charset=\"utf-8\"");
    headers.put("Accept", "");
    try {
      String response = HttpUtil.requestUrl(url, Method.GET, headers, Optional.of(payload));
      Optional<Element> rootResponse = XmlUtil.getRootFromString(response);
      return rootResponse.isPresent();
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Cannot send switch request", ex);
      return false;
    }
  }

  private static String createSwitchOnOffPayload(boolean on) {
    String xmlPayloadFmt = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
        "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
        "<s:Body>" +
        "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">" +
        "<BinaryState>%d</BinaryState>" +
        "</u:SetBinaryState>" +
        "</s:Body>" +
        "</s:Envelope>";
    return String.format(Locale.US, xmlPayloadFmt, on ? 1 : 0);
  }

  private static String getQueryPayload() {
    return "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
        "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
        "<s:Body>" +
        "<u:GetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">" +
        "<BinaryState>1</BinaryState>" +
        "</u:GetBinaryState>" +
        "</s:Body>" +
        "</s:Envelope>";
  }
}
