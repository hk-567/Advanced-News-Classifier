package uob.oop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Toolkit {
    public static List<String> listVocabulary = null;
    public static List<double[]> listVectors = null;
    private static final String FILENAME_GLOVE = "glove.6B.50d_Reduced.csv";

    public static final String[] STOPWORDS = {"a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your"};

    public void loadGlove() throws IOException {
        BufferedReader myReader = null;
        //TODO Task 4.1 - 5 marks
        try{
            listVocabulary = new ArrayList<>();
            listVectors = new ArrayList<>();
            File gloveFile = getFileFromResource(FILENAME_GLOVE);
            myReader = new BufferedReader(new FileReader(gloveFile));

            String line;
            while((line = myReader.readLine())!= null){
                String [] eachPart = line.split(",");
                // String word = eachPart[0].trim();
                String word = eachPart[0];
                listVocabulary.add(word);

                // double[] vector = Stream.of(eachPart).skip(1).mapToDouble(Double::parseDouble).toArray();
                double[]vector = new double[eachPart.length-1];
                for (int i = 1; i< eachPart.length;i++){
                    vector[i-1] = Double.parseDouble(eachPart[i]);
                }
                listVectors.add(vector);
            }
        }
        catch(IOException| URISyntaxException e){
            e.printStackTrace();
            throw new IOException("Error loading Glove file: " + e.getMessage());
        }
        finally{
            if(myReader!=null){
                try{
                    myReader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    System.err.println("Error closing BufferedReader: " + e.getMessage());
                }
            }
        }
    }

    private static File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = Toolkit.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    public List<NewsArticles> loadNews() {
        List<NewsArticles> listNews = new ArrayList<>();
        //TODO Task 4.2 - 5 Marks
        // ClassLoader classloader = getClass().getClassLoader();
        // final String newsFolderPath = "src\\main\\resources\\News";
        try {
         //   ClassLoader cl = getClass().getClassLoader();
          //  URL resource = cl.getResource("News");
         //   if (resource == null){
          //      return listNews;
        //    }
            File resource = getFileFromResource("News");
            Path newsFolderPath = Paths.get(resource.toURI());
            try(Stream<Path> pathStream = Files.list(newsFolderPath)) {
                for (Path filePath : pathStream.toArray(Path[]::new)) {
                    if (filePath.toString().endsWith(".htm")) {
                        try {
                            String htmlCode = Files.readString(filePath);
                            String title = HtmlParser.getNewsTitle(htmlCode);
                            String content = HtmlParser.getNewsContent(htmlCode);
                            NewsArticles.DataType dataType = HtmlParser.getDataType(htmlCode);
                            String label = HtmlParser.getLabel(htmlCode);

                            NewsArticles news = new NewsArticles(title, content, dataType, label);
                            listNews.add(news);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Error reading news file: " + e.getMessage());
                        }
                    }
                }
            }  catch(IOException e)
            {
                e.printStackTrace();
                System.err.println("Error listing news folder: " + e.getMessage());
            }

        }catch (URISyntaxException e ){
            e.printStackTrace();
            System.err.println("Error getting resource URI: " + e.getMessage());
        }

        return listNews;
    }

    public static List<String> getListVocabulary() {
        return listVocabulary;
    }

    public static List<double[]> getlistVectors() {
        return listVectors;
    }
}
