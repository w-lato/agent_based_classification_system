package agh.edu.agents.experiment;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import weka.core.Instances;

import java.util.Optional;

// TODO is class_method necessary?
public final class RunConf
{
    String conf_name;

    Instances train;
    Instances test;

    S_Type[] agents;
    ClassStrat class_method;

    Split split_meth;
    Optional<Double> fill;

    private RunConf(Builder builder)
    {
        conf_name = builder.conf_name;
        train = builder.train;
        test = builder.test;
        agents = builder.agents;
        class_method = builder.class_method;
        split_meth = builder.split_meth;
        fill = builder.fill;
    }

    public String getConf_name() { return conf_name; }

    public ClassStrat getClass_method() {
        return class_method;
    }

    public Instances getTrain() {
        return train;
    }

    public Instances getTest() {
        return test;
    }

    public S_Type[] getAgents() {
        return agents;
    }

    public Split getSplit_meth() {
        return split_meth;
    }

    public Optional<Double> getFill() {
        return fill;
    }

    public static final class Builder
    {
        private String conf_name;
        private Instances train;
        private Instances test;
        private S_Type[] agents;
        private ClassStrat class_method;
        private Split split_meth;
        private Optional<Double> fill;

        public Builder() {}

        public Builder conf_name(String val) {
            conf_name = val;
            return this;
        }

        public Builder train(Instances val) {
            train = val;
            return this;
        }

        public Builder test(Instances val) {
            test = val;
            return this;
        }

        public Builder agents(S_Type[] val) {
            agents = val;
            return this;
        }

        public Builder class_method(ClassStrat val) {
            class_method = val;
            return this;
        }

        public Builder split_meth(Split val) {
            split_meth = val;
            return this;
        }

        public Builder fill(Optional<Double> val) {
            fill = val;
            return this;
        }

        public RunConf build() {
            return new RunConf(this);
        }
    }
}
