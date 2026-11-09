package proyectofinalcasino.modelo;

/**
 * Configuración operativa de una mesa de blackjack.
 */
public final class ConfiguracionMesa {

    private int cantidadBarajas;
    private int maximoJugadores;
    private String varianteReglas;
    private boolean efectosSonido;
    private boolean recargaAutomatica;
    private int multiplesApuesta = 2;

    public ConfiguracionMesa() {
        cantidadBarajas = 6;
        maximoJugadores = 7;
        varianteReglas = "Clásico (Paga 3:2)";
        efectosSonido = true;
        recargaAutomatica = false;
    }

    public int getCantidadBarajas() {
        return cantidadBarajas;
    }

    public void setCantidadBarajas(int cantidadBarajas) {
        this.cantidadBarajas = cantidadBarajas;
    }

    public int getMaximoJugadores() {
        return maximoJugadores;
    }

    public void setMaximoJugadores(int maximoJugadores) {
        this.maximoJugadores = maximoJugadores;
    }

    public String getVarianteReglas() {
        return varianteReglas;
    }

    public void setVarianteReglas(String varianteReglas) {
        this.varianteReglas = varianteReglas;
    }

    public boolean isEfectosSonido() {
        return efectosSonido;
    }

    public void setEfectosSonido(boolean efectosSonido) {
        this.efectosSonido = efectosSonido;
    }

    public boolean isRecargaAutomatica() {
        return recargaAutomatica;
    }

    public void setRecargaAutomatica(boolean recargaAutomatica) {
        this.recargaAutomatica = recargaAutomatica;
    }

    public int getMultiplesApuesta() {
        return multiplesApuesta;
    }

    public void setMultiplesApuesta(int multiplesApuesta) {
        this.multiplesApuesta = multiplesApuesta;
    }
}
