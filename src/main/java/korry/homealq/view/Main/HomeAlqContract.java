package korry.homealq.view.Main;

public interface HomeAlqContract {
    interface View {
        void setLabelText(String text);
        void setPresenter(Presenter presenter);
    }
    interface Presenter {
        void onButtonClick();

    }

}
