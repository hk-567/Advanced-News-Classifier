package uob.oop;

import org.apache.commons.lang3.time.StopWatch;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdvancedNewsClassifier {
    public static List<NewsArticles> listNews = null;
    public static List<Glove> listGlove = null;
    private static StopWatch mySW = new StopWatch();
    public final int BATCHSIZE = 10;
    public Toolkit myTK = null;
    public List<ArticlesEmbedding> listEmbedding = null;
    public MultiLayerNetwork myNeuralNetwork = null;
    public int embeddingSize = 0;

    public AdvancedNewsClassifier() throws IOException {
        myTK = new Toolkit();
        myTK.loadGlove();
        listNews = myTK.loadNews();
        listGlove = createGloveList();
        listEmbedding = loadData();
    }

    public static void main(String[] args) throws Exception {
        mySW.start();
        AdvancedNewsClassifier myANC = new AdvancedNewsClassifier();

        myANC.embeddingSize = myANC.calculateEmbeddingSize(myANC.listEmbedding);
        myANC.populateEmbedding();
        myANC.myNeuralNetwork = myANC.buildNeuralNetwork(2);
        myANC.predictResult(myANC.listEmbedding);
        myANC.printResults();
        mySW.stop();
        System.out.println("Total elapsed time: " + mySW.getTime());
    }

    public static List<ArticlesEmbedding> loadData() {
        List<ArticlesEmbedding> listEmbedding = new ArrayList<>();
        for (NewsArticles news : listNews) {
            ArticlesEmbedding myAE = new ArticlesEmbedding(news.getNewsTitle(), news.getNewsContent(), news.getNewsType(), news.getNewsLabel());
            listEmbedding.add(myAE);
        }
        return listEmbedding;
    }

    public List<Glove> createGloveList() {
        List<Glove> listResult = new ArrayList<>();
        //TODO Task 6.1 - 5 Marks
      /*  for (int i=0; i< Toolkit.listVocabulary.size();i++){
            String term = Toolkit.listVocabulary.get(i);
            double[] vector = Toolkit.listVectors.get(i);

            if (!isStopWord(term)){
                Glove glove = new Glove(term, new Vector(vector));
                listResult.add(glove);
            }
        }

       */
        try {
            for (String word : Toolkit.getListVocabulary()) {
                boolean isStopWord = false;
                for (String stopword : Toolkit.STOPWORDS) {
                    if (stopword.equalsIgnoreCase(word)) {
                        isStopWord = true;
                        break;
                    }
                }
                if (!isStopWord) {
                    Vector vctr = new Vector(Toolkit.getlistVectors().get(Toolkit.getListVocabulary().indexOf(word)));
                    Glove glove = new Glove(word, vctr);
                    listResult.add(glove);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating Glove list: " + e.getMessage());
        }
        return listResult;
    }

//    private boolean isStopWord(String term){
//        for (String stopword: Toolkit.STOPWORDS){
//            if(stopword.equals(term)){
//                return true;
//            }
//        }
//        return false;
//    }

    public int calculateEmbeddingSize(List<ArticlesEmbedding> _listEmbedding) {
        int intMedian = -1;
        //TODO Task 6.2 - 5 Marks
        List<Integer> docLengths = new ArrayList<>();
        try {
            for (ArticlesEmbedding embedding : _listEmbedding) {
                String[] words = embedding.getNewsContent().split(" ");
                int count = 0;
                for (String word : words) {
                    if (Toolkit.getListVocabulary().contains(word)) {
                        count++;
                    }
                }
                docLengths.add(count);
            }
            docLengths.sort(null);
            //docLengths.sort(Integer::compareTo);
            int n = docLengths.size();
            // System.out.println("Sorted Lengths: " + docLengths);
            //System.out.println("Size of list : " + n);

            if (n % 2 == 0) {
                // even
               // double firstMedian = docLengths.get((n / 2) + 1);
                //  System.out.println("Median 1: " + median1);
               // double  secondMedian = docLengths.get(n / 2);
                //  System.out.println("Median 2: " + median2);
                intMedian = ((docLengths.get((n/2) +1))+(docLengths.get(n/2)))/2;
               // intMedian = ((docLengths.get((n / 2) + 1))+ (docLengths.get(n / 2))) / 2;
                //System.out.println("If Median T: " + intMedian);
            } else {
                intMedian = docLengths.get((docLengths.size() + 1) / 2);
                //  System.out.println("Else Median T: " + intMedian);

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error calculating embedding size: " + e.getMessage());
        }

        return intMedian;
    }


    public void populateEmbedding() {
        //TODO Task 6.3 - 10 Marks
        for (ArticlesEmbedding embedding : listEmbedding) {
            try {
                embedding.setEmbeddingSize(embeddingSize);
                embedding.getEmbedding();
            } catch (InvalidSizeException e) {
                embedding.setEmbeddingSize(calculateEmbeddingSize(listEmbedding));
                e.printStackTrace();
            } catch (InvalidTextException e) {
                embedding.getNewsContent();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error population embedding: " + e.getMessage());
            }
        }
    }

    public DataSetIterator populateRecordReaders(int _numberOfClasses) throws Exception {
        ListDataSetIterator myDataIterator = null;
        List<DataSet> listDS = new ArrayList<>();
        INDArray inputNDArray = null;
        INDArray outputNDArray = null;

        //TODO Task 6.4 - 8 Marks
        for (ArticlesEmbedding embedding : listEmbedding) {
            if (embedding.getNewsType() == NewsArticles.DataType.Training) {
                try {
                    inputNDArray = embedding.getEmbedding();
                    int classIndex = Integer.parseInt(embedding.getNewsLabel()) - 1;
                    //outputNDArray = createOutputArray(embedding.getNewsLabel(), _numberOfClasses);
                    outputNDArray = Nd4j.zeros(1, _numberOfClasses);
                    outputNDArray.putScalar(0, classIndex, 1);
                    DataSet ds = new DataSet(inputNDArray, outputNDArray);
                    listDS.add(ds);
                } catch (InvalidSizeException | InvalidTextException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error populating record readers: " + e.getMessage());
                }
            }
        }

        return new ListDataSetIterator(listDS, BATCHSIZE);
    }


    public MultiLayerNetwork buildNeuralNetwork(int _numOfClasses) throws Exception {
        DataSetIterator trainIter = populateRecordReaders(_numOfClasses);
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(42)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .updater(Adam.builder().learningRate(0.02).beta1(0.9).beta2(0.999).build())
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder().nIn(embeddingSize).nOut(15)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.HINGE)
                        .activation(Activation.SOFTMAX)
                        .nIn(15).nOut(_numOfClasses).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        for (int n = 0; n < 100; n++) {
            model.fit(trainIter);
            trainIter.reset();
        }
        return model;
    }

    public List<Integer> predictResult(List<ArticlesEmbedding> _listEmbedding) throws Exception {
        List<Integer> listResult = new ArrayList<>();
        //TODO Task 6.5 - 8 Marks
        for (ArticlesEmbedding embedding : _listEmbedding) {
            if (embedding.getNewsType() == NewsArticles.DataType.Testing) {
                try {
                    INDArray inputArray = embedding.getEmbedding();
                    int[] predictedLabels = myNeuralNetwork.predict(inputArray);
                    if (predictedLabels.length > 0) {
                        int predictedLabel = predictedLabels[0];
                        listResult.add(predictedLabel);
                        embedding.setNewsLabel(String.valueOf(predictedLabel));

                    }
                } catch (InvalidSizeException | InvalidTextException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error predicting results: " + e.getMessage());
                }
            }
        }

        return listResult;
    }

    public void printResults() {
        //TODO Task 6.6 - 6.5 Marks
        List<String> uniqueGroup = new ArrayList<>();

        for (ArticlesEmbedding embedding : listEmbedding) {
            if (embedding.getNewsType() == NewsArticles.DataType.Testing) {
                String label = embedding.getNewsLabel();
                if (!uniqueGroup.contains(label)) {
                    uniqueGroup.add(label);
                }
            }

        }
        uniqueGroup.sort(null);
        // uniqueGroup.sort(String::compareTo);

        for (String group : uniqueGroup) {
            System.out.println("Group " + (uniqueGroup.indexOf(group) + 1));
            for (ArticlesEmbedding embedding : listEmbedding) {
                if (embedding.getNewsType() == NewsArticles.DataType.Testing && embedding.getNewsLabel().equals(group)) {
                    System.out.println(embedding.getNewsTitle());
                }
            }
        }

       /* for( int i =0; i< uniqueGroup.size();i++) {
            System.out.println("Group " + (i+1));
            for (ArticlesEmbedding embedding : listEmbedding) {
                if (embedding.getNewsType() == NewsArticles.DataType.Testing && embedding.getNewsLabel().equals(uniqueGroup.get(i))) {
                    System.out.println(embedding.getNewsTitle());
                }
            }
        }

        */
    }

}
