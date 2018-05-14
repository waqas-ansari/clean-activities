package talosdev.clean.features.main;

public interface MainContract {

    interface View {
        void navigateToLocation();

        void navigateToPhone();
    }

    interface Presenter {
        void onLocationClicked();

        void onPhoneClicked();
    }

}
