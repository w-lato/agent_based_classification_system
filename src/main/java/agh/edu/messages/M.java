package agh.edu.messages;

public class M
{
    // TODO - data format

    static public class Learn
    {
        public final String agentId;
        public final String who;

        public Learn(String who, String agentId)
        {
            this.agentId = agentId;
            this.who = who;
        }
    }

    static public class Classify
    {
        public final String agentId;

        public Classify(String agentId)
        {
            this.agentId = agentId;
        }
    }

    static public class SetAlgorithm
    {
        public final String agentId;
        public final int alg;

        public SetAlgorithm(String agentId, int alg)
        {
            this.alg = alg;
            this.agentId = agentId;
        }
    }


    static public class Greet {
        public Greet() {
        }
    }
}
