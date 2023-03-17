package api.tanks.model;

import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class ClieInfo {

        public int clieId;
        public int tankId;
        public String client;
        public String city;
        public int cityId;
        public String phone;
        public String tankSerial;
        public Double remainCritic;
        public Double remainZero;
        public Integer lastVhId;
        public String lastVhPlate;
        public String cityDbName;
        public int tankCapa;
        public int categId;
        public int mirrorId;
        public int criticLevelPerc;
        public Integer lastEmpId;
        public Double kdp;
        public String clieType;

        public ClieInfo(){}
        
        public ClieInfo(Object[] row) {
            this.clieId = MySQLQuery.getAsInteger(row[0]);
            this.tankId = MySQLQuery.getAsInteger(row[1]);
            this.client = MySQLQuery.getAsString(row[2]);
            this.city = MySQLQuery.getAsString(row[3]);
            this.phone = MySQLQuery.getAsString(row[4]);
            this.tankSerial = MySQLQuery.getAsString(row[5]);
            this.cityDbName = MySQLQuery.getAsString(row[6]);
            this.tankCapa = MySQLQuery.getAsInteger(row[7]);
            this.cityId = MySQLQuery.getAsInteger(row[8]);
            this.categId = MySQLQuery.getAsInteger(row[9]);
            this.mirrorId = MySQLQuery.getAsInteger(row[10]);
            this.criticLevelPerc = MySQLQuery.getAsInteger(row[11]);
            this.clieType = MySQLQuery.getAsString(row[12]);
        }

        public static List<ClieInfo> getClieInfoList(Object[][] data) {
            List<ClieInfo> lst = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                lst.add(new ClieInfo(data[i]));
            }
            return lst;
        }
    }
