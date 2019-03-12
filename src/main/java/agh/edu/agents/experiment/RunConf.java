package agh.edu.agents.experiment;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import weka.core.Instances;

import java.util.Optional;

public final class RunConf
{
    Instances train;
    Optional<Instances> test;

    S_Type[] agents;
    ClassStrat class_method;

    Split split_meth;
    Optional<Double> OL;

    public ClassStrat getClass_method() {
        return class_method;
    }


    public static final class RunConfBuilder {
        Instances train;
        Optional<Instances> test;
        S_Type[] agents;
        ClassStrat class_method;
        Split split_meth;
        Optional<Double> OL;

        public RunConfBuilder() {}

        public static RunConfBuilder aRunConf() {
            return new RunConfBuilder();
        }

        public RunConfBuilder withTrain(Instances train) {
            this.train = train;
            return this;
        }

        public RunConfBuilder withTest(Optional<Instances> test) {
            this.test = test;
            return this;
        }

        public RunConfBuilder withAgents(S_Type[] agents) {
            this.agents = agents;
            return this;
        }

        public RunConfBuilder withClass_method(ClassStrat class_method) {
            this.class_method = class_method;
            return this;
        }

        public RunConfBuilder withSplit_meth(Split split_meth) {
            this.split_meth = split_meth;
            return this;
        }

        public RunConfBuilder withOL(Optional<Double> OL) {
            this.OL = OL;
            return this;
        }

        public RunConf build() {
            RunConf runConf = new RunConf();
            runConf.class_method = this.class_method;
            runConf.split_meth = this.split_meth;
            runConf.OL = this.OL;
            runConf.test = this.test;
            runConf.train = this.train;
            runConf.agents = this.agents;
            return runConf;
        }
    }
}
