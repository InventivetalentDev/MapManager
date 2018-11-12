// From https://github.com/bergerhealer/BKCommonLib, modified by Joiubaxas#4650
package org.inventivetalent.mapmanager.util.bit;

public class BitPacket implements Cloneable {
    public int data, bits;

    public BitPacket() {
        this.data = 0;
        this.bits = 0;
    }

    public BitPacket(int data, int bits) {
        this.data = data;
        this.bits = bits;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BitPacket) {
            BitPacket other = (BitPacket) o;
            if (other.bits == bits) {
                int mask = ((1 << bits) - 1);
                return (data & mask) == (other.data & mask);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public BitPacket clone() {
        return new BitPacket(this.data, this.bits);
    }

    @Override
    public String toString() {
        String str = Integer.toBinaryString(data & ((1 << bits) - 1));
        while (str.length() < this.bits) {
            str = "0" + str;
        }
        return str;
    }
}
