package de.bwaldvogel.liblinear;

import global.GlobalConfig;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class Predict {

    private static boolean flag_predict_probability = false;

    private static final Pattern COLON = Pattern.compile(":");

    private Model model;
    private double[] prob_estimates;

    /**
     * <p>
     * <b>Note: The streams are NOT closed</b></p>
     */
    public Predict(GlobalConfig gConfig) throws IOException {
        model = Linear.loadModel(new File(gConfig.modefFileForliblinear));
        int nr_class = model.getNrClass();
        prob_estimates = new double[nr_class];

    }

    public double predictSignal(Feature[] nodes) throws IOException {
        double predict_label;

        if (flag_predict_probability) {
            assert prob_estimates != null;
            predict_label = Linear.predictProbability(model, nodes, prob_estimates);
        } else {
            predict_label = Linear.predict(model, nodes);
        }
        return predict_label;
    }

}
