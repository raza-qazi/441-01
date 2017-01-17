import java.text.ParseException;

/**
 * A simple test driver
 *
 * @author 	Majid Ghaderi
 * @version	3.1, Sep 30, 2016
 *
 */
public class Main {

	public static void main(String[] args) throws UrlCacheException {

		// include whatever URL you like
		// these are just some samples
		String[] url = {"people.ucalgary.ca/~mghaderi/index.html",
						"people.ucalgary.ca/~mghaderi/test/uc.gif",
						"people.ucalgary.ca/~mghaderi/test/a.pdf",
						"people.ucalgary.ca:80/~mghaderi/test/test.html"};

		// this is a very basic tester
		// the TAs will use a more comprehensive set of tests
		try {
			UrlCache cache = new UrlCache();

			//for (String i : url)
			cache.getObject("localhost:2225/m.s");

			System.out.println("Last-Modified for " + url[0] + " is: " + cache.getLastModified(url[0]));
			cache.getObject(url[0]);
			System.out.println("Last-Modified for " + url[0] + " is: " + cache.getLastModified(url[0]));
		}
		catch (UrlCacheException e) {
			System.out.println("There was a problem: " + e.getMessage());
		}
	}

}


