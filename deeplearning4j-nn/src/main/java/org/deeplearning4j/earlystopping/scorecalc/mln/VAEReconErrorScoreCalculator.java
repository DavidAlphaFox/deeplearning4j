package org.deeplearning4j.earlystopping.scorecalc.mln;

import org.deeplearning4j.earlystopping.scorecalc.base.BaseMLNScoreCalculator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.layers.variational.VariationalAutoencoder;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Score function for variational autoencoder reconstruction error.
 *
 * @see VAEReconProbScoreCalculator for reconstruction probability
 */
public class VAEReconErrorScoreCalculator extends BaseMLNScoreCalculator {

    protected final RegressionEvaluation.Metric metric;
    protected RegressionEvaluation evaluation;

    /**
     * Constructor for reconstruction *ERROR*
     *
     * @param metric
     * @param iterator
     */
    public VAEReconErrorScoreCalculator(RegressionEvaluation.Metric metric, DataSetIterator iterator) {
        super(iterator);
        this.metric = metric;
    }

    @Override
    protected void reset() {
        evaluation = new RegressionEvaluation();
    }

    @Override
    protected INDArray output(MultiLayerNetwork network, INDArray input, INDArray fMask, INDArray lMask) {
        Layer l = network.getLayer(0);
        if(!(l instanceof VariationalAutoencoder)){
            throw new UnsupportedOperationException("Can only score networks with VariationalAutoencoder layers as first layer -" +
                    " got " + l.getClass().getSimpleName());
        }
        VariationalAutoencoder vae = (VariationalAutoencoder)l;
        INDArray z = vae.activate(input, false);
        return vae.generateAtMeanGivenZ(z);
    }

    @Override
    protected double scoreMinibatch(MultiLayerNetwork network, INDArray features, INDArray labels, INDArray fMask,
                                    INDArray lMask, INDArray output) {
        evaluation.eval(features, output);
        return 0.0; //Not used
    }

    @Override
    protected double finalScore(double scoreSum, int minibatchCount, int exampleCount) {
        return evaluation.scoreForMetric(metric);
    }
}
