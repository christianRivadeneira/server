package api.bill.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EqualPayment {

    public BigDecimal capital;
    public BigDecimal interest;
    public BigDecimal interVat;

    public EqualPayment(BigDecimal capital, BigDecimal interest, BigDecimal interVat) {
        this.capital = capital.setScale(2, RoundingMode.HALF_EVEN);
        this.interest = interest.setScale(2, RoundingMode.HALF_EVEN);
        this.interVat = interVat.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static EqualPayment[] getValues(BigDecimal total, BigDecimal saleVat, BigDecimal interRate, BigDecimal interVat, int cuotas) throws Exception {
        BigDecimal net;
        BigDecimal vat;
        if (saleVat != null && saleVat.compareTo(BigDecimal.ZERO) != 0) {
            net = total.divide(BigDecimal.ONE.add(saleVat.divide(new BigDecimal(100), 32, RoundingMode.HALF_EVEN)), 32, RoundingMode.HALF_EVEN).setScale(4, RoundingMode.HALF_EVEN);
            vat = total.subtract(net);
        } else {
            net = total;
            vat = BigDecimal.ZERO;
        }
              
        interRate = interRate.divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN);
        BigDecimal cuota = getValue(net, interRate, cuotas);
        EqualPayment[] rta = new EqualPayment[cuotas];

        BigDecimal totalInte = BigDecimal.ZERO;
        for (int i = 0; i < cuotas; i++) {
            BigDecimal curInte = net.multiply(interRate);
            BigDecimal curCapi = cuota.subtract(curInte);
            cuota.subtract(curCapi);
            net = net.subtract(curCapi);
            totalInte = totalInte.add(curInte);
            rta[i] = new EqualPayment(curCapi, curInte, BigDecimal.ZERO);
        }

        BigDecimal iva = interVat != null ? totalInte.multiply(interVat).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN).divide(new BigDecimal(cuotas), 4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
        for (EqualPayment p : rta) {
            p.interVat = iva;
        }
        rta[0].capital = rta[0].capital.add(vat);
        return rta;
    }

    private static BigDecimal getValue(BigDecimal total, BigDecimal interes, int cuotas) throws Exception {
        if (total == null) {
            throw new Exception("Indique el valor total.");
        } else if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("El total debe ser mayor que 0");
        }

        if (interes == null) {
            throw new Exception("Indique el interés.");
        }
        if (cuotas <= 0) {
            throw new Exception("El número de cuotas debe ser mayor que 0");
        }

        if (interes.compareTo(BigDecimal.ZERO) == 0) {
            return total.divide(new BigDecimal(cuotas), RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
        }
        BigDecimal k = interes.add(BigDecimal.ONE).pow(cuotas);
        return k.multiply(interes).divide(k.subtract(BigDecimal.ONE), RoundingMode.HALF_EVEN).multiply(total).setScale(2, RoundingMode.HALF_EVEN);
    }
}
