package structures;

import utils.OprType;

import java.util.*;

public class Operations {
    int t_idx;
    OprType oprType;

    public List<Integer> utilities;
    public List<SetOpr> oprs;

    Operations(int t_idx, OprType oprType) {
        this.t_idx = t_idx;
        this.oprType = oprType;

        this.utilities = new ArrayList<>();
        this.oprs = new ArrayList<>();
    }

    public void print() {
        System.out.print(t_idx + " ");
        if (oprType == OprType.ADD) {
            System.out.println("ADD");
        } else {
            System.out.println("DEL");
        }
        for (int u_idx : utilities) {
            System.out.print(u_idx + " ");
        }
        System.out.println();
        for (SetOpr opr : oprs) {
            System.out.print(opr.t_idx + "," + opr.u_idx + " ");
        }
        System.out.println();
    }

    static class SetOpr {
        int t_idx;
        int u_idx;

        SetOpr(int t_idx, int u_idx) {
            this.t_idx = t_idx;
            this.u_idx = u_idx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SetOpr)) return false;
            SetOpr setOpr = (SetOpr) o;
            return t_idx == setOpr.t_idx &&
                    u_idx == setOpr.u_idx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(t_idx, u_idx);
        }
    }
}
