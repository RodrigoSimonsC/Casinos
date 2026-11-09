package proyectofinalcasino.interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import proyectofinalcasino.funciones.GestorMesa;
import proyectofinalcasino.modelo.Jugador;

public final class PanelJugadores extends JPanel {

    private final GestorMesa gestor;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;
    private final JTextField campoNombre = new JTextField();
    private final JTextField campoSaldo = new JTextField();
    private final JLabel etiquetaDisponibles = new JLabel();

    public PanelJugadores(GestorMesa gestor) {
        this.gestor = gestor;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Jugadores de la mesa"));

        modeloTabla = new DefaultTableModel(new Object[]{"Jugador", "Saldo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(2, 4, 8, 8));
        formulario.add(new JLabel("Nombre"));
        formulario.add(campoNombre);
        formulario.add(new JLabel("Saldo inicial"));
        formulario.add(campoSaldo);
        JButton agregar = new JButton("Agregar jugador");
        agregar.addActionListener(evento -> agregarJugador());
        JButton eliminar = new JButton("Retirar seleccionado");
        eliminar.addActionListener(evento -> eliminarJugador());
        JButton historial = new JButton("Ver historial");
        historial.addActionListener(evento -> mostrarHistorial());
        formulario.add(agregar);
        formulario.add(eliminar);
        formulario.add(etiquetaDisponibles);
        formulario.add(historial);
        add(formulario, BorderLayout.SOUTH);
        refrescar();
    }

    public void refrescar() {
        modeloTabla.setRowCount(0);
        List<Jugador> jugadores = gestor.obtenerJugadores();
        for (Jugador jugador : jugadores) {
            modeloTabla.addRow(new Object[]{jugador.getNombre(),
                String.format("$ %.2f", jugador.getSaldo())});
        }
        etiquetaDisponibles.setText("Ocupados: " + jugadores.size() + " / "
                + gestor.obtenerConfiguracion().getMaximoJugadores());
    }

    private void agregarJugador() {
        try {
            String error = gestor.agregarJugador(campoNombre.getText(),
                    Double.parseDouble(campoSaldo.getText().trim()));
            if (!error.isEmpty()) {
                mostrarError(error);
                return;
            }
            campoNombre.setText("");
            campoSaldo.setText("");
            refrescar();
        } catch (NumberFormatException exception) {
            mostrarError("El saldo debe ser un número válido.");
        }
    }

    private void eliminarJugador() {
        String error = gestor.eliminarJugador(tabla.getSelectedRow());
        if (!error.isEmpty()) {
            mostrarError(error);
            return;
        }
        refrescar();
    }

    private void mostrarHistorial() {
        List<Jugador> historial = gestor.obtenerHistorialJugadores();
        if (historial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todavía no hay jugadores registrados.",
                    "Historial", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder mensaje = new StringBuilder("Jugadores que han estado en la mesa:\n\n");
        for (Jugador jugador : historial) {
            mensaje.append("- ").append(jugador.getNombre())
                    .append(" ($ ").append(String.format("%.2f", jugador.getSaldo()))
                    .append(")\n");
        }
        JOptionPane.showMessageDialog(this, mensaje.toString(),
                "Historial de jugadores", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos no válidos",
                JOptionPane.WARNING_MESSAGE);
    }
}
