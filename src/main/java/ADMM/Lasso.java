package ADMM;

import Utils.*;
import math.DenseMap;
import math.DenseVector;
import java.util.List;

//TODO: To be checked ...
/**
 * Created by 王羚宇 on 2016/7/24.
 */
//https://github.com/niangaotuantuan/LASSO-Regression/blob/8338930ca6017927efcb362c17a37a68a160290f/LASSO_ADMM.m
//https://github.com/niangaotuantuan/LASSO-Regression/blob/8338930ca6017927efcb362c17a37a68a160290f/LASSO_ADMM.m
//https://web.stanford.edu/~boyd/papers/pdf/admm_slides.pdf
//https://web.stanford.edu/~boyd/papers/pdf/admm_distr_stats.pdf
//https://web.stanford.edu/~boyd/papers/admm/lasso/lasso.html
//http://www.simonlucey.com/lasso-using-admm/
//http://users.ece.gatech.edu/~justin/CVXOPT-Spring-2015/resources/14-notes-admm.pdf
public class Lasso {
    public double test(List<LabeledData> list, DenseVector model) {
        double residual = 0;
        for (LabeledData labeledData : list) {
            double dot_prod = model.dot(labeledData.data);
            residual += Math.pow(labeledData.label - dot_prod, 2);
        }

        return residual;
    }
    public void train(DenseMap[] features, List<LabeledData> labeledData,
                      ADMMState model, double lambda, double trainRatio) {
        int testBegin = (int)(labeledData.size() * trainRatio);
        int testEnd = labeledData.size();
        List<LabeledData> trainCorpus = labeledData.subList(0, testBegin);
        List<LabeledData> testCorpus = labeledData.subList(testBegin, testEnd);
        int featureDim = features.length - 1;
        int i;
        double rho = 1e-4;
        double maxRho = 5;

        for (i = 0; i < 30; i ++) {
            //Initialize the second part of B
            double []part2OfB = new double[featureDim];
            //Calculate (A^Tb+rho*C-L)
            for(int r = 0; r < featureDim; r++){
                part2OfB[r] = rho * (model.C.values[r] - model.L.values[r]);
                for(int ite = 0; ite < features[r].index.size(); ite++){
                    int idx = features[r].index.get(ite);
                    part2OfB[r] += features[r].value.get(ite) * features[featureDim].value.get(idx);
                }
            }
            long startTrain = System.currentTimeMillis();
            //Update B;
            for(int j = 0; j < featureDim; j++){
                model.B.values[j] = 0;
                for(int ite = 0; ite < featureDim; ite++){
                    //Calculate (A^T*A+rho*I)_j_ite
                    double part1OfB_j_ite = features[j].mutilply(features[ite]);
                    if(j == ite){
                        part1OfB_j_ite += rho * 1;
                    }
                    model.B.values[j] += part1OfB_j_ite * part2OfB[ite];
                }
            }

            //Update C
            for(int j = 0; j < featureDim; j++) {
                //C=Soft_threshold(lambda/rho,B+L/rho);
                model.C.values[j] = Utils.soft_threshold(lambda / rho, model.B.values[j]
                        + model.L.values[j]);
            }

            //Update L
            for(int j = 0; j < featureDim; j++) {
                //L=L+rho*(B-C)
                model.L.values[j] +=  (model.B.values[j] - model.C.values[j]);
            }
            for(int id = 0; id < featureDim; id++){
                System.out.print(model.B.values[id] + " ");
            }
            System.out.println();
            long trainTime = System.currentTimeMillis() - startTrain;
            long startTest = System.currentTimeMillis();

            double loss = test(trainCorpus, model.B);
            double accuracy = test(testCorpus, model.B);
            long testTime = System.currentTimeMillis() - startTest;
            System.out.println("loss=" + loss + " testResidual=" + accuracy +
                    " trainTime=" + trainTime + " testTime=" + testTime);
        }
    }


    public static void train(DenseMap[] corpus, List<LabeledData> labeledData,
                             double lambda, double trainRatio) {
        int dim = corpus.length;
        Lasso lassoADMM = new Lasso();
        //https://www.microsoft.com/en-us/research/wp-content/uploads/2012/01/tricks-2012.pdf  Pg 3.
        ADMMState model = new ADMMState(dim);
        long start = System.currentTimeMillis();
        lassoADMM.train(corpus, labeledData, model, lambda, trainRatio);
        long cost = System.currentTimeMillis() - start;
        System.out.println(cost + " ms");
    }
    public static void main(String[] argv) throws Exception {
        System.out.println("Usage: ADMM.Lasso FeatureDim SampleDim train_path lamda trainRatio");
        int featureDim = Integer.parseInt(argv[0]);
        int sampleDim = Integer.parseInt(argv[1]);
        String path = argv[2];
        double lambda = Double.parseDouble(argv[3]);
        double trainRatio = 0.5;
        if(argv.length >= 5){
            trainRatio = Double.parseDouble(argv[4]);
            if(trainRatio >= 1 || trainRatio <= 0){
                System.out.println("Error Train Ratio!");
                System.exit(1);
            }
        }
        long startLoad = System.currentTimeMillis();
        DenseMap[] features = Utils.LoadLibSVMByFeature(path, featureDim, sampleDim, trainRatio);
        List<LabeledData> labeledData = Utils.loadLibSVM(path, featureDim);
        long loadTime = System.currentTimeMillis() - startLoad;
        System.out.println("Loading corpus completed, takes " + loadTime + " ms");
        //TODO Need to think how to min hash numeric variables
        train(features, labeledData, lambda, trainRatio);
    }
}