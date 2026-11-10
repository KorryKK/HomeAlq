package korry.homealq.view.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import korry.homealq.base.ViewController;

public class HomeAlqAppController extends ViewController implements HomeAlqContract.View{

    @FXML
    private Label lblAlq;

    private HomeAlqContract.Presenter presenter;

    public void onButton() {
        presenter.onButtonClick();
    }

    @Override
    public void setLabelText(String text) {
        lblAlq.setText(text);
    }

    @Override
    public void setPresenter(HomeAlqContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
