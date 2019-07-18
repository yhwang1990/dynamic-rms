package utils;

import java.util.Objects;
import java.util.PriorityQueue;

public class TopKResult {

    double k_score;
    PriorityQueue<Token> exact_result;
    PriorityQueue<Token> approximate_result;

    public TopKResult() {
        this.k_score = 0.0;
        this.exact_result = new PriorityQueue<>();
        this.approximate_result = new PriorityQueue<>();
    }

    public class Token implements Comparable<Token> {
        int idx;
        double score;
        public Token(int idx, double score) {
            this.idx = idx;
            this.score = score;
        }

        @Override
        public int compareTo(Token other) {
            if (this.score > other.score) {
                return -1;
            } else if (this.score < other.score) {
                return 1;
            } else {
                return (this.idx - other.idx);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Token)) return false;
            Token token = (Token) o;
            return idx == token.idx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(idx);
        }
    }
}
