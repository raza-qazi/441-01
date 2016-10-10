import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
/**
 * UrlCache Class
 * 
 * @author 	Majid Ghaderi
 * @version	1.1, Sep 30, 2016
 *
 */
public class UrlCache {
    private Map<String, String> keyMap;
    private String fileName = "catalog.txt";
    private FileReader fileReader;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    //private FileWriter fileWriter;
    private File f;

    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw exception.
	 *
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	public UrlCache() throws UrlCacheException {
        this.keyMap = new HashMap<String, String>();
        String line = "";

        try {
            // FileReader reads text files in the default encoding.
            this.fileReader = new FileReader(fileName);
            this.bufferedReader = new BufferedReader(fileReader);

            while ((line = this.bufferedReader.readLine()) != null) {
                String b;
                keyMap.put(line,b = this.bufferedReader.readLine());
                System.out.println("Inserted: " + line + "\n" + b);
            }

            System.out.println("Completed initialization...");

            //bufferedReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Could not open file. Creating catalog.txt");
            f = new File("catalog.txt");
            try {
                f.createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create catalog.txt: " + e1.getMessage());
            }

        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        }
    }
	
    /**
     * Downloads the object specified by the parameter url if the local copy is out of date.
	 *
     * @param url	URL of the object to be downloaded. It is a fully qualified URL.
     * @throws UrlCacheException if encounters any errors/exceptions
     *
     */
	public void getObject(String url) throws UrlCacheException {
        try {
            String pathname = "";
            int port = 80;

            String[] pathNameConstructor = url.split("/");
            String[] hostname = pathNameConstructor[0].split(":");

            Socket socket = new Socket(hostname[0], port);   // Create a new socket
            PrintWriter outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));  // outputStream will send request

            if (pathNameConstructor[0].contains(":"))
                port = Integer.parseInt(hostname[1]);

            for (int i = 1; i < pathNameConstructor.length; i++)
                pathname += "/" + pathNameConstructor[i];

            // If file does not exist, create new entry in text file
            // and get last modified result
            if (!this.keyMap.containsKey(url)) {
                System.out.println("Key is already present! Will check if file is modified!");
                outputStream.println("GET " + pathname + " HTTP/1.0");
                outputStream.println("If-Modified-Since: " + keyMap.get(url));   // C-Get
                outputStream.println();
                outputStream.flush();
            }
            else {
                outputStream.println("GET " + pathname + " HTTP/1.0");
                outputStream.println();
                outputStream.flush();
            }

          //  int read = socket.getInputStream().read(byteArray);
            int off = 0;
            int counter = 0;
            String header = "";
            byte[] headerReponse = new byte[2048];
            try {
                while(true) {
                    socket.getInputStream().read(headerReponse,off, 1);
                    char test = (char) (headerReponse[off]);
                    header += test;
                    off++;

                    if(header.contains("\r\n\r\n")) {
                        System.out.println("Header Recieved!");
                        break;
                    }
                }
                //  socket.getInputStream().read(objectBytes);

                // Logic to get byte count from header response
                String[] contentLength = header.split("Content-Length: ");
                String[] contentLength2 = contentLength[1].split("\\r?\\n", 2);
                int totalByteCount = Integer.parseInt(contentLength2[0]);
                byte[] objectBytes = new byte[totalByteCount+1];
                while (counter != totalByteCount) {
                    socket.getInputStream().read(objectBytes,counter, 1);
                    counter++;
                }
                FileOutputStream fos = new FileOutputStream(pathNameConstructor[pathNameConstructor.length-1]);
                fos.write(objectBytes);
                fos.close();

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
            int e = 2;

        } catch (Exception e) {
            System.out.println("There was a problem: " + e.getMessage());
        }

	}
	
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     * @throws UrlCacheException if the specified url is not in the cache, or there are other errors/exceptions
     */
	public long getLastModified(String url) throws UrlCacheException {
        if(keyMap.containsKey(url)) {
            try {
                String temp = keyMap.get(url);
                DateFormat mask = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");
                Date date = mask.parse(temp);
                System.out.println(date.getTime());

            } catch (ParseException e) {
                System.out.println("failed to parse date: " + e.getMessage());
            }
        }
        else {

        }
        return (long) Math.random();
    }

}
