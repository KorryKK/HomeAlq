package korry.homealq.view.main;

public class HomeAlqAppPresenter implements HomeAlqContract.Presenter {

    private final HomeAlqContract.View view;

    public HomeAlqAppPresenter(HomeAlqContract.View view) {
        this.view = view;
    }

    @Override
    public void onButtonClick() {
        view.setLabelText("¡Eres un pedazo de crack!");
    }
}
