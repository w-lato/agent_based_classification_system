package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import agh.edu.agents.enums.Vote;
import weka.core.Instances;

public final class RunConf
{
    final Instances train;
    final Instances test;
    final S_Type[] agents;
    final Vote class_method;

    final Split split_meth;
    final double split_ratio;
    final double ol_ratio;

    public Vote getClass_method() {
        return class_method;
    }

    private RunConf(Builder builder) {
        train = builder.train;
        test = builder.test;
        agents = builder.agents;
        class_method = builder.class_method;
        split_meth = builder.split_meth;
        split_ratio = builder.split_ratio;
        ol_ratio = builder.ol_ratio;
    }


    public static final class Builder {
        private Instances train;
        private Instances test;
        private S_Type[] agents;
        private Vote class_method;
        private Split split_meth;
        private double split_ratio;
        private double ol_ratio;

        public Builder() {
        }

        public Builder train(Instances train) {
            this.train = train;
            return this;
        }

        public Builder test(Instances test) {
            this.test = test;
            return this;
        }

        public Builder agents(S_Type[] agents) {
            this.agents = agents;
            return this;
        }

        public Builder class_method(Vote class_method) {
            this.class_method = class_method;
            return this;
        }

        public Builder split_meth(Split split_meth) {
            this.split_meth = split_meth;
            return this;
        }

        public Builder split_ratio(double split_ratio) {
            this.split_ratio = split_ratio;
            return this;
        }

        public Builder ol_ratio(double ol_ratio) {
            this.ol_ratio = ol_ratio;
            return this;
        }

        public RunConf build() {
            return new RunConf(this);
        }
    }
}
