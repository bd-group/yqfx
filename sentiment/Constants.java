package sentiment;

public class Constants {

    public static final String WORED_PATH = null;

    public static final String POS_SEN_DICT = "rs/positiveWords";

    public static final String NEG_SEN_DICT = "rs/negativeWords";

    public static final String POS_PMI_SCORE = "rs/positive.txt";

    public static final String NEG_PMI_SCORE = "rs/negative.txt";

    /* 情感词的默认权重  */
    public static final Double NORMAL_WEIGHT = 1.0D;
    /* 情感判定的阈值 */
    public static final Double SEN_THRESHOLD = 0.8D;

    /* 词性 */
    public static enum WordType {

        POS(1), //积极情感词
        NEG(2), //消极情感词
        NEUTRAL_WORD(3), //中性词
        UN_KONWN(4); //未知类型

        private int value = 0;

        private WordType(int value) {
            this.value = value;
        }

        public static WordType valueOf(int value) {

            switch (value) {
                case 1:
                    return POS;
                case 2:
                    return NEG;
                case 3:
                    return NEUTRAL_WORD;
                default:
                    return UN_KONWN;
            }
        }

        public int value() {
            return this.value;
        }
    }

    /* 情感属性分类 */
    public static enum SentimentType {

        POSITIVE(1),
        NEGATIVE(2),
        NEUTRAL(3),
        UN_KNOWN(4);

        private int value;

        private SentimentType(int value) {
            this.value = value;
        }

        public static SentimentType valueOf(int value) {
            switch (value) {
                case 1:
                    return POSITIVE;
                case 2:
                    return NEGATIVE;
                case 3:
                    return NEUTRAL;
                default:
                    return UN_KNOWN;
            }
        }

        public int value() {
            return this.value;
        }
    }

}
