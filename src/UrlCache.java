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
            Properties prop = new Properties();
            prop.load(new FileInputStream(fileName));

            for (String key : prop.stringPropertyNames()) {
                keyMap.put(key,prop.get(key).toString());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find " + fileName + ". Creating catalog.txt");
            f = new File(fileName);
            try {
                f.createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create " + fileName + ": " + e1.getMessage());
                throw new UrlCacheException();
            }
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
            throw new UrlCacheException();
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

            if (pathNameConstructor[0].contains(":"))
                port = Integer.parseInt(hostname[1]);

            Socket socket = new Socket(hostname[0], port);   // Create a new socket
            PrintWriter outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));  // outputStream will send request

            for (int i = 1; i < pathNameConstructor.length; i++)
                pathname += "/" + pathNameConstructor[i];

            // If file does not exist, create new entry in hash map
            // and get last modified result
            if (this.keyMap.containsKey(url)) {
                System.out.println(url + " is already present! Checking if file is modified!");
                outputStream.println("GET " + pathname + " HTTP/1.0");
                outputStream.println("If-Modified-Since: " + keyMap.get(url));   // C-Get
                outputStream.println();
                outputStream.flush();

                // Download header and check if its date is modified.
                String header = getHeader(socket);

                if(!header.contains("304 Not Modified")) {
                    // if modified, redownload a new file
                    System.out.println(url + " is modified. Beginning new download");

                    // Logic to get byte count from header response
                    String[] contentLength = header.split("Content-Length: ");
                    String[] contentLength2 = contentLength[1].split("\\r?\\n", 2);

                    int totalByteCount = Integer.parseInt(contentLength2[0]);

                    // Download body and save to file
                    getBody(totalByteCount,pathNameConstructor,socket);

                    // Update catalog file with replaced date
                    String[] date = header.split("Last-Modified: ");
                    String[] date2 = date[1].split("\\r?\\n", 2);

                    keyMap.put(url,date2[0]);
                }
                else {
                    // else: do nothing: ie... return getObject
                    System.out.println(url + " is not modified. Will not update file.");
                    return;
                }
            }
            else {
                System.out.println("New URL: " + url + " . New file to download");
                outputStream.println("GET " + pathname + " HTTP/1.0");
                outputStream.println();
                outputStream.flush();

                // Return header information from socket
                String header = getHeader(socket);

                // Logic to get byte count from header response
                String[] contentLength = header.split("Content-Length: ");
                String[] contentLength2 = contentLength[1].split("\\r?\\n", 2);

                int totalByteCount = Integer.parseInt(contentLength2[0]);

                // Recieve body and store to file.
                getBody(totalByteCount,pathNameConstructor,socket);


                // Extract date modified and save to keyMap
                String[] date = header.split("Last-Modified: ");
                String[] date2 = date[1].split("\\r?\\n", 2);

                keyMap.put(url,date2[0]);
            }
            Properties prop = new Properties();
            for(Map.Entry<String,String> entry : keyMap.entrySet()) {
                prop.put(entry.getKey(),entry.getValue());
            }
            prop.store(new FileOutputStream("catalog.txt"), null);
        } catch (IOException e) {
            throw new UrlCacheException();
        }
	}
	// Method to capture header information from server
	public String getHeader(Socket sc) throws IOException {
        int off = 0;
        String header = "";
        byte[] headerReponse = new byte[2048];
        while(!header.contains("\r\n\r\n")) {
            sc.getInputStream().read(headerReponse, off, 1);
            char test = (char) (headerReponse[off++]);
            header += test;
        }
        return header;
    }

    // Method to capture body from server
    public void getBody(int totalBytes, String[] pNC, Socket sc) throws IOException {
        int counter = 0;
        byte[] objectBytes = new byte[totalBytes+1];
        while (counter != totalBytes)
            sc.getInputStream().read(objectBytes,counter++, 1);
        writeToFile(pNC[pNC.length-1],objectBytes);
    }
	// method to write bytes to file
	public void writeToFile(String nameOFFile, byte[] byteArray) throws IOException {
        FileOutputStream fos = new FileOutputStream(nameOFFile);
        fos.write(byteArray);
        fos.close();
    }
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     * @throws UrlCacheException if the specified url is not in the cache, or there are other errors/exceptions
     */
	public long getLastModified(String url) throws UrlCacheException {
        if(!keyMap.containsKey(url)) {
            throw new UrlCacheException();
        }
        String temp = keyMap.get(url);
        DateFormat mask = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");
        Date date;
        try {
            date = mask.parse(temp);
        } catch (ParseException e) {
            throw new UrlCacheException();
        }
        return date.getTime();
    }

}
