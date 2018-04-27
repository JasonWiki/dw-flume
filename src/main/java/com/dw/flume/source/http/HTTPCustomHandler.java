package com.dw.flume.source.http;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.BufferedReader;
import java.nio.charset.UnsupportedCharsetException;

import javax.servlet.http.HttpServletRequest;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTTPCustomHandler implements HTTPSourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPCustomHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Event> getEvents(HttpServletRequest request) throws Exception {
      String charset = request.getCharacterEncoding();
      //UTF-8 is default for JSON. If no charset is specified, UTF-8 is to
      //be assumed.
      if (charset == null) {
        LOG.debug("Charset is null, default charset of UTF-8 will be used.");
        charset = "UTF-8";
      } else if (!(charset.equalsIgnoreCase("utf-8")
              || charset.equalsIgnoreCase("utf-16")
              || charset.equalsIgnoreCase("utf-32"))) {
        LOG.error("Unsupported character set in request {}. "
                + "HTTP handler supports UTF-8, "
                + "UTF-16 and UTF-32 only.", charset);
        throw new UnsupportedCharsetException("HTTP handler supports UTF-8, "
                + "UTF-16 and UTF-32 only.");
      }

      /*
       * Gson throws Exception if the data is not parseable to JSON.
       * Need not catch it since the source will catch it and return error.
       */
      List<Event> eventList = new ArrayList<Event>();
      try {
          // 解析 request body 数据
          BufferedReader reader = request.getReader();
          String str = "";
          StringBuffer body = new StringBuffer(); 

          BufferedReader br = request.getReader();
          while((str = br.readLine()) != null){
              body.append(str);
          }

          // headers
          Map<String, String> headers = new HashMap<String, String>();
          headers.put("type", "HTTPCustom");
          headers.put("timestamp", String.valueOf( System.currentTimeMillis() ));

          byte[] bodyBytes = body.toString().getBytes(charset);
          // body
          Event event = EventBuilder.withBody(bodyBytes, headers);
          eventList.add(event);
          
          //System.out.println(bodyBytes);
          //System.out.println(body);
          //System.out.println(eventList);
      } catch (HTTPBadRequestException ex) {
        throw new HTTPBadRequestException("Request has invalid Http Syntax.", ex);
      }

      return eventList;
    }

    //@Override
    public void configure(Context context) {
    }

}
