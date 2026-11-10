package korry.homealq.view.Main;

public class HomeAlqAppPresenter implements HomeAlqContract.Presenter {

    private final HomeAlqContract.View view;

    public HomeAlqAppPresenter(HomeAlqContract.View view) {
        this.view = view;
    }

    @Override
    public void onButtonClick() {
        view.setLabelText("¡Eres una pedazo de Puta!");
    }
}
