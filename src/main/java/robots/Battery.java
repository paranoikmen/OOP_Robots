package robots;

import java.util.Objects;

public class Battery {

    private Integer charge;

    public Battery(Integer charge) {
        this.charge = charge;
    }

    public Integer releaseCharge(int chargeAmount) {
        if(chargeAmount > charge) return 0;
        charge -= chargeAmount;
        return chargeAmount;
    }

    public Integer charge() {
        return charge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Battery battery = (Battery) o;
        return Objects.equals(charge, battery.charge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charge);
    }

    @Override
    public String toString() {
        return "Battery{" +
                "charge=" + charge +
                '}';
    }
}
