package korry.homealq.base;

import korry.homealq.util.enums.Titulo;
import korry.homealq.util.navegator.Navegable;
import korry.homealq.util.navegator.Navegador;

public class ViewController implements Navegable, Titleable {
    protected Navegador navegador;

    @Override
    public void setNavegador(Navegador navegador) {
        this.navegador = navegador;
    }

    @Override
    public void setTitle(Titulo title) {
        if (navegador == null) {
            throw new IllegalStateException();
        } else {
            navegador.stage().setTitle(title.getTitle());
        }
    }
}
