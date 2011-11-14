package edu.mit.people.landa.secretmessages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Constructs and processes a call to a web service.
 * 
 * @author Yafim Landa <landa@csail.mit.edu>
 * @author Aubrey Tatarowicz <altat@csail.mit.edu>
 */
public class Request {
  private static final String ENCODING = "UTF-8";

  /**
   * Forms an HTTP request and parses the response for a request.
   * 
   * @param URL The URL to query
   * @param requestMethod The request method to use (GET or POST)
   * @param params A collection of query parameters
   * @return The response JSONObject
   * @throws IOException
   */
  private static JSONObject request(String URL, String requestMethod, HashMap<String, String> params) throws JSONException, IOException {
    StringBuilder url = new StringBuilder();
    url.append(URL).append("?");
    for (String key : params.keySet()) {
      url.append(key).append("=").append(URLEncoder.encode(params.get(key), ENCODING)).append("&");
    }

    HttpURLConnection uc = (HttpURLConnection) new URL(url.toString()).openConnection();
    uc.setRequestMethod(requestMethod);
    uc.setDoInput(true);
    uc.setDoOutput(true);
    InputStream is = uc.getInputStream();
    String result = toString(is);
    return new JSONObject(result);
  }
  
  /**
   * Forms an HTTP GET request and parses the response.
   * 
   * @param URL The URL to query
   * @param params A collection of query parameters
   * @return The response JSONObject
   * @throws Exception
   */
  public static JSONObject get(String URL, HashMap<String, String> params) throws Exception {
    return request(URL, "GET", params);
  }
  /**
   * Forms an HTTP GET request and parses the response.
   * 
   * @param URL The URL to query
   * @param params A string that will be parsed into a Map @see str2map
   * @return The response JSONObject
   * @throws Exception
   */
  public static JSONObject get(String URL, String params) throws Exception {
    return request(URL, "GET", str2map(params));
  }
  
  /**
   * Forms an HTTP POST request and parses the response.
   * 
   * @param URL The URL to query
   * @param params A collection of query parameters
   * @return The response JSONObject
   * @throws Exception
   */
  public static JSONObject post(String URL, HashMap<String, String> params) throws Exception {
    return request(URL, "POST", params);
  }
  /**
   * Forms an HTTP POST request and parses the response.
   * 
   * @param URL The URL to query
   * @param params A string that will be parsed into a Map @see str2map
   * @return The response JSONObject
   * @throws Exception
   */
  public static JSONObject post(String URL, String params) throws Exception {
    return request(URL, "POST", str2map(params));
  }
  
  /**
   * Turns a JSON-formatted map into a HashMap
   * 
   * @param params A String of parameters formatted like a JSON object
   * @return A HashMap of parameter key-value pairs
   * @throws JSONException
   */
  public static HashMap<String, String> str2map(String params) throws JSONException {
    JSONObject jsonObject = new JSONObject(params);
    HashMap<String, String> ret = new HashMap<String, String>();
    Iterator it = jsonObject.keys();
    while (it.hasNext()) {
      String key = (String) it.next();
      ret.put(key, jsonObject.getString(key));
    }
    return ret;
  }
  
  /**
   * Upload a file using POST multipart data.
   * This code is modified from a post on this page: http://www.anddev.org/upload_files_to_web_server-t443.html
   * 
   * @param fileName The path to the file that needs to be uploaded
   * @param targetURL The URL to the script that can accept the file
   * @return The server response
   * @throws IOException
   */
  public static String upload(String fileName, String targetURL) throws IOException {
    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    DataInputStream inStream = null;

    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;

    FileInputStream fileInputStream = new FileInputStream(new File(fileName));
    URL url = new URL(targetURL);
    conn = (HttpURLConnection) url.openConnection();

    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    conn.setRequestMethod("POST");

    conn.setRequestProperty("Connection", "Keep-Alive");
    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

    dos = new DataOutputStream(conn.getOutputStream());
    dos.writeBytes(twoHyphens + boundary + lineEnd);
    dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
    dos.writeBytes(lineEnd);

    bytesAvailable = fileInputStream.available();
    bufferSize = Math.min(bytesAvailable, maxBufferSize);
    buffer = new byte[bufferSize];

    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

    while (bytesRead > 0) {
      dos.write(buffer, 0, bufferSize);
      bytesAvailable = fileInputStream.available();
      bufferSize = Math.min(bytesAvailable, maxBufferSize);
      bytesRead = fileInputStream.read(buffer, 0, bufferSize);
    }

    dos.writeBytes(lineEnd);
    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

    fileInputStream.close();
    dos.flush();
    dos.close();

    inStream = new DataInputStream(conn.getInputStream());
    String str;
    StringBuffer result = new StringBuffer();
    
    try {
      while ((str = inStream.readUTF()) != null)
        result.append(str);
    }
    catch (EOFException e) {
      e.printStackTrace();
    }
    inStream.close();
    
    return result.toString();
  }
  
  /**
   * Downloads a file.
   * 
   * @param url The URL of the file to download
   * @param destination The path to which the file will be downloaded
   * @throws MalformedURLException
   * @throws IOException
   */
  public static void download(String url, String destination) throws MalformedURLException, IOException {
    BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
    FileOutputStream out = new FileOutputStream(destination);
    BufferedOutputStream bufout = new BufferedOutputStream(out, 1024);
    byte data[] = new byte[1024];
    while (in.read(data, 0, 1024) >= 0) {
      bufout.write(data);
    }
    bufout.close();
    in.close();
  }
  
  /**
   * Reads an InputStream and returns its contents as a String. Also effects
   * rate control.
   * 
   * @param inputStream The InputStream to read from.
   * @return The contents of the InputStream as a String.
   * @throws Exception
   */
  private static String toString(InputStream inputStream) throws IOException {
    StringBuilder outputBuilder = new StringBuilder();
    try {
      String string;
      if (inputStream != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
        while (null != (string = reader.readLine())) {
          outputBuilder.append(string).append('\n');
        }
      }
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    return outputBuilder.toString();
  }
}