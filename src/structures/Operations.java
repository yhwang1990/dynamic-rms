package structures;

import java.util.*;

public class Operations {
    final int t_idx;
    final int oprType;

    List<Integer> utilities;
    List<SetOpr> oprs;

    public Operations(final int idx, final int type) {
        this.t_idx = idx;
        this.oprType = type;

        this.utilities = new ArrayList<>();
        this.oprs = new ArrayList<>();
    }

    public void print() {
        if (oprType > 0)
            System.out.print("ADD ");
        else
            System.out.print("DEL ");
        System.out.println(t_idx);

        for (int u_idx : utilities)
            System.out.print(u_idx + " ");
        System.out.println();

        for (SetOpr opr : oprs)
            System.out.print(opr.t_idx + "," + opr.u_idx + " ");
        System.out.println();
    }

    static class SetOpr {
        final int t_idx;
        final int u_idx;

        SetOpr(final int t_idx, final int u_idx) {
            this.t_idx = t_idx;
            this.u_idx = u_idx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SetOpr))
                return false;
            SetOpr setOpr = (SetOpr) o;
            return t_idx == setOpr.t_idx && u_idx == setOpr.u_idx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(t_idx, u_idx);
        }
    }
}
