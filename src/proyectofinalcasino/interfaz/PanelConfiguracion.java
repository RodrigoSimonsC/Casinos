package proyectofinalcasino.interfaz;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import proyectofinalcasino.funciones.GestorMesa;
import proyectofinalcasino.modelo.ConfiguracionMesa;

public final class PanelConfiguracion extends JPanel {

    private final GestorMesa gestor;
    private final JComboBox<Integer> campoBarajas =
            new JComboBox<>(new Integer[]{1, 2, 4, 6, 8});
    private final JComboBox<Integer> campoJugadores =
            new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7});
    private final JComboBox<Integer> campoMultiplesApuesta =
            new JComboBox<>(new Integer[]{2, 3, 5, 10, 20, 50, 100});
    private final JComboBox<String> campoReglas = new JComboBox<>(new String[]{
        "Clásico (Paga 3:2)", "Europeo", "Vegas Strip"
    });
    private final JCheckBox campoSonido = new JCheckBox("Efectos de sonido");
    private final JCheckBox campoRecarga = new JCheckBox("Recarga automática");
    private final Runnable alGuardar;

    public PanelConfiguracion(GestorMesa gestor, Runnable alGuardar) {
        this.gestor = gestor;
        this.alGuardar = alGuardar;
        setBorder(BorderFactory.createTitledBorder("Configuración de blackjack"));
        setLayout(new GridBagLayout());

        agregarCampo("Cantidad de barajas", campoBarajas, 0);
        agregarCampo("Máximo de jugadores", campoJugadores, 1);
        agregarCampo("Múltiplo de apuesta", campoMultiplesApuesta, 2);
        agregarCampo("Variante de reglas", campoReglas, 3);
        agregarCampo("", campoSonido, 4);
        agregarCampo("", campoRecarga, 5);
        JButton guardar = new JButton("Guardar configuración");
        guardar.addActionListener(evento -> guardarConfiguracion());
        GridBagConstraints restricciones = restricciones(0, 6);
        restricciones.gridwidth = 2;
        add(guardar, restricciones);
        cargarConfiguracion();
    }

    private void agregarCampo(String texto, java.awt.Component campo, int fila) {
        GridBagConstraints etiqueta = restricciones(0, fila);
        add(new JLabel(texto), etiqueta);
        add(campo, restricciones(1, fila));
    }

    private GridBagConstraints restricciones(int columna, int fila) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = columna;
        restricciones.gridy = fila;
        restricciones.weightx = columna == 1 ? 1 : 0;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.insets = new Insets(6, 6, 6, 6);
        return restricciones;
    }

    private void cargarConfiguracion() {
        ConfiguracionMesa configuracion = gestor.obtenerConfiguracion();
        campoBarajas.setSelectedItem(configuracion.getCantidadBarajas());
        campoJugadores.setSelectedItem(configuracion.getMaximoJugadores());
        campoMultiplesApuesta.setSelectedItem(configuracion.getMultiplesApuesta());
        campoReglas.setSelectedItem(configuracion.getVarianteReglas());
        campoSonido.setSelected(configuracion.isEfectosSonido());
        campoRecarga.setSelected(configuracion.isRecargaAutomatica());
    }

    private void guardarConfiguracion() {
        try {
            String error = gestor.actualizarConfiguracion(
                    (Integer) campoBarajas.getSelectedItem(),
                    (Integer) campoJugadores.getSelectedItem(),
                    (Integer) campoMultiplesApuesta.getSelectedItem(),
                    (String) campoReglas.getSelectedItem(),
                    campoSonido.isSelected(),
                    campoRecarga.isSelected());
            if (!error.isEmpty()) {
                JOptionPane.showMessageDialog(this, error,
                        "Datos no válidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            alGuardar.run();
            JOptionPane.showMessageDialog(this, "Configuración guardada.",
                    "Mesa actualizada", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Selecciona valores válidos.",
                    "Datos no válidos", JOptionPane.WARNING_MESSAGE);
        }
    }
}
