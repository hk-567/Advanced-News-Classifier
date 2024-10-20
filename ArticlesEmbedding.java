package uob.oop;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Properties;


public class ArticlesEmbedding extends NewsArticles {
    private int intSize = -1;
    private String processedText = "";

    private INDArray newsEmbedding = Nd4j.create(0);

    public ArticlesEmbedding(String _title, String _content, NewsArticles.DataType _type, String _label) {
        //TODO Task 5.1 - 1 Mark
        super(_title, _content, _type, _label);
    }

    public void setEmbeddingSize(int _size) {
        //TODO Task 5.2 - 0.5 Marks
        intSize = _size;
    }

    public int getEmbeddingSize() {
        return intSize;
    }

    @Override
    public String getNewsContent() {
        //TODO Task 5.3 - 10 Marks

        if (processedText.isEmpty()) {
            try {
               // String baseContent = super.getNewsContent();
                String cleanContent = textCleaning(super.getNewsContent());

                Properties props = new Properties();
                props.setProperty("annotators", "tokenize,pos,lemma");
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

                CoreDocument document = new CoreDocument(cleanContent);
                pipeline.annotate(document);

                StringBuilder finalContent = new StringBuilder();
                for (CoreLabel token : document.tokens()) {
                   // String lemma = token.lemma();
                    boolean isStopWord = false;
                    for (String stopword : Toolkit.STOPWORDS) {
                        if (stopword.equalsIgnoreCase(token.lemma())) {
                            isStopWord = true;
                            break;
                        }
                    }
                    if (!isStopWord) {
                        finalContent.append(token.lemma()).append(" ");
                    }
                }
                processedText = finalContent.toString().toLowerCase();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error during annotation with StanfordCoreNLP: " + e.getMessage());
            }
        }


        //  String lemmaContent = lemmatizeText(cleanContent);
        //String finalContent = removeStopWords(lemmaContent);

        //processedText = finalContent.toLowerCase();
        return processedText.trim();
    }

//    private static boolean isStopword(String word) {
//        for (String stopWord : Toolkit.STOPWORDS) {
//            if (stopWord.equalsIgnoreCase(word)) {
//                return true;
//            }
//        }
//        return false;
//    }

    public INDArray getEmbedding() throws Exception {
        //TODO Task 5.4 - 20 Marks
        if (intSize == -1) {
            throw new InvalidSizeException("Invalid size");
        } else if (processedText.isEmpty()) {
            throw new InvalidTextException("Invalid text");
        } else if (newsEmbedding.isEmpty()) {
            String[] allWords = processedText.split(" ");
            //  int embeddingSize = Toolkit.getlistVectors().get(0).length;
            // int embeddingSize = AdvancedNewsClassifier.listGlove.get(0).getVector().getVectorSize();
            newsEmbedding = Nd4j.create(intSize, AdvancedNewsClassifier.listGlove.get(0).getVector().getVectorSize());

            int nonZeroRows = 0;
            // int noOfRows = intSize;
            //  newsEmbedding = Nd4j.create(noOfRows, embeddingSize);
            for (int i = 0; i < allWords.length && nonZeroRows < intSize; i++) {
                String word = allWords[i];
                // Glove glove = findGloveObject(word);
                //  if (glove != null) {
                //    double[] gloveVector = glove.getVector().getAllElements();
                //   newsEmbedding.putRow(nonZeroRows++, Nd4j.create(gloveVector));
                //   }
                Glove glove = null;
                for (Glove glv : AdvancedNewsClassifier.listGlove) {
                    if (glv.getVocabulary().equalsIgnoreCase(word)) {
                        glove = glv;
                        break;
                    }
                }
                if (glove != null) {
                    double[] gloveVector = glove.getVector().getAllElements();
                    newsEmbedding.putRow(nonZeroRows++, Nd4j.create(gloveVector));

                }
            }
        }
        return Nd4j.vstack(newsEmbedding.mean(1));
    }

//    private Glove findGloveObject(String word) {
//
//        for (Glove glove : AdvancedNewsClassifier.listGlove) {
//            if (glove.getVocabulary().equalsIgnoreCase(word)) {
//                return glove;
//            }
//        }
//        return null;
//    }

    /***
     * Clean the given (_content) text by removing all the characters that are not 'a'-'z', '0'-'9' and white space.
     * @param _content Text that need to be cleaned.
     * @return The cleaned text.
     */
    private static String textCleaning(String _content) {
        StringBuilder sbContent = new StringBuilder();

        for (char c : _content.toLowerCase().toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || Character.isWhitespace(c)) {
                sbContent.append(c);
            }
        }

        return sbContent.toString().trim();
    }
    /*private static String lemmatizeText(String text){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document  = new Annotation(text);
        pipeline.annotate(document);

        StringBuilder lemmatizedText = new StringBuilder();
        for (CoreLabel tok : document.get(CoreAnnotations.TokensAnnotation.class)){
            //  System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
            lemmatizedText.append(tok.lemma()).append(" ");
        }
        return lemmatizedText.toString();

    }*/

  /*  private static String removeStopWords(String text) {
        StringBuilder mySB = new StringBuilder();
        String[] wordsList = text.split(" ");
        for (String word : wordsList) {
            if (isNotStopword(Toolkit.STOPWORDS, word)) {
                mySB.append(word).append(" ");
            }
        }

        return mySB.toString().trim();

    }private static boolean isNotStopword(String[] arr, String searchVal) {
        for (String word : arr) {
            if (searchVal.equalsIgnoreCase(word)) {
                return false;
            }
        }
        return true;
    }*/

}
