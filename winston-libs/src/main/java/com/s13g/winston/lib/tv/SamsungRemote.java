/*
 * Changes made 2016 an onwards by the Winston authors.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Maarten Visscher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of  this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.s13g.winston.lib.tv;

import com.s13g.winston.lib.core.io.NoOpReader;
import com.s13g.winston.lib.core.io.NoOpWriter;
import com.s13g.winston.lib.core.util.MultiCloseable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Base64;

/**
 * This class has been forked from Maarten Visscher's SamsungRemote project at:
 * <p>
 * https://github.com/mhvis/samsung-tv-control
 * <p>
 * API for controlling Samsung Smart TVs using a mSocket connection on port
 * 55000. The protocol information has been gathered from
 * http://sc0ty.pl/2012/02/samsung-tv-network-remote-control-protocol/ .
 *
 * @author Maarten Visscher <mail@maartenvisscher.nl>
 */
public class SamsungRemote {
  private static final Logger LOG = LogManager.getLogger("SamsungRemote");
  private final int PORT = 55000;
  private final int SO_TIMEOUT = 3 * 1000; // Socket connect and read timeout mIn milliseconds.
  private final int SO_AUTHENTICATE_TIMEOUT = 300 * 1000; // Socket read timeout while
  // authenticating (waiting for user response) mIn milliseconds.
  private final String APP_STRING = "iphone.iapp.samsung";

  private final char[] ALLOWED = {0x64, 0x00, 0x01, 0x00}; // TV return payload.
  private final char[] DENIED = {0x64, 0x00, 0x00, 0x00};
  private final char[] TIMEOUT = {0x65, 0x00};

  private final InetSocketAddress mInetSocketAddress;
  private final Base64.Encoder mEncoder;
  private final String mRemoteName;

  private Socket mSocket;
  private Writer mOut;
  private Reader mIn;
  private MultiCloseable mSocketStreamCloseable;

  /**
   * Opens a mSocket connection to the television and keeps a simple mLog when
   * mDebug is true.
   *
   * @param remoteName the human-readable name of this controller. Will be displaed on the TV during
   * authentication.
   * @param host the host name.
   * @param encoder base64 encoder to be used to encode the commands.
   * @throws IOException if an I/O error occurs when creating the mSocket.
   */
  public SamsungRemote(String remoteName, String host, Base64.Encoder encoder) {
    mRemoteName = remoteName;
    mEncoder = encoder;
    mInetSocketAddress = new InetSocketAddress(host, PORT);
    resetSocketAndStreams();
  }

  /** Recreates the socket and the Input/Output streams. */
  private void resetSocketAndStreams() {
    // First close the streams and socket if they exist.
    if (mSocketStreamCloseable != null) {
      try {
        mSocketStreamCloseable.close();
      } catch (IOException ex) {
        LOG.info("Issue when trying to close socket.", ex);
      }
    }

    // Then create a new socket. The streams wil be no-op until an actual connection is made.
    mOut = new NoOpWriter();
    mIn = new NoOpReader();
    mSocket = new Socket();
    mSocketStreamCloseable = new MultiCloseable().add(mSocket);
  }

  /**
   * Authenticates with the television using host IP address for the ip and id
   * parameters.
   *
   * @param name the name for this controller, which is displayed on the television.
   * @return the response from the television.
   * @throws IOException if an I/O error occurs.
   * @see SamsungRemote#authenticate(java.lang.String, java.lang.String, java.lang.String)
   * authenticate
   */
  private TVReply authenticate(String name) throws IOException {
    String hostAddress = mSocket.getLocalAddress().getHostAddress();
    return authenticate(hostAddress, hostAddress, name);
  }

  /**
   * Authenticates with the television using host IP address for the ip
   * parameter.
   *
   * @param id a parameter for the television.
   * @param name the name for this controller, which is displayed on the television.
   * @return the response from the television.
   * @throws IOException if an I/O error occurs.
   * @see SamsungRemote#authenticate(java.lang.String, java.lang.String, java.lang.String)
   * authenticate
   */
  private TVReply authenticate(String id, String name) throws IOException {
    String hostAddress = mSocket.getLocalAddress().getHostAddress();
    return authenticate(hostAddress, id, name);
  }

  /**
   * Authenticates with the television. Has to be done every time when a new
   * mSocket connection has been made, prior to sending key codes. Blocks
   * while waiting for the television response.
   *
   * @param ip a parameter for the television.
   * @param id a parameter for the television.
   * @param name the name for this controller, which is displayed on the television.
   * @return the response from the television.
   * @throws IOException if an I/O error occurs.
   */
  private TVReply authenticate(String ip, String id, String name)
      throws IOException {
    if (mIn == null) {
      throw new IllegalStateException("Connection not initialized.");
    }
    LOG.info("Authenticating with ip: " + ip + ", id: " + id + ", name: " + name + ".");
    sendPayload(getAuthenticationPayload(ip, id, name));

    mSocket.setSoTimeout(SO_AUTHENTICATE_TIMEOUT);
    char[] payload = readRelevantMessage(mIn);
    mSocket.setSoTimeout(SO_TIMEOUT);

    if (Arrays.equals(payload, ALLOWED)) {
      LOG.info("Authentication response: access granted.");
      return TVReply.ALLOWED; // Access granted.
    } else if (Arrays.equals(payload, DENIED)) {
      LOG.warn("Authentication response: access denied.");
      return TVReply.DENIED; // Access denied.
    } else if (Arrays.equals(payload, TIMEOUT)) {
      LOG.warn("Authentication response: timeout.");
      return TVReply.TIMEOUT; // Timeout.
    }
    LOG.error("Authentication message is unknown: " + new String(payload));
    throw new IOException("Got unknown response.");
  }

  /**
   * Sends a key code to TV, blocks shortly waiting for TV response to check
   * delivery. Only works when you are successfully authenticated.
   *
   * @param keycode the key code to send.
   * @throws IOException if an I/O error occurs.
   */
  public void sendKeycode(KeyCode keycode) throws IOException {
    sendKeycode(keycode.name());
  }

  /**
   * Sends a key code to TV, blocks shortly waiting for TV response to check
   * delivery. Only works when you are successfully authenticated.
   *
   * @param keycode the key code to send.
   * @throws IOException if an I/O error occurs.
   */
  public void sendKeycode(String keycode) throws IOException {
    LOG.info("Sending sendKeycode: " + keycode + ".");
    sendPayload(getKeycodePayload(keycode));
    readMessage(mIn);
  }

  /**
   * Sends a key code to TV mIn a non-blocking manner, thus it does not check
   * the delivery (use checkConnection() to poll the TV status). Only works
   * when you are successfully authenticated.
   *
   * @param keycode the key code to send.
   * @throws IOException if an I/O error occurs.
   */
  public void sendKeycodeAsync(KeyCode keycode) throws IOException {
    sendKeycodeAsync(keycode.name());
  }

  /**
   * Sends a key code to TV mIn a non-blocking manner, thus it does not check
   * the delivery (use checkConnection() to poll the TV status). Only works
   * when you are successfully authenticated.
   *
   * @param keycode the key code to send.
   * @throws IOException if an I/O error occurs.
   */
  public void sendKeycodeAsync(String keycode) throws IOException {
    LOG.info("Sending sendKeycode without reading: " + keycode + ".");
    sendPayload(getKeycodePayload(keycode), false);
  }

  private void sendPayload(String payload) throws IOException {
    sendPayload(payload, true);
  }

  private void sendPayload(String payload, boolean emptyReaderBuffer) throws IOException {
    if (!mSocket.isConnected()) {
      throw new IOException("TV not connected");
    }

    if (emptyReaderBuffer) {
      emptyReaderBuffer(mIn);
    }
    mOut.write(0x00);
    writeString(mOut, APP_STRING);
    writeString(mOut, payload);
    mOut.flush();
  }

  /**
   * Checks the connection by sending an empty key code, does not return
   * anything but instead throws an exception when a problem arose (for
   * instance the TV turned off).
   *
   * @return Whether the connection to the TV is active.
   */
  public boolean isConnected() {
    try {
      sendKeycode("PING");
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * Returns the authentication payload.
   *
   * @param ip the ip of the controller.
   * @param id the id of the controller.
   * @param name the name of the controller.
   * @return the authentication payload.
   * @throws IOException if an I/O error occurs.
   */
  private String getAuthenticationPayload(String ip, String id, String name)
      throws IOException {
    StringWriter writer = new StringWriter();
    writer.write(0x64);
    writer.write(0x00);
    writeBase64(writer, ip);
    writeBase64(writer, id);
    writeBase64(writer, name);
    writer.flush();
    return writer.toString();
  }

  /**
   * Returns the key code payload.
   *
   * @param keycode the key code.
   * @return the key code payload.
   * @throws IOException if an I/O error occurs.
   */
  private String getKeycodePayload(String keycode) throws IOException {
    StringWriter writer = new StringWriter();
    writer.write(0x00);
    writer.write(0x00);
    writer.write(0x00);
    writeBase64(writer, keycode);
    writer.flush();
    return writer.toString();
  }

  /**
   * Reads an incoming message or waits for a new one when it is not relevant.
   * I believe non-relevant messages has to do with showing or hiding of
   * windows on the TV, and start with 0x0a. This method returns the payload
   * of the relevant message.
   *
   * @param reader the reader.
   * @return the payload which was sent with the relevant message.
   */
  private char[] readRelevantMessage(Reader reader) throws IOException {
    char[] payload = readMessage(reader);
    while (payload[0] == 0x0a) {
      LOG.info("Message is not relevant, waiting for new message.");
      payload = readMessage(reader);
    }
    return payload;
  }

  /**
   * Reads an incoming message from the television and returns the payload.
   *
   * @param reader the reader.
   * @return the payload which was sent with the message.
   */
  private char[] readMessage(Reader reader) throws IOException {
    int first = reader.read();
    if (first == -1) {
      throw new IOException("End of stream has been reached (TV could have powered off).");
    }
    String response = readString(reader);
    char[] payload = readCharArray(reader);
    LOG.info("Message: first byte: " + Integer.toHexString(first) + ", response: " +
        response + ", " + "payload: " + readable(payload));
    return payload;
  }

  /**
   * Returns a human readable string mIn hexadecimal of the char array.
   *
   * @param charArray the characters to translate.
   * @return the human readable string.
   */
  private String readable(char[] charArray) {
    String readable = Integer.toHexString(charArray[0]);
    for (int i = 1; i < charArray.length; i++) {
      readable += " " + Integer.toHexString(charArray[i]);
    }
    return readable;
  }

  /**
   * Writes the string length and the string itself to the writer.
   *
   * @param writer the writer.
   * @param string the string to write.
   * @throws IOException if an I/O error occurs.
   */
  private void writeString(Writer writer, String string) throws IOException {
    writer.write(string.length());
    writer.write(0x00);
    writer.write(string);
  }

  /**
   * Encodes the string with base64 and writes the result length and the
   * result itself to the writer.
   *
   * @param writer the writer.
   * @param string the string to encode using base64 and write.
   * @throws IOException if an I/O error occurs.
   */
  private void writeBase64(Writer writer, String string) throws IOException {
    String base64 = mEncoder.encodeToString(string.getBytes());
    writeString(writer, base64);
  }

  /**
   * Reads the next string from the reader.
   *
   * @param reader the reader.
   * @return the string which is read.
   * @throws IOException if an I/O error occurs.
   */
  private String readString(Reader reader) throws IOException {
    return new String(readCharArray(reader));
  }

  /**
   * Reads the next characters from the reader using the length given mIn the
   * first byte.
   *
   * @param reader the reader.
   * @return the characters which were read.
   * @throws IOException if an I/O error occurs.
   */
  private char[] readCharArray(Reader reader) throws IOException {
    int length = reader.read();
    reader.read();
    char[] charArray = new char[length];
    reader.read(charArray);
    return charArray;
  }

  /**
   * Reads all messages which are left mIn the buffer and therefore empties it.
   *
   * @param reader the reader.
   * @throws IOException if an I/O error occurs.
   */
  private void emptyReaderBuffer(Reader reader) throws IOException {
    LOG.info("Emptying reader buffer.");
    while (reader.ready()) {
      readMessage(reader);
    }
  }

  /*package*/ boolean connectAndAuthenticate() {
    if (!connect()) {
      return false;
    }
    try {
      authenticate(mRemoteName);
      return true;
    } catch (IOException ex) {
      LOG.warn("Cannot authenticate", ex);
      return false;
    }
  }

  /**
   * Tries to connect to the TV.
   *
   * @return Whether the connection was successful.
   */
  private boolean connect() {
    try {
      // If the TV is shut down, there is no way to tell whether the connection is shut down and
      // the socket will think it is still connected, even though it's not. In this case, let's
      // make sure we close the socket and the streams before setting it back up.
      if (mSocket.isConnected()) {
        resetSocketAndStreams();
      }
      try {
        mSocket.connect(mInetSocketAddress, SO_TIMEOUT);
      } catch (SocketException ex) {
        LOG.info("Failed to connect to TV, retrying ...");
        resetSocketAndStreams();
        mSocket.connect(mInetSocketAddress, SO_TIMEOUT);
      }
      mSocket.setSoTimeout(SO_TIMEOUT);
      mOut = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
      mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
      mSocketStreamCloseable.add(mOut, mIn);
      return true;
    } catch (IOException ex) {
      LOG.warn("Cannot connect to TV", ex);
      return false;
    }
  }

  /**
   * Closes the mSocket connection. Should always be called at the end of a
   * session.
   */
  public void close() {
    LOG.info("Closing mSocket connection.");
    try {
      mOut = new NoOpWriter();
      mIn = new NoOpReader();
      mSocketStreamCloseable.close();
    } catch (IOException ex) {
      LOG.warn("Error while closing connection", ex);
    }
  }

  /** TV response after authentication. */
  private enum TVReply {
    /** Authenticated, TV will respond to key codes. */
    ALLOWED,
    /** We are not allowed to send key codes (TV user denied this controller). */
    DENIED,
    /** Control request timed mOut or was canceled by the TV user. */
    TIMEOUT
  }
}