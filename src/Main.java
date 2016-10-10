import java.io.*;
import java.util.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws UrlCacheException {

        // include whatever URL you like
        // these are just some samples
        String[] url = {"pages.cpsc.ucalgary.ca/~asehati/cpsc441/index.html",
                "people.ucalgary.ca/~mghaderi/test/uc.gif",
                "people.ucalgary.ca/~mghaderi/test/a.pdf",
                "people.ucalgary.ca:80/~mghaderi/test/test.html"};

        // this is a very basic tester
        // the TAs will use a more comprehensive set of tests
        try {
            UrlCache cache = new UrlCache();
            cache.getLastModified(url[0]);
            for (int i = 0; i < url.length; i++)
                cache.getObject(url[i]);

        } catch (UrlCacheException e) {
            System.out.println("There was a problem: " + e.getMessage());
        }

    }
}
