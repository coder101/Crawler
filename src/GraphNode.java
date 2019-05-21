import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GraphNode {

    private static final int BUFFER_SIZE = 1024;
    public static String[] keyWords;
    static int count = 1;
    String parent, child, content;

    List<String> links = new LinkedList<String>();
    static double totalTime = 0;

    GraphNode(String src, String link) {
        parent = src;
        child = link;
    }

    Boolean DowloadPageAndLinks(String baseURL) {

        try {
            Matcher mLink;
            Pattern pLink = null;

            //String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
            String hrefPattern = "/wiki/(?:[A-Za-z0-9-._~!#$&'()*+,;=:@]|%[0-9a-fA-F]{2})*";
            pLink = Pattern.compile(hrefPattern);


            if (count % 100 == 0) {
                Thread.sleep(5000);
            }
            count++;

            long startTime = System.currentTimeMillis();

            URL urlStream = new URL(baseURL + this.child);

            Set<String> keys = new HashSet<String>();
            for (String str : keyWords) {
                keys.add(str);
            }
            // System.out.println("Downloading "+count);
            InputStream is = urlStream.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            content = "";
            Boolean foundp = false;
            while ((line = br.readLine()) != null) {

                line = line.toLowerCase();

                if (line.contains("<p>")) { //revert
                    foundp = true;
                }


                for (String str : keyWords) {
                    if (line.contains(str))
                        keys.remove(str);
                }
                if (foundp && line.contains("<a href=")) {

                    mLink = pLink.matcher(line);

                    while (mLink.find()) {
                        String address = line.substring(mLink.start(0), mLink.end(0));//mLink.group(1);
                        links.add(address);
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;
            totalTime += timeTaken;
            is.close();

            if (!keys.isEmpty()) {
                return false;
            }
        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println("Exception during file download " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    Boolean DownloadPage(String baseURL) {
        try {


            if (count % 100 == 0) {
                Thread.sleep(5000);
            }
            count++;

            long startTime = System.currentTimeMillis();

            URL urlStream = new URL(baseURL + this.child);

            // System.out.println("Downloading "+count);
            InputStream is = urlStream.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            content = getString(is);
            content = content.toLowerCase();
            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;
            totalTime += timeTaken;


            is.close();

        } catch (Exception e) {

            System.out.println("Exception during file download " + e.getLocalizedMessage());
            return false;
        }


        return true;

    }

    public static String getString(InputStream inputStream) throws IOException {
        if (inputStream == null)
            return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE);
        int charsRead;
        char[] copyBuffer = new char[BUFFER_SIZE];
        StringBuffer sb = new StringBuffer();
        while ((charsRead = in.read(copyBuffer, 0, BUFFER_SIZE)) != -1)
            sb.append(copyBuffer, 0, charsRead);
        in.close();
        return sb.toString();
    }
}
