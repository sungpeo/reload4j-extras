package org.apache.log4j.filter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.util.Compare;
import org.apache.log4j.util.ControlFilter;
import org.apache.log4j.util.Filter;
import org.apache.log4j.util.JunitTestRunnerFilter;
import org.apache.log4j.util.LineNumberFilter;
import org.apache.log4j.util.SunReflectFilter;
import org.apache.log4j.util.Transformer;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * Various tests verifying that filters work properly and that 
 * JoranConfigurator can effectively parse config files containing them.
 * 
 * @author Ceki Gulcu
 *
 */
public class SimpleFilterTest extends TestCase {
  Logger root; 
  Logger logger;

  public final static String FILTERED = "output/filtered";
  public final static String TEMP = "output/temp";
  
  static String TEST1_PAT = "(DEBUG|INFO|WARN|ERROR|FATAL) - Message \\d";
  static String EXCEPTION1 = "java.lang.Exception: Just testing";
  static String EXCEPTION2 = "\\s*at .*\\(.*:\\d{1,4}\\)";
  static String EXCEPTION3 = "\\s*at .*\\(Native Method\\)";
  
  public SimpleFilterTest(String name) {
    super(name);
  }

  public void setUp() {
    root = Logger.getRootLogger();
    logger = Logger.getLogger(SimpleFilterTest.class);
  }
 
  public void tearDown() {  
    root.getLoggerRepository().resetConfiguration();
  }

  
  public void test1() throws Exception {
    InputStream is = getClass().getResourceAsStream("simpleFilter1.xml");
    if (is == null) {
        throw new FileNotFoundException(
                "Could not find resource simpleFilter1.xml");
    }
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(is);
    DOMConfigurator.configure(doc.getDocumentElement());

    common();
    
    ControlFilter cf = new ControlFilter(new String[]{TEST1_PAT, EXCEPTION1, EXCEPTION2, EXCEPTION3});
    

    Transformer.transform(TEMP, FILTERED, new Filter[] {cf,
        new LineNumberFilter(), 
        new SunReflectFilter(), 
        new JunitTestRunnerFilter()});

     assertTrue(Compare.compare(SimpleFilterTest.class,
             FILTERED,
             "witness/filter/simpleFilter.1"));
  }
  
  void common() {
    int i = -1;
 
    logger.debug("Message " + ++i);
    root.debug("Message " + i);        

    logger.info ("Message " + ++i);
    root.info("Message " + i);        

    logger.warn ("Message " + ++i);
    root.warn("Message " + i);        

    logger.error("Message " + ++i);
    root.error("Message " + i);
    
    logger.log(Level.FATAL, "Message " + ++i);
    root.log(Level.FATAL, "Message " + i);    
    
    Exception e = new Exception("Just testing");
    logger.debug("Message " + ++i, e);
    root.debug("Message " + i, e);
    
    logger.error("Message " + ++i, e);
    root.error("Message " + i, e);    
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SimpleFilterTest("test1"));
    return suite;
   }

}
