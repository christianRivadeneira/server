package web.vicky.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TaskInfo implements Serializable {

    public static final int CHECK = 1;
    public static final int SCHEDULE = 2;

    public int orderId;
    public int type;

    public TaskInfo(int orderId, int type) {
        this.orderId = orderId;
        this.type = type;
    }

    public TaskInfo() {
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(orderId);
        out.writeInt(type);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        orderId = in.readInt();
        type = in.readInt();
    }
}
