//package cc.ddrpa.dorian.forvariz;
//
//import static cc.ddrpa.dorian.forvariz.utils.http.URIManipulation.uri;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class CreatePublicAccessURLTests {
//
//    private static final String bucket = "public";
//    private static final String object = "downloads/test.txt";
//
//    private static final Logger logger = LoggerFactory.getLogger(CreatePublicAccessURLTests.class);
//
//    private String directConcatURL(String endpoint) {
//        return endpoint + bucket + "/" + object;
//    }
//
//    @Test
//    void constructPublicAccessURLTest() {
//        assertEquals("https://oss.not-a.site/public/downloads/test.txt",
//            directConcatURL("https://oss.not-a.site/"));
//        assertEquals("https://oss.not-a.site/public/downloads/test.txt",
//            uri("https://oss.not-a.site/", bucket, object).toString());
//    }
//
//    @Test
//    void constructPublicAccessURLWithoutSlashAtEndTest() {
//        assertEquals("https://oss.not-a.site/public/downloads/test.txt",
//            uri("https://oss.not-a.site", bucket, object).toString());
//    }
//
//    @Test
//    void constructPublicAccessURLWithPortTest() {
//        assertEquals("https://oss.not-a.site:8443/public/downloads/test.txt",
//            uri("https://oss.not-a.site:8443", bucket, object).toString());
//    }
//}