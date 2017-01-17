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
 * @author  Raza Qazi
 *          10134926
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
        this.keyMap = new HashMap<>();
        String line = "";

        try {
            // Use properties class to load information from file, if it exists
            Properties prop = new Properties();
            prop.load(new FileInputStream(fileName));

            // Extract key:value pair from properties and store into hashmap.
            for (String key : prop.stringPropertyNames()) {
                keyMap.put(key,prop.get(key).toString());
            }
            // If prop cannot find "fileName", an exception is thrown and a new file
            // is created instead.
        } catch (FileNotFoundException e) {
            System.out.println("Could not find " + fileName + ". Creating catalog.txt");
            f = new File(fileName);
            try {
                f.createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create " + fileName);
                throw new UrlCacheException(e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("IO error: ");
            throw new UrlCacheException(e.getMessage());
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
            int port = 80;

            // Extract components of the url.
            String[] pathNameConstructor = url.split("/");
            String[] hostname = pathNameConstructor[0].split(":");

            // If port number is specified in the url, parse the port number
            // else, assume port 80.
            if (pathNameConstructor[0].contains(":"))
                port = Integer.parseInt(hostname[1]);

            // Initialize socket and outputStream to send information given url and port number
            Socket socket = new Socket(hostname[0], port);   // Create a new socket

            // outputStream will send request
            PrintWriter outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));

            StringBuilder build = new StringBuilder();

            for (int i = 1; i < pathNameConstructor.length; i++)
                build.append("/" + pathNameConstructor[i]);

            // Rebuild pathname from url
            String pathname = build.toString();

            // If file does not exist, create new entry in hash map
            // and get last modified result
            if (this.keyMap.containsKey(url)) {
                System.out.println(url + " is already present! Checking if modified...");
                outputStream.println("GET " + pathname + " HTTP/1.0");
                outputStream.println("If-Modified-Since: " + keyMap.get(url));   // C-Get
                outputStream.println();
                outputStream.flush();

                // Download header and check if its date is modified.
                String header = getHeader(socket);

                if(!header.contains("304 Not Modified")) {
                    // if modified, redownload a new file
                    System.out.println(url + " is modified. Starting new download");

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
                    System.out.println(url + " is not modified. Do not update anything.");
                    return;
                }
            }
            else {
                // Send a request to the server, requesting for object.
                System.out.println("New URL: " + url + " . Beginning new download...");
                outputStream.println("GET " + pathname + " HTTP/1.0");
                outputStream.println("Host: www.petmd.com\n");
                outputStream.println();
                outputStream.flush();

                // Return header information from socket
                String header = getHeader(socket);

                // Logic to get byte count from header response
                String[] contentLength = header.split("Content-Length: ");
                String[] contentLength2 = contentLength[1].split("\\r?\\n");

                // Parse int from content length.
                int totalByteCount = Integer.parseInt(contentLength2[0]);

                // Recieve body and store to file.
                getBody(totalByteCount,pathNameConstructor,socket);

                // Extract date modified and save to keyMap
               // String[] date = header.split("Last-Modified: ");
                //String[] date2 = date[1].split("\\r?\\n", 2);

                // Update the file content and date url into hash map
               // keyMap.put(url,date2[0]);
            }
            // Shut down socket
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();

            // Save modified hash table to file.
            Properties prop = new Properties();
            for(Map.Entry<String,String> entry : keyMap.entrySet()) {
           //     prop.put(entry.getKey(),entry.getValue());
            }
            // Store updated catalog to file.
            prop.store(new FileOutputStream("catalog.txt"), null);


        } catch (IOException e) {
            throw new UrlCacheException(e.getMessage());
        }
	}

    /**
     * Method to capture header information from socket.
     *
     * @param sc   Socket to receive information from
     * @return header     Returns header from server
     * @throws IOException
     */
	public String getHeader(Socket sc) throws IOException {
        // header string will contain header information from the server.
        String header = "";
        // Initialize a string builder to recreate header.
        StringBuilder build = new StringBuilder();
        byte[] headerReponse = new byte[1]; // Create a one byte array to store a byte of data
        while(!header.contains("\r\n\r\n")) {   // If header contains the following, header has been received.
            sc.getInputStream().read(headerReponse, 0, 1); // Get one byte
            char test = (char) (headerReponse[0]);      // store as a char
            build.append(test); // Append to string
            header = build.toString();
        }
        return build.toString();
    }

    /**
     *  Method that receives object bytes from the server and saves to file.
     *
     * @param totalBytes    The total number of bytes to download.
     * @param pNC           object file name
     * @param sc            socket passed in to recieve object.
     * @throws IOException
     */
    public void getBody(int totalBytes, String[] pNC, Socket sc) throws IOException {
        int counter = 0;  // Initialize read counter
        byte[] objectBytes = new byte[totalBytes+1];  // Initialize array of bytes
        while (counter != totalBytes)  // If total bytes read is equal to total num of bytes, break
            sc.getInputStream().read(objectBytes,counter++, 1); // Receive byte and save to array
        writeToFile(pNC[pNC.length-1],objectBytes); // Write bytes to file
    }

    /**
     *  Method to write bytes to file given the following arguments.
     *
     * @param nameOFFile    Name of file to create.
     * @param byteArray     Bytes to write to file
     * @throws IOException
     */
	public void writeToFile(String nameOFFile, byte[] byteArray) throws IOException {
        FileOutputStream fos = new FileOutputStream(nameOFFile);
        fos.write(byteArray);  // Save bytes to file
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
        if(!keyMap.containsKey(url)) {  // throw exception if url not found in catalog
            throw new UrlCacheException();
        }
        String temp = keyMap.get(url);      // Get Date modified
        DateFormat mask = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");
        Date date;
        try {
            date = mask.parse(temp);  // Parse date to Date format
        } catch (ParseException e) {
            throw new UrlCacheException(e.getMessage());
        }
        return date.getTime(); // return date in milliseconds
    }

}
