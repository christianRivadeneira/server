package api.bill.model.dto;

public class BillReadingsCheck {

    public int clients;
    public int tanks;
    public int clientReads;
    public int tankReads;

    public BillReadingsCheck(int clients, int tanks, int clientReads, int tankReads) {
        this.clients = clients;
        this.tanks = tanks;
        this.clientReads = clientReads;
        this.tankReads = tankReads;
    }

}
