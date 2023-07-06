package su.nightexpress.lootconomy.data.impl;

public class UserSettings {

    private boolean pickupSound;

    public UserSettings() {
        this(true);
    }

    public UserSettings(boolean pickupSound) {
        this.setPickupSound(pickupSound);
    }

    public boolean isPickupSound() {
        return pickupSound;
    }

    public void setPickupSound(boolean pickupSound) {
        this.pickupSound = pickupSound;
    }
}
